package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.domain.WaitlistEntry;
import com.irms.table.domain.WaitlistStatus;
import com.irms.table.dto.WaitlistRequest;
import com.irms.table.exception.TableResourceNotFoundException;
import com.irms.table.repository.RestaurantTableRepository;
import com.irms.table.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WaitlistService implements WaitlistManagementService {

    private final WaitlistRepository waitlistRepository;
    private final RestaurantTableRepository tableRepository;
    private final WaitTimeEstimator waitTimeEstimator;
    private final TableSeatingPolicy tableSeatingPolicy;
    private final WaitlistStatusPolicy waitlistStatusPolicy;
    private final TableEventPublisher eventPublisher;

    // ────────────────────────────────────────────────────────────
    // Truy vấn
    // ────────────────────────────────────────────────────────────

    @Override
    public List<WaitlistEntry> getActiveWaitlist() {
        return waitlistRepository.findByStatusInOrderByCreatedAtAsc(
                Arrays.asList(WaitlistStatus.WAITING, WaitlistStatus.NOTIFIED));
    }

    @Override
    public List<WaitlistEntry> getAllWaitlistEntries() {
        return waitlistRepository.findAll();
    }

    @Override
    public WaitlistEntry getEntryById(UUID id) {
        return waitlistRepository.findById(id)
                .orElseThrow(() -> new TableResourceNotFoundException("Không tìm thấy khách trong danh sách chờ với id: " + id));
    }

    // ────────────────────────────────────────────────────────────
    // Thêm vào danh sách chờ
    // ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WaitlistEntry addToWaitlist(WaitlistRequest request) {
        List<WaitlistEntry> currentWaiting = waitlistRepository.findByStatusOrderByCreatedAtAsc(WaitlistStatus.WAITING);
        int position = currentWaiting.size(); // Vị trí mới (0-indexed)

        List<RestaurantTable> occupiedTables = tableRepository.findByStatus(TableStatus.OCCUPIED);
        int estimatedWait = waitTimeEstimator.estimateWaitMinutes(position, occupiedTables);

        WaitlistEntry entry = WaitlistEntry.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .partySize(request.getPartySize())
                .status(WaitlistStatus.WAITING)
                .estimatedWaitMinutes(estimatedWait)
                .queuePosition(position + 1)
                .build();

        WaitlistEntry savedW = waitlistRepository.save(entry);
        eventPublisher.broadcast("waitlist.changed", Map.of("id", savedW.getId(), "status", savedW.getStatus().name()));
        return savedW;
    }

    // ────────────────────────────────────────────────────────────
    // Cập nhật trạng thái
    // ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WaitlistEntry notifyGuest(UUID id) {
        WaitlistEntry entry = getEntryById(id);
        waitlistStatusPolicy.validateCanNotify(entry);
        entry.setStatus(WaitlistStatus.NOTIFIED);
        entry.setNotifiedAt(LocalDateTime.now());
        WaitlistEntry savedW = waitlistRepository.save(entry);
        eventPublisher.broadcast("waitlist.changed", Map.of("id", savedW.getId(), "status", savedW.getStatus().name()));
        return savedW;
    }

    @Override
    @Transactional
    public WaitlistEntry seatFromWaitlist(UUID id, UUID tableId) {
        WaitlistEntry entry = getEntryById(id);
        waitlistStatusPolicy.validateCanSeat(entry);

        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new TableResourceNotFoundException("Không tìm thấy bàn với id: " + tableId));

        tableSeatingPolicy.validateAvailable(table);

        table.setStatus(TableStatus.OCCUPIED);
        table.setSeatedAt(LocalDateTime.now());
        tableRepository.save(table);

        entry.setStatus(WaitlistStatus.SEATED);
        WaitlistEntry saved = waitlistRepository.save(entry);

        recalculateWaitTimes();
        return saved;
    }

    @Override
    @Transactional
    public WaitlistEntry removeFromWaitlist(UUID id) {
        WaitlistEntry entry = getEntryById(id);
        entry.setStatus(WaitlistStatus.LEFT);
        WaitlistEntry saved = waitlistRepository.save(entry);
        eventPublisher.broadcast("waitlist.changed", Map.of("id", saved.getId(), "status", saved.getStatus().name()));
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
        eventPublisher.broadcast("waitlist.recalculated", Map.of("timestamp", LocalDateTime.now()));
    }

    @Override
    @Transactional
    public void recalculateWaitTimes() {
        List<WaitlistEntry> waitingEntries = waitlistRepository
                .findByStatusOrderByCreatedAtAsc(WaitlistStatus.WAITING);

        if (waitingEntries.isEmpty()) return;

        List<RestaurantTable> occupiedTables = tableRepository.findByStatus(TableStatus.OCCUPIED);
        long overdueCount = waitTimeEstimator.countOverdueTables(occupiedTables);

        for (int i = 0; i < waitingEntries.size(); i++) {
            WaitlistEntry entry = waitingEntries.get(i);
            entry.setQueuePosition(i + 1);
            entry.setEstimatedWaitMinutes(waitTimeEstimator.estimateWaitMinutes(i, occupiedTables.size(), (int) overdueCount));
        }

        waitlistRepository.saveAll(waitingEntries);
    }
}
