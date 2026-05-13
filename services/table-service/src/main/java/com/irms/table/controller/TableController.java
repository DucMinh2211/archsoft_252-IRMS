package com.irms.table.controller;

import com.irms.table.domain.TableStatus;
import com.irms.table.dto.SeatGuestRequest;
import com.irms.table.dto.TableRequest;
import com.irms.table.dto.TableResponse;
import com.irms.table.dto.TableStatusUpdateRequest;
import com.irms.table.mapper.TableResponseMapper;
import com.irms.table.service.TableManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableManagementService tableService;
    private final TableResponseMapper tableResponseMapper;

    /**
     * UC01 — Lấy tất cả bàn để hiển thị sơ đồ Floor Plan.
     * Query param: ?status=AVAILABLE để lọc theo trạng thái.
     */
    @GetMapping
    public ResponseEntity<List<TableResponse>> getTables(
            @RequestParam(required = false) TableStatus status) {
        if (status != null) {
            return ResponseEntity.ok(tableResponseMapper.toTableResponses(tableService.getTablesByStatus(status)));
        }
        return ResponseEntity.ok(tableResponseMapper.toTableResponses(tableService.getAllTables()));
    }

    /**
     * UC10 — Tìm bàn AVAILABLE phù hợp với số lượng khách.
     */
    @GetMapping("/available")
    public ResponseEntity<List<TableResponse>> getAvailableTables(
            @RequestParam(defaultValue = "1") Integer partySize) {
        return ResponseEntity.ok(tableResponseMapper.toTableResponses(tableService.getAvailableTablesForParty(partySize)));
    }

    /**
     * Lấy thông tin một bàn theo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableResponseMapper.toTableResponse(tableService.getTableById(id)));
    }

    /**
     * Tạo bàn mới (Admin).
     */
    @PostMapping
    public ResponseEntity<TableResponse> createTable(@Valid @RequestBody TableRequest request) {
        return new ResponseEntity<>(tableResponseMapper.toTableResponse(tableService.createTable(request)), HttpStatus.CREATED);
    }

    /**
     * Cập nhật trạng thái bàn (UC10 — xếp bàn, dọn bàn...).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TableResponse> updateTableStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TableStatusUpdateRequest request) {
        return ResponseEntity.ok(tableResponseMapper.toTableResponse(tableService.updateTableStatus(id, request)));
    }

    /**
     * UC10 — Xếp bàn cho khách (từ RESERVATION, WAITLIST hoặc WALK_IN).
     */
    @PostMapping("/seat")
    public ResponseEntity<TableResponse> seatGuest(@Valid @RequestBody SeatGuestRequest request) {
        return ResponseEntity.ok(tableResponseMapper.toTableResponse(tableService.seatGuest(request)));
    }

    @PutMapping("/{fromTableId}/move/{toTableId}")
    public ResponseEntity<TableResponse> moveTable(
            @PathVariable UUID fromTableId,
            @PathVariable UUID toTableId) {
        return ResponseEntity.ok(tableResponseMapper.toTableResponse(tableService.moveTable(fromTableId, toTableId)));
    }
}
