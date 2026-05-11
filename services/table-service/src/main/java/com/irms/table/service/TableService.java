package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.dto.SeatGuestRequest;
import com.irms.table.dto.TableRequest;
import com.irms.table.dto.TableStatusUpdateRequest;
import com.irms.table.exception.TableBusinessException;
import com.irms.table.exception.TableResourceNotFoundException;
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
public class TableService implements TableManagementService {

    private final RestaurantTableRepository tableRepository;
    private final TableSeatingPolicy tableSeatingPolicy;
    private final List<SeatingSourceHandler> seatingSourceHandlers;
    private final TableEventPublisher eventPublisher;

    // ────────────────────────────────────────────────────────────
    // Truy vấn
    // ────────────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ danh sách bàn (dùng cho Floor Plan - UC01).
     */
    @Override
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    /**
     * Lọc bàn theo trạng thái.
     */
    @Override
    public List<RestaurantTable> getTablesByStatus(TableStatus status) {
        return tableRepository.findByStatus(status);
    }

    /**
     * Tìm các bàn AVAILABLE có sức chứa >= partySize (UC10).
     */
    @Override
    public List<RestaurantTable> getAvailableTablesForParty(Integer partySize) {
        return tableRepository.findByStatusAndCapacityGreaterThanEqual(TableStatus.AVAILABLE, partySize);
    }

    /**
     * Lấy thông tin một bàn theo ID.
     */
    @Override
    public RestaurantTable getTableById(UUID id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new TableResourceNotFoundException("Không tìm thấy bàn với id: " + id));
    }

    // ────────────────────────────────────────────────────────────
    // Tạo / cập nhật bàn
    // ────────────────────────────────────────────────────────────

    /**
     * Tạo bàn mới (Admin).
     */
    @Override
    @Transactional
    public RestaurantTable createTable(TableRequest request) {
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new TableBusinessException("Số bàn \"" + request.getTableNumber() + "\" đã tồn tại");
        }

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .status(TableStatus.AVAILABLE)
                .build();

        RestaurantTable saved = tableRepository.save(table);
        eventPublisher.broadcast("table.created", Map.of("id", saved.getId(), "tableNumber", saved.getTableNumber()));
        return saved;
    }

    /**
     * Cập nhật trạng thái bàn (UC10 — Tiếp tân xếp bàn / nhân viên dọn xong).
     */
    @Override
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
        eventPublisher.broadcast("table.status", Map.of("id", saved.getId(), "tableNumber", saved.getTableNumber(), "status", saved.getStatus().name()));
        return saved;
    }

    /**
     * Xếp bàn cho khách (UC10) — hỗ trợ 3 nguồn: RESERVATION, WAITLIST, WALK_IN.
     */
    @Override
    @Transactional
    public RestaurantTable seatGuest(SeatGuestRequest request) {
        RestaurantTable table = getTableById(request.getTableId());
        tableSeatingPolicy.validateCanSeat(table);

        // Đánh dấu bàn là OCCUPIED
        table.setStatus(TableStatus.OCCUPIED);
        table.setSeatedAt(LocalDateTime.now());
        tableRepository.save(table);

        updateSeatingSource(request);

        String sourceName = request.getSource() != null ? request.getSource().name() : "UNKNOWN";
        eventPublisher.broadcast("table.seated", Map.of("id", table.getId(), "tableNumber", table.getTableNumber(), "source", sourceName));
        return table;
    }

    private void updateSeatingSource(SeatGuestRequest request) {
        if (request.getSource() == null) {
            return;
        }

        SeatingSourceHandler handler = seatingSourceHandlers.stream()
                .filter(candidate -> candidate.supports(request.getSource()))
                .findFirst()
                .orElseThrow(() -> new TableBusinessException("Nguồn xếp bàn không được hỗ trợ: " + request.getSource()));

        handler.handle(request.getSourceId());
    }

    /**
     * Chuyển khách/order từ bàn này sang bàn khác (UC04).
     */
    @Override
    @Transactional
    public RestaurantTable moveTable(UUID fromTableId, UUID toTableId) {
        RestaurantTable fromTable = getTableById(fromTableId);
        RestaurantTable toTable = getTableById(toTableId);

        tableSeatingPolicy.validateMoveSource(fromTable);
        tableSeatingPolicy.validateMoveDestination(toTable);

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

        eventPublisher.broadcast("table.moved", Map.of("from", fromTableId, "to", toTableId));
        return toTable;
    }
}
