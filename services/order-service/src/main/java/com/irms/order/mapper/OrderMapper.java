package com.irms.order.mapper;

import com.irms.order.domain.Order;
import com.irms.order.domain.OrderItem;
import com.irms.order.dto.OrderItemRequestDTO;
import com.irms.order.dto.OrderItemResponseDTO;
import com.irms.order.dto.OrderRequestDTO;
import com.irms.order.dto.OrderResponseDTO;
import com.irms.order.service.OrderCreateInput;
import com.irms.order.service.OrderItemInput;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponseDTO toDto(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponseDTO.builder()
                .id(order.getId())
                .tableId(order.getTableId())
                .waiterId(order.getWaiterId())
                .status(order.getStatus())
                .type(order.getType())
                .totalAmount(order.getTotalAmount())
                .specialNote(order.getSpecialNote())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems() != null ? 
                        order.getItems().stream().map(this::toDto).collect(Collectors.toList()) : null)
                .build();
    }

    public OrderItemResponseDTO toDto(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .note(item.getNote())
                .status(item.getStatus())
                .build();
    }

    public List<OrderResponseDTO> toDtos(List<Order> orders) {
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    public OrderCreateInput toOrderCreateInput(OrderRequestDTO request) {
        return new OrderCreateInput(
                request.getTableId(),
                request.getWaiterId(),
                request.getType(),
                request.getSpecialNote(),
                request.getItems() != null ? request.getItems().stream().map(this::toOrderItemInput).collect(Collectors.toList()) : null
        );
    }

    public OrderItemInput toOrderItemInput(OrderItemRequestDTO request) {
        return new OrderItemInput(
                request.getMenuItemId(),
                request.getQuantity(),
                request.getNote()
        );
    }
}
