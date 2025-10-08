package com.ticketsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;
    
    private Boolean isInternal = false; // Public comment by default
}