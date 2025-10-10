package com.ticketsystem.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsystem.dto.request.CreateTicketRequest;
import com.ticketsystem.dto.request.UpdateTicketRequest;
import com.ticketsystem.dto.response.TicketResponse;
import com.ticketsystem.entity.ChangeType;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.TicketHistory;
import com.ticketsystem.entity.TicketPriority;
import com.ticketsystem.entity.TicketStatus;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.UserRole;
import com.ticketsystem.exception.BadRequestException;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.exception.UnauthorizedException;
import com.ticketsystem.repository.CommentRepository;
import com.ticketsystem.repository.TicketHistoryRepository;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.UserRepository;

@Service
public class TicketService {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private SlaService slaService;
    
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        User customer = authService.getCurrentUser();
        
        String ticketNumber = generateTicketNumber();
        LocalDateTime now = LocalDateTime.now();
        
        logger.info("Creating ticket with number: {}, createdAt will be: {}", ticketNumber, now);
        
        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(TicketPriority.valueOf(request.getPriority().toUpperCase()))
                .status(TicketStatus.OPEN)
                .customer(customer)
                .company(customer.getCompany())
                .slaBreached(false)
                .escalated(false)
                .createdAt(now)  // Explicitly set createdAt
                .updatedAt(now)  // Explicitly set updatedAt
                .build();
        
        logger.debug("Ticket object created, createdAt before save: {}", ticket.getCreatedAt());
        
        // Save the ticket first
        ticket = ticketRepository.save(ticket);
        
        logger.debug("Ticket saved with ID: {}, createdAt after save: {}", ticket.getId(), ticket.getCreatedAt());
        
        // Flush to ensure the ticket is persisted and @PrePersist has been called
        ticketRepository.flush();
        
        logger.debug("Repository flushed, createdAt after flush: {}", ticket.getCreatedAt());
        
        // Now calculate SLA - createdAt is guaranteed to be set
        try {
            slaService.calculateSlaDeadlines(ticket);
            ticket = ticketRepository.save(ticket);
            logger.debug("SLA calculated successfully");
        } catch (Exception e) {
            logger.error("Failed to calculate SLA for ticket {}: {}", ticket.getId(), e.getMessage(), e);
            throw e; // Re-throw to see the full stack trace
        }
        
        createHistoryEntry(ticket, customer, "Ticket created", ChangeType.CREATED);
        
        logger.info("Ticket {} created successfully", ticket.getId());
        
