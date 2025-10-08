package com.ticketsystem.entity;

public enum UserRole {
    CUSTOMER,  // Can create tickets and view own tickets
    AGENT,     // Can view assigned tickets and respond
    MANAGER,   // Can view team performance and all tickets
    ADMIN      // Full system access
}