import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Navbar from './components/Navbar';
import NavbarAuth from './components/NavbarAuth';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminRegister from './pages/AdminRegister';
import Profile from './pages/Profile';
import Bookings from './pages/Bookings';
import EventDetails from './pages/EventDetails';
import ManageEvents from './pages/ManageEvents';
import Reservation from './pages/Reservation';
import EventsSearchPage from './pages/EventsSearchPage';
import './App.css';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { token, user, loading } = useAuth();
  
  if (loading) {
    return <div className="loading">Loading...</div>;
  }
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  
  if (requireAdmin && user?.role !== 'ROLE_ADMIN') {
    return <Navigate to="/" replace />;
  }
  
  return children;
};

const NavbarWrapper = () => {
  const { token } = useAuth();
  return token ? <NavbarAuth /> : <Navbar />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <NavbarWrapper />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/admin-register" element={<AdminRegister />} />
            <Route path="/events" element={<EventsSearchPage />} />
            <Route path="/event/:id" element={<EventDetails />} />
            
            {}
            <Route path="/profile" element={
              <ProtectedRoute>
                <Profile />
              </ProtectedRoute>
            } />
            <Route path="/bookings" element={
              <ProtectedRoute>
                <Bookings />
              </ProtectedRoute>
            } />
            <Route path="/reservation/:reservationId" element={
              <ProtectedRoute>
                <Reservation />
              </ProtectedRoute>
            } />
            <Route path="/admin/events" element={
              <ProtectedRoute requireAdmin={true}>
                <ManageEvents />
              </ProtectedRoute>
            } />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;