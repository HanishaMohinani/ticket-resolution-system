// src/components/Navbar.js
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/dashboard" className="navbar-logo">
          🎫 Ticket System
        </Link>

        <div className="navbar-menu">
          <Link to="/dashboard" className="navbar-link">
            Dashboard
          </Link>

          {user?.role === 'CUSTOMER' && (
            <>
              <Link to="/tickets/my-tickets" className="navbar-link">
                My Tickets
              </Link>
              <Link to="/tickets/create" className="navbar-link">
                Create Ticket
              </Link>
            </>
          )}

          {(user?.role === 'AGENT' || user?.role === 'MANAGER') && (
            <Link to="/tickets/assigned" className="navbar-link">
              Assigned Tickets
            </Link>
          )}

          {(user?.role === 'MANAGER' || user?.role === 'ADMIN') && (
            <Link to="/tickets/all" className="navbar-link">
              All Tickets
            </Link>
          )}
        </div>

        <div className="navbar-user">
          <div className="user-info">
            <span className="user-name">{user?.firstName} {user?.lastName}</span>
            <span className="user-role">{user?.role}</span>
          </div>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;