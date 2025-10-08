package com.ticketsystem.entity;

public enum TicketStatus {
    OPEN,         // Newly created
    IN_PROGRESS,  // Agent working on it
    RESOLVED,     // Solution provided
    CLOSED        // Ticket closed
}