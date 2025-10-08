package com.ticketsystem.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ticketsystem.entity.Comment;
import com.ticketsystem.entity.Company;
import com.ticketsystem.entity.CompanyTier;
import com.ticketsystem.entity.SlaRule;
import com.ticketsystem.entity.Ticket;
import com.ticketsystem.entity.TicketPriority;
import com.ticketsystem.entity.TicketStatus;
import com.ticketsystem.entity.User;
import com.ticketsystem.entity.UserRole;
import com.ticketsystem.repository.CommentRepository;
import com.ticketsystem.repository.CompanyRepository;
import com.ticketsystem.repository.SlaRuleRepository;
import com.ticketsystem.repository.TicketRepository;
import com.ticketsystem.repository.UserRepository;

@Configuration
public class DataSeeder {
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private SlaRuleRepository slaRuleRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (companyRepository.count() > 0) {
                System.out.println("✅ Database already seeded. Skipping...");
                return;
            }
            
            System.out.println("🌱 Seeding database with sample data...");
            
            Company acme = createCompany("Acme Corporation", CompanyTier.ENTERPRISE, 1000);
            Company techStart = createCompany("TechStart Inc", CompanyTier.PRO, 500);
            Company smallBiz = createCompany("Small Business LLC", CompanyTier.FREE, 100);
            
            createSlaRules(acme);
            createSlaRules(techStart);
            createSlaRules(smallBiz);

            User admin = createUser(acme, "admin@acme.com", "Admin", "User", UserRole.ADMIN);
            User manager = createUser(acme, "manager@acme.com", "John", "Manager", UserRole.MANAGER);
            User agent1 = createUser(acme, "agent1@acme.com", "Sarah", "Agent", UserRole.AGENT);
            User agent2 = createUser(acme, "agent2@acme.com", "Mike", "Smith", UserRole.AGENT);
            User customer1 = createUser(acme, "customer1@acme.com", "Alice", "Johnson", UserRole.CUSTOMER);
            User customer2 = createUser(acme, "customer2@acme.com", "Bob", "Williams", UserRole.CUSTOMER);
        
            User techAgent = createUser(techStart, "agent@techstart.com", "Emma", "Brown", UserRole.AGENT);
            User techCustomer = createUser(techStart, "customer@techstart.com", "David", "Lee", UserRole.CUSTOMER);
        
            createSampleTickets(acme, customer1, customer2, agent1, agent2);
            
