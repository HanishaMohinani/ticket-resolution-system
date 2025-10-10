package com.ticketsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.TicketPriority;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.SlaRuleRepository;
import com.ticketsystem.repository.TicketRepository;

@Service
public class SlaService {
    
    private static final Logger logger = LoggerFactory.getLogger(SlaService.class);
    
    @Autowired
    private SlaRuleRepository slaRuleRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Transactional
    public void calculateSlaDeadlines(Ticket ticket) {
        if (ticket == null) {
            logger.error("calculateSlaDeadlines called with null ticket");
            return;
        }
        
        logger.debug("Calculating SLA for ticket {}, createdAt: {}", ticket.getId(), ticket.getCreatedAt());
        
        if (ticket.getCreatedAt() == null) {
            logger.error("Ticket {} has null createdAt! Setting to now.", ticket.getId());
            ticket.setCreatedAt(LocalDateTime.now());
        }
        
        SlaRule slaRule = slaRuleRepository
                .findByCompanyIdAndPriority(ticket.getCompany().getId(), ticket.getPriority())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "SLA Rule not found for priority: " + ticket.getPriority()));
        
        LocalDateTime baseTime = ticket.getCreatedAt() != null ? 
                                 ticket.getCreatedAt() : LocalDateTime.now();
        
        ticket.setSlaResponseDueAt(baseTime.plusHours(slaRule.getResponseTimeHours()));
        ticket.setSlaResolutionDueAt(baseTime.plusHours(slaRule.getResolutionTimeHours()));
        
        logger.debug("SLA calculated - Response due: {}, Resolution due: {}", 
                     ticket.getSlaResponseDueAt(), ticket.getSlaResolutionDueAt());
    }
    
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
    
    public boolean checkEscalation(Ticket ticket) {
        if (ticket.getEscalated() || 
            ticket.getSlaResolutionDueAt() == null || 
            ticket.getCreatedAt() == null) {
            
            if (ticket.getCreatedAt() == null) {
                logger.warn("Ticket {} has null createdAt, cannot check escalation", ticket.getId());
            }
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        try {
            long totalMinutes = Duration.between(ticket.getCreatedAt(), ticket.getSlaResolutionDueAt()).toMinutes();
            long elapsedMinutes = Duration.between(ticket.getCreatedAt(), now).toMinutes();
            
            // Prevent division by zero
            if (totalMinutes <= 0) {
                logger.warn("Ticket {} has invalid SLA time window", ticket.getId());
                return false;
            }
            
            boolean needsEscalation = (double) elapsedMinutes / totalMinutes >= 0.8;
            
            if (needsEscalation) {
                ticket.setEscalated(true);
                ticket.setEscalatedAt(now);
                ticketRepository.save(ticket);
            }
            
            return needsEscalation;
        } catch (Exception e) {
            logger.error("Error checking escalation for ticket {}: {}", ticket.getId(), e.getMessage());
            return false;
        }
    }
    
    public Long getMinutesUntilDue(Ticket ticket) {
        if (ticket.getSlaResolutionDueAt() == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(ticket.getSlaResolutionDueAt())) {
            return 0L;
        }
        
        return Duration.between(now, ticket.getSlaResolutionDueAt()).toMinutes();
    }
    
    @Transactional
    public void checkAllTicketsSla() {
        List<Ticket> activeTickets = ticketRepository.findTicketsNeedingEscalation();
        
        for (Ticket ticket : activeTickets) {
            try {
                checkSlaBreach(ticket);
                checkEscalation(ticket);
            } catch (Exception e) {
                logger.error("Error checking SLA for ticket {}: {}", ticket.getId(), e.getMessage());
            }
        }
    }
    
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