        return convertToResponse(ticket);
    }
    
    public TicketResponse getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        User currentUser = authService.getCurrentUser();
        validateTicketAccess(ticket, currentUser);
        
        return convertToResponse(ticket);
    }
    
    public List<TicketResponse> getMyTickets() {
        User customer = authService.getCurrentUser();
        List<Ticket> tickets = ticketRepository.findByCustomerId(customer.getId());
        
        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TicketResponse> getAssignedTickets() {
        User agent = authService.getCurrentUser();
        List<Ticket> tickets = ticketRepository.findByAssignedAgentId(agent.getId());
        
        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TicketResponse> getAllTickets() {
        User user = authService.getCurrentUser();
        List<Ticket> tickets = ticketRepository.findByCompanyId(user.getCompany().getId());
        
        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TicketResponse updateTicket(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        User currentUser = authService.getCurrentUser();
        
        if (request.getTitle() != null) {
            String oldValue = ticket.getTitle();
            ticket.setTitle(request.getTitle());
            createHistoryEntry(ticket, currentUser, "title", oldValue, request.getTitle(), ChangeType.UPDATED);
        }
        
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        
        if (request.getPriority() != null) {
            String oldValue = ticket.getPriority().name();
            ticket.setPriority(TicketPriority.valueOf(request.getPriority().toUpperCase()));
            createHistoryEntry(ticket, currentUser, "priority", oldValue, request.getPriority(), ChangeType.UPDATED);
            
            // Recalculate SLA with new priority
            try {
                slaService.calculateSlaDeadlines(ticket);
            } catch (Exception e) {
                logger.error("Failed to recalculate SLA for ticket {}: {}", ticket.getId(), e.getMessage(), e);
            }
        }
        
        ticket = ticketRepository.save(ticket);
        return convertToResponse(ticket);
    }
    
    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, String newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        User currentUser = authService.getCurrentUser();
        String oldStatus = ticket.getStatus().name();
        
        ticket.setStatus(TicketStatus.valueOf(newStatus.toUpperCase()));
        
        if (newStatus.equalsIgnoreCase("RESOLVED")) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (newStatus.equalsIgnoreCase("CLOSED")) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticket = ticketRepository.save(ticket);
        
        createHistoryEntry(ticket, currentUser, "status", oldStatus, newStatus, ChangeType.STATUS_CHANGED);
        
        return convertToResponse(ticket);
    }
    
    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", agentId));
        
        if (agent.getRole() != UserRole.AGENT && agent.getRole() != UserRole.MANAGER) {
            throw new BadRequestException("User is not an agent or manager");
        }
        
        String oldAgent = ticket.getAssignedAgent() != null ? 
                ticket.getAssignedAgent().getFullName() : "Unassigned";
        
        ticket.setAssignedAgent(agent);
        ticket = ticketRepository.save(ticket);
        
        User currentUser = authService.getCurrentUser();
        createHistoryEntry(ticket, currentUser, "assigned_agent", oldAgent, agent.getFullName(), ChangeType.ASSIGNED);
        
        return convertToResponse(ticket);
    }
    
    private String generateTicketNumber() {
        int year = Year.now().getValue();
        long count = ticketRepository.count() + 1;
        return String.format("TKT-%d-%06d", year, count);
    }
    
    private void createHistoryEntry(Ticket ticket, User user, String description, ChangeType changeType) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .changedBy(user)
                .fieldName("general")
                .oldValue("")
                .newValue(description)
                .changeType(changeType)
                .build();
        
        ticketHistoryRepository.save(history);
    }
    
    private void createHistoryEntry(Ticket ticket, User user, String fieldName, 
                                    String oldValue, String newValue, ChangeType changeType) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .changedBy(user)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .changeType(changeType)
                .build();
        
        ticketHistoryRepository.save(history);
    }
    
    private void validateTicketAccess(Ticket ticket, User user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER) {
            if (!ticket.getCompany().getId().equals(user.getCompany().getId())) {
                throw new UnauthorizedException("You don't have access to this ticket");
            }
            return;
        }
        
        if (user.getRole() == UserRole.AGENT) {
            if (ticket.getAssignedAgent() != null && 
                ticket.getAssignedAgent().getId().equals(user.getId())) {
                return;
            }
        }
        
        if (user.getRole() == UserRole.CUSTOMER) {
            if (ticket.getCustomer().getId().equals(user.getId())) {
                return;
            }
        }
        
        throw new UnauthorizedException("You don't have access to this ticket");
    }
    
    private TicketResponse convertToResponse(Ticket ticket) {
        Long commentCount = commentRepository.countByTicketId(ticket.getId());
        Long minutesUntilDue = null;
        Boolean isOverdue = false;
        
        try {
            minutesUntilDue = slaService.getMinutesUntilDue(ticket);
        } catch (Exception e) {
            logger.error("Error calculating minutes until due for ticket {}: {}", ticket.getId(), e.getMessage());
        }
        
        try {
            isOverdue = ticket.isOverdue();
        } catch (Exception e) {
            logger.error("Error calculating isOverdue for ticket {}: {}", ticket.getId(), e.getMessage(), e);
            isOverdue = false;
        }
        
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus().name())
                .priority(ticket.getPriority().name())
                .customerId(ticket.getCustomer().getId())
                .customerName(ticket.getCustomer().getFullName())
                .customerEmail(ticket.getCustomer().getEmail())
                .assignedAgentId(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null)
                .assignedAgentName(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : null)
                .slaResponseDueAt(ticket.getSlaResponseDueAt())
                .slaResolutionDueAt(ticket.getSlaResolutionDueAt())
                .slaBreached(ticket.getSlaBreached())
                .escalated(ticket.getEscalated())
                .minutesUntilDue(minutesUntilDue)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .firstResponseAt(ticket.getFirstResponseAt())
                .resolvedAt(ticket.getResolvedAt())
                .commentCount(commentCount.intValue())
                .isOverdue(isOverdue)
                .build();
    }
}