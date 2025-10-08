package com.ticketsystem.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketsystem.dto.request.CreateCommentRequest;
import com.ticketsystem.dto.response.CommentResponse;
import com.ticketsystem.entity.Comment;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.UserRole;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.CommentRepository;
import com.ticketsystem.repository.TicketRepository;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private AuthService authService;
    
    @Transactional
    public CommentResponse addComment(Long ticketId, CreateCommentRequest request) {
        User currentUser = authService.getCurrentUser();
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        if (ticket.getFirstResponseAt() == null && 
            (currentUser.getRole() == UserRole.AGENT || currentUser.getRole() == UserRole.MANAGER)) {
            ticket.setFirstResponseAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
        
        Comment comment = Comment.builder()
                .ticket(ticket)
                .user(currentUser)
                .content(request.getContent())
                .isInternal(request.getIsInternal() != null ? request.getIsInternal() : false)
                .build();
        
        comment = commentRepository.save(comment);
        
        return convertToResponse(comment);
    }
    
    public List<CommentResponse> getCommentsByTicketId(Long ticketId) {
        User currentUser = authService.getCurrentUser();
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));
        
        List<Comment> comments;
        
        if (currentUser.getRole() == UserRole.CUSTOMER) {
            comments = commentRepository.findByTicketIdAndIsInternal(ticketId, false);
        } else {
            comments = commentRepository.findByTicketId(ticketId);
        }
        
        return comments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private CommentResponse convertToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket().getId())
                .content(comment.getContent())
                .isInternal(comment.getIsInternal())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFullName())
                .userRole(comment.getUser().getRole().name())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}