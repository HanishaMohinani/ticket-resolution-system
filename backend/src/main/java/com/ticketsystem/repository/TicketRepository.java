package com.ticketsystem.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.TicketPriority;
import com.ticketsystem.entity.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByCompanyId(Long companyId);

    List<Ticket> findByCustomerId(Long customerId);

    List<Ticket> findByAssignedAgentId(Long agentId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByCompanyIdAndStatus(Long companyId, TicketStatus status);

    List<Ticket> findByPriority(TicketPriority priority);

    List<Ticket> findBySlaBreached(Boolean breached);

    @Query("SELECT t FROM Ticket t WHERE t.escalated = false AND t.slaResolutionDueAt IS NOT NULL " +
            "AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    List<Ticket> findTicketsNeedingEscalation();

    Long countByCompanyIdAndStatus(Long companyId, TicketStatus status);

    List<Ticket> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Ticket> findByCompanyIdAndCreatedAtBetween(Long companyId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.company.id = :companyId AND t.slaBreached = true")
    Long countSlaBreachedTickets(@Param("companyId") Long companyId);
}