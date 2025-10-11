# Ticket Resolution System - Frontend

A modern, responsive React application for managing support tickets with role-based dashboards, real-time SLA tracking, and comprehensive analytics.

## 🚀 Tech Stack

- **Framework**: React 18.2.0
- **Routing**: React Router DOM 6.x
- **HTTP Client**: Axios
- **Charts**: Recharts
- **Styling**: CSS3 (Custom)
- **Build Tool**: Create React App

## ✨ Features

### Customer Features
- 📝 Create support tickets with priority levels
- 📋 View personal tickets (My Tickets)
- 💬 Add comments to tickets
- 🔔 Track ticket status in real-time
- ⏰ View SLA deadlines

### Agent Features
- 📌 View assigned tickets
- 🔄 Update ticket status (Open → In Progress → Resolved → Closed)
- 💬 Respond to customers via comments
- 📊 Personal performance dashboard
- ⚠️ SLA breach alerts

### Manager/Admin Features
- 📊 Comprehensive analytics dashboard
- 📈 Visual charts (Bar & Pie charts)
- 👥 Team performance metrics
- 🎯 SLA compliance tracking
- 🔧 Assign tickets to agents
- 📋 View all company tickets

### General Features
- 🔐 Secure JWT authentication
- 🎨 Modern, clean UI design
- 📱 Fully responsive (mobile, tablet, desktop)
- 🔍 Advanced search and filtering
- 🎨 Color-coded priorities and statuses
- ⚡ Real-time updates

## 🛠️ Prerequisites

- Node.js 14+ or higher
- npm 6+ or yarn 1.22+
- Running backend on `http://localhost:8080`

## 📦 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/HanishaMohinani/ticket-resolution-system.git
cd ticket-resolution-system/frontend
```

### 2. Install Dependencies
```bash
npm install
```

Or with yarn:
```bash
yarn install
```

### 3. Configure API URL

The API URL is configured in `src/services/api.js`:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

### 4. Start Development Server
```bash
npm start
```

The app will open at `http://localhost:3000`

## 🔑 Demo Credentials

Quick login with pre-seeded accounts:

| Role     | Email                | Password     | Access                           |
|----------|---------------------|--------------|----------------------------------|
| Admin    | admin@acme.com      | password123  | Full system access               |
| Manager  | manager@acme.com    | password123  | All tickets, analytics, assign   |
| Agent    | agent1@acme.com     | password123  | Assigned tickets, update status  |
| Customer | customer1@acme.com  | password123  | Create & view own tickets        |

## 🎨 Features by Role

### Customer Dashboard
- Personal ticket statistics
- Recent tickets table
- Quick access to create ticket

### Agent Dashboard
- Assigned tickets count
- Resolved tickets count
- Overdue tickets alert
- SLA compliance rate
- Personal performance metrics

### Manager/Admin Dashboard
- Total tickets overview
- Status breakdown (Open, In Progress, Resolved, Closed)
- Priority distribution
- Interactive charts:
  - **Bar Chart**: Tickets by Status
  - **Pie Chart**: Tickets by Priority
- Team performance metrics
- Company-wide SLA compliance

## 🔌 API Integration

### Authentication Endpoints
```javascript
POST /api/auth/login      - User login
POST /api/auth/register   - User registration
GET  /api/auth/me         - Get current user
```

### Ticket Endpoints
```javascript
POST /api/tickets                   - Create ticket
GET  /api/tickets/my-tickets        - Get customer tickets
GET  /api/tickets/assigned          - Get agent tickets
GET  /api/tickets                   - Get all tickets
GET  /api/tickets/{id}              - Get ticket details
PUT  /api/tickets/{id}/status       - Update status
PUT  /api/tickets/{id}/assign       - Assign to agent
```

### Comment Endpoints
```javascript
POST /api/tickets/{id}/comments     - Add comment
GET  /api/tickets/{id}/comments     - Get comments
```

### Dashboard Endpoints
```javascript
GET  /api/dashboard/stats           - Manager stats
GET  /api/dashboard/agent           - Agent stats
```

## 🎯 Usage Guide

### For Customers

1. **Login** with customer credentials
2. Click **"Create Ticket"** in navbar
3. Fill in:
   - Title (required)
   - Description (required)
   - Priority (Low, Medium, High, Critical)
4. Click **"Create Ticket"**
5. View in **"My Tickets"**
6. Click ticket to view details and add comments

### For Agents

1. **Login** with agent credentials
2. Click **"Assigned Tickets"** in navbar
3. Click on a ticket to open details
4. Update status using buttons:
   - **Open** → **In Progress** → **Resolved** → **Closed**
5. Add comments to communicate with customer
6. Monitor SLA deadlines

### For Managers/Admins

1. **Login** with manager/admin credentials
2. View comprehensive **Dashboard** with charts
3. Click **"All Tickets"** to see company tickets
4. Click ticket to:
   - View details
   - Assign to agents
   - Update status
5. Monitor team performance in dashboard

## 🎨 Color Coding

### Status Colors
- 🔵 **Open**: Blue
- 🟡 **In Progress**: Yellow/Orange
- 🟢 **Resolved**: Green
- ⚪ **Closed**: Gray

### Priority Colors
- 🟢 **Low**: Green
- 🟡 **Medium**: Yellow
- 🟠 **High**: Orange
- 🔴 **Critical**: Red

### SLA Indicators
- ✅ **On Time**: Normal display
- ⚠️ **At Risk**: Orange warning (80% time elapsed)
- 🚨 **Breached**: Red alert

## 🚀 Build & Deployment

### Development Build
```bash
npm start
```

### Production Build
```bash
npm run build
```

This creates an optimized build in the `build/` folder.

### Serve Production Build
```bash
npm install -g serve
serve -s build -p 3000
```

## 🧪 Testing

### Run Tests
```bash
npm test
```

### Run Tests with Coverage
```bash
npm test -- --coverage
```

## 🐛 Troubleshooting

### Backend Connection Error
```
Error: Network Error
```
**Solution**: Ensure backend is running on `http://localhost:8080`

### Login Failed
```
Error: Login failed
```
**Solutions**:
1. Check backend is running
2. Verify credentials
3. Clear localStorage: `localStorage.clear()`
4. Check browser console for CORS errors

### Blank Dashboard
**Solutions**:
1. Check if you're logged in
2. Verify role permissions
3. Check API responses in Network tab (F12)
4. Clear cache and reload (Ctrl+Shift+R)

### Charts Not Displaying
**Solution**: Ensure recharts is installed:
```bash
npm install recharts
```

## 🔐 Security Features

- JWT token stored in localStorage
- Automatic token inclusion in API calls
- Auto-logout on 401 errors
- Protected routes based on authentication
- Role-based UI rendering

## 📝 Scripts

```bash
npm start          # Start development server
npm run build      # Create production build
npm test           # Run tests
npm run eject      # Eject from Create React App (one-way)
```

## 🔄 State Management

Uses React Context API:
- **AuthContext**: User authentication state
- localStorage for token persistence
- Component-level state with useState
