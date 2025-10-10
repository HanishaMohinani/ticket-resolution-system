import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketAPI } from '../services/api';
import './CreateTicket.css';

const CreateTicket = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await ticketAPI.createTicket(formData);
      navigate('/tickets/my-tickets');
    } catch (error) {
      setError(error.response?.data?.message || 'Failed to create ticket');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-ticket-container">
      <div className="create-ticket-box">
        <div className="create-ticket-header">
          <h1>📝 Create New Ticket</h1>
          <p>Describe your issue and we'll help you resolve it</p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="create-ticket-form">
          <div className="form-group">
            <label>Title *</label>
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="Brief description of the issue"
              maxLength="500"
              required
            />
            <small>{formData.title.length}/500 characters</small>
          </div>

          <div className="form-group">
            <label>Priority *</label>
            <select
              name="priority"
              value={formData.priority}
              onChange={handleChange}
              required
            >
              <option value="LOW">🟢 Low - Minor issue, no urgency</option>
              <option value="MEDIUM">🟡 Medium - Normal priority</option>
              <option value="HIGH">🟠 High - Important, needs attention</option>
              <option value="CRITICAL">🔴 Critical - System down, urgent!</option>
            </select>
            <small>
              {formData.priority === 'CRITICAL' && 'Response in 1 hour, Resolution in 4 hours'}
              {formData.priority === 'HIGH' && 'Response in 2 hours, Resolution in 8 hours'}
              {formData.priority === 'MEDIUM' && 'Response in 4 hours, Resolution in 24 hours'}
              {formData.priority === 'LOW' && 'Response in 8 hours, Resolution in 48 hours'}
            </small>
          </div>

          <div className="form-group">
            <label>Description *</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Please provide detailed information about your issue..."
              rows="8"
              required
            />
            <small>Be as specific as possible to help us resolve your issue faster</small>
          </div>

          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create Ticket'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateTicket;