# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a full-stack carpooling and real-time traffic monitoring system with three main components:
- **Frontend (carpool-f/)**: Vue 3 application for traffic visualization and carpooling interface
- **Backend (carpool-b/)**: Spring Boot application with REST API for traffic data and user management
- **Database**: MySQL database with traffic data, user accounts, and carpooling tables
- **Data Collection (script/)**: Python script for collecting traffic data from Baidu Maps API

The system combines real-time traffic monitoring with social carpooling features including user authentication, carpool requests, invitations, and trip management.

## Development Commands

### Frontend (carpool-f/)
```bash
cd carpool-f
npm install          # Install dependencies
npm run dev          # Start development server (http://localhost:5173)
npm run build        # Build for production
npm run preview      # Preview production build
```

### Backend (carpool-b/)
```bash
cd carpool-b
./gradlew bootRun    # Start Spring Boot application (http://localhost:8080)
./gradlew build      # Build the application
./gradlew test       # Run tests
```

### Database Setup
```bash
# Create database and tables
mysql -u root -p < script/traffic_table.sql
mysql -u root -p < script/carpool_table.sql

# Or run individual scripts
mysql -u root -p carpool < script/traffic_table.sql
mysql -u root -p carpool < script/carpool_table.sql
mysql -u root -p carpool < script/create_historical_indexes.sql  # Performance indexes
```

### Data Collection Script
```bash
cd script
python run.py        # Run traffic data collection (requires API key and DB config)
```

## Architecture

### Frontend Architecture
- **Vue 3.5.22** with Composition API
- **Pinia 3.0.4** for state management (user authentication state)
- **Vue Router 4.6.3** for navigation
- **ECharts 6.0.0** with vue-echarts for data visualization
- **AMap (高德地图)** for mapping functionality (API key embedded in `index.html`)
- **Element Plus 2.11.8** UI components
- **Axios 1.13.2** for API communication with backend

**Routes**:
- `/` - Home page (public)
- `/traffic` - Traffic monitoring dashboard with real-time data, filtering, and statistics (public)
- `/historical` - Historical traffic data view (public)
- `/monitor` - Real-time monitoring page (public)
- `/carpool` - Carpooling interface (requires authentication)
- `/user` - User profile (requires authentication)
- `/login` - Login page (public)
- `/register` - Registration page (public)
- `/demo` - Component testing page (public)

**Key pages**:
- `Traffic.vue` - Main traffic monitoring interface with search, filters, and statistics
- `HistoricalTraffic.vue` - Historical traffic data visualization
- `Carpool.vue` - Carpooling functionality with request creation and search
- `User.vue` - User profile management

**Key components**:
- `Traffic.vue` - Main traffic monitoring interface
- `RoadCardGrid.vue` - Grid layout for displaying road traffic cards
- `RoadCard.vue` - Individual road traffic status card component
- `HistoricalTrafficCard.vue` - Historical traffic data card
- `NavBar.vue` - Navigation component
- `AMapContainer.vue` - Map integration using AMap API
- `CarpoolPanel.vue` - Carpool request creation form
- `CarpoolCard.vue` - Carpool request card
- `CarpoolCardGrid.vue` - Grid of carpool requests
- `InvitationPanel.vue` - Invitation form
- `InvitationCard.vue` - Single invitation
- `InvitationList.vue` - List of invitations

**State Management** (`stores/user.js`):
- User authentication state with JWT token persistence
- Auto-includes JWT token in API requests via axios interceptor
- Route guards for protected pages

### Backend Architecture
- **Spring Boot 3.5.7** with Java 21
- **Spring Data JPA** for database operations with Hibernate ORM
- **JWT (jjwt 0.12.3)** for stateless authentication
- **BCrypt (spring-security-crypto 6.2.1)** for password encryption
- **Apache Flink 1.16.3** for stream processing
- **Spring WebSocket** for real-time updates
- **MySQL 8.0.33** database

