package com.ticketsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStatsResponse {
    private Long agentId;
    private String agentName;
    private Long assignedTickets;
    private Long resolvedTickets;
    private Long overdueTickets;
    private Double slaComplianceRate;
    private Double averageResolutionTimeHours;
}