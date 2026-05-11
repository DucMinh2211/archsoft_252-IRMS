package com.irms.kitchen.controller;

import com.irms.kitchen.domain.KitchenTicket;
import com.irms.kitchen.dto.CreateTicketRequest;
import com.irms.kitchen.dto.KitchenTicketDto;
import com.irms.kitchen.mapper.KitchenTicketMapper;
import com.irms.kitchen.service.KitchenTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kitchen/tickets")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenTicketService kitchenService;
    private final KitchenTicketMapper kitchenTicketMapper;

    @PostMapping
    public ResponseEntity<KitchenTicketDto> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        KitchenTicket ticket = kitchenService.createTicket(request);
        return new ResponseEntity<>(kitchenTicketMapper.toDto(ticket), HttpStatus.CREATED);
    }
    
    @GetMapping("/{ticketId}")
    public ResponseEntity<KitchenTicketDto> getTicketById(@PathVariable UUID ticketId) {
        KitchenTicket ticket = kitchenService.getTicketById(ticketId);
        return ResponseEntity.ok(kitchenTicketMapper.toDto(ticket));
    }
}
