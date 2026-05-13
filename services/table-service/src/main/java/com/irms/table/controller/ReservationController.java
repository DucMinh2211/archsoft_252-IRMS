package com.irms.table.controller;

import com.irms.table.domain.ReservationStatus;
import com.irms.table.dto.ReservationRequest;
import com.irms.table.dto.ReservationResponse;
import com.irms.table.mapper.TableResponseMapper;
import com.irms.table.service.ReservationManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationManagementService reservationService;
    private final TableResponseMapper tableResponseMapper;

    /**
     * UC08 — Lấy danh sách đặt bàn.
     * Query param: ?status=CONFIRMED để lọc theo trạng thái.
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @RequestParam(required = false) ReservationStatus status) {
        if (status != null) {
            return ResponseEntity.ok(tableResponseMapper.toReservationResponses(reservationService.getReservationsByStatus(status)));
        }
        return ResponseEntity.ok(tableResponseMapper.toReservationResponses(reservationService.getAllReservations()));
    }

    /**
     * Lấy đặt bàn trong khoảng thời gian (cho lịch ngày).
     */
    @GetMapping("/between")
    public ResponseEntity<List<ReservationResponse>> getReservationsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(tableResponseMapper.toReservationResponses(reservationService.getReservationsBetween(from, to)));
    }

    /**
     * Lấy thông tin một đặt bàn.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable UUID id) {
        return ResponseEntity.ok(tableResponseMapper.toReservationResponse(reservationService.getReservationById(id)));
    }

    /**
     * UC08 — Tạo đặt bàn mới.
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(tableResponseMapper.toReservationResponse(reservationService.createReservation(request)), HttpStatus.CREATED);
    }

    /**
     * UC08 — Xác nhận đặt bàn và gán bàn cụ thể.
     * Query param: ?tableId=UUID
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable UUID id,
            @RequestParam UUID tableId) {
        return ResponseEntity.ok(tableResponseMapper.toReservationResponse(reservationService.confirmReservation(id, tableId)));
    }

    /**
     * UC08 — Hủy đặt bàn.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable UUID id) {
        return ResponseEntity.ok(tableResponseMapper.toReservationResponse(reservationService.cancelReservation(id)));
    }

    /**
     * Đánh dấu khách No-Show.
     */
    @PutMapping("/{id}/no-show")
    public ResponseEntity<ReservationResponse> markNoShow(@PathVariable UUID id) {
        return ResponseEntity.ok(tableResponseMapper.toReservationResponse(reservationService.markNoShow(id)));
    }
}
