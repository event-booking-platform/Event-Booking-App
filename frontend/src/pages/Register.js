import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../services/api';
import { useAuth } from '../context/AuthContext';

const Register = () => {
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
  const [showAdminCode, setShowAdminCode] = useState(false);
  const [adminCode, setAdminCode] = useState('');
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
      const response = await registerUser(formData);
      // Remove the auto-login and redirect to login page
      alert('Registration successful! Please login with your credentials.');
      navigate('/login'); // Redirect to login page
    } catch (error) {
      setError(error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const handleAdminCodeSubmit = (e) => {
    e.preventDefault();
    if (adminCode === 'EVENT2024') {
      navigate('/admin-register');
    } else {
      alert('Invalid admin code. Please contact organization.');
    }
  };

  return (
    <div className="register-page">
      <div className="register-container">
        <h2>Create Your Account</h2>
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

          <button type="submit" disabled={loading} className="register-btn">
            {loading ? 'Creating Account...' : 'Register'}
          </button>

          <p className="login-link">
            Already have an account? <Link to="/login">Login here</Link>
          </p>
        </form>

        {/* Admin Code */}
        {showAdminCode ? (
          <div className="admin-code-section">
            <h4>Organizer Registration</h4>
            <p>Enter organization code to proceed</p>
            <form onSubmit={handleAdminCodeSubmit} className="admin-code-form">
              <input
                type="password"
                placeholder="Enter admin code"
                value={adminCode}
                onChange={(e) => setAdminCode(e.target.value)}
                className="admin-code-input"
                required
              />
              <button type="submit" className="admin-code-btn">
                Verify Code
              </button>
            </form>
            <button 
              onClick={() => setShowAdminCode(false)}
              className="cancel-admin-btn"
            >
              Cancel
            </button>
          </div>
        ) : (
          <div className="admin-section">
            <p>Are you an Organizer?</p>
            <span 
              className="admin-register-link"
              onClick={() => setShowAdminCode(true)}
            >
              Register as Organizer
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

export default Register;