**Package Structure**:
- `controller/` - REST API endpoints (TrafficController, AuthController, CarpoolController, InvitationController, TripController)
- `service/` - Business logic layer (UserService, TrafficService, CarpoolService, InvitationService, TripService)
- `repository/` - Spring Data JPA repositories
- `entity/` - JPA entities with lifecycle callbacks
- `dto/` - Request/response data transfer objects
- `util/JwtUtil.java` - JWT token generation and validation
- `exception/GlobalExceptionHandler.java` - Centralized error handling
- `config/CorsConfig.java` - CORS configuration
- `flink/` - Flink streaming processing
- `websocket/` - WebSocket handlers for real-time updates

**Database Tables**:
- Traffic monitoring: `road_traffic_overall`, `congestion_sections`
- User system: `users`
- Carpooling: `carpool_request`, `carpool_invitation`, `trip_record`, `match_record`

### API Endpoints

**Traffic API** (`/api/traffic/*`):
- `GET /api/traffic` - Get all traffic data (paginated)
- `GET /api/traffic/city/{city}` - Get traffic by city
- `GET /api/traffic/road/{roadName}/city/{city}` - Get traffic by road and city
- `GET /api/traffic/status/{status}` - Get traffic by congestion status
- `GET /api/traffic/search?keyword=` - Search traffic data
- `GET /api/traffic/stats` - Get traffic statistics
- `GET /api/traffic/overview` - Get traffic overview for homepage
- `GET /api/traffic/popular` - Get popular roads
- `GET /api/traffic/historical` - Get historical traffic data
- `GET /api/traffic/cities` - Get supported cities
- `GET /api/traffic/cities/{city}/roads` - Get roads in city

**Authentication API** (`/api/auth/*`):
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login (returns JWT token)
- `GET /api/auth/me` - Get current user info
- `GET /api/auth/validate` - Validate JWT token

**Carpool API** (`/api/carpool/*`):
- `POST /api/carpool/request` - Create carpool request
- `GET /api/carpool/requests` - Search carpool requests
- `POST /api/carpool/invitation` - Create invitation
- `GET /api/carpool/invitation` - Get invitations
- `PUT /api/carpool/invitation/{id}/accept` - Accept invitation
- `PUT /api/carpool/invitation/{id}/reject` - Reject invitation
- `PUT /api/carpool/invitation/{id}/cancel` - Cancel invitation

**Trip API** (`/api/trip/*`):
- `GET /api/trip/{tripId}` - Get trip details
- `GET /api/trip/request/{requestId}` - Get trip by request ID
- `PUT /api/trip/{tripId}/status` - Update trip status

### Data Collection Architecture
- **Python script** (`script/run.py`) collects traffic data from Baidu Maps API
- **Baidu Maps Traffic API** integration for real-time traffic information
- **Scheduled collection** every 5 minutes for configured roads
- **Database persistence** with proper error handling and logging

## Database Schema

### Traffic Monitoring Tables

**road_traffic_overall** - Overall road traffic status:
- `id` - Primary key
- `request_time` - API request timestamp
- `road_name`, `city` - Road and city identifiers
- `api_status` - API status code
- `message` - API response message
- `description` - Semantic traffic description
- `evaluation_status` - Traffic status (0:未知, 1:畅通, 2:缓行, 3:拥堵, 4:严重拥堵)
- `evaluation_status_desc` - Status description
- `created_at` - Record creation timestamp
- Indexes: `idx_road_city (road_name, city)`, `idx_request_time (request_time)`

**congestion_sections** - Detailed congestion information:
- `id` - Primary key
- `overall_id` - Foreign key to road_traffic_overall
- `road_name` - Road name
- `section_desc` - Section congestion description
- `status` - Section status (0-4)
- `status_desc` - Section status description
- `speed` - Average speed (km/h)
- `congestion_distance` - Congestion distance (meters)
- `congestion_trend` - Trend (持平/缓解/加重)
- `created_at` - Creation timestamp
- Indexes: `idx_overall_id (overall_id)`

