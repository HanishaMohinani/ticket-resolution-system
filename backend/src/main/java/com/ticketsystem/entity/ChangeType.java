package com.ticketsystem.entity;

public enum ChangeType {
    CREATED,        // Ticket created
    UPDATED,        // Field updated
    ASSIGNED,       // Agent assigned
    ESCALATED,      // Ticket escalated
    STATUS_CHANGED  // Status changed
}
