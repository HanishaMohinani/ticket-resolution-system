// src/App.js
import React from 'react';
import { Navigate, Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import './App.css';
import Navbar from './components/Navbar';
import { AuthProvider, useAuth } from './context/AuthContext';
import CreateTicket from './pages/CreateTicket';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import Register from './pages/Register';
import TicketList from './pages/TicketList';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

const PublicRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="loading-screen">Loading...</div>;
  }

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return <>{children}</>;
};

function AppContent() {
  const { user } = useAuth();

  return (
    <div className="app">
      {user && <Navbar />}
      <Routes>
        <Route
          path="/login"
          element={
            <PublicRoute>
              <Login />
            </PublicRoute>
          }
        />
        <Route
          path="/register"
          element={
            <PublicRoute>
              <Register />
            </PublicRoute>
          }
        />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/tickets/my-tickets"
          element={
            <ProtectedRoute allowedRoles={['CUSTOMER']}>
              <TicketList type="my-tickets" />
            </ProtectedRoute>
          }
        />
        
        <Route
          path="/tickets/create"
          element={
            <ProtectedRoute allowedRoles={['CUSTOMER']}>
              <CreateTicket />
            </ProtectedRoute>
          }
        />

        <Route
          path="/tickets/assigned"
          element={
            <ProtectedRoute allowedRoles={['AGENT', 'MANAGER', 'ADMIN']}>
              <TicketList type="assigned" />
            </ProtectedRoute>
          }
        />

        <Route
          path="/tickets/all"
          element={
            <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']}>
              <TicketList type="all" />
            </ProtectedRoute>
          }
        />

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
}

export default App;