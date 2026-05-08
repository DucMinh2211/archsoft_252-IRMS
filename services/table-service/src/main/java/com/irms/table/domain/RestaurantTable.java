package com.irms.table.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.irms.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RestaurantTable extends BaseEntity {

    @Column(name = "table_number", nullable = false, unique = true, length = 20)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(length = 100)
    private String location;

    // ID đơn hàng đang phục vụ tại bàn này (nullable khi bàn trống)
    @Column(name = "current_order_id")
    private UUID currentOrderId;

    // Thời điểm khách bắt đầu ngồi vào bàn
    @Column(name = "seated_at")
    private LocalDateTime seatedAt;
}
