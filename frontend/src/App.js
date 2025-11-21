import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
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
            <Route path="/profile" element={<Profile />} />
            <Route path="/bookings" element={<Bookings />} />
            <Route path="/event/:id" element={<EventDetails />} />
            <Route path="/admin/events" element={<ManageEvents />} />  
            <Route path="/reservation/:reservationId" element={<Reservation />} />     
            <Route path="/events" element={<EventsSearchPage />} />   
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;