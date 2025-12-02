import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const NavbarAuth = () => {
  const { user, logout } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    if (window.confirm('Are you sure you want to logout?')) {
      logout();
      setIsMobileMenuOpen(false);
      window.location.href = '/';
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/events?search=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
      setIsMobileMenuOpen(false);
    }
  };

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const handleNavClick = () => {
    setIsMobileMenuOpen(false);
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <nav className="navbar">
      <div className="nav-container">
        {}
        <div className="nav-logo">
          <Link to="/" onClick={handleNavClick}>
            <div className="logo-text">
              <h1>BookEasy</h1>
              <p className="tagline">Just book it — we've got you covered</p>
            </div>
          </Link>
        </div>

        {}
        <div className="nav-search">
          <form onSubmit={handleSearch} className="search-form">
            <input
              type="text"
              placeholder="Search events..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="search-input"
            />
            <button type="submit" className="search-btn">
              🔍
            </button>
          </form>
        </div>

        {}
        <div 
          className={`mobile-menu-btn ${isMobileMenuOpen ? 'active' : ''}`}
          onClick={toggleMobileMenu}
        >
          <span></span>
          <span></span>
          <span></span>
        </div>

        {}
        <div className={`nav-links ${isMobileMenuOpen ? 'nav-links-mobile' : ''}`}>
          <Link to="/" onClick={handleNavClick}>Events</Link>
          
          {isAdmin ? (
            <>
              <Link to="/admin/events" onClick={handleNavClick}>Manage Events</Link>
            </>
          ) : (
            <>
              <Link to="/profile" onClick={handleNavClick}>Profile</Link>
              <Link to="/bookings" onClick={handleNavClick}>My Bookings</Link>
            </>
          )}
          
          <span className="user-welcome">
            Hello, {user?.firstName || user?.username}
            {isAdmin && <span className="admin-badge"> (Admin)</span>}
          </span>
          
          <button onClick={handleLogout} className="logout-btn">Logout</button>
        </div>
      </div>
    </nav>
  );
};

export default NavbarAuth;