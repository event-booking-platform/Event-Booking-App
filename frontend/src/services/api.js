import axios from 'axios';

const API = axios.create({
  baseURL: 'https://event-booking-backend-909770785650.asia-south1.run.app/api',
});

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  console.log(' API Request:', config.method?.toUpperCase(), config.url);
  console.log(' Token:', token ? 'Present' : 'Missing');
  
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log(' Token added to headers');
  }
  
  return config;
}, (error) => {
  return Promise.reject(error);
});

API.interceptors.response.use(
  (response) => {
    console.log(' API Success:', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error(' API Error:', {
      status: error.response?.status,
      url: error.config?.url,
      message: error.response?.data?.message || error.message,
      headers: error.config?.headers
    });
    
    if (error.response?.status === 403) {
      console.error(' 403 Forbidden - Check if token is valid and user has proper role');
    }
    
    return Promise.reject(error);
  }
);

export const getAllEvents = () => API.get('/events');
export const getEventById = (id) => API.get(`/events/${id}`);
export const getEventsByCategory = (category) => API.get(`/events/category/${category}`);

export const loginUser = (credentials) => API.post('/auth/login', credentials);
export const registerUser = (userData) => API.post('/auth/register', userData);
export const registerAdmin = (userData) => API.post('/auth/register/admin', userData);

export const getUserProfile = () => API.get('/users/profile');
export const getUserBookings = () => API.get('/bookings/user');
export const createBooking = (bookingData) => API.post('/bookings', bookingData);
export const cancelBooking = (bookingId) => {
  console.log(' Cancel booking called for ID:', bookingId);
  return API.delete(`/bookings/${bookingId}`);
};

export const createEvent = (eventData) => API.post('/events', eventData);
export const updateEvent = (eventId, eventData) => API.put(`/admin/events/${eventId}`, eventData);
export const deleteEvent = (eventId) => API.delete(`/admin/events/${eventId}`);

export const reserveTickets = (reservationData) => {
  console.log(' Reserve tickets called with:', reservationData);
  return API.post('/bookings/reserve', reservationData);
};

export const confirmReservation = (reservationId) => {
  return API.post(`/bookings/reservations/${reservationId}/confirm`);
};

export const cancelReservation = (reservationId) => {
  return API.delete(`/bookings/reservations/${reservationId}`);
};
export const getReservation = (reservationId) => API.get(`/bookings/reservations/${reservationId}`);

export default API;