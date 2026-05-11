package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import com.irms.table.domain.TableStatus;
import com.irms.table.exception.TableBusinessException;
import org.springframework.stereotype.Component;

@Component
public class TableSeatingPolicy {

    public void validateCanSeat(RestaurantTable table) {
        if (table.getStatus() != TableStatus.AVAILABLE && table.getStatus() != TableStatus.RESERVED) {
            throw new TableBusinessException("Bàn " + table.getTableNumber() + " không sẵn sàng để xếp khách (trạng thái: " + table.getStatus() + ")");
        }
    }

    public void validateAvailable(RestaurantTable table) {
        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new TableBusinessException("Bàn " + table.getTableNumber() + " không còn trống");
        }
    }

    public void validateMoveSource(RestaurantTable table) {
        if (table.getStatus() != TableStatus.OCCUPIED) {
            throw new TableBusinessException("Chỉ có thể chuyển bàn đang có khách (OCCUPIED)");
        }
    }

    public void validateMoveDestination(RestaurantTable table) {
        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new TableBusinessException("Bàn đích " + table.getTableNumber() + " không còn trống");
        }
    }
}
