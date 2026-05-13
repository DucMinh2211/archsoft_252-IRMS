package com.irms.table.service;

import com.irms.table.domain.RestaurantTable;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaitTimeEstimatorTest {

    private final WaitTimeEstimator estimator = new WaitTimeEstimator();

    @Test
    void calculateLinearWait_ShouldPreserveCurrentFormula() {
        assertEquals(5, estimator.calculateLinearWait(0, 0, 0));
        assertEquals(60, estimator.calculateLinearWait(0, 1, 0));
        assertEquals(40, estimator.calculateLinearWait(1, 3, 0));
        assertEquals(50, estimator.calculateLinearWait(1, 3, 1));
    }

    @Test
    void estimateWaitMinutes_ShouldAddPenaltyForOverdueTables() {
        RestaurantTable overdue = new RestaurantTable();
        overdue.setSeatedAt(LocalDateTime.now().minusMinutes(61));

        RestaurantTable notOverdue = new RestaurantTable();
        notOverdue.setSeatedAt(LocalDateTime.now().minusMinutes(30));

        assertEquals(40, estimator.estimateWaitMinutes(0, List.of(overdue, notOverdue)));
    }
}
