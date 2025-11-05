import axios from 'axios';

export const api = axios.create({
    baseURL: '/api/v1'
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// login
export const login = async (email, password) => {
    const response = await axios.post('/api/v1/users/login', { email, password });
    localStorage.setItem('token', response.data);
    return response;
};

// register
export const register = async (name, email, password) => {
    const response = await axios.post('/api/v1/users/register', {
        name, email, password
    });
    return response;
};

// files
export const getFiles = () => api.get('/files');

export const uploadFile = (file) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileName', file.name);
    formData.append('size', file.size);
    return api.post('/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
};

export const downloadFile = (fileId) =>
    api.get(`/files/download?fileId=${fileId}`, { responseType: 'blob' });

export const deleteFile = (fileId) =>
    api.delete(`/files?fileId=${fileId}`);