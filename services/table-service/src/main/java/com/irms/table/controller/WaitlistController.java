package com.irms.table.controller;

import com.irms.table.dto.WaitlistRequest;
import com.irms.table.dto.WaitlistEntryResponse;
import com.irms.table.mapper.TableResponseMapper;
import com.irms.table.service.WaitlistManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistManagementService waitlistService;
    private final TableResponseMapper tableResponseMapper;

    /**
     * UC09 / UC12 — Lấy danh sách khách đang chờ (WAITING + NOTIFIED).
     */
    @GetMapping
    public ResponseEntity<List<WaitlistEntryResponse>> getActiveWaitlist() {
        return ResponseEntity.ok(tableResponseMapper.toWaitlistEntryResponses(waitlistService.getActiveWaitlist()));
    }

    /**
     * UC09 — Thêm khách vào danh sách chờ.
     */
    @PostMapping
    public ResponseEntity<WaitlistEntryResponse> addToWaitlist(@Valid @RequestBody WaitlistRequest request) {
        return new ResponseEntity<>(tableResponseMapper.toWaitlistEntryResponse(waitlistService.addToWaitlist(request)), HttpStatus.CREATED);
    }

    /**
     * UC11 — Thông báo cho khách bàn đã sẵn sàng.
     */
    @PutMapping("/{id}/notify")
    public ResponseEntity<WaitlistEntryResponse> notifyGuest(@PathVariable UUID id) {
        return ResponseEntity.ok(tableResponseMapper.toWaitlistEntryResponse(waitlistService.notifyGuest(id)));
    }

    /**
     * UC10 — Xếp bàn từ danh sách chờ.
     * Query param: ?tableId=UUID
     */
    @PutMapping("/{id}/seat")
    public ResponseEntity<WaitlistEntryResponse> seatFromWaitlist(
            @PathVariable UUID id,
            @RequestParam UUID tableId) {
        return ResponseEntity.ok(tableResponseMapper.toWaitlistEntryResponse(waitlistService.seatFromWaitlist(id, tableId)));
    }

    /**
     * UC09 — Xóa khách khỏi danh sách chờ (khách từ chối chờ).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<WaitlistEntryResponse> removeFromWaitlist(@PathVariable UUID id) {
        return ResponseEntity.ok(tableResponseMapper.toWaitlistEntryResponse(waitlistService.removeFromWaitlist(id)));
    }

    /**
     * UC12 — Tính lại thời gian chờ thủ công (cũng được tự động gọi khi có thay đổi).
     */
    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculateWaitTimes() {
        waitlistService.recalculateWaitTimes();
        return ResponseEntity.ok().build();
    }
}
