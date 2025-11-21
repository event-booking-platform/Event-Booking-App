import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerAdmin } from '../services/api';
import { useAuth } from '../context/AuthContext';

const AdminRegister = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNumber: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

    const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
        const response = await registerAdmin(formData);
        alert('Organizer registration successful! Please login with your credentials.');
        navigate('/login'); 
    } catch (error) {
        setError(error.response?.data?.message || 'Admin registration failed');
    } finally {
        setLoading(false);
    }
    };

  return (
    <div className="register-page">
      <div className="register-container">
        <h2>Register as Organizer</h2>
        <form onSubmit={handleSubmit} className="register-form">
          {error && <div className="error-message">{error}</div>}
          
          <div className="form-group">
            <label>Username *</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Email *</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Password *</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <div className="name-fields">
            <div className="form-group">
              <label>First Name *</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
            />
          </div>

          <button type="submit" disabled={loading} className="admin-register-btn">
            {loading ? 'Creating Organizer Account...' : 'Register as Organizer'}
          </button>

          <p className="login-link">
            Already have an account? <Link to="/login">Login here</Link>
          </p>
          
          <div className="user-register-link">
            <Link to="/register">Register as regular user instead</Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AdminRegister;