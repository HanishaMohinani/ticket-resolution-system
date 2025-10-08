package com.ticketsystem.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ticketsystem.repository.TicketHistoryRepository;
import com.ticketsystem.repository.UserRepository;


@Aspect
@Component
public class AuditAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    
    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @AfterReturning(
        pointcut = "execution(* com.ticketsystem.service.TicketService.createTicket(..))",
        returning = "result"
    )
    public void logTicketCreation(JoinPoint joinPoint, Object result) {
        try {
            logger.info("Audit: Ticket created - Method: {}", joinPoint.getSignature().getName());
        } catch (Exception e) {
            logger.error("Error in audit logging", e);
        }
    }
    

    @AfterReturning(
        pointcut = "execution(* com.ticketsystem.service.TicketService.updateTicketStatus(..))",
        returning = "result"
    )
    public void logTicketStatusChange(JoinPoint joinPoint, Object result) {
        try {
            logger.info("Audit: Ticket status changed - Method: {}", joinPoint.getSignature().getName());
        } catch (Exception e) {
            logger.error("Error in audit logging", e);
        }
    }
    
    @AfterReturning(
        pointcut = "execution(* com.ticketsystem.service.TicketService.assignTicket(..))",
        returning = "result"
    )
    public void logTicketAssignment(JoinPoint joinPoint, Object result) {
        try {
            logger.info("Audit: Ticket assigned - Method: {}", joinPoint.getSignature().getName());
        } catch (Exception e) {
            logger.error("Error in audit logging", e);
        }
    }
    

    @AfterReturning(
        pointcut = "execution(* com.ticketsystem.service.*.*(..))"
    )
    public void logServiceMethodCall(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        
        logger.debug("Audit: User '{}' called method: {}", 
            username, joinPoint.getSignature().toShortString());
    }
}