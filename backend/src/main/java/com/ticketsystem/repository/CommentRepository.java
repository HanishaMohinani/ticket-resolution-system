package com.ticketsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketsystem.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicketId(Long ticketId);
    List<Comment> findByTicketIdAndIsInternal(Long ticketId, Boolean isInternal);
    List<Comment> findByUserId(Long userId);
    Long countByTicketId(Long ticketId);
}