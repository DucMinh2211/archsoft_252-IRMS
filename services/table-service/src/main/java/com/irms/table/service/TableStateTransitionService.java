package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.dto.TableStatusUpdateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TableStateTransitionService {

    public void applyStatusUpdate(RestaurantTable table, TableStatusUpdateRequest request) {
        table.setStatus(request.getStatus());
        table.setCurrentOrderId(request.getCurrentOrderId());

        if (request.getStatus() == TableStatus.OCCUPIED) {
            table.setSeatedAt(LocalDateTime.now());
        } else if (request.getStatus() == TableStatus.AVAILABLE || request.getStatus() == TableStatus.CLEANING) {
            table.setSeatedAt(null);
        }
    }

    public void markOccupied(RestaurantTable table) {
        table.setStatus(TableStatus.OCCUPIED);
        table.setSeatedAt(LocalDateTime.now());
    }

    public void copyOccupancy(RestaurantTable fromTable, RestaurantTable toTable) {
        toTable.setStatus(TableStatus.OCCUPIED);
        toTable.setCurrentOrderId(fromTable.getCurrentOrderId());
        toTable.setSeatedAt(fromTable.getSeatedAt());
    }

    public void markAvailable(RestaurantTable table) {
        table.setStatus(TableStatus.AVAILABLE);
        table.setCurrentOrderId(null);
        table.setSeatedAt(null);
    }
}
