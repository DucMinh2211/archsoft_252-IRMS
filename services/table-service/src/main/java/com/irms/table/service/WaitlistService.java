package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.domain.WaitlistEntry;
import com.irms.table.domain.WaitlistStatus;
import com.irms.table.dto.WaitlistRequest;
import com.irms.table.infrastructure.sse.SseBroadcaster;
import com.irms.table.repository.RestaurantTableRepository;
import com.irms.table.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WaitlistService {

    private static final int AVG_DINE_TIME_MINUTES = 60;
    private static final int OVERDUE_THRESHOLD_MINUTES = 60;
    private static final int OVERDUE_PENALTY_PER_TABLE = 10;
    private static final int MIN_WAIT_MINUTES = 5;

    private final WaitlistRepository waitlistRepository;
    private final RestaurantTableRepository tableRepository;
    private final SseBroadcaster sseBroadcaster;

    // ────────────────────────────────────────────────────────────
    // Truy vấn
    // ────────────────────────────────────────────────────────────

    public List<WaitlistEntry> getActiveWaitlist() {
        return waitlistRepository.findByStatusInOrderByCreatedAtAsc(
                Arrays.asList(WaitlistStatus.WAITING, WaitlistStatus.NOTIFIED));
    }

    public List<WaitlistEntry> getAllWaitlistEntries() {
        return waitlistRepository.findAll();
    }

    public WaitlistEntry getEntryById(UUID id) {
        return waitlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách trong danh sách chờ với id: " + id));
    }

    // ────────────────────────────────────────────────────────────
    // Thêm vào danh sách chờ
    // ────────────────────────────────────────────────────────────

    @Transactional
    public WaitlistEntry addToWaitlist(WaitlistRequest request) {
        List<WaitlistEntry> currentWaiting = waitlistRepository.findByStatusOrderByCreatedAtAsc(WaitlistStatus.WAITING);
        int position = currentWaiting.size(); // Vị trí mới (0-indexed)

        List<RestaurantTable> occupiedTables = tableRepository.findByStatus(TableStatus.OCCUPIED);
        
        long overdueCount = countOverdueTables(occupiedTables);
        int estimatedWait = calculateLinearWait(position, occupiedTables.size(), (int) overdueCount);

        WaitlistEntry entry = WaitlistEntry.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .partySize(request.getPartySize())
                .status(WaitlistStatus.WAITING)
                .estimatedWaitMinutes(estimatedWait)
                .queuePosition(position + 1)
                .build();

        WaitlistEntry savedW = waitlistRepository.save(entry);
        sseBroadcaster.broadcast("waitlist.changed", Map.of("id", savedW.getId(), "status", savedW.getStatus().name()));
        return savedW;
    }

    // ────────────────────────────────────────────────────────────
    // Cập nhật trạng thái
    // ────────────────────────────────────────────────────────────

    @Transactional
    public WaitlistEntry notifyGuest(UUID id) {
        WaitlistEntry entry = getEntryById(id);
        if (entry.getStatus() != WaitlistStatus.WAITING) {
            throw new RuntimeException("Chỉ có thể thông báo cho khách đang ở trạng thái WAITING");
        }
        entry.setStatus(WaitlistStatus.NOTIFIED);
        entry.setNotifiedAt(LocalDateTime.now());
        WaitlistEntry savedW = waitlistRepository.save(entry);
        sseBroadcaster.broadcast("waitlist.changed", Map.of("id", savedW.getId(), "status", savedW.getStatus().name()));
        return savedW;
    }

    @Transactional
    public WaitlistEntry seatFromWaitlist(UUID id, UUID tableId) {
        WaitlistEntry entry = getEntryById(id);
        if (entry.getStatus() == WaitlistStatus.SEATED || entry.getStatus() == WaitlistStatus.LEFT) {
            throw new RuntimeException("Khách này đã được xếp bàn hoặc đã rời đi");
        }

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với id: " + tableId));

        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new RuntimeException("Bàn " + table.getTableNumber() + " không còn trống");
        }

        table.setStatus(TableStatus.OCCUPIED);
        table.setSeatedAt(LocalDateTime.now());
        tableRepository.save(table);

        entry.setStatus(WaitlistStatus.SEATED);
        WaitlistEntry saved = waitlistRepository.save(entry);

        recalculateWaitTimes();
        return saved;
    }

    @Transactional
    public WaitlistEntry removeFromWaitlist(UUID id) {
        WaitlistEntry entry = getEntryById(id);
        entry.setStatus(WaitlistStatus.LEFT);
        WaitlistEntry saved = waitlistRepository.save(entry);
        sseBroadcaster.broadcast("waitlist.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
        recalculateWaitTimes();
        return saved;
    }

    // ────────────────────────────────────────────────────────────
    // Tính toán thời gian chờ (UC12)
    // ────────────────────────────────────────────────────────────

    @Scheduled(fixedRate = 60000) // Cập nhật mỗi 1 phút cho chính xác
    @Transactional
    public void recalculateScheduled() {
        recalculateWaitTimes();
        sseBroadcaster.broadcast("waitlist.recalculated", Map.of("timestamp", LocalDateTime.now()));
    }

    @Transactional
    public void recalculateWaitTimes() {
        List<WaitlistEntry> waitingEntries = waitlistRepository
                .findByStatusOrderByCreatedAtAsc(WaitlistStatus.WAITING);

        if (waitingEntries.isEmpty()) return;

        List<RestaurantTable> occupiedTables = tableRepository.findByStatus(TableStatus.OCCUPIED);
        long overdueCount = countOverdueTables(occupiedTables);

        for (int i = 0; i < waitingEntries.size(); i++) {
            WaitlistEntry entry = waitingEntries.get(i);
            entry.setQueuePosition(i + 1);
            entry.setEstimatedWaitMinutes(calculateLinearWait(i, occupiedTables.size(), (int) overdueCount));
        }

        waitlistRepository.saveAll(waitingEntries);
    }

    // ────────────────────────────────────────────────────────────
    // Helper
    // ────────────────────────────────────────────────────────────

    private long countOverdueTables(List<RestaurantTable> tables) {
        return tables.stream()
                .filter(t -> t.getSeatedAt() != null)
                .filter(t -> {
                    long minutes = Duration.between(t.getSeatedAt(), LocalDateTime.now()).toMinutes();
                    return minutes > OVERDUE_THRESHOLD_MINUTES;
                })
                .count();
    }

    /**
     * Thuật toán tính ETA tuyến tính:
     * ETA = (Thời gian ăn TB / Số bàn bận) * (Vị trí + 1) + (Số bàn trễ * Phạt)
     */
    private int calculateLinearWait(int position, int occupiedCount, int overdueCount) {
        if (occupiedCount == 0) return MIN_WAIT_MINUTES;

        // Tốc độ quay vòng: cứ X phút có 1 bàn trống
        double turnoverRateMinutes = (double) AVG_DINE_TIME_MINUTES / Math.max(occupiedCount, 1);
        
        // Thời gian chờ gốc dựa trên vị trí
        double baseWait = turnoverRateMinutes * (position + 1);
        
        // Cộng dồn trễ hệ thống
        int penalty = overdueCount * OVERDUE_PENALTY_PER_TABLE;
        
        int total = (int) Math.round(baseWait + penalty);
        return Math.max(total, MIN_WAIT_MINUTES);
    }
}
