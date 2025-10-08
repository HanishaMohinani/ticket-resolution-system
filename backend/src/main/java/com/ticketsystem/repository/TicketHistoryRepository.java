package com.ticketsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketsystem.entity.TicketHistory;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    List<TicketHistory> findByTicketId(Long ticketId);
    List<TicketHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    List<TicketHistory> findByChangedById(Long userId);
}