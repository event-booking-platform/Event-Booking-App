-- Create database
CREATE DATABASE event_booking;

-- Connect to the database
\c event_booking;

-- Create tables (UPDATED to match Java entities)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    event_time TIME NOT NULL,
    venue VARCHAR(255) NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,
    available_tickets INTEGER NOT NULL,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    event_id INTEGER REFERENCES events(id),
    ticket_count INTEGER NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    booking_reference VARCHAR(50) UNIQUE
);

-- Insert sample data with proper DATE and TIME separation
INSERT INTO events (title, description, event_date, event_time, venue, ticket_price, available_tickets, category) VALUES
('Avengers: Endgame', 'The epic conclusion to the Avengers saga', '2024-02-15', '19:00:00', 'IMAX Theater', 15.99, 100, 'MOVIE'),
('Coldplay Concert', 'Music of the Spheres World Tour', '2024-03-20', '20:00:00', 'National Stadium', 89.99, 5000, 'CONCERT'),
('Hamlet', 'Shakespeare classic theater performance', '2024-02-28', '18:30:00', 'Royal Theater', 25.50, 200, 'THEATER'),
('Tech Conference 2024', 'Annual technology and innovation conference', '2024-04-10', '09:00:00', 'Convention Center', 199.99, 1000, 'CONFERENCE');

-- Create indexes
CREATE INDEX idx_events_date ON events(event_date);
CREATE INDEX idx_events_category ON events(category);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_event_id ON bookings(event_id);



GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO event_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO event_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO event_user;

-- Grant permissions for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO event_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO event_user;