package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignTicketRequest {
    @NotNull(message = "Agent ID is required")
    private Long agentId;
}