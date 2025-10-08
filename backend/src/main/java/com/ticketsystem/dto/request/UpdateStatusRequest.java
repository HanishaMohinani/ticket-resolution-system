package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotBlank(message = "Status is required")
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
}