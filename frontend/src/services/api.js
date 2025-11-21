import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api',
});

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Event API calls
export const getAllEvents = () => API.get('/events');
export const getEventById = (id) => API.get(`/events/${id}`);
export const getEventsByCategory = (category) => API.get(`/events/category/${category}`);

// Auth API calls  
export const loginUser = (credentials) => API.post('/auth/login', credentials);
export const registerUser = (userData) => API.post('/auth/register', userData);
export const registerAdmin = (userData) => API.post('/auth/register/admin', userData);

// Booking API calls
export const getUserProfile = () => API.get('/users/profile');
export const getUserBookings = () => API.get('/bookings/user');
export const createBooking = (bookingData) => API.post('/bookings', bookingData);

// Manage Event API calls
export const createEvent = (eventData) => API.post('/events', eventData);
export const updateEvent = (eventId, eventData) => API.put(`/admin/events/${eventId}`, eventData);
export const deleteEvent = (eventId) => API.delete(`/admin/events/${eventId}`);

// Reserve API calls
export const reserveTickets = (reservationData) => API.post('/bookings/reserve', reservationData);
export const confirmReservation = (reservationId) => API.post(`/bookings/reservations/${reservationId}/confirm`);
export const cancelReservation = (reservationId) => API.delete(`/bookings/reservations/${reservationId}`);
export const getReservation = (reservationId) => API.get(`/bookings/reservations/${reservationId}`);


export default API;