            System.out.println("✅ Database seeded successfully!");
            System.out.println("\n========================================");
            System.out.println("📋 SAMPLE LOGIN CREDENTIALS");
            System.out.println("========================================");
            System.out.println("👤 Admin:");
            System.out.println("   Email: admin@acme.com");
            System.out.println("   Password: password123");
            System.out.println("\n👤 Manager:");
            System.out.println("   Email: manager@acme.com");
            System.out.println("   Password: password123");
            System.out.println("\n👤 Agent:");
            System.out.println("   Email: agent1@acme.com");
            System.out.println("   Password: password123");
            System.out.println("\n👤 Customer:");
            System.out.println("   Email: customer1@acme.com");
            System.out.println("   Password: password123");
            System.out.println("========================================\n");
        };
    }
    
    private Company createCompany(String name, CompanyTier tier, int ticketLimit) {
        Company company = Company.builder()
                .name(name)
                .tier(tier)
                .ticketLimitPerDay(ticketLimit)
                .isActive(true)
                .build();
        return companyRepository.save(company);
    }
    
    private void createSlaRules(Company company) {
        List<SlaRule> rules = new ArrayList<>();
        
        // CRITICAL priority
        rules.add(SlaRule.builder()
                .company(company)
                .priority(TicketPriority.CRITICAL)
                .responseTimeHours(1)
                .resolutionTimeHours(4)
                .build());
        
        // HIGH priority
        rules.add(SlaRule.builder()
                .company(company)
                .priority(TicketPriority.HIGH)
                .responseTimeHours(2)
                .resolutionTimeHours(8)
                .build());
        
        // MEDIUM priority
        rules.add(SlaRule.builder()
                .company(company)
                .priority(TicketPriority.MEDIUM)
                .responseTimeHours(4)
                .resolutionTimeHours(24)
                .build());
        
        // LOW priority
        rules.add(SlaRule.builder()
                .company(company)
                .priority(TicketPriority.LOW)
                .responseTimeHours(8)
                .resolutionTimeHours(48)
                .build());
        
        slaRuleRepository.saveAll(rules);
    }
    
    private User createUser(Company company, String email, String firstName, 
                           String lastName, UserRole role) {
        User user = User.builder()
                .company(company)
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(true)
                .build();
        return userRepository.save(user);
    }
    
    private void createSampleTickets(Company company, User customer1, User customer2, 
                                     User agent1, User agent2) {
        Random random = new Random();
        TicketPriority[] priorities = TicketPriority.values();
        TicketStatus[] statuses = TicketStatus.values();
        
        String[] titles = {
            "Login issue - Cannot access account",
            "Payment processing error",
            "Website is loading slowly",
            "Feature request: Dark mode",
            "Bug: Dashboard not displaying data",
            "Password reset not working",
            "Mobile app crashes on startup",
            "Email notifications not received",
            "Data export functionality broken",
            "User profile update fails",
            "Search function returns no results",
            "Invoice generation error",
            "Integration with third-party API failing",
            "Performance issues during peak hours",
            "Security concern: Suspicious activity"
        };
        
        String[] descriptions = {
            "I'm unable to log into my account. It keeps saying 'Invalid credentials' even though I'm using the correct password.",
            "When I try to process a payment, the system shows an error message and the transaction fails.",
            "The website takes more than 30 seconds to load. This is affecting our productivity.",
            "It would be great if we could have a dark mode option for the interface.",
            "The dashboard is not showing any data even though we have recent activity.",
            "I requested a password reset but haven't received any email after 30 minutes.",
            "The mobile app crashes immediately after opening. This happens on both iOS and Android.",
            "I'm not receiving any email notifications for new messages or updates.",
            "When I try to export data to CSV, I get an error and the file doesn't download.",
            "I can't update my user profile. The save button doesn't work.",
            "The search functionality is not returning any results even for exact matches.",
            "Invoice generation is failing with error code 500. We need this fixed urgently.",
            "Our integration with the payment gateway API is failing intermittently.",
            "During peak business hours (9 AM - 5 PM), the system becomes very slow.",
            "We've noticed some suspicious login attempts. Please investigate immediately."
        };
        
        List<Ticket> tickets = new ArrayList<>();
        
        for (int i = 0; i < 15; i++) {
            TicketPriority priority = priorities[random.nextInt(priorities.length)];
            TicketStatus status = statuses[random.nextInt(statuses.length)];
            User customer = random.nextBoolean() ? customer1 : customer2;
            User agent = random.nextBoolean() ? agent1 : agent2;
            
            LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(7));
            
            Ticket ticket = Ticket.builder()
                    .company(company)
                    .ticketNumber(String.format("TKT-2025-%06d", i + 1))
                    .title(titles[i])
                    .description(descriptions[i])
                    .priority(priority)
                    .status(status)
                    .customer(customer)
                    .assignedAgent(status != TicketStatus.OPEN ? agent : null)
                    .slaBreached(random.nextInt(10) < 2) // 20% chance of SLA breach
                    .escalated(random.nextInt(10) < 1) // 10% chance of escalation
                    .createdAt(createdAt)
                    .updatedAt(LocalDateTime.now())
                    .build();
        
            int responseHours = priority == TicketPriority.CRITICAL ? 1 :
                               priority == TicketPriority.HIGH ? 2 :
                               priority == TicketPriority.MEDIUM ? 4 : 8;
            
            int resolutionHours = priority == TicketPriority.CRITICAL ? 4 :
                                 priority == TicketPriority.HIGH ? 8 :
                                 priority == TicketPriority.MEDIUM ? 24 : 48;
            
            ticket.setSlaResponseDueAt(createdAt.plusHours(responseHours));
            ticket.setSlaResolutionDueAt(createdAt.plusHours(resolutionHours));
            
            // Set response time if not OPEN
            if (status != TicketStatus.OPEN) {
                ticket.setFirstResponseAt(createdAt.plusHours(random.nextInt(responseHours) + 1));
            }
            
            // Set resolved time if RESOLVED or CLOSED
            if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
                ticket.setResolvedAt(createdAt.plusHours(random.nextInt(resolutionHours) + 1));
            }
            
            if (status == TicketStatus.CLOSED) {
                ticket.setClosedAt(ticket.getResolvedAt().plusHours(1));
            }
            
            tickets.add(ticket);
        }
        
        ticketRepository.saveAll(tickets);
    
        tickets.forEach(ticket -> {
            if (random.nextBoolean() && ticket.getStatus() != TicketStatus.OPEN) {
                // Add customer comment
                Comment customerComment = Comment.builder()
                        .ticket(ticket)
                        .user(ticket.getCustomer())
                        .content("Thank you for looking into this issue. Looking forward to the resolution.")
                        .isInternal(false)
                        .build();
                commentRepository.save(customerComment);
                
                // Add agent response
                if (ticket.getAssignedAgent() != null) {
                    Comment agentComment = Comment.builder()
                            .ticket(ticket)
                            .user(ticket.getAssignedAgent())
                            .content("I've reviewed the issue and am working on a solution. Will update you shortly.")
                            .isInternal(false)
                            .build();
                    commentRepository.save(agentComment);
                }
            }
        });
    }
}