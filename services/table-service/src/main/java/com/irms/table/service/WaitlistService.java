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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WaitlistService {

    private static final int ESTIMATED_MINUTES_PER_TABLE = 20;

    private final WaitlistRepository waitlistRepository;
    private final RestaurantTableRepository tableRepository;
    private final SseBroadcaster sseBroadcaster;

    // ────────────────────────────────────────────────────────────
    // Truy vấn
    // ────────────────────────────────────────────────────────────

    /**
     * Lấy danh sách chờ hiện tại (WAITING + NOTIFIED) — UC09, UC12.
     */
    public List<WaitlistEntry> getActiveWaitlist() {
        return waitlistRepository.findByStatusInOrderByCreatedAtAsc(
                Arrays.asList(WaitlistStatus.WAITING, WaitlistStatus.NOTIFIED));
    }

    /**
     * Lấy toàn bộ danh sách chờ.
     */
    public List<WaitlistEntry> getAllWaitlistEntries() {
        return waitlistRepository.findAll();
    }

    /**
     * Lấy một entry theo ID.
     */
    public WaitlistEntry getEntryById(UUID id) {
        return waitlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách trong danh sách chờ với id: " + id));
    }

    // ────────────────────────────────────────────────────────────
    // Thêm vào danh sách chờ
    // ────────────────────────────────────────────────────────────

    /**
     * Thêm khách vào danh sách chờ và tính thời gian chờ dự kiến (UC09).
     */
    @Transactional
    public WaitlistEntry addToWaitlist(WaitlistRequest request) {
        long currentWaiting = waitlistRepository.countByStatus(WaitlistStatus.WAITING);
        long occupiedTables = tableRepository.findByStatus(TableStatus.OCCUPIED).size();

        // Ước tính đơn giản: mỗi bàn chiếm trung bình ESTIMATED_MINUTES_PER_TABLE phút
        int estimatedWait = calculateEstimatedWait((int) currentWaiting, (int) occupiedTables);

        WaitlistEntry entry = WaitlistEntry.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .partySize(request.getPartySize())
                .status(WaitlistStatus.WAITING)
                .estimatedWaitMinutes(estimatedWait)
                .queuePosition((int) currentWaiting + 1)
                .build();

        WaitlistEntry savedW = waitlistRepository.save(entry);
        sseBroadcaster.broadcast("waitlist.changed", Map.of("id", savedW.getId(), "status", savedW.getStatus().name()));
        return savedW;
    }

    // ────────────────────────────────────────────────────────────
    // Cập nhật trạng thái
    // ────────────────────────────────────────────────────────────

    /**
     * Thông báo cho khách bàn đã sẵn sàng (UC11).
     * Cập nhật status → NOTIFIED và ghi nhận thời điểm thông báo.
     */
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

    /**
     * Xếp bàn cho khách từ danh sách chờ (UC10).
     */
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

        // Cập nhật bàn
        table.setStatus(TableStatus.OCCUPIED);
        table.setSeatedAt(LocalDateTime.now());
        tableRepository.save(table);

        // Cập nhật entry
        entry.setStatus(WaitlistStatus.SEATED);
        WaitlistEntry saved = waitlistRepository.save(entry);

        // Tính lại thời gian chờ cho các entry còn lại
        recalculateWaitTimes();

        return saved;
    }

    /**
     * Đánh dấu khách đã rời đi khỏi hàng chờ (hủy chờ) (UC09).
     */
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

    /**
     * Tính lại thời gian chờ dự kiến và vị trí hàng đợi cho tất cả khách đang WAITING.
     * Gọi khi có bàn mới giải phóng hoặc khách rời hàng.
     */
    @Transactional
    public void recalculateWaitTimes() {
        List<WaitlistEntry> waitingEntries = waitlistRepository
                .findByStatusOrderByCreatedAtAsc(WaitlistStatus.WAITING);

        long occupiedTables = tableRepository.findByStatus(TableStatus.OCCUPIED).size();

        for (int i = 0; i < waitingEntries.size(); i++) {
            WaitlistEntry entry = waitingEntries.get(i);
            entry.setQueuePosition(i + 1);
            entry.setEstimatedWaitMinutes(calculateEstimatedWait(i, (int) occupiedTables));
        }

        waitlistRepository.saveAll(waitingEntries);
    }

    // ────────────────────────────────────────────────────────────
    // Helper
    // ────────────────────────────────────────────────────────────

    /**
     * Ước tính thời gian chờ dựa trên vị trí trong hàng và số bàn đang bận.
     *
     * @param positionInQueue Vị trí trong hàng đợi (0-indexed)
     * @param occupiedTables  Số bàn đang có khách
     * @return Thời gian chờ ước tính (phút)
     */
    private int calculateEstimatedWait(int positionInQueue, int occupiedTables) {
        if (occupiedTables == 0) {
            return 5; // Tối thiểu 5 phút để dọn bàn
        }
        // Mỗi nhóm khách cần chờ ít nhất 1 vòng bàn bận
        int roundsToWait = (positionInQueue / Math.max(occupiedTables, 1)) + 1;
        return roundsToWait * ESTIMATED_MINUTES_PER_TABLE;
    }
}
