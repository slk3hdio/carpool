-- Carpool Database Initialization Script for Docker
-- This script will be automatically executed when the MySQL container starts for the first time

-- Create database if not exists (already created by MYSQL_DATABASE environment variable)
-- CREATE DATABASE IF NOT EXISTS carpool CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE carpool;

-- ============================================
-- Traffic Monitoring Tables
-- ============================================

-- Overall road traffic status table
CREATE TABLE IF NOT EXISTS road_traffic_overall (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_time DATETIME NOT NULL COMMENT 'API request timestamp',
    road_name VARCHAR(255) NOT NULL COMMENT 'Road name',
    city VARCHAR(100) NOT NULL COMMENT 'City name',
    api_status INT COMMENT 'API status code',
    message VARCHAR(500) COMMENT 'API response message',
    description VARCHAR(1000) COMMENT 'Semantic traffic description',
    evaluation_status INT COMMENT 'Traffic status: 0=Unknown, 1=Smooth, 2=Slow, 3=Congested, 4=Severely Congested',
    evaluation_status_desc VARCHAR(50) COMMENT 'Status description',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    INDEX idx_road_city (road_name, city),
    INDEX idx_request_time (request_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Overall road traffic status';

-- Detailed congestion information table
CREATE TABLE IF NOT EXISTS congestion_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    overall_id BIGINT NOT NULL COMMENT 'Foreign key to road_traffic_overall',
    road_name VARCHAR(255) NOT NULL COMMENT 'Road name',
    section_desc VARCHAR(1000) COMMENT 'Section congestion description',
    status INT COMMENT 'Section status: 0-4',
    status_desc VARCHAR(50) COMMENT 'Section status description',
    speed DECIMAL(6,2) COMMENT 'Average speed (km/h)',
    congestion_distance INT COMMENT 'Congestion distance (meters)',
    congestion_trend VARCHAR(20) COMMENT 'Trend: Stable/Relief/Worsen',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    INDEX idx_overall_id (overall_id),
    FOREIGN KEY (overall_id) REFERENCES road_traffic_overall(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Detailed congestion information';

-- ============================================
-- User System Tables
-- ============================================

-- User accounts table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique username',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt encrypted password',
    phone_number VARCHAR(20) COMMENT 'Phone number',
    email VARCHAR(100) COMMENT 'Email address',
    real_name VARCHAR(100) COMMENT 'Real name',
    status INT DEFAULT 1 COMMENT 'Account status: 1=Normal, 0=Disabled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    INDEX idx_username (username),
    INDEX idx_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts';

-- ============================================
-- Carpooling Tables
-- ============================================

-- Carpool ride requests table
CREATE TABLE IF NOT EXISTS carpool_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'User ID (foreign key to users)',
    has_car BOOLEAN DEFAULT false COMMENT 'Whether user has a car',
    passenger_count INT DEFAULT 1 COMMENT 'Number of passengers',
    max_passenger_count INT DEFAULT 4 COMMENT 'Maximum passenger capacity',
    start_location VARCHAR(500) NOT NULL COMMENT 'Start location',
    end_location VARCHAR(500) NOT NULL COMMENT 'End location',
    start_latitude DECIMAL(10, 7) COMMENT 'Start latitude',
    start_longitude DECIMAL(10, 7) COMMENT 'Start longitude',
    end_latitude DECIMAL(10, 7) COMMENT 'End latitude',
    end_longitude DECIMAL(10, 7) COMMENT 'End longitude',
    earliest_departure_time DATETIME COMMENT 'Earliest departure time',
    latest_departure_time DATETIME COMMENT 'Latest departure time',
    phone_number VARCHAR(20) COMMENT 'Contact phone',
    status_desc VARCHAR(50) DEFAULT 'Active' COMMENT 'Request status description',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    INDEX idx_user (user_id),
    INDEX idx_status (status_desc),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Carpool ride requests';

-- Carpool invitations table
CREATE TABLE IF NOT EXISTS carpool_invitation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inviter_id BIGINT NOT NULL COMMENT 'Inviter user ID (foreign key to users)',
    carpool_request_id BIGINT NOT NULL COMMENT 'Related request ID (foreign key to carpool_request)',
    passenger_count INT DEFAULT 1 COMMENT 'Number of passengers from inviter',
    message VARCHAR(500) COMMENT 'Invitation message',
    status INT DEFAULT 1 COMMENT 'Invitation status: 1=Pending, 2=Accepted, 3=Rejected, 4=Cancelled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    INDEX idx_inviter (inviter_id),
    INDEX idx_request (carpool_request_id),
    INDEX idx_status (status),
    FOREIGN KEY (inviter_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (carpool_request_id) REFERENCES carpool_request(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Carpool invitations';

-- Trip records table
CREATE TABLE IF NOT EXISTS trip_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_location VARCHAR(500) NOT NULL COMMENT 'Start location',
    end_location VARCHAR(500) NOT NULL COMMENT 'End location',
    start_latitude DECIMAL(10, 7) COMMENT 'Start latitude',
    start_longitude DECIMAL(10, 7) COMMENT 'Start longitude',
    end_latitude DECIMAL(10, 7) COMMENT 'End latitude',
    end_longitude DECIMAL(10, 7) COMMENT 'End longitude',
    departure_at DATETIME COMMENT 'Departure time',
    arrival_at DATETIME COMMENT 'Arrival time',
    status_desc VARCHAR(50) DEFAULT 'Planned' COMMENT 'Trip status',
    passenger_count INT DEFAULT 0 COMMENT 'Total passenger count',
    match_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Match timestamp',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Trip records';

-- Match records table
CREATE TABLE IF NOT EXISTS match_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL COMMENT 'Carpool request ID (foreign key to carpool_request)',
    user_id BIGINT NOT NULL COMMENT 'User ID (foreign key to users)',
    trip_id BIGINT NOT NULL COMMENT 'Trip record ID (foreign key to trip_record)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Match timestamp',
    INDEX idx_request (request_id),
    INDEX idx_user (user_id),
    INDEX idx_trip (trip_id),
    FOREIGN KEY (request_id) REFERENCES carpool_request(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (trip_id) REFERENCES trip_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Match records';

-- ============================================
-- Initial Data (Optional)
-- ============================================

-- Create a demo user (password: demo123, hashed with BCrypt)
-- Note: This is a demo user. Remove this in production!
INSERT INTO users (username, password, phone_number, email, real_name, status)
VALUES ('demo', '$demo123', '13800138000', 'demo@example.com', 'Demo User', 1)
ON DUPLICATE KEY UPDATE username=username;
