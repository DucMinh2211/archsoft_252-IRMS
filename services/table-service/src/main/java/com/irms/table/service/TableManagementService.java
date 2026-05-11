package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.dto.SeatGuestRequest;
import com.irms.table.dto.TableRequest;
import com.irms.table.dto.TableStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface TableManagementService {
    List<RestaurantTable> getAllTables();
    List<RestaurantTable> getTablesByStatus(TableStatus status);
    List<RestaurantTable> getAvailableTablesForParty(Integer partySize);
    RestaurantTable getTableById(UUID id);
    RestaurantTable createTable(TableRequest request);
    RestaurantTable updateTableStatus(UUID tableId, TableStatusUpdateRequest request);
    RestaurantTable seatGuest(SeatGuestRequest request);
    RestaurantTable moveTable(UUID fromTableId, UUID toTableId);
}
