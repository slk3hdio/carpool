# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a carpooling and real-time traffic monitoring system with two main components:
- **Frontend (carpool-f/)**: Vue 3 application for traffic visualization and carpooling interface
- **Backend (carpool-b/)**: Spring Boot application with REST API for traffic data management
- **Database**: MySQL database with traffic data storage
- **Data Collection (script/)**: Python script for collecting traffic data from Baidu Maps API

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
mysql -u your_user -p your_database < script/table.sql
```

### Data Collection Script
```bash
cd script
python run.py        # Run traffic data collection (requires API key and DB config)
```

## Architecture

### Frontend Architecture
- **Vue 3** with Composition API
- **Vue Router** for navigation (routes: `/`, `/traffic`, `/carpool`, `/user`, `/demo`)
- **ECharts** with vue-echarts for data visualization
- **AMap (高德地图)** for mapping functionality (API key required in `index.html`)
- **Element Plus** UI components
- **Axios** for API communication with backend

Key pages:
- `/` - Home page
- `/traffic` - Traffic monitoring dashboard with real-time data, filtering, and statistics
- `/carpool` - Carpooling functionality (in development)
- `/user` - User management (in development)
- `/demo` - Road demo page for testing components

Key components:
- `Traffic.vue` - Main traffic monitoring interface with search, filters, and statistics
- `RoadCardGrid.vue` - Grid layout for displaying road traffic cards
- `RoadCard.vue` - Individual road traffic status card component
- `NavBar.vue` - Navigation component
- `AMapContainer.vue` - Map integration using AMap API

### Backend Architecture
- **Spring Boot 3.5.7** with Java 21
- **Spring Data JPA** for database operations
- **MySQL** database with two main tables:
  - `road_traffic_overall` - Stores traffic request data and overall road status
  - `congestion_sections` - Stores detailed congestion information per road section

Key packages:
- `controller.TrafficController` - REST API endpoints for traffic data
- `service.TrafficService` - Business logic for traffic operations
- `repository.*Repository` - Data access layer using Spring Data JPA
- `entity.*` - JPA entities mapping to database tables
- `dto.*` - Data transfer objects for API responses

### API Endpoints
Backend provides REST API at `/api/traffic/*`:
- `GET /api/traffic` - Get all traffic data (paginated)
- `GET /api/traffic/city/{city}` - Get traffic by city
- `GET /api/traffic/status/{status}` - Get traffic by congestion status
- `GET /api/traffic/search` - Search traffic data
- `GET /api/traffic/stats` - Get traffic statistics
- `GET /api/traffic/overview` - Get traffic overview for homepage

### Data Collection Architecture
- **Python script** (`script/run.py`) collects traffic data from Baidu Maps API
- **Baidu Maps Traffic API** integration for real-time traffic information
- **Scheduled collection** every 5 minutes for configured roads
- **Database persistence** with proper error handling and logging

## Database Schema

### road_traffic_overall table
- `id` - Primary key
- `request_time` - API request timestamp
- `road_name`, `city` - Road and city identifiers
- `evaluation_status` - Traffic status (0:未知, 1:畅通, 2:缓行, 3:拥堵, 4:严重拥堵)
- `description` - Semantic traffic description
- `created_at` - Record creation timestamp

### congestion_sections table
- `id` - Primary key
- `overall_id` - Foreign key to road_traffic_overall
- `section_desc` - Detailed congestion description
- `status` - Section congestion status
- `speed` - Average speed (km/h)
- `congestion_distance` - Congestion length in meters
- `congestion_trend` - Trend (持平/缓解/加重)

## Configuration Requirements

### Before Running
1. **Database Setup**: Create MySQL database and run `script/table.sql`
2. **Backend Configuration**: Update `carpool-b/src/main/resources/application.properties` with database connection
3. **Frontend Configuration**: Configure AMap API key in `carpool-f/index.html`
4. **Data Collection**: Configure Baidu API key and database connection in `script/run.py`

### Environment Variables (for data collection script)
- Baidu Maps API key
- Database connection parameters (host, user, password, database)

## Development Notes

### Frontend Development
- The app expects traffic data in specific format from backend API
- Traffic status mapping: 1=畅通(green), 2=缓行(yellow), 3=拥堵(orange), 4=严重拥堵(red)
- Responsive design with mobile-first approach
- Uses modern Vue 3 Composition API patterns

### Backend Development
- Uses Spring Data JPA with pagination and sorting
- Implements proper error handling with GlobalExceptionHandler
- CORS configured for frontend development servers
- RESTful API design with proper HTTP status codes

### Data Collection
- Currently configured for Shanghai roads (can be expanded)
- Includes error handling for API failures and database issues
- Logging system for monitoring data collection process
- Configurable polling interval (currently 5 minutes)

## External Dependencies

### Frontend
- Vue 3.5.22 + Vue Router 4.6.3
- ECharts 6.0.0 + vue-echarts 8.0.1
- Element Plus 2.11.8 (UI components)
- Axios 1.13.2 (HTTP client)

### Backend
- Spring Boot 3.5.7
- Spring Data JPA + MySQL Connector
- Jackson for JSON serialization
- Spring Boot DevTools for development

### Data Collection
- Python 3.x with requests and pymysql
- Baidu Maps Traffic API