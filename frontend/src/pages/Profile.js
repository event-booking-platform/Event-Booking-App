import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getUserProfile } from '../services/api';

const Profile = () => {
  const { user } = useAuth();
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        const response = await getUserProfile();
        setUserData(response.data);
      } catch (error) {
        console.error('Error fetching profile:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchUserProfile();
  }, []);

  if (loading) return <div className="loading">Loading profile...</div>;
  if (!userData) return <div>Error loading profile</div>;

  return (
    <div className="profile-page">
      <div className="profile-container">
        <h2>User Profile</h2>
        <div className="profile-card">
          <div className="profile-header">
            <h3>Personal Information</h3>
          </div>
          <div className="profile-info">
            <div className="info-item">
              <label>Username:</label>
              <span>{userData.username}</span>
            </div>
            <div className="info-item">
              <label>Email:</label>
              <span>{userData.email}</span>
            </div>
            <div className="info-item">
              <label>First Name:</label>
              <span>{userData.firstName || 'Not provided'}</span>
            </div>
            <div className="info-item">
              <label>Last Name:</label>
              <span>{userData.lastName || 'Not provided'}</span>
            </div>
            <div className="info-item">
              <label>Phone Number:</label>
              <span>{userData.phoneNumber || 'Not provided'}</span>
            </div>
            <div className="info-item">
              <label>Role:</label>
              <span className={`role-badge ${userData.role === 'ROLE_ADMIN' ? 'admin' : 'user'}`}>
                {userData.role === 'ROLE_ADMIN' ? 'Organizer' : 'User'}
              </span>
            </div>
            <div className="info-item">
              <label>Member Since:</label>
              <span>{new Date(userData.createdAt).toLocaleDateString()}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;