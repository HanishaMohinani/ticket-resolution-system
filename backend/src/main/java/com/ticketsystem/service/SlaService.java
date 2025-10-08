package com.ticketsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.TicketPriority;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.SlaRuleRepository;
import com.ticketsystem.repository.TicketRepository;

/**
 * SLA Service
 * Manages SLA rules and calculates SLA deadlines
 */
@Service
public class SlaService {
    
    @Autowired
    private SlaRuleRepository slaRuleRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    /**
     * Calculate and set SLA deadlines for a ticket
     */
    @Transactional
    public void calculateSlaDeadlines(Ticket ticket) {
        SlaRule slaRule = slaRuleRepository
                .findByCompanyIdAndPriority(ticket.getCompany().getId(), ticket.getPriority())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "SLA Rule not found for priority: " + ticket.getPriority()));
        
        // Use current time if createdAt is null (will be set by @PrePersist)
        LocalDateTime now = ticket.getCreatedAt() != null ? ticket.getCreatedAt() : LocalDateTime.now();
        
        // Set response deadline
        ticket.setSlaResponseDueAt(now.plusHours(slaRule.getResponseTimeHours()));
        
        // Set resolution deadline
        ticket.setSlaResolutionDueAt(now.plusHours(slaRule.getResolutionTimeHours()));
    }
    
    /**
     * Check if ticket has breached SLA
     */
    public boolean checkSlaBreach(Ticket ticket) {
        if (ticket.getSlaResolutionDueAt() == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean breached = now.isAfter(ticket.getSlaResolutionDueAt()) &&
                          (ticket.getStatus().name().equals("OPEN") || 
                           ticket.getStatus().name().equals("IN_PROGRESS"));
        
        if (breached && !ticket.getSlaBreached()) {
            ticket.setSlaBreached(true);
            ticketRepository.save(ticket);
        }
        
        return breached;
    }
    
    /**
     * Check if ticket needs escalation (80% of time elapsed)
     */
    public boolean checkEscalation(Ticket ticket) {
        if (ticket.getEscalated() || ticket.getSlaResolutionDueAt() == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long totalMinutes = Duration.between(ticket.getCreatedAt(), ticket.getSlaResolutionDueAt()).toMinutes();
        long elapsedMinutes = Duration.between(ticket.getCreatedAt(), now).toMinutes();
        
        boolean needsEscalation = (double) elapsedMinutes / totalMinutes >= 0.8;
        
        if (needsEscalation) {
            ticket.setEscalated(true);
            ticket.setEscalatedAt(now);
            ticketRepository.save(ticket);
        }
        
        return needsEscalation;
    }
    
    /**
     * Get minutes remaining until SLA breach
     */
    public Long getMinutesUntilDue(Ticket ticket) {
        if (ticket.getSlaResolutionDueAt() == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(ticket.getSlaResolutionDueAt())) {
            return 0L; // Already breached
        }
        
        return Duration.between(now, ticket.getSlaResolutionDueAt()).toMinutes();
    }
    
    /**
     * Check all tickets for SLA breaches and escalations
     */
    @Transactional
    public void checkAllTicketsSla() {
        List<Ticket> activeTickets = ticketRepository.findTicketsNeedingEscalation();
        
        for (Ticket ticket : activeTickets) {
            checkSlaBreach(ticket);
            checkEscalation(ticket);
        }
    }
    
    /**
     * Create default SLA rules for a company
     */
    @Transactional
    public void createDefaultSlaRules(Long companyId) {
        // CRITICAL priority
        SlaRule critical = SlaRule.builder()
                .company(new com.ticketsystem.entity.Company())
                .priority(TicketPriority.CRITICAL)
                .responseTimeHours(1)
                .resolutionTimeHours(4)
                .build();
        critical.getCompany().setId(companyId);
        
        // HIGH priority
        SlaRule high = SlaRule.builder()
                .company(new com.ticketsystem.entity.Company())
                .priority(TicketPriority.HIGH)
                .responseTimeHours(2)
                .resolutionTimeHours(8)
                .build();
        high.getCompany().setId(companyId);
        
        // MEDIUM priority
        SlaRule medium = SlaRule.builder()
                .company(new com.ticketsystem.entity.Company())
                .priority(TicketPriority.MEDIUM)
                .responseTimeHours(4)
                .resolutionTimeHours(24)
                .build();
        medium.getCompany().setId(companyId);
        
        // LOW priority
        SlaRule low = SlaRule.builder()
                .company(new com.ticketsystem.entity.Company())
                .priority(TicketPriority.LOW)
                .responseTimeHours(8)
                .resolutionTimeHours(48)
                .build();
        low.getCompany().setId(companyId);
        
        slaRuleRepository.saveAll(List.of(critical, high, medium, low));
    }
}