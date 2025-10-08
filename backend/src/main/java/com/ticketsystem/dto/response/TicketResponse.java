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
public class TicketResponse {
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private String status;
    private String priority;
    
    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;
    
    // Agent info
    private Long assignedAgentId;
    private String assignedAgentName;
    
    // SLA info
    private LocalDateTime slaResponseDueAt;
    private LocalDateTime slaResolutionDueAt;
    private Boolean slaBreached;
    private Boolean escalated;
    private Long minutesUntilDue; // Time remaining until SLA breach
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    
    // Calculated fields
    private Integer commentCount;
    private Boolean isOverdue;
}