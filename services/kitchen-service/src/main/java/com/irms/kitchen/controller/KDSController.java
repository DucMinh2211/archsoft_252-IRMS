package com.irms.kitchen.controller;

import com.irms.kitchen.domain.StationType;
import com.irms.kitchen.dto.KitchenTicketDto;
import com.irms.kitchen.dto.KitchenTicketItemDto;
import com.irms.kitchen.dto.UpdateItemStatusRequest;
import com.irms.kitchen.mapper.KitchenTicketMapper;
import com.irms.kitchen.service.KdsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/kds")
@RequiredArgsConstructor
public class KDSController {

    private final KdsService kitchenService;
    private final KitchenTicketMapper kitchenTicketMapper;

    @GetMapping("/tickets")
    public ResponseEntity<List<KitchenTicketDto>> getActiveTickets() {
        List<KitchenTicketDto> tickets = kitchenService.getActiveTickets().stream()
                .map(kitchenTicketMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/stations/{station}/items")
    public ResponseEntity<List<KitchenTicketItemDto>> getItemsByStation(@PathVariable StationType station) {
        List<KitchenTicketItemDto> items = kitchenService.getItemsByStation(station).stream()
                .map(kitchenTicketMapper::toItemDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @PutMapping("/items/{itemId}/status")
    public ResponseEntity<Void> updateItemStatus(@PathVariable UUID itemId, 
                                                 @Valid @RequestBody UpdateItemStatusRequest request) {
        kitchenService.updateItemStatus(itemId, request.getStatus());
        return ResponseEntity.noContent().build();
    }
    
    // Add additional endpoint to update ticket status directly if needed (e.g. for complete cancellation)
    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<Void> updateTicketStatus(@PathVariable UUID ticketId, 
                                                   @RequestBody com.irms.kitchen.domain.TicketStatus status) {
        kitchenService.updateTicketStatus(ticketId, status);
        return ResponseEntity.noContent().build();
    }
}
