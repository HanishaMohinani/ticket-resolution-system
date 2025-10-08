package com.ticketsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.TicketPriority;

@Repository
public interface SlaRuleRepository extends JpaRepository<SlaRule, Long> {
    Optional<SlaRule> findByCompanyIdAndPriority(Long companyId, TicketPriority priority);
    List<SlaRule> findByCompanyId(Long companyId);
}