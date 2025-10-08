package com.ticketsystem.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Example usage:
 * @RateLimited(maxRequests = 50, windowSeconds = 3600, action = "create_ticket")
 * public ResponseEntity createTicket() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Maximum number of requests allowed in the time window
     */
    int maxRequests() default 100;
    
    /**
     * Time window in seconds
     * Default: 3600 seconds (1 hour)
     */
    int windowSeconds() default 3600;
    
    /**
     * Action identifier (used to create unique bucket keys)
     * Examples: "create_ticket", "add_comment", "update_ticket"
     */
    String action();
    
    /**
     * Whether to rate limit by user or by company
     * Default: true (by user)
     */
    boolean byUser() default true;
}