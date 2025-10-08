package com.ticketsystem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketsystem.dto.response.AgentStatsResponse;
import com.ticketsystem.dto.response.ApiResponse;
import com.ticketsystem.dto.response.DashboardStatsResponse;
import com.ticketsystem.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/agent")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<ApiResponse<AgentStatsResponse>> getAgentDashboard() {
        AgentStatsResponse stats = dashboardService.getAgentDashboard();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AgentStatsResponse>>> getAgentStats() {
        List<AgentStatsResponse> stats = dashboardService.getAgentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}