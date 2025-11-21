import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getUserBookings } from '../services/api';

const Bookings = () => {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBookings = async () => {
      try {
        const response = await getUserBookings();
        setBookings(response.data);
      } catch (error) {
        console.error('Error fetching bookings:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchBookings();
  }, []);

  if (loading) return <div className="loading">Loading your bookings...</div>;

  return (
    <div className="bookings-page">
      <div className="bookings-container">
        <h2>My Bookings</h2>
        
        {bookings.length === 0 ? (
          <div className="no-bookings">
            <h3>No bookings yet</h3>
            <p>Start exploring events and make your first booking!</p>
          </div>
        ) : (
          <div className="bookings-list">
            {bookings.map(booking => (
              <div key={booking.id} className="booking-card">
                <div className="booking-header">
                  <h3>{booking.event.title}</h3>
                  <span className={`status-badge ${booking.status.toLowerCase()}`}>
                    {booking.status}
                  </span>
                </div>
                
                <div className="booking-details">
                  <div className="detail-item">
                    <label>Booking Reference:</label>
                    <span>{booking.bookingReference}</span>
                  </div>
                  <div className="detail-item">
                    <label>Date:</label>
                    <span>{new Date(booking.bookingDate).toLocaleDateString()}</span>
                  </div>
                  <div className="detail-item">
                    <label>Tickets:</label>
                    <span>{booking.ticketCount}</span>
                  </div>
                  <div className="detail-item">
                    <label>Total Amount:</label>
                    <span>Rs.{booking.totalAmount}</span>
                  </div>
                  <div className="detail-item">
                    <label>Event Date:</label>
                    <span>{booking.event.eventDate} at {booking.event.eventTime}</span>
                  </div>
                  <div className="detail-item">
                    <label>Venue:</label>
                    <span>{booking.event.venue}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Bookings;