### User System Tables

**users** - User accounts and authentication:
- `id` - Primary key
- `username` - Unique username
- `password` - BCrypt encrypted password
- `phone_number` - Phone number
- `email` - Email address
- `real_name` - Real name
- `status` - Account status (1:正常, 0:禁用)
- `created_at`, `updated_at` - Timestamps
- Indexes: `idx_username (username)`, `idx_phone (phone_number)`

### Carpooling Tables

**carpool_request** - Carpool ride requests:
- `id` - Primary key
- `user_id` - User ID (foreign key to users)
- `has_car` - Whether user has a car
- `passenger_count` - Number of passengers
- `max_passenger_count` - Maximum passenger capacity
- `start_location`, `end_location` - Route locations
- `start_latitude`, `start_longitude` - Start coordinates
- `end_latitude`, `end_longitude` - End coordinates
- `earliest_departure_time`, `latest_departure_time` - Departure time window
- `phone_number` - Contact phone
- `status_desc` - Request status description
- `created_at` - Creation timestamp

**carpool_invitation** - Carpool invitations between users:
- `id` - Primary key
- `inviter_id` - Inviter user ID (foreign key to users)
- `carpool_request_id` - Related request ID (foreign key to carpool_request)
- `passenger_count` - Number of passengers from inviter
- `message` - Invitation message
- `status` - Invitation status (1:待处理, 2:已接受, 3:已拒绝, 4:已取消)
- `created_at`, `updated_at` - Timestamps
- Indexes: `idx_inviter (inviter_id)`, `idx_request (carpool_request_id)`, `idx_status (status)`

**match_record** - Matches between requests and trips:
- `id` - Primary key
- `request_id` - Carpool request ID (foreign key to carpool_request)
- `user_id` - User ID (foreign key to users)
- `trip_id` - Trip record ID (foreign key to trip_record)
- `created_at` - Match timestamp

**trip_record** - Actual trips created from matched requests:
- `id` - Primary key
- `start_location`, `end_location` - Trip locations
- `start_latitude`, `start_longitude` - Start coordinates
- `end_latitude`, `end_longitude` - End coordinates
- `departure_at`, `arrival_at` - Departure and arrival times
- `status_desc` - Trip status
- `passenger_count` - Total passenger count
- `match_at` - Match timestamp
- `created_at` - Creation timestamp

## Configuration Requirements

### Backend Configuration (`carpool-b/src/main/resources/application.properties`)
```properties
server.port=8080
server.servlet.context-path=/api

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/carpool
spring.datasource.username=root
spring.datasource.password=114514

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# CORS
spring.web.cors.allowed-origins=http://localhost:5173,5174,5175,8080

# Logging
logging.level.com.example.carpool=DEBUG
```

### Frontend Configuration
- **AMap API Key**: Embedded in `carpool-f/index.html`: `35b12f2cc34fbe5817c0eea4ea388e73`
- **API Base URL**: Configured in `services/trafficService.js` (default: `http://localhost:8080/api`)

### Python Script Configuration (`script/run.py`)
Edit the following variables in the script:
```python
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': '114514',
    'database': 'carpool'
}

BAIDU_AK = "MU5oUiBPhetOfK4EO62VrIgxtM3gw4EI"

roads_to_monitor = [
    {"road_name": "四平路", "city": "上海市"},
    # Add more roads here
]
```

## Development Notes

### Authentication Flow
1. **Registration**: User submits credentials → BCrypt password encryption → JWT token generation → Token stored in localStorage
2. **Login**: User submits credentials → BCrypt password verification → JWT token generation → Token stored in Pinia store
3. **Authenticated Requests**: Axios interceptor automatically adds `Authorization: Bearer <token>` header
4. **Protected Routes**: Vue Router guards check authentication status before navigation

