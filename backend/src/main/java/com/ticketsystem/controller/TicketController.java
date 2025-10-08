package com.ticketsystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsystem.annotation.RateLimited;
import com.ticketsystem.dto.request.AssignTicketRequest;
import com.ticketsystem.dto.request.CreateTicketRequest;
import com.ticketsystem.dto.request.UpdateStatusRequest;
import com.ticketsystem.dto.request.UpdateTicketRequest;
import com.ticketsystem.dto.response.ApiResponse;
import com.ticketsystem.dto.response.TicketResponse;
import com.ticketsystem.service.TicketService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:3000")
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    @PostMapping
    @RateLimited(maxRequests = 50, windowSeconds = 3600, action = "create_ticket")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        
        TicketResponse ticket = ticketService.createTicket(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created successfully", ticket));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets() {
        List<TicketResponse> tickets = ticketService.getMyTickets();
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }
    
    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('AGENT', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getAssignedTickets() {
        List<TicketResponse> tickets = ticketService.getAssignedTickets();
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getAllTickets() {
        List<TicketResponse> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request) {
        
        TicketResponse ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket updated successfully", ticket));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        
        TicketResponse ticket = ticketService.updateTicketStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated", ticket));
    }
    
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request) {
        
        TicketResponse ticket = ticketService.assignTicket(id, request.getAgentId());
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", ticket));
    }
}