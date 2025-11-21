import React, { useState, useEffect } from 'react';
import { getAllEvents } from '../services/api';
import { useNavigate } from 'react-router-dom';

const Home = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await getAllEvents();
        setEvents(response.data);
      } catch (error) {
        console.error('Error fetching events:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  if (loading) return <div className="loading">Loading events...</div>;

  return (
    <>
      <div className="home">
        <h1>Upcoming Events</h1>
        <div className="events-grid">
          {events.map(event => (
            <div key={event.id} className="event-card">
              <div className="event-image">
                <img src={`https://picsum.photos/300/200?random=${event.id}`} alt={event.title} />
              </div>
              <div className="event-content">
                <h5>{event.title}</h5>
                <p className="event-date">📅 {event.eventDate}</p>
                <button onClick={() => navigate(`/event/${event.id}`)}>
                  View Details
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
      
      {/* Contact Section */}
      <div className="contact-section">
        <div className="contact-container">
          <h3>Contact Us</h3>
          <div className="contact-info">
            <div className="contact-item">
              <span>📧</span>
              <span>ShreyaGarg@CMEgroup.com</span>
            </div>
            <div className="contact-item">
              <span>📞</span>
              <span>+(91) 8445282664</span>
            </div>
            <div className="contact-item">
              <span>🏢</span>
              <span>CME Group, Bangaluru</span>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Home;