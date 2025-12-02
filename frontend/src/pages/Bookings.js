import React, { useState, useEffect } from 'react';
import { getUserBookings, cancelBooking } from '../services/api';

const Bookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    fetchBookings();
  }, []);

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

  const handleCancelBooking = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
      return;
    }

    setCancellingId(bookingId);
    try {
      await cancelBooking(bookingId);
      alert('Booking cancelled successfully!');
      
      fetchBookings();
    } catch (error) {
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          'Failed to cancel booking';
      alert(`Cancellation failed: ${errorMessage}`);
    } finally {
      setCancellingId(null);
    }
  };

  const canCancelBooking = (booking) => {
    if (booking.status !== 'CONFIRMED') {
      return false;
    }
    
    const eventDate = new Date(booking.event.eventDate);
    const today = new Date();
    return eventDate > today;
  };

  const isEventInFuture = (eventDate) => {
    const today = new Date();
    const event = new Date(eventDate);
    return event > today;
  };

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
                  <div className="booking-status-container">
                    <span className={`status-badge ${booking.status.toLowerCase()}`}>
                      {booking.status}
                    </span>
                    {canCancelBooking(booking) && (
                      <button 
                        onClick={() => handleCancelBooking(booking.id)}
                        disabled={cancellingId === booking.id}
                        className="cancel-booking-btn"
                      >
                        {cancellingId === booking.id ? 'Cancelling...' : 'Cancel Booking'}
                      </button>
                    )}
                  </div>
                </div>
                
                <div className="booking-details">
                  <div className="detail-item">
                    <label>Booking Reference:</label>
                    <span className="reference-code">{booking.bookingReference}</span>
                  </div>
                  <div className="detail-item">
                    <label>Booking Date:</label>
                    <span>{new Date(booking.bookingDate).toLocaleDateString()}</span>
                  </div>
                  <div className="detail-item">
                    <label>Tickets:</label>
                    <span>{booking.ticketCount}</span>
                  </div>
                  <div className="detail-item">
                    <label>Total Amount:</label>
                    <span className="amount">Rs.{booking.totalAmount}</span>
                  </div>
                  <div className="detail-item">
                    <label>Event Date:</label>
                    <span>
                      {booking.event.eventDate} at {booking.event.eventTime}
                      {!isEventInFuture(booking.event.eventDate) && (
                        <span className="past-event-note"> (Past event)</span>
                      )}
                    </span>
                  </div>
                  <div className="detail-item">
                    <label>Venue:</label>
                    <span>{booking.event.venue}</span>
                  </div>
                </div>

                {}
                {booking.status === 'CONFIRMED' && isEventInFuture(booking.event.eventDate) && (
                  <div className="cancellation-info">
                    <p>⚠️ You can cancel this booking until the event date</p>
                  </div>
                )}

                {booking.status === 'CANCELLED' && (
                  <div className="cancelled-notice">
                    <p>❌ This booking has been cancelled</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Bookings;