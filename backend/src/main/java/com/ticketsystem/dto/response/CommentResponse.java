package com.ticketsystem.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private String content;
    private Boolean isInternal;
    
    // User info
    private Long userId;
    private String userName;
    private String userRole;
    
    private LocalDateTime createdAt;
}