package com.irms.table.service;

import com.irms.table.domain.ReservationStatus;
import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.domain.WaitlistStatus;
import com.irms.table.dto.SeatGuestRequest;
import com.irms.table.dto.TableRequest;
import com.irms.table.dto.TableStatusUpdateRequest;
import com.irms.table.infrastructure.sse.SseBroadcaster;
import com.irms.table.repository.ReservationRepository;
import com.irms.table.repository.RestaurantTableRepository;
import com.irms.table.repository.WaitlistRepository;
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
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final SseBroadcaster sseBroadcaster;

    // ────────────────────────────────────────────────────────────
    // Truy vấn
    // ────────────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ danh sách bàn (dùng cho Floor Plan - UC01).
     */
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    /**
     * Lọc bàn theo trạng thái.
     */
    public List<RestaurantTable> getTablesByStatus(TableStatus status) {
        return tableRepository.findByStatus(status);
    }

    /**
     * Tìm các bàn AVAILABLE có sức chứa >= partySize (UC10).
     */
    public List<RestaurantTable> getAvailableTablesForParty(Integer partySize) {
        return tableRepository.findByStatusAndCapacityGreaterThanEqual(TableStatus.AVAILABLE, partySize);
    }

    /**
     * Lấy thông tin một bàn theo ID.
     */
    public RestaurantTable getTableById(UUID id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với id: " + id));
    }

    // ────────────────────────────────────────────────────────────
    // Tạo / cập nhật bàn
    // ────────────────────────────────────────────────────────────

    /**
     * Tạo bàn mới (Admin).
     */
    @Transactional
    public RestaurantTable createTable(TableRequest request) {
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new RuntimeException("Số bàn \"" + request.getTableNumber() + "\" đã tồn tại");
        }

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .status(TableStatus.AVAILABLE)
                .build();

        RestaurantTable saved = tableRepository.save(table);
        sseBroadcaster.broadcast("table.created", Map.of("id", saved.getId(), "tableNumber", saved.getTableNumber()));
        return saved;
    }

    /**
     * Cập nhật trạng thái bàn (UC10 — Tiếp tân xếp bàn / nhân viên dọn xong).
     */
    @Transactional
    public RestaurantTable updateTableStatus(UUID tableId, TableStatusUpdateRequest request) {
        RestaurantTable table = getTableById(tableId);
        table.setStatus(request.getStatus());
        table.setCurrentOrderId(request.getCurrentOrderId());

        if (request.getStatus() == TableStatus.OCCUPIED) {
            table.setSeatedAt(LocalDateTime.now());
        } else if (request.getStatus() == TableStatus.AVAILABLE || request.getStatus() == TableStatus.CLEANING) {
            table.setSeatedAt(null);
        }

        RestaurantTable saved = tableRepository.save(table);
        sseBroadcaster.broadcast("table.status", Map.of("id", saved.getId(), "tableNumber", saved.getTableNumber(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Xếp bàn cho khách (UC10) — hỗ trợ 3 nguồn: RESERVATION, WAITLIST, WALK_IN.
     */
    @Transactional
    public RestaurantTable seatGuest(SeatGuestRequest request) {
        RestaurantTable table = getTableById(request.getTableId());

        if (table.getStatus() != TableStatus.AVAILABLE && table.getStatus() != TableStatus.RESERVED) {
            throw new RuntimeException("Bàn " + table.getTableNumber() + " không sẵn sàng để xếp khách (trạng thái: " + table.getStatus() + ")");
        }

        // Đánh dấu bàn là OCCUPIED
        table.setStatus(TableStatus.OCCUPIED);
        table.setSeatedAt(LocalDateTime.now());
        tableRepository.save(table);

        // Cập nhật trạng thái nguồn
        if ("RESERVATION".equalsIgnoreCase(request.getSource()) && request.getSourceId() != null) {
            reservationRepository.findById(request.getSourceId()).ifPresent(r -> {
                r.setStatus(ReservationStatus.SEATED);
                reservationRepository.save(r);
            });
        } else if ("WAITLIST".equalsIgnoreCase(request.getSource()) && request.getSourceId() != null) {
            waitlistRepository.findById(request.getSourceId()).ifPresent(w -> {
                w.setStatus(WaitlistStatus.SEATED);
                waitlistRepository.save(w);
            });
        }

        sseBroadcaster.broadcast("table.seated", Map.of("id", table.getId(), "tableNumber", table.getTableNumber(), "source", request.getSource()));
        return table;
    }

    /**
     * Chuyển khách/order từ bàn này sang bàn khác (UC04).
     */
    @Transactional
    public RestaurantTable moveTable(UUID fromTableId, UUID toTableId) {
        RestaurantTable fromTable = getTableById(fromTableId);
        RestaurantTable toTable = getTableById(toTableId);

        if (fromTable.getStatus() != TableStatus.OCCUPIED) {
            throw new RuntimeException("Chỉ có thể chuyển bàn đang có khách (OCCUPIED)");
        }

        if (toTable.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Bàn đích " + toTable.getTableNumber() + " không còn trống");
        }

        // Chuyển dữ liệu
        toTable.setStatus(TableStatus.OCCUPIED);
        toTable.setCurrentOrderId(fromTable.getCurrentOrderId());
        toTable.setSeatedAt(fromTable.getSeatedAt());
        tableRepository.save(toTable);

        // Giải phóng bàn cũ
        fromTable.setStatus(TableStatus.AVAILABLE);
        fromTable.setCurrentOrderId(null);
        fromTable.setSeatedAt(null);
        tableRepository.save(fromTable);

        sseBroadcaster.broadcast("table.moved", Map.of("from", fromTableId, "to", toTableId));
        return toTable;
    }
}
