import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Unauthorized - clear token and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ==================== AUTH APIs ====================
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  getCurrentUser: () => api.get('/auth/me'),
};

// ==================== TICKET APIs ====================
export const ticketAPI = {
  createTicket: (ticketData) => api.post('/tickets', ticketData),
  getMyTickets: () => api.get('/tickets/my-tickets'),
  getAssignedTickets: () => api.get('/tickets/assigned'),
  getAllTickets: () => api.get('/tickets'),
  getTicketById: (id) => api.get(`/tickets/${id}`),
  updateTicket: (id, data) => api.put(`/tickets/${id}`, data),
  updateTicketStatus: (id, status) => api.put(`/tickets/${id}/status`, { status }),
  assignTicket: (id, agentId) => api.put(`/tickets/${id}/assign`, { agentId }),
};

// ==================== COMMENT APIs ====================
export const commentAPI = {
  addComment: (ticketId, commentData) => api.post(`/tickets/${ticketId}/comments`, commentData),
  getComments: (ticketId) => api.get(`/tickets/${ticketId}/comments`),
};

// ==================== DASHBOARD APIs ====================
export const dashboardAPI = {
  getStats: () => api.get('/dashboard/stats'),
  getAgentDashboard: () => api.get('/dashboard/agent'),
  getManagerDashboard: () => api.get('/dashboard/manager'),
};

export default api;