### Frontend Development
- Traffic status mapping: 1=畅通(green), 2=缓行(yellow), 3=拥堵(orange), 4=严重拥堵(red)
- Responsive design with mobile-first approach
- Composition API with `<script setup>` syntax
- Centralized API calls in `services/trafficService.js`
- Route guards in `router/index.js` for protected pages

### Backend Development
- Spring Data JPA with custom `@Query` annotations for complex queries
- JPA lifecycle callbacks (`@PrePersist`, `@PreUpdate`) for automatic timestamp management
- Repository pattern with Spring Data JPA repositories
- Service layer handles business logic, controllers handle HTTP
- DTO pattern separates request/response objects from entities
- Global exception handling with `GlobalExceptionHandler`
- CORS configured for multiple frontend development ports
- JWT tokens stored in `util/JwtUtil.java` (consider moving to config)

### Data Flow Patterns
**Traffic Data Collection**: Python script → Baidu API → MySQL → Spring Boot → Vue 3 frontend
**Carpooling Flow**: User creates request → Database → Other users see requests → Send invitation → Accept/reject → Trip created → MatchRecord links request to trip

### Security Practices
- BCrypt with auto-salting for password encryption (never use plaintext or Base64)
- JWT tokens for stateless authentication
- CORS whitelist for allowed origins
- JPA parameterized queries prevent SQL injection
- Input validation on both frontend and backend

## External Dependencies

### Frontend
- **Vue 3.5.22** + Vue Router 4.6.3 + Pinia 3.0.4
- **ECharts 6.0.0** + vue-echarts 8.0.1
- **Element Plus 2.11.8** (UI components)
- **Axios 1.13.2** (HTTP client)
- **Vite 7.1.11** (Build tool)
- Node version: ^20.19.0 || >=22.12.0

### Backend
- **Spring Boot 3.5.7** with Java 21
- **Spring Data JPA** (Hibernate ORM)
- **MySQL Connector 8.0.33**
- **JWT 0.12.3** (jjwt-api, jjwt-impl, jjwt-jackson)
- **Spring Security Crypto 6.2.1** (BCrypt password encryption)
- **Bouncy Castle 1.77** (Cryptography provider)
- **Apache Flink 1.16.3** (Stream processing)
- **Spring WebSocket** (Real-time communication)
- **Jackson** (JSON serialization)
- **Spring Boot DevTools** (Development)

### Data Collection
- **Python 3.x**
- **requests** (HTTP client)
- **pymysql** (MySQL driver)
- **Baidu Maps Traffic API**

### External APIs
- **Baidu Maps Traffic API**: Traffic data collection endpoint
- **AMap (高德地图) API**: Frontend map visualization (API key: `35b12f2cc34fbe5817c0eea4ea388e73`)

## Important Implementation Notes

### When Adding New Features
1. **Backend**: Create DTO → Repository method → Service logic → Controller endpoint
2. **Frontend**: Add API service method → Create/update component → Update router if needed
3. **Database**: Add entity fields → Create migration/update SQL → Update repository queries

### Common Patterns
- **Pagination**: Use Spring Data `Pageable` and return `Page<Entity>`
- **Error Handling**: Throw exceptions in service, catch in `GlobalExceptionHandler`
- **Timestamps**: Use `@PrePersist` and `@PreUpdate` for automatic `created_at`/`updated_at`
- **Authentication**: Check JWT token in service or controller, return 401 if invalid
- **Foreign Keys**: Use JPA `@ManyToOne` and `@OneToMany` relationships

### Testing the Application
1. Start MySQL database
2. Run SQL scripts to create tables
3. Start backend: `cd carpool-b && ./gradlew bootRun`
4. Start frontend: `cd carpool-f && npm run dev`
5. Access frontend at http://localhost:5173
6. Test API endpoints at http://localhost:8080/api

### Git Workflow
- Current branch: `login`
- Main branch: `main`
- Recent features: trip management, invitations, user system, traffic monitoring