import React, { useState, useEffect } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getEventById, reserveTickets } from '../services/api';
import { getEventImageWithFallback } from '../utils/eventImages';

const EventDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { token, user } = useAuth();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [ticketCount, setTicketCount] = useState(1);
  const [reservationLoading, setReservationLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        const response = await getEventById(id);
        setEvent(response.data);
      } catch (error) {
        console.error('Error fetching event:', error);
        setError('Event not found');
      } finally {
        setLoading(false);
      }
    };
    fetchEvent();
  }, [id]);

  const handleReserveTickets = async () => {
    if (!token) {
      alert('Please login to book tickets');
      navigate('/login');
      return;
    }

    if (user?.role === 'ROLE_ADMIN') {
      alert('Organizers cannot book events. Please use a regular user account.');
      return;
    }

    setReservationLoading(true);
    setError('');

    try {
      console.log(' Starting reservation process...');
      console.log(' User token:', token ? 'Present' : 'Missing');
      console.log(' User role:', user?.role);
      
      const response = await reserveTickets({
        eventId: event.id,
        ticketCount: ticketCount
      });
      
      console.log(' Reservation response:', response);
      
      const reservationId = response.data.id || response.data.reservationId;
      
      if (reservationId) {
        console.log(' Reservation created with ID:', reservationId);
        navigate(`/reservation/${reservationId}`);
      } else {
        throw new Error('Reservation ID not received in response');
      }
    } catch (error) {
      console.error(' Reservation error:', error);
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          'Reservation failed';
      setError(errorMessage);
      alert(`Reservation failed: ${errorMessage}`);
    } finally {
      setReservationLoading(false);
    }
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';

  if (loading) return <div className="loading">Loading event details...</div>;
  if (error) return <div className="error-message">{error}</div>;
  if (!event) return <div>Event not found</div>;

  return (
    <div className="event-details-page">
      <div className="event-details-container">
        <div className="event-header">
          <h1>{event.title}</h1>
          <span className="event-category">{event.category}</span>
        </div>

        <div className="event-content">
          <div className="event-info">
            <div className="event-image">
              <img 
                src={getEventImageWithFallback(event, 600, 400).src} 
                alt={event.title}
                onError={(e) => {
                  const { fallback, placeholder } = getEventImageWithFallback(event, 600, 400);
                  e.target.src = fallback;
                  e.target.onerror = () => {
                    e.target.src = placeholder;
                  };
                }}
              />
            </div>
            <div className="event-details">
              <h3>Event Details</h3>
              <div className="detail-item">
                <label>Date:</label>
                <span>{event.eventDate}</span>
              </div>
              <div className="detail-item">
                <label>Time:</label>
                <span>{event.eventTime}</span>
              </div>
              <div className="detail-item">
                <label>Venue:</label>
                <span>{event.venue}</span>
              </div>
              <div className="detail-item">
                <label>Price per ticket:</label>
                <span>Rs.{event.ticketPrice}</span>
              </div>
              <div className="detail-item">
                <label>Tickets Available:</label>
                <span className={event.availableTickets < 10 ? 'low-tickets' : ''}>
                  {event.availableTickets}
                </span>
              </div>
            </div>

            <div className="booking-section">
              <h3>Reserve Tickets</h3>
              {isAdmin ? (
                <div className="admin-message">
                  <p>📋 <strong>Admin View</strong></p>
                  <p>As an Admin, you can manage events but cannot book tickets.</p>
                  <Link to="/admin/events" className="manage-events-btn">
                    Manage Events
                  </Link>
                </div>
              ) : event.availableTickets > 0 ? (
                <>
                  <div className="ticket-selector">
                    <label>Tickets:</label>
                    <select 
                      value={ticketCount} 
                      onChange={(e) => setTicketCount(parseInt(e.target.value))}
                    >
                      {[...Array(Math.min(event.availableTickets, 10))].map((_, i) => (
                        <option key={i + 1} value={i + 1}>
                          {i + 1} ticket{i + 1 > 1 ? 's' : ''}
                        </option>
                      ))}
                    </select>
                  </div>
                  
                  <div className="price-summary">
                    <div className="price-item">
                      <span>{ticketCount} x Rs.{event.ticketPrice}</span>
                      <span>Rs.{(ticketCount * event.ticketPrice).toFixed(2)}</span>
                    </div>
                  </div>

                  <div className="reservation-info">
                    <p>⏰ You'll have 5 minutes to confirm your booking</p>
                    <p>🎫 Tickets will be temporarily reserved for you</p>
                  </div>

                  <button 
                    onClick={handleReserveTickets}
                    disabled={reservationLoading}
                    className="reserve-btn"
                  >
                    {reservationLoading ? 'Reserving...' : `Reserve Tickets - Rs.${(ticketCount * event.ticketPrice).toFixed(2)}`}
                  </button>
                </>
              ) : (
                <div className="sold-out">
                  <span>Sold Out</span>
                </div>
              )}
              {error && <div className="error-message">{error}</div>}
            </div>
          </div>

          <div className="event-description">
            <h3>Description</h3>
            <p>{event.description}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EventDetails;