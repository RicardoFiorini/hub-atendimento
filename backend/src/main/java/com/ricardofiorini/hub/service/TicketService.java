package com.ricardofiorini.hub.service;

import com.ricardofiorini.hub.dto.TicketDtos.*;
import com.ricardofiorini.hub.model.*;
import com.ricardofiorini.hub.repository.AgentRepository;
import com.ricardofiorini.hub.repository.TicketMessageRepository;
import com.ricardofiorini.hub.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final AgentRepository agentRepository;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = Ticket.builder()
                .subject(request.subject())
                .customerName(request.customerName())
                .customerContact(request.customerContact())
                .channel(request.channel())
                .build();

        TicketMessage firstMessage = TicketMessage.builder()
                .ticket(ticket)
                .author(request.customerName())
                .senderType(TicketMessage.Sender.CUSTOMER)
                .content(request.initialMessage())
                .build();

        ticket.getMessages().add(firstMessage);

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    public Page<TicketSummary> listTickets(TicketStatus status, Channel channel, Pageable pageable) {
        Page<Ticket> page;
        if (status != null && channel != null) {
            page = ticketRepository.findByStatusAndChannel(status, channel, pageable);
        } else if (status != null) {
            page = ticketRepository.findByStatus(status, pageable);
        } else if (channel != null) {
            page = ticketRepository.findByChannel(channel, pageable);
        } else {
            page = ticketRepository.findAll(pageable);
        }
        return page.map(this::toSummary);
    }

    public TicketResponse getTicket(Long id) {
        Ticket ticket = findTicketOrThrow(id);
        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = findTicketOrThrow(id);

        if (request.status() != null) ticket.setStatus(request.status());
        if (request.priority() != null) ticket.setPriority(request.priority());
        if (request.assignedAgentId() != null) {
            Agent agent = agentRepository.findById(request.assignedAgentId())
                    .orElseThrow(() -> new EntityNotFoundException("Agente não encontrado"));
            ticket.setAssignedAgent(agent);
        }

        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse addMessage(Long ticketId, String authorEmail, NewMessageRequest request) {
        Ticket ticket = findTicketOrThrow(ticketId);
        Agent agent = agentRepository.findByEmail(authorEmail).orElseThrow();

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .author(agent.getName())
                .senderType(TicketMessage.Sender.AGENT)
                .content(request.content())
                .build();

        ticket.getMessages().add(message);
        messageRepository.save(message);

        return toResponse(ticketRepository.save(ticket));
    }

    private Ticket findTicketOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket não encontrado: " + id));
    }

    private TicketSummary toSummary(Ticket t) {
        return new TicketSummary(t.getId(), t.getSubject(), t.getCustomerName(), t.getChannel(),
                t.getStatus(), t.getPriority(), t.getUpdatedAt());
    }

    private TicketResponse toResponse(Ticket t) {
        List<MessageResponse> messages = t.getMessages().stream()
                .map(m -> new MessageResponse(m.getId(), m.getAuthor(), m.getSenderType().name(), m.getContent(), m.getCreatedAt()))
                .toList();

        return new TicketResponse(
                t.getId(), t.getSubject(), t.getCustomerName(), t.getCustomerContact(),
                t.getChannel(), t.getStatus(), t.getPriority(),
                t.getAssignedAgent() != null ? t.getAssignedAgent().getName() : null,
                t.getCreatedAt(), t.getUpdatedAt(), messages
        );
    }
}
