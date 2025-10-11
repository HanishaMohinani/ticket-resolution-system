# Ticket Resolution System - Backend

A robust Spring Boot backend for managing support tickets with role-based access control, SLA tracking, and comprehensive audit logging.

## 🚀 Tech Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **ORM**: Hibernate/JPA
- **Build Tool**: Maven
- **AOP**: AspectJ for logging and rate limiting

## 📋 Features

### Core Features
- ✅ User Authentication & Authorization (JWT)
- ✅ Role-Based Access Control (Customer, Agent, Manager, Admin)
- ✅ Ticket Management (CRUD operations)
- ✅ SLA Rules & Tracking
- ✅ Comment System
- ✅ Ticket Assignment
- ✅ Ticket History/Audit Trail
- ✅ Dashboard Analytics
- ✅ Rate Limiting (AOP-based)
- ✅ Performance Monitoring
- ✅ Auto-escalation for critical tickets

### Security Features
- JWT token-based authentication
- Password encryption with BCrypt
- CORS configuration for frontend
- Role-based endpoint protection

## 🛠️ Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

## 📦 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/HanishaMohinani/ticket-resolution-system.git
cd ticket-resolution-system/backend
```

### 2. Configure Database

Create PostgreSQL database:
```bash
psql -U postgres
CREATE DATABASE ticket_system;
CREATE USER admin WITH PASSWORD 'hainey123';
GRANT ALL PRIVILEGES ON DATABASE ticket_system TO admin;
\q
```

### 3. Update Application Properties

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ticket_system
spring.datasource.username=admin
spring.datasource.password=hainey123
```

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/ticket-resolution-system-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 🔑 Demo Credentials

The application comes pre-seeded with demo accounts:

| Role     | Email                | Password     |
|----------|---------------------|--------------|
| Admin    | admin@acme.com      | password123  |
| Manager  | manager@acme.com    | password123  |
| Agent    | agent1@acme.com     | password123  |
| Customer | customer1@acme.com  | password123  |

## 📚 API Endpoints

### Authentication
```
POST   /api/auth/register    - Register new user
POST   /api/auth/login       - Login user
GET    /api/auth/me          - Get current user
```

### Tickets
```
POST   /api/tickets                    - Create ticket
GET    /api/tickets                    - Get all tickets (Manager/Admin)
GET    /api/tickets/my-tickets         - Get customer tickets
GET    /api/tickets/assigned           - Get assigned tickets (Agent)
GET    /api/tickets/{id}               - Get ticket by ID
PUT    /api/tickets/{id}               - Update ticket
PUT    /api/tickets/{id}/status        - Update ticket status
PUT    /api/tickets/{id}/assign        - Assign ticket to agent
```

### Comments
```
POST   /api/tickets/{id}/comments      - Add comment
GET    /api/tickets/{id}/comments      - Get comments
```

### Dashboard
```
GET    /api/dashboard/stats            - Get dashboard stats (Manager/Admin)
GET    /api/dashboard/agent            - Get agent dashboard
GET    /api/dashboard/manager          - Get manager stats
```

## 📊 Database Schema

### Main Tables
- `companies` - Company/organization data
- `users` - User accounts with roles
- `tickets` - Support tickets
- `comments` - Ticket comments
- `sla_rules` - SLA configuration by priority
- `ticket_history` - Audit trail
- `rate_limit_buckets` - Rate limiting data

## 🔒 Security Configuration

### JWT Configuration
- Token expiration: 24 hours (configurable)
- Secret key stored in `application.properties`
- Automatic token validation on all protected endpoints

### CORS Configuration
```java
Allowed Origins: http://localhost:3000
Allowed Methods: GET, POST, PUT, DELETE
Allowed Headers: *
```

## ⚡ Performance Features

### AOP-Based Monitoring
- **Performance Monitoring**: Logs execution time of all service methods
- **Audit Logging**: Tracks all ticket operations
- **Rate Limiting**: Token bucket algorithm for API throttling

### Rate Limiting
- Configurable per endpoint using `@RateLimited` annotation
- Example: 50 ticket creations per hour per user
- Automatic token refill based on time windows

## 🧪 Testing

Run tests:
```bash
mvn test
```

Run with coverage:
```bash
mvn clean test jacoco:report
```

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Test connection
psql -U admin -d ticket_system -h localhost
```

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Build Failures
```bash
# Clean and rebuild
mvn clean install -U
```

## 🔧 Configuration

### Application Properties
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ticket_system
spring.datasource.username=admin
spring.datasource.password=hainey123

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=YOUR_SECRET_KEY
jwt.expiration=86400000

# Logging
logging.level.com.ticketsystem=DEBUG
```

## 📈 SLA Configuration

| Priority | Response Time | Resolution Time |
|----------|--------------|-----------------|
| CRITICAL | 1 hour       | 4 hours         |
| HIGH     | 2 hours      | 8 hours         |
| MEDIUM   | 4 hours      | 24 hours        |
| LOW      | 8 hours      | 48 hours        |

## 🚀 Deployment

### Production Build
```bash
mvn clean package -DskipTests
```

### Run Production JAR
```bash
java -jar -Dspring.profiles.active=prod target/ticket-resolution-system.jar
```