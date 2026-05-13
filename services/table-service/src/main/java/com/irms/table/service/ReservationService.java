package com.irms.table.service;

import com.irms.table.domain.Reservation;
import com.irms.table.domain.ReservationStatus;
import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.dto.ReservationRequest;
import com.irms.table.exception.TableBusinessException;
import com.irms.table.exception.TableResourceNotFoundException;
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
public class ReservationService implements ReservationManagementService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final TableSeatingPolicy tableSeatingPolicy;
    private final ReservationPolicy reservationPolicy;
    private final TableEventPublisher eventPublisher;

    // ────────────────────────────────────────────────────────────
    // Truy vấn
    // ────────────────────────────────────────────────────────────

    /**
     * Lấy tất cả đặt bàn.
     */
    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Lọc đặt bàn theo trạng thái.
     */
    @Override
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatusOrderByReservationTimeAsc(status);
    }

    /**
     * Lấy đặt bàn trong một khoảng thời gian.
     */
    @Override
    public List<Reservation> getReservationsBetween(LocalDateTime from, LocalDateTime to) {
        return reservationRepository.findByReservationTimeBetween(from, to);
    }

    /**
     * Lấy thông tin một đặt bàn theo ID.
     */
    @Override
    public Reservation getReservationById(UUID id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new TableResourceNotFoundException("Không tìm thấy đặt bàn với id: " + id));
    }

    // ────────────────────────────────────────────────────────────
    // Tạo / cập nhật đặt bàn
    // ────────────────────────────────────────────────────────────

    /**
     * Tạo đặt bàn mới (UC08).
     * Kiểm tra xem nhà hàng có bàn nào đủ sức chứa không.
     */
    @Override
    @Transactional
    public Reservation createReservation(ReservationRequest request) {
        // Kiểm tra xem nhà hàng có bàn nào chứa được số người này không
        List<RestaurantTable> allSuitableTables = tableRepository
                .findByCapacityGreaterThanEqual(request.getPartySize());

        if (allSuitableTables.isEmpty()) {
            throw new TableBusinessException("Nhà hàng không có bàn nào đủ lớn cho " + request.getPartySize() + " người");
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
        eventPublisher.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Xác nhận đặt bàn và gán bàn cụ thể (UC08 - bước confirm).
     */
    @Override
    @Transactional
    public Reservation confirmReservation(UUID reservationId, UUID tableId) {
        Reservation reservation = getReservationById(reservationId);
        reservationPolicy.validateCanAssign(reservation);

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableResourceNotFoundException("Không tìm thấy bàn với id: " + tableId));

        tableSeatingPolicy.validateAvailable(table);
        reservationPolicy.validateNoOverlap(reservation, table, reservationId);

        // Gán bàn và đổi trạng thái
        reservation.setTable(table);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Đặt bàn ở trạng thái RESERVED
        table.setStatus(TableStatus.RESERVED);
        tableRepository.save(table);

        Reservation saved = reservationRepository.save(reservation);
        eventPublisher.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Hủy đặt bàn (UC08).
     */
    @Override
    @Transactional
    public Reservation cancelReservation(UUID id) {
        Reservation reservation = getReservationById(id);
        reservationPolicy.validateCanCancel(reservation);

        // Giải phóng bàn nếu đã được gán
        if (reservation.getTable() != null) {
            RestaurantTable table = reservation.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        eventPublisher.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Đánh dấu khách không xuất hiện (No-Show) sau khi hết thời gian chờ.
     */
    @Override
    @Transactional
    public Reservation markNoShow(UUID id) {
        Reservation reservation = getReservationById(id);
        reservationPolicy.validateCanMarkNoShow(reservation);

        // Giải phóng bàn
        if (reservation.getTable() != null) {
            RestaurantTable table = reservation.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }

        reservation.setStatus(ReservationStatus.NO_SHOW);
        Reservation saved = reservationRepository.save(reservation);
        eventPublisher.broadcast("reservation.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        return saved;
    }
}
