package com.ticketsystem.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

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

/**
 * Ticket Service - Core business logic for ticket management
 */
@Service
public class TicketService {
    
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
    
    /**
     * Create a new ticket
     */
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        User customer = authService.getCurrentUser();
        
        // Generate unique ticket number
        String ticketNumber = generateTicketNumber();
        
        // Create ticket
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
                .build();
        
        // Save ticket first to trigger @PrePersist (sets createdAt)
        ticket = ticketRepository.save(ticket);
        
        // Now calculate SLA deadlines (after createdAt is set)
        slaService.calculateSlaDeadlines(ticket);
        
        // Save again with SLA deadlines
        ticket = ticketRepository.save(ticket);
        
        // Create history entry
        createHistoryEntry(ticket, customer, "Ticket created", ChangeType.CREATED);
        
        return convertToResponse(ticket);
    }
    
    /**
     * Get ticket by ID
     */
    public TicketResponse getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        // Check access permission
        User currentUser = authService.getCurrentUser();
        validateTicketAccess(ticket, currentUser);
        
        return convertToResponse(ticket);
    }
    
    /**
     * Get tickets for customer (my tickets)
     */
    public List<TicketResponse> getMyTickets() {
        User customer = authService.getCurrentUser();
        List<Ticket> tickets = ticketRepository.findByCustomerId(customer.getId());
        
        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tickets assigned to agent
     */
    public List<TicketResponse> getAssignedTickets() {
        User agent = authService.getCurrentUser();
        List<Ticket> tickets = ticketRepository.findByAssignedAgentId(agent.getId());
        
        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all tickets for company (for managers/admins)
     */
    public List<TicketResponse> getAllTickets() {
        User user = authService.getCurrentUser();
        List<Ticket> tickets = ticketRepository.findByCompanyId(user.getCompany().getId());
        
        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update ticket
     */
    @Transactional
    public TicketResponse updateTicket(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        User currentUser = authService.getCurrentUser();
        
        // Update fields if provided
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
            
            // Recalculate SLA for new priority
            slaService.calculateSlaDeadlines(ticket);
        }
        
        ticket = ticketRepository.save(ticket);
        return convertToResponse(ticket);
    }
    
    /**
     * Update ticket status
     */
    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, String newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        User currentUser = authService.getCurrentUser();
        String oldStatus = ticket.getStatus().name();
        
        ticket.setStatus(TicketStatus.valueOf(newStatus.toUpperCase()));
        
        // Set timestamps based on status
        if (newStatus.equalsIgnoreCase("RESOLVED")) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (newStatus.equalsIgnoreCase("CLOSED")) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticket = ticketRepository.save(ticket);
        
        // Create history entry
        createHistoryEntry(ticket, currentUser, "status", oldStatus, newStatus, ChangeType.STATUS_CHANGED);
        
        return convertToResponse(ticket);
    }
    
    /**
     * Assign ticket to agent
     */
    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", agentId));
        
        // Verify agent role
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
    
    /**
     * Generate unique ticket number
     */
    private String generateTicketNumber() {
        int year = Year.now().getValue();
        long count = ticketRepository.count() + 1;
        return String.format("TKT-%d-%06d", year, count);
    }
    
    /**
     * Create ticket history entry
     */
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
    
    /**
     * Create detailed history entry
     */
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
    
    /**
     * Validate if user has access to ticket
     */
    private void validateTicketAccess(Ticket ticket, User user) {
        // Admin and Manager can see all tickets in company
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.MANAGER) {
            if (!ticket.getCompany().getId().equals(user.getCompany().getId())) {
                throw new UnauthorizedException("You don't have access to this ticket");
            }
            return;
        }
        
        // Agent can see assigned tickets
        if (user.getRole() == UserRole.AGENT) {
            if (ticket.getAssignedAgent() != null && 
                ticket.getAssignedAgent().getId().equals(user.getId())) {
                return;
            }
        }
        
        // Customer can only see their own tickets
        if (user.getRole() == UserRole.CUSTOMER) {
            if (ticket.getCustomer().getId().equals(user.getId())) {
                return;
            }
        }
        
        throw new UnauthorizedException("You don't have access to this ticket");
    }
    
    /**
     * Convert Ticket entity to TicketResponse DTO
     */
    private TicketResponse convertToResponse(Ticket ticket) {
        Long commentCount = commentRepository.countByTicketId(ticket.getId());
        Long minutesUntilDue = slaService.getMinutesUntilDue(ticket);
        
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
                .isOverdue(ticket.isOverdue())
                .build();
    }
}