# 🎫 Event Booking Platform

A full-stack event management and booking platform with advanced concurrency control, built with Spring Boot backend and React frontend.

## ✨ Features

### 🔐 Security & Authentication
- JWT-based authentication with 24-hour token expiration
- Role-based access control (User vs Organizer)
- BCrypt password encryption
- Protected API endpoints with Spring Security

### 🎭 Event Management
- Create, read, update, delete events (Organizer role)
- Event categorization (Movies, Concerts, Theater, Sports, etc.)
- Real-time ticket availability tracking

### 🎫 Advanced Booking System
- **5-minute ticket reservation** with concurrency control
- Real-time countdown timer for reservation confirmation
- Automatic ticket release on expiry or cancellation
- Prevents double-booking with reservation locking

### 👥 User Management
- User registration with mandatory profile fields
- Organizer registration with admin code verification
- Profile management with booking history

## 🏗️ Architecture

### Backend (Spring Boot)


📁 com.eventbooking

├── 📁 config/ # Application Configuration

├── 📁 controller/ # REST API Controllers

├── 📁 entity/ # JPA Entities

├── 📁 repository/ # Spring Data JPA

├── 📁 service/ # Business Logic

├── 📁 security/ # JWT Authentication

└── 📁 dto/ # Data Transfer Objects


### Frontend 

📁 src/

├── 📁 components/ # Reusable UI Components

├── 📁 pages/ # Application Pages

├── 📁 context/ # React Context (Auth)

├── 📁 services/ # API Communication

└── 📁 styles/ # CSS Stylesheets



## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 16+
- PostgreSQL 15+
- Maven 3.6+

### Backend Setup
```bash
cd backend

# Configure database in application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/event_booking
spring.datasource.username=event_user
spring.datasource.password=EventUser@123

# Run application
mvn spring-boot:run
```

### Frontend Setup
```bash

cd frontend

# Install dependencies
npm install

# Start development server
npm start
```


## 📊 API Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | User registration | Public |
| POST | `/api/auth/login` | User login | Public |
| GET | `/api/events` | Get all events | Public |
| POST | `/api/bookings/reserve` | Reserve tickets | User |
| POST | `/api/bookings/reservations/{id}/confirm` | Confirm booking | User |
| POST | `/api/events` | Create event | Organizer |


🔮 Future Enhancements

* Payment gateway integration
* Email notifications
* Advanced analytics dashboard

👨‍💻 Developer

Shreya Garg | Tanishq Anand


