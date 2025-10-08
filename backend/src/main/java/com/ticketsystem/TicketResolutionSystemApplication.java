package com.ticketsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
public class TicketResolutionSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketResolutionSystemApplication.class, args);
        System.out.println("========================================");
        System.out.println("🎫 Ticket Resolution System Started!");
        System.out.println("========================================");
        System.out.println("📍 Backend running on: http://localhost:8080");
        System.out.println("📖 API Documentation: http://localhost:8080/api");
        System.out.println("========================================");
    }
}