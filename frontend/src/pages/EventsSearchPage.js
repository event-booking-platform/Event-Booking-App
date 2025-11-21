import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getAllEvents } from '../services/api';

const EventsSearchPage = () => {
  const [events, setEvents] = useState([]);
  const [filteredEvents, setFilteredEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const location = useLocation();
  const navigate = useNavigate();

  // Get search query from URL
  const searchParams = new URLSearchParams(location.search);
  const searchQuery = searchParams.get('search') || '';

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

  useEffect(() => {
    if (searchQuery) {
      const filtered = events.filter(event => 
        event.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.venue.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.category.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setFilteredEvents(filtered);
    } else {
      setFilteredEvents(events);
    }
  }, [searchQuery, events]);

  if (loading) return <div className="loading">Loading events...</div>;

  return (
    <div className="events-search-page">
      <div className="search-container">
        <div className="search-header">
          <h2>
            {searchQuery ? `Search Results for "${searchQuery}"` : 'All Events'}
          </h2>
          <p className="results-count">
            {filteredEvents.length} event{filteredEvents.length !== 1 ? 's' : ''} found
          </p>
        </div>

        {filteredEvents.length === 0 ? (
          <div className="no-results">
            <h3>No events found</h3>
            <p>Try searching with different keywords or browse all events.</p>
            <button 
              onClick={() => navigate('/')}
              className="browse-events-btn"
            >
              Browse All Events
            </button>
          </div>
        ) : (
          <div className="events-grid">
            {filteredEvents.map(event => (
              <div key={event.id} className="event-card">
                <div className="event-image">
                  <img src={`https://picsum.photos/300/200?random=${event.id}`} alt={event.title} />
                </div>
                <div className="event-content">
                  <h5>{event.title}</h5>
                  <p className="event-category">{event.category}</p>
                  <p className="event-date">📅 {event.eventDate}</p>
                  <p className="event-venue">📍 {event.venue}</p>
                  <button onClick={() => navigate(`/event/${event.id}`)}>
                    View Details
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default EventsSearchPage;