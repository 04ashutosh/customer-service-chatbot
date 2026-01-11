import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add token to requests
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Auth APIs
export const authAPI = {
    register: (data) => api.post('/auth/register', data),
    login: (data) => api.post('/auth/login', data),
};

// Chat APIs
export const chatAPI = {
    sendMessage: (question) => api.post('/chat', { question }),
};

// Knowledge Base APIs
export const kbAPI = {
    getAll: (page = 0, size = 10) => api.get(`/admin/kb?page=${page}&size=${size}`),
    create: (data) => api.post('/admin/kb', data),
    update: (id, data) => api.put(`/admin/kb/${id}`, data),
    delete: (id) => api.delete(`/admin/kb/${id}`),
    uploadCsv: (formData) => api.post('/admin/kb/upload-csv', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
};

// Unanswered Questions APIs
export const unansweredAPI = {
    getAll: (status = 'NEW', page = 0, size = 10) =>
        api.get(`/admin/unanswered?status=${status}&page=${page}&size=${size}`),
    approve: (id, answer) => api.post(`/admin/unanswered/${id}/approve`, { answer }),
    reject: (id) => api.delete(`/admin/unanswered/${id}`),
};

export default api;
