package com.ticketsystem.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber; // Format: TKT-2025-001234
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.OPEN;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketPriority priority = TicketPriority.MEDIUM;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;
    
    // SLA Tracking fields
    @Column(name = "sla_response_due_at")
    private LocalDateTime slaResponseDueAt;
    
    @Column(name = "sla_resolution_due_at")
    private LocalDateTime slaResolutionDueAt;
    
    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @Column(name = "sla_breached", nullable = false)
    private Boolean slaBreached = false;
    
    @Column(nullable = false)
    private Boolean escalated = false;
    
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOverdue() {
        if (slaResolutionDueAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(slaResolutionDueAt) && 
               (status != TicketStatus.RESOLVED && status != TicketStatus.CLOSED);
    }
    
    public boolean needsEscalation() {
        if (slaResolutionDueAt == null || escalated) {
            return false;
        }
        
        long totalMinutes = java.time.Duration.between(createdAt, slaResolutionDueAt).toMinutes();
        long elapsedMinutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        
        return (double) elapsedMinutes / totalMinutes >= 0.8;
    }
}