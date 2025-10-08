package com.ticketsystem.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketsystem.dto.response.AgentStatsResponse;
import com.ticketsystem.dto.response.DashboardStatsResponse;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.TicketPriority;
import com.ticketsystem.entity.TicketStatus;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.UserRole;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.UserRepository;

@Service
public class DashboardService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthService authService;
    
    public DashboardStatsResponse getDashboardStats() {
        User currentUser = authService.getCurrentUser();
        Long companyId = currentUser.getCompany().getId();
        
        List<Ticket> allTickets = ticketRepository.findByCompanyId(companyId);
        
        Long totalTickets = (long) allTickets.size();
        Long openTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN)
                .count();
        Long inProgressTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
        Long resolvedTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.RESOLVED)
                .count();
        Long closedTickets = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CLOSED)
                .count();
        
        Long slaBreachedTickets = allTickets.stream()
                .filter(Ticket::getSlaBreached)
                .count();
        
        double slaComplianceRate = totalTickets > 0 ? 
                ((double)(totalTickets - slaBreachedTickets) / totalTickets) * 100 : 100.0;
        
        Double avgResponseTime = calculateAverageResponseTime(allTickets);
        Double avgResolutionTime = calculateAverageResolutionTime(allTickets);
        
        Map<String, Long> ticketsByPriority = new HashMap<>();
        for (TicketPriority priority : TicketPriority.values()) {
            long count = allTickets.stream()
                    .filter(t -> t.getPriority() == priority)
                    .count();
            ticketsByPriority.put(priority.name(), count);
        }
        
        Map<String, Long> ticketsByStatus = new HashMap<>();
        ticketsByStatus.put("OPEN", openTickets);
        ticketsByStatus.put("IN_PROGRESS", inProgressTickets);
        ticketsByStatus.put("RESOLVED", resolvedTickets);
        ticketsByStatus.put("CLOSED", closedTickets);
        
        return DashboardStatsResponse.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .closedTickets(closedTickets)
                .slaBreachedTickets(slaBreachedTickets)
                .slaComplianceRate(slaComplianceRate)
                .averageResponseTimeHours(avgResponseTime)
                .averageResolutionTimeHours(avgResolutionTime)
                .ticketsByPriority(ticketsByPriority)
                .ticketsByStatus(ticketsByStatus)
                .build();
    }
    
    public List<AgentStatsResponse> getAgentStats() {
        User currentUser = authService.getCurrentUser();
        Long companyId = currentUser.getCompany().getId();
        
        List<User> agents = userRepository.findByCompanyIdAndRole(companyId, UserRole.AGENT);
        
        return agents.stream()
                .map(this::calculateAgentStats)
                .collect(Collectors.toList());
    }
    
    public AgentStatsResponse getAgentDashboard() {
        User agent = authService.getCurrentUser();
        return calculateAgentStats(agent);
    }
    

    private AgentStatsResponse calculateAgentStats(User agent) {
        List<Ticket> assignedTickets = ticketRepository.findByAssignedAgentId(agent.getId());
        
        Long totalAssigned = (long) assignedTickets.size();
        Long resolved = assignedTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.RESOLVED || 
                            t.getStatus() == TicketStatus.CLOSED)
                .count();
        
        Long overdue = assignedTickets.stream()
                .filter(Ticket::isOverdue)
                .count();
        
        Long slaBreached = assignedTickets.stream()
                .filter(Ticket::getSlaBreached)
                .count();
        
        double slaCompliance = totalAssigned > 0 ? 
                ((double)(totalAssigned - slaBreached) / totalAssigned) * 100 : 100.0;
        
        Double avgResolutionTime = calculateAverageResolutionTime(assignedTickets);
        
        return AgentStatsResponse.builder()
                .agentId(agent.getId())
                .agentName(agent.getFullName())
                .assignedTickets(totalAssigned)
                .resolvedTickets(resolved)
                .overdueTickets(overdue)
                .slaComplianceRate(slaCompliance)
                .averageResolutionTimeHours(avgResolutionTime)
                .build();
    }
    
    private Double calculateAverageResponseTime(List<Ticket> tickets) {
        List<Ticket> respondedTickets = tickets.stream()
                .filter(t -> t.getFirstResponseAt() != null)
                .collect(Collectors.toList());
        
        if (respondedTickets.isEmpty()) {
            return 0.0;
        }
        
        double totalHours = respondedTickets.stream()
                .mapToDouble(t -> {
                    Duration duration = Duration.between(t.getCreatedAt(), t.getFirstResponseAt());
                    return duration.toHours();
                })
                .sum();
        
        return totalHours / respondedTickets.size();
    }
    
    private Double calculateAverageResolutionTime(List<Ticket> tickets) {
        List<Ticket> resolvedTickets = tickets.stream()
                .filter(t -> t.getResolvedAt() != null)
                .collect(Collectors.toList());
        
        if (resolvedTickets.isEmpty()) {
            return 0.0;
        }
        
        double totalHours = resolvedTickets.stream()
                .mapToDouble(t -> {
                    Duration duration = Duration.between(t.getCreatedAt(), t.getResolvedAt());
                    return duration.toHours();
                })
                .sum();
        
        return totalHours / resolvedTickets.size();
    }
}