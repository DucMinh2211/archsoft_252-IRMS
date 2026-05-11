package com.irms.table.service;

import com.irms.table.domain.WaitlistEntry;
import com.irms.table.dto.WaitlistRequest;

import java.util.List;
import java.util.UUID;

public interface WaitlistManagementService {
    List<WaitlistEntry> getActiveWaitlist();
    List<WaitlistEntry> getAllWaitlistEntries();
    WaitlistEntry getEntryById(UUID id);
    WaitlistEntry addToWaitlist(WaitlistRequest request);
    WaitlistEntry notifyGuest(UUID id);
    WaitlistEntry seatFromWaitlist(UUID id, UUID tableId);
    WaitlistEntry removeFromWaitlist(UUID id);
    void recalculateWaitTimes();
}
