import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { confirmReservation, cancelReservation, getReservation } from '../services/api';

const Reservation = () => {
  const { reservationId } = useParams();
  const navigate = useNavigate();
  const [reservation, setReservation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [timeLeft, setTimeLeft] = useState(300);

  const fetchReservation = useCallback(async () => {
    try {
      const response = await getReservation(reservationId);
      setReservation(response.data);
      
      if (response.data.reservationExpiry) {
        const expiryTime = new Date(response.data.reservationExpiry).getTime();
        const currentTime = new Date().getTime();
        const remainingSeconds = Math.max(0, Math.floor((expiryTime - currentTime) / 1000));
        setTimeLeft(remainingSeconds);
      } else {
        setTimeLeft(response.data.secondsRemaining || 300);
      }
    } catch (error) {
      console.error('Error fetching reservation:', error);
      alert('Reservation not found or expired');
      navigate('/');
    } finally {
      setLoading(false);
    }
  }, [reservationId, navigate]);

  const handleAutoCancel = useCallback(() => {
    alert('Reservation time has expired. Tickets have been released.');
    navigate('/');
  }, [navigate]);

  useEffect(() => {
    fetchReservation();
  }, [fetchReservation]);

  useEffect(() => {
    if (!reservation) return;

    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          handleAutoCancel();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [reservation, handleAutoCancel]);

  const handleConfirm = async () => {
    setProcessing(true);
    try {
      await confirmReservation(reservationId);
      alert('Booking confirmed successfully!');
      navigate('/bookings');
    } catch (error) {
      alert(error.response?.data?.message || error.response?.data?.error || 'Failed to confirm booking');
    } finally {
      setProcessing(false);
    }
  };

  const handleCancel = async () => {
    if (window.confirm('Are you sure you want to cancel this reservation?')) {
      setProcessing(true);
      try {
        await cancelReservation(reservationId);
        alert('Reservation cancelled');
        navigate('/');
      } catch (error) {
        alert(error.response?.data?.message || error.response?.data?.error || 'Failed to cancel reservation');
      } finally {
        setProcessing(false);
      }
    }
  };

  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
  };

  if (loading) return <div className="loading">Loading reservation...</div>;
  if (!reservation) return <div>Reservation not found</div>;

  return (
    <div className="reservation-page">
      <div className="reservation-container">
        <div className="reservation-card">
          <div className="reservation-header">
            <h2>Confirm Your Booking</h2>
            <div className={`time-remaining ${timeLeft < 60 ? 'warning' : ''}`}>
              ⏰ Time remaining: {formatTime(timeLeft)}
            </div>
          </div>

          <div className="reservation-details">
            <div className="event-info">
              <h3>{reservation.event.title}</h3>
              <p className="event-category">{reservation.event.category}</p>
              <div className="detail-grid">
                <div className="detail-item">
                  <label>Date:</label>
                  <span>{reservation.event.eventDate}</span>
                </div>
                <div className="detail-item">
                  <label>Time:</label>
                  <span>{reservation.event.eventTime}</span>
                </div>
                <div className="detail-item">
                  <label>Venue:</label>
                  <span>{reservation.event.venue}</span>
                </div>
              </div>
            </div>

            <div className="booking-summary">
              <h4>Booking Summary</h4>
              <div className="summary-item">
                <label>Tickets:</label>
                <span>{reservation.ticketCount} x Rs.{reservation.event.ticketPrice}</span>
              </div>
              <div className="summary-item total">
                <label>Total Amount:</label>
                <span>Rs.{reservation.totalAmount}</span>
              </div>
              <div className="summary-item">
                <label>Reference:</label>
                <span className="reference">{reservation.bookingReference}</span>
              </div>
            </div>
          </div>

          <div className="reservation-actions">
            <button 
              onClick={handleConfirm}
              disabled={processing || timeLeft === 0}
              className="confirm-btn"
            >
              {processing ? 'Confirming...' : 'Confirm Booking'}
            </button>
            <button 
              onClick={handleCancel}
              disabled={processing}
              className="cancel-btn"
            >
              Cancel Reservation
            </button>
          </div>

          <div className="reservation-note">
            <p>💡 <strong>Important:</strong> Your tickets are reserved for 5 minutes. 
            Please confirm your booking before the timer expires to secure your tickets.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Reservation;