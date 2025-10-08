package com.ticketsystem.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    // Overall counts
    private Long totalTickets;
    private Long openTickets;
    private Long inProgressTickets;
    private Long resolvedTickets;
    private Long closedTickets;
    
    // SLA metrics
    private Long slaBreachedTickets;
    private Double slaComplianceRate; // Percentage
    
    // Time metrics
    private Double averageResponseTimeHours;
    private Double averageResolutionTimeHours;
    
    // Priority breakdown
    private Map<String, Long> ticketsByPriority;
    
    // Status breakdown
    private Map<String, Long> ticketsByStatus;
}