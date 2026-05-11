package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class WaitTimeEstimator {

    private static final int AVG_DINE_TIME_MINUTES = 60;
    private static final int OVERDUE_THRESHOLD_MINUTES = 60;
    private static final int OVERDUE_PENALTY_PER_TABLE = 10;
    private static final int MIN_WAIT_MINUTES = 5;

    public int estimateWaitMinutes(int position, List<RestaurantTable> occupiedTables) {
        long overdueCount = countOverdueTables(occupiedTables);
        return estimateWaitMinutes(position, occupiedTables.size(), (int) overdueCount);
    }

    public int estimateWaitMinutes(int position, int occupiedCount, int overdueCount) {
        return calculateLinearWait(position, occupiedCount, overdueCount);
    }

    long countOverdueTables(List<RestaurantTable> tables) {
        return tables.stream()
                .filter(t -> t.getSeatedAt() != null)
                .filter(t -> {
                    long minutes = Duration.between(t.getSeatedAt(), LocalDateTime.now()).toMinutes();
                    return minutes > OVERDUE_THRESHOLD_MINUTES;
                })
                .count();
    }

    /**
     * ETA = (Thời gian ăn TB / Số bàn bận) * (Vị trí + 1) + (Số bàn trễ * Phạt)
     */
    int calculateLinearWait(int position, int occupiedCount, int overdueCount) {
        if (occupiedCount == 0) return MIN_WAIT_MINUTES;

        double turnoverRateMinutes = (double) AVG_DINE_TIME_MINUTES / Math.max(occupiedCount, 1);
        double baseWait = turnoverRateMinutes * (position + 1);
        int penalty = overdueCount * OVERDUE_PENALTY_PER_TABLE;

        int total = (int) Math.round(baseWait + penalty);
        return Math.max(total, MIN_WAIT_MINUTES);
    }
}
