package com.ricardofiorini.hub.controller;

import com.ricardofiorini.hub.dto.TicketDtos.*;
import com.ricardofiorini.hub.model.Channel;
import com.ricardofiorini.hub.model.TicketStatus;
import com.ricardofiorini.hub.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // Endpoint público — simula recebimento de ticket vindo de um canal externo
    // (formulário do site, webhook de e-mail, chat, etc.)
    @PostMapping("/public")
    public ResponseEntity<TicketResponse> createFromPublicChannel(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.ok(ticketService.createTicket(request));
    }

    @GetMapping
    public ResponseEntity<Page<TicketSummary>> listTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Channel channel,
            Pageable pageable) {
        return ResponseEntity.ok(ticketService.listTickets(status, channel, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable Long id,
                                                        @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<TicketResponse> addMessage(@PathVariable Long id,
                                                      @Valid @RequestBody NewMessageRequest request,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ticketService.addMessage(id, userDetails.getUsername(), request));
    }
}
