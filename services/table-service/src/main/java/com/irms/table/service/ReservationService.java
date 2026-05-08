package com.irms.table.service;

import com.irms.table.domain.Reservation;
import com.irms.table.domain.ReservationStatus;
import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.dto.ReservationRequest;
import com.irms.table.infrastructure.sse.SseBroadcaster;
import com.irms.table.repository.ReservationRepository;
import com.irms.table.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final SseBroadcaster sseBroadcaster;

    // ────────────────────────────────────────────────────────────
    // Truy vấn
    // ────────────────────────────────────────────────────────────

    /**
     * Lấy tất cả đặt bàn.
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Lọc đặt bàn theo trạng thái.
     */
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusOrderByReservationTimeAsc(status);
    }

    /**
     * Lấy đặt bàn trong một khoảng thời gian.
     */
    public List<Reservation> getReservationsBetween(LocalDateTime from, LocalDateTime to) {
        return reservationRepository.findByReservationTimeBetween(from, to);
    }

    /**
     * Lấy thông tin một đặt bàn theo ID.
     */
    public Reservation getReservationById(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn với id: " + id));
    }

    // ────────────────────────────────────────────────────────────
    // Tạo / cập nhật đặt bàn
    // ────────────────────────────────────────────────────────────

    /**
     * Tạo đặt bàn mới (UC08).
     * Kiểm tra xem còn bàn trống phù hợp trong khung giờ đó không.
     */
    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        // Kiểm tra sơ bộ: có bàn đủ sức chứa không
        List<RestaurantTable> suitableTables = tableRepository
                .findByStatusAndCapacityGreaterThanEqual(TableStatus.AVAILABLE, request.getPartySize());

        if (suitableTables.isEmpty()) {
            // Cũng kiểm tra các bàn không bị giữ bởi reservation khác trong khung giờ
            throw new RuntimeException("Không đủ bàn trống cho " + request.getPartySize() + " người trong khung giờ này");
        }

        Reservation reservation = Reservation.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .partySize(request.getPartySize())
                .reservationTime(request.getReservationTime())
                .notes(request.getNotes())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        sseBroadcaster.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Xác nhận đặt bàn và gán bàn cụ thể (UC08 - bước confirm).
     */
    @Transactional
    public Reservation confirmReservation(UUID reservationId, UUID tableId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xác nhận đặt bàn ở trạng thái PENDING");
        }

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với id: " + tableId));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Bàn " + table.getTableNumber() + " không còn trống");
        }

        // Kiểm tra xem có reservation nào bị trùng giờ trên bàn này không
        LocalDateTime startTime = reservation.getReservationTime();
        LocalDateTime endTime = startTime.plusMinutes(reservation.getExpectedDurationMinutes());
        
        List<Reservation> overlapping = reservationRepository.findOverlappingReservations(tableId, startTime, endTime);
        // Exclude the current reservation from the overlap check
        overlapping.removeIf(r -> r.getId().equals(reservationId));
        
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Bàn " + table.getTableNumber() + " đã có người đặt trong khung giờ này");
        }

        // Gán bàn và đổi trạng thái
        reservation.setTable(table);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Đặt bàn ở trạng thái RESERVED
        table.setStatus(TableStatus.RESERVED);
        tableRepository.save(table);

        Reservation saved = reservationRepository.save(reservation);
        sseBroadcaster.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Hủy đặt bàn (UC08).
     */
    @Transactional
    public Reservation cancelReservation(UUID id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatus() == ReservationStatus.SEATED) {
            throw new RuntimeException("Không thể hủy đặt bàn khi khách đã vào chỗ");
        }

        // Giải phóng bàn nếu đã được gán
        if (reservation.getTable() != null) {
            RestaurantTable table = reservation.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        sseBroadcaster.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Đánh dấu khách không xuất hiện (No-Show) sau khi hết thời gian chờ.
     */
    @Transactional
    public Reservation markNoShow(UUID id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể đánh dấu No-Show với đặt bàn đã được xác nhận");
        }

        // Giải phóng bàn
        if (reservation.getTable() != null) {
            RestaurantTable table = reservation.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }

        reservation.setStatus(ReservationStatus.NO_SHOW);
        Reservation saved = reservationRepository.save(reservation);
        sseBroadcaster.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }
}
