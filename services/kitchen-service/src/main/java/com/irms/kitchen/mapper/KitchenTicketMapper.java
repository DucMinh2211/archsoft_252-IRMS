package com.irms.kitchen.mapper;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.domain.KitchenTicketItem;
import com.irms.kitchen.dto.KitchenTicketDto;
import com.irms.kitchen.dto.KitchenTicketItemDto;
import com.irms.kitchen.service.KitchenTicketTimingPolicy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class KitchenTicketMapper {

    private final KitchenTicketTimingPolicy ticketTimingPolicy;

    public KitchenTicketMapper(KitchenTicketTimingPolicy ticketTimingPolicy) {
        this.ticketTimingPolicy = ticketTimingPolicy;
    }

    public KitchenTicketDto toDto(KitchenTicket ticket) {
        if (ticket == null) {
            return null;
        }

        return KitchenTicketDto.builder()
                .id(ticket.getId())
                .orderId(ticket.getOrderId())
                .tableId(ticket.getTableId())
                .status(ticket.getStatus())
                .expectedReadyTime(ticket.getExpectedReadyTime())
                .createdAt(ticket.getCreatedAt())
                .isBreached(ticketTimingPolicy.isTicketBreached(ticket))
                .isAtRisk(ticketTimingPolicy.isTicketAtRisk(ticket))
                .items(ticket.getItems() != null ? 
                        ticket.getItems().stream().map(this::toItemDto).collect(Collectors.toList()) : null)
                .build();
    }

    public KitchenTicketItemDto toItemDto(KitchenTicketItem item) {
        if (item == null) {
            return null;
        }

        return KitchenTicketItemDto.builder()
                .id(item.getId())
                .ticketId(item.getTicket() != null ? item.getTicket().getId() : null)
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .notes(item.getNotes())
                .station(item.getStation())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
