// src/pages/TicketList.js
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { commentAPI, ticketAPI } from '../services/api';
import './TicketList.css';

const TicketList = ({ type }) => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    loadTickets();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [type]);

  const loadTickets = async () => {
    try {
      setLoading(true);
      let response;
      
      console.log('Loading tickets for type:', type);
      
      if (type === 'my-tickets') {
        response = await ticketAPI.getMyTickets();
      } else if (type === 'assigned') {
        response = await ticketAPI.getAssignedTickets();
      } else if (type === 'all') {
        response = await ticketAPI.getAllTickets();
      }
      
      console.log('Tickets response:', response);
      setTickets(response.data.data || []);
    } catch (error) {
      console.error('Error loading tickets:', error);
      console.error('Error details:', error.response?.data);
    } finally {
      setLoading(false);
    }
  };

  const openTicketDetails = async (ticket) => {
    try {
      const [ticketRes, commentsRes] = await Promise.all([
        ticketAPI.getTicketById(ticket.id),
        commentAPI.getComments(ticket.id)
      ]);
      
      setSelectedTicket(ticketRes.data.data);
      setComments(commentsRes.data.data || []);
      setShowModal(true);
    } catch (error) {
      console.error('Error loading ticket details:', error);
    }
  };

  const handleStatusChange = async (ticketId, newStatus) => {
    try {
      await ticketAPI.updateTicketStatus(ticketId, newStatus);
      loadTickets();
      
      if (selectedTicket?.id === ticketId) {
        const response = await ticketAPI.getTicketById(ticketId);
        setSelectedTicket(response.data.data);
      }
    } catch (error) {
      console.error('Error updating status:', error);
      alert('Failed to update ticket status');
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) return;

    try {
      await commentAPI.addComment(selectedTicket.id, {
        content: newComment,
        isInternal: false
      });
      
      setNewComment('');
      
      const response = await commentAPI.getComments(selectedTicket.id);
      setComments(response.data.data || []);
    } catch (error) {
      console.error('Error adding comment:', error);
      alert('Failed to add comment');
    }
  };

  const filteredTickets = tickets.filter(ticket => {
    const matchesFilter = filter === 'all' || ticket.status === filter;
    const matchesSearch = 
      ticket.ticketNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ticket.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ticket.description?.toLowerCase().includes(searchQuery.toLowerCase());
    
    return matchesFilter && matchesSearch;
  });

  const getStatusClass = (status) => {
    return status?.toLowerCase().replace('_', '-') || 'open';
  };

  const getPriorityClass = (priority) => {
    return priority?.toLowerCase() || 'medium';
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString();
  };

  const getTimeRemaining = (minutesUntilDue) => {
    if (!minutesUntilDue || minutesUntilDue === 0) return 'Overdue';
    
    if (minutesUntilDue < 60) {
      return `${minutesUntilDue} mins left`;
    } else if (minutesUntilDue < 1440) {
      return `${Math.floor(minutesUntilDue / 60)} hours left`;
    } else {
      return `${Math.floor(minutesUntilDue / 1440)} days left`;
    }
  };

  if (loading) {
    return (
      <div className="ticket-list-container">
        <div className="loading">Loading tickets...</div>
      </div>
    );
  }

  return (
    <div className="ticket-list-container">
      <div className="ticket-list-header">
        <div>
          <h1>
            {type === 'my-tickets' && '📋 My Tickets'}
            {type === 'assigned' && '📌 Assigned Tickets'}
            {type === 'all' && '📊 All Tickets'}
          </h1>
          <p>{filteredTickets.length} tickets found</p>
        </div>
        
        {type === 'my-tickets' && (
          <button 
            onClick={() => navigate('/tickets/create')} 
            className="btn-create"
          >
            + Create Ticket
          </button>
        )}
      </div>

      <div className="ticket-filters">
        <div className="search-box">
          <input
            type="text"
            placeholder="Search tickets..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        
        <div className="filter-buttons">
          <button 
            className={filter === 'all' ? 'active' : ''}
            onClick={() => setFilter('all')}
          >
            All
          </button>
          <button 
            className={filter === 'OPEN' ? 'active' : ''}
            onClick={() => setFilter('OPEN')}
          >
            Open
          </button>
          <button 
            className={filter === 'IN_PROGRESS' ? 'active' : ''}
            onClick={() => setFilter('IN_PROGRESS')}
          >
            In Progress
          </button>
          <button 
            className={filter === 'RESOLVED' ? 'active' : ''}
            onClick={() => setFilter('RESOLVED')}
          >
            Resolved
          </button>
          <button 
            className={filter === 'CLOSED' ? 'active' : ''}
            onClick={() => setFilter('CLOSED')}
          >
            Closed
          </button>
        </div>
      </div>

      <div className="tickets-grid">
        {filteredTickets.map(ticket => (
          <div 
            key={ticket.id} 
            className={`ticket-card ${ticket.slaBreached ? 'breached' : ''} ${ticket.isOverdue ? 'overdue' : ''}`}
            onClick={() => openTicketDetails(ticket)}
          >
            <div className="ticket-card-header">
              <span className="ticket-number">{ticket.ticketNumber}</span>
              <div className="ticket-badges">
                <span className={`priority-badge ${getPriorityClass(ticket.priority)}`}>
                  {ticket.priority}
                </span>
                <span className={`status-badge ${getStatusClass(ticket.status)}`}>
                  {ticket.status?.replace('_', ' ')}
                </span>
              </div>
            </div>

            <h3 className="ticket-title">{ticket.title}</h3>
            <p className="ticket-description">
              {ticket.description?.substring(0, 150)}
              {ticket.description?.length > 150 && '...'}
            </p>

            <div className="ticket-meta">
              <div className="meta-item">
                <span className="meta-label">Customer:</span>
                <span className="meta-value">{ticket.customerName}</span>
              </div>
              
              {ticket.assignedAgentName && (
                <div className="meta-item">
                  <span className="meta-label">Agent:</span>
                  <span className="meta-value">{ticket.assignedAgentName}</span>
                </div>
              )}
              
              <div className="meta-item">
                <span className="meta-label">Created:</span>
                <span className="meta-value">{new Date(ticket.createdAt).toLocaleDateString()}</span>
              </div>
              
              {ticket.minutesUntilDue && (
                <div className="meta-item">
                  <span className="meta-label">SLA:</span>
                  <span className={`meta-value ${ticket.isOverdue ? 'text-danger' : ''}`}>
                    {getTimeRemaining(ticket.minutesUntilDue)}
                  </span>
                </div>
              )}
            </div>

            {ticket.slaBreached && (
              <div className="sla-alert">
                ⚠️ SLA Breached
              </div>
            )}

            {ticket.escalated && (
              <div className="escalation-alert">
                🔥 Escalated
              </div>
            )}

            <div className="ticket-footer">
              <span>💬 {ticket.commentCount || 0} comments</span>
            </div>
          </div>
        ))}
      </div>

      {filteredTickets.length === 0 && (
        <div className="no-tickets">
          <p>📭 No tickets found</p>
        </div>
      )}

      {showModal && selectedTicket && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h2>{selectedTicket.ticketNumber}</h2>
                <div className="modal-badges">
                  <span className={`priority-badge ${getPriorityClass(selectedTicket.priority)}`}>
                    {selectedTicket.priority}
                  </span>
                  <span className={`status-badge ${getStatusClass(selectedTicket.status)}`}>
                    {selectedTicket.status?.replace('_', ' ')}
                  </span>
                </div>
              </div>
              <button className="btn-close" onClick={() => setShowModal(false)}>×</button>
            </div>

            <div className="modal-body">
              <div className="ticket-details">
                <h3>{selectedTicket.title}</h3>
                <p className="ticket-detail-description">{selectedTicket.description}</p>

                <div className="detail-grid">
                  <div className="detail-item">
                    <strong>Customer:</strong>
                    <span>{selectedTicket.customerName}</span>
                  </div>
                  <div className="detail-item">
                    <strong>Email:</strong>
                    <span>{selectedTicket.customerEmail}</span>
                  </div>
                  <div className="detail-item">
                    <strong>Assigned To:</strong>
                    <span>{selectedTicket.assignedAgentName || 'Unassigned'}</span>
                  </div>
                  <div className="detail-item">
                    <strong>Created:</strong>
                    <span>{formatDateTime(selectedTicket.createdAt)}</span>
                  </div>
                  <div className="detail-item">
                    <strong>SLA Response Due:</strong>
                    <span>{formatDateTime(selectedTicket.slaResponseDueAt)}</span>
                  </div>
                  <div className="detail-item">
                    <strong>SLA Resolution Due:</strong>
                    <span>{formatDateTime(selectedTicket.slaResolutionDueAt)}</span>
                  </div>
                  {selectedTicket.firstResponseAt && (
                    <div className="detail-item">
                      <strong>First Response:</strong>
                      <span>{formatDateTime(selectedTicket.firstResponseAt)}</span>
                    </div>
                  )}
                  {selectedTicket.resolvedAt && (
                    <div className="detail-item">
                      <strong>Resolved:</strong>
                      <span>{formatDateTime(selectedTicket.resolvedAt)}</span>
                    </div>
                  )}
                </div>

                {(user.role === 'AGENT' || user.role === 'MANAGER' || user.role === 'ADMIN') && (
                  <div className="status-actions">
                    <strong>Change Status:</strong>
                    <div className="status-buttons">
                      <button 
                        onClick={() => handleStatusChange(selectedTicket.id, 'OPEN')}
                        className="btn-status"
                      >
                        Open
                      </button>
                      <button 
                        onClick={() => handleStatusChange(selectedTicket.id, 'IN_PROGRESS')}
                        className="btn-status"
                      >
                        In Progress
                      </button>
                      <button 
                        onClick={() => handleStatusChange(selectedTicket.id, 'RESOLVED')}
                        className="btn-status"
                      >
                        Resolved
                      </button>
                      <button 
                        onClick={() => handleStatusChange(selectedTicket.id, 'CLOSED')}
                        className="btn-status"
                      >
                        Closed
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="comments-section">
                <h3>💬 Comments ({comments.length})</h3>
                
                <div className="comments-list">
                  {comments.map(comment => (
                    <div key={comment.id} className="comment">
                      <div className="comment-header">
                        <strong>{comment.userName}</strong>
                        <span className="comment-role">{comment.userRole}</span>
                        <span className="comment-time">
                          {formatDateTime(comment.createdAt)}
                        </span>
                      </div>
                      <p className="comment-content">{comment.content}</p>
                      {comment.isInternal && (
                        <span className="internal-badge">🔒 Internal</span>
                      )}
                    </div>
                  ))}
                </div>

                <div className="add-comment">
                  <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="Add a comment..."
                    rows="3"
                  />
                  <button 
                    onClick={handleAddComment}
                    className="btn-primary"
                    disabled={!newComment.trim()}
                  >
                    Post Comment
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TicketList;