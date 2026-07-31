package com.ricardofiorini.hub.dto;

import com.ricardofiorini.hub.model.Channel;
import com.ricardofiorini.hub.model.Priority;
import com.ricardofiorini.hub.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDtos {

    public record CreateTicketRequest(
            @NotBlank String subject,
            @NotBlank String customerName,
            @NotBlank String customerContact,
            @NotNull Channel channel,
            @NotBlank String initialMessage
    ) {}

    public record UpdateTicketRequest(
            TicketStatus status,
            Priority priority,
            Long assignedAgentId
    ) {}

    public record NewMessageRequest(
            @NotBlank String content
    ) {}

    public record MessageResponse(
            Long id,
            String author,
            String senderType,
            String content,
            LocalDateTime createdAt
    ) {}

    public record TicketResponse(
            Long id,
            String subject,
            String customerName,
            String customerContact,
            Channel channel,
            TicketStatus status,
            Priority priority,
            String assignedAgentName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<MessageResponse> messages
    ) {}

    public record TicketSummary(
            Long id,
            String subject,
            String customerName,
            Channel channel,
            TicketStatus status,
            Priority priority,
            LocalDateTime updatedAt
    ) {}
}
