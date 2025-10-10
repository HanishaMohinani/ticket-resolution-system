// src/pages/Dashboard.js
import React, { useEffect, useState } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { useAuth } from '../context/AuthContext';
import { dashboardAPI, ticketAPI } from '../services/api';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, [user]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);

      if (user?.role === 'CUSTOMER') {
        // Load customer tickets
        const response = await ticketAPI.getMyTickets();
        setTickets(response.data.data);
      } else if (user?.role === 'AGENT') {
        // Load agent dashboard
        const [statsRes, ticketsRes] = await Promise.all([
          dashboardAPI.getAgentDashboard(),
          ticketAPI.getAssignedTickets()
        ]);
        setStats(statsRes.data.data);
        setTickets(ticketsRes.data.data);
      } else if (user?.role === 'MANAGER' || user?.role === 'ADMIN') {
        // Load manager dashboard
        const [statsRes, ticketsRes] = await Promise.all([
          dashboardAPI.getStats(),
          ticketAPI.getAllTickets()
        ]);
        setStats(statsRes.data.data);
        setTickets(ticketsRes.data.data);
      }
    } catch (error) {
      console.error('Error loading dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Welcome back, {user?.firstName}!</h1>
        <p>Here's what's happening with your tickets</p>
      </div>

      {/* Stats Cards */}
      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">📊</div>
            <div className="stat-content">
              <h3>{stats.totalTickets || stats.assignedTickets || 0}</h3>
              <p>{user?.role === 'AGENT' ? 'Assigned Tickets' : 'Total Tickets'}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">✅</div>
            <div className="stat-content">
              <h3>{stats.resolvedTickets || 0}</h3>
              <p>Resolved Tickets</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">⏰</div>
            <div className="stat-content">
              <h3>{stats.overdueTickets || stats.slaBreachedTickets || 0}</h3>
              <p>Overdue Tickets</p>
            </div>
          </div>

          <div className="stat-card success">
            <div className="stat-icon">🎯</div>
            <div className="stat-content">
              <h3>{stats.slaComplianceRate?.toFixed(1) || 0}%</h3>
              <p>SLA Compliance</p>
            </div>
          </div>
        </div>
      )}

      {/* Charts for Manager/Admin */}
      {(user?.role === 'MANAGER' || user?.role === 'ADMIN') && stats && (
        <div className="charts-grid">
          {/* Tickets by Status */}
          <div className="chart-card">
            <h3>Tickets by Status</h3>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={[
                { name: 'Open', count: stats.openTickets },
                { name: 'In Progress', count: stats.inProgressTickets },
                { name: 'Resolved', count: stats.resolvedTickets },
                { name: 'Closed', count: stats.closedTickets },
              ]}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="#667eea" />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Tickets by Priority */}
          <div className="chart-card">
            <h3>Tickets by Priority</h3>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={Object.entries(stats.ticketsByPriority || {}).map(([key, value]) => ({
                    name: key,
                    value: value
                  }))}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={(entry) => `${entry.name}: ${entry.value}`}
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {Object.keys(stats.ticketsByPriority || {}).map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={['#667eea', '#48bb78', '#ed8936', '#f56565'][index % 4]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Recent Tickets */}
      <div className="recent-tickets">
        <h2>Recent Tickets</h2>
        <div className="tickets-table">
          <table>
            <thead>
              <tr>
                <th>Ticket #</th>
                <th>Title</th>
                <th>Status</th>
                <th>Priority</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {tickets.slice(0, 10).map(ticket => (
                <tr key={ticket.id}>
                  <td>{ticket.ticketNumber}</td>
                  <td>{ticket.title}</td>
                  <td>
                    <span className={`status-badge ${ticket.status.toLowerCase()}`}>
                      {ticket.status}
                    </span>
                  </td>
                  <td>
                    <span className={`priority-badge ${ticket.priority.toLowerCase()}`}>
                      {ticket.priority}
                    </span>
                  </td>
                  <td>{new Date(ticket.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;