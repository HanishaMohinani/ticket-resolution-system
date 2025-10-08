package com.ticketsystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsystem.annotation.RateLimited;
import com.ticketsystem.dto.request.CreateCommentRequest;
import com.ticketsystem.dto.response.ApiResponse;
import com.ticketsystem.dto.response.CommentResponse;
import com.ticketsystem.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:3000")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @PostMapping("/{ticketId}/comments")
    @RateLimited(maxRequests = 10, windowSeconds = 60, action = "add_comment")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request) {
        
        CommentResponse comment = commentService.addComment(ticketId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", comment));
    }

    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable Long ticketId) {
        
        List<CommentResponse> comments = commentService.getCommentsByTicketId(ticketId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
}