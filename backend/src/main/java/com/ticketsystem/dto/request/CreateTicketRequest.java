package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTicketRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
}