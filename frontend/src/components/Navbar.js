import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false); 

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen); 
  };

  const handleNavClick = () => {
    setIsMobileMenuOpen(false); 
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        {}
        <div className="nav-logo">
          <a href="/" onClick={handleNavClick}>
            <div className="logo-text">
              <h1>BookEasy</h1>
              <p className="tagline">Just book it — we've got you covered</p>
            </div>
          </a>
        </div>

        {}
        <div className="nav-search">
          <form className="search-form" onSubmit={(e) => {
            e.preventDefault();
            const searchInput = e.target.querySelector('.search-input');
            if (searchInput.value.trim()) {
              navigate(`/events?search=${encodeURIComponent(searchInput.value)}`);
              setIsMobileMenuOpen(false);
            }
          }}>
            <input 
              type="text" 
              className="search-input" 
              placeholder="Search events..." 
            />
            <button type="submit" className="search-btn">Search</button>
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
          <a href="/" onClick={handleNavClick}>Home</a>
          <a href="/login" onClick={handleNavClick}>Login</a>
          <a href="/register" onClick={handleNavClick}>Register</a>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;