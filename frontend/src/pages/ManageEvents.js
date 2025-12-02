import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getAllEvents, deleteEvent, createEvent, updateEvent } from '../services/api';

const ManageEvents = () => {
  const { user } = useAuth();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingEvent, setEditingEvent] = useState(null);

  const [eventForm, setEventForm] = useState({
    title: '',
    description: '',
    eventDate: '',
    eventTime: '',
    venue: '',
    ticketPrice: '',
    availableTickets: '',
    category: ''
  });

  useEffect(() => {
    fetchEvents();
  }, []);

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

  const handleInputChange = (e) => {
    setEventForm({
      ...eventForm,
      [e.target.name]: e.target.value
    });
  };

  const handleCreateEvent = async (e) => {
    e.preventDefault();
    try {
      await createEvent({
        ...eventForm,
        ticketPrice: parseFloat(eventForm.ticketPrice),
        availableTickets: parseInt(eventForm.availableTickets)
      });
      alert('Event created successfully!');
      setShowCreateForm(false);
      resetForm();
      fetchEvents();
    } catch (error) {
      alert('Error creating event: ' + error.response?.data?.message);
    }
  };

  const handleUpdateEvent = async (e) => {
    e.preventDefault();
    try {
      await updateEvent(editingEvent.id, {
        ...eventForm,
        ticketPrice: parseFloat(eventForm.ticketPrice),
        availableTickets: parseInt(eventForm.availableTickets)
      });
      alert('Event updated successfully!');
      setEditingEvent(null);
      resetForm();
      fetchEvents();
    } catch (error) {
      alert('Error updating event: ' + error.response?.data?.message);
    }
  };

  const handleDeleteEvent = async (eventId) => {
    if (window.confirm('Are you sure you want to delete this event?')) {
      try {
        await deleteEvent(eventId);
        alert('Event deleted successfully!');
        fetchEvents();
      } catch (error) {
        alert('Error deleting event: ' + error.response?.data?.message);
      }
    }
  };

  const resetForm = () => {
    setEventForm({
      title: '',
      description: '',
      eventDate: '',
      eventTime: '',
      venue: '',
      ticketPrice: '',
      availableTickets: '',
      category: ''
    });
  };

  const startEdit = (event) => {
    setEditingEvent(event);
    setEventForm({
      title: event.title,
      description: event.description,
      eventDate: event.eventDate,
      eventTime: event.eventTime,
      venue: event.venue,
      ticketPrice: event.ticketPrice,
      availableTickets: event.availableTickets,
      category: event.category
    });
  };

  if (loading) return <div className="loading">Loading events...</div>;

  return (
    <div className="manage-events-page">
      <div className="manage-events-container">
        <div className="page-header">
          <h2>Manage Events</h2>
          <button 
            onClick={() => setShowCreateForm(true)}
            className="create-event-btn"
          >
            + Create New Event
          </button>
        </div>

        {}
        {(showCreateForm || editingEvent) && (
          <div className="event-form-modal">
            <div className="event-form-container">
              <h3>{editingEvent ? 'Edit Event' : 'Create New Event'}</h3>
              <form onSubmit={editingEvent ? handleUpdateEvent : handleCreateEvent}>
                <div className="form-row">
                  <div className="form-group">
                    <label>Event Title *</label>
                    <input
                      type="text"
                      name="title"
                      value={eventForm.title}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Category *</label>
                    <select
                      name="category"
                      value={eventForm.category}
                      onChange={handleInputChange}
                      required
                    >
                      <option value="">Select Category</option>
                      <option value="MOVIE">Movie</option>
                      <option value="CONCERT">Concert</option>
                      <option value="THEATER">Theater</option>
                      <option value="CONFERENCE">Conference</option>
                      <option value="SPORTS">Sports</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    name="description"
                    value={eventForm.description}
                    onChange={handleInputChange}
                    rows="3"
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Event Date *</label>
                    <input
                      type="date"
                      name="eventDate"
                      value={eventForm.eventDate}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Event Time *</label>
                    <input
                      type="time"
                      name="eventTime"
                      value={eventForm.eventTime}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Venue *</label>
                  <input
                    type="text"
                    name="venue"
                    value={eventForm.venue}
                    onChange={handleInputChange}
                    required
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Ticket Price (Rs.) *</label>
                    <input
                      type="number"
                      step="0.01"
                      name="ticketPrice"
                      value={eventForm.ticketPrice}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Available Tickets *</label>
                    <input
                      type="number"
                      name="availableTickets"
                      value={eventForm.availableTickets}
                      onChange={handleInputChange}
                      required
                    />
                  </div>
                </div>

                <div className="form-actions">
                  <button type="submit" className="submit-btn">
                    {editingEvent ? 'Update Event' : 'Create Event'}
                  </button>
                  <button 
                    type="button" 
                    onClick={() => {
                      setShowCreateForm(false);
                      setEditingEvent(null);
                      resetForm();
                    }}
                    className="cancel-btn"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {}
        <div className="events-list">
          {events.length === 0 ? (
            <div className="no-events">
              <h3>No events found</h3>
              <p>Create your first event to get started!</p>
            </div>
          ) : (
            events.map(event => (
              <div key={event.id} className="event-management-card">
                <div className="event-info">
                  <h3>{event.title}</h3>
                  <span className="event-category">{event.category}</span>
                  <p className="event-description">{event.description}</p>
                  <div className="event-details">
                    <span>📅 {event.eventDate} at {event.eventTime}</span>
                    <span>📍 {event.venue}</span>
                    <span>💰 Rs.{event.ticketPrice}</span>
                    <span>🎫 {event.availableTickets} tickets available</span>
                  </div>
                </div>
                <div className="event-actions">
                  <button 
                    onClick={() => startEdit(event)}
                    className="edit-btn"
                  >
                    Edit
                  </button>
                  <button 
                    onClick={() => handleDeleteEvent(event.id)}
                    className="delete-btn"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default ManageEvents;