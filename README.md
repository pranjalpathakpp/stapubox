# Sports Venue Availability & Booking Service

A dockerized backend service built with Spring Boot to manage sports venues, time slots, and bookings with availability checks and conflict prevention. This simulates a real-world sports ground/turf booking system.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **MySQL 8.0**
- **Docker & Docker Compose**
- **REST APIs** (no UI)

## Prerequisites

- Docker and Docker Compose installed
- Java 17+ (for local development without Docker)
- Maven 3.6+ (for local development without Docker)

## Quick Start

### Using Docker Compose (Recommended)

1. Navigate to the project directory:
```bash
cd stapubox
```

2. Start the application and MySQL database:
```bash
docker-compose up --build
```

This will:
- Build the Spring Boot application
- Start MySQL database container
- Start the application container
- The application will be available at `http://localhost:8080`

3. To stop the services:
```bash
docker-compose down
```

4. To stop and remove volumes (clean database):
```bash
docker-compose down -v
```

## Database Schema

### Sports Table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `code` (VARCHAR(50), NOT NULL, UNIQUE, INDEXED)
- `name` (VARCHAR(255))

**Indexes:**
- `idx_sport_code` on `code` (unique)

**Note:** Sports are automatically seeded at application startup via `DataInitializer`. Common sports (FOOTBALL, BASKETBALL, TENNIS, etc.) are pre-populated.

### Venues Table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `name` (VARCHAR(255), NOT NULL, INDEXED)
- `location` (VARCHAR(500), NOT NULL)
- `sport_code` (VARCHAR(50), NOT NULL, INDEXED)
- `sport_id` (BIGINT, FOREIGN KEY to sports.id, NOT NULL)
- `description` (VARCHAR(1000))
- `capacity` (INT)
- `created_at` (DATETIME, NOT NULL)
- `updated_at` (DATETIME)

**Indexes:**
- `idx_venue_sport_code` on `sport_code`
- `idx_venue_name` on `name`

**Constraints:**
- Foreign key constraint `fk_venue_sport` on `sport_id` (references sports.id)

### Slots Table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `venue_id` (BIGINT, FOREIGN KEY to venues.id, NOT NULL, INDEXED)
- `slot_date` (DATE, NOT NULL)
- `start_time` (TIME, NOT NULL)
- `end_time` (TIME, NOT NULL)
- `is_available` (BOOLEAN, NOT NULL, DEFAULT true, INDEXED)
- `price` (DOUBLE)
- `created_at` (DATETIME, NOT NULL)
- `updated_at` (DATETIME)

**Indexes:**
- `idx_slot_venue` on `venue_id`
- `idx_slot_date_time` on `slot_date, start_time, end_time`
- `idx_slot_available` on `is_available`

**Constraints:**
- Foreign key constraint `fk_slot_venue` on `venue_id`

### Bookings Table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `slot_id` (BIGINT, FOREIGN KEY to slots.id, NOT NULL, UNIQUE, INDEXED)
- `customer_name` (VARCHAR(255), NOT NULL)
- `customer_email` (VARCHAR(255), NOT NULL, INDEXED)
- `customer_phone` (VARCHAR(20))
- `status` (VARCHAR(20), NOT NULL, DEFAULT 'CONFIRMED', INDEXED)
- `total_amount` (DOUBLE)
- `created_at` (DATETIME, NOT NULL)
- `updated_at` (DATETIME)
- `cancelled_at` (DATETIME)

**Indexes:**
- `idx_booking_slot` on `slot_id`
- `idx_booking_status` on `status`
- `idx_booking_customer_email` on `customer_email`

**Constraints:**
- Unique constraint `uk_booking_slot` on `slot_id` (prevents double booking at DB level)
- Foreign key constraint `fk_booking_slot` on `slot_id`

## API Documentation

### Base URL
```
http://localhost:8080
```

### 1. Create Venue
**POST** `/venues`

Creates a new sports venue.

**Request Body:**
```json
{
  "name": "Central Football Ground",
  "location": "123 Sports Street, City",
  "sportCode": "FOOTBALL",
  "description": "Professional football ground with artificial turf",
  "capacity": 22
}
```

**Note:** `sportId` is optional in the request. The system automatically resolves `sportCode` to the corresponding `Sport` entity and sets `sportId`.

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Central Football Ground",
  "location": "123 Sports Street, City",
  "sportCode": "FOOTBALL",
  "sportId": "1",
  "description": "Professional football ground with artificial turf",
  "capacity": 22,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Central Football Ground",
    "location": "123 Sports Street, City",
    "sportCode": "FOOTBALL",
    "description": "Professional football ground with artificial turf",
    "capacity": 22
  }'
```

---

### 2. Get All Venues
**GET** `/venues`

Retrieves all venues.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Central Football Ground",
    "location": "123 Sports Street, City",
    "sportCode": "FOOTBALL",
    "sportId": "1",
    "description": "Professional football ground with artificial turf",
    "capacity": 22,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/venues
```

---

### 3. Get Venue by ID
**GET** `/venues/{id}`

Retrieves a specific venue by ID.

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "Central Football Ground",
  "location": "123 Sports Street, City",
  "sportCode": "FOOTBALL",
  "sportId": "1",
  "description": "Professional football ground with artificial turf",
  "capacity": 22,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/venues/1
```

---

### 4. Delete Venue
**DELETE** `/venues/{id}`

Deletes a venue by ID.

**Response:** `204 No Content`

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/venues/1
```

---

### 5. Create Slot
**POST** `/venues/{venueId}/slots`

Creates a time slot for a specific venue. Prevents overlapping slots.

**Request Body:**
```json
{
  "slotDate": "2024-01-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "price": 500.00
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "venueId": 1,
  "venueName": "Central Football Ground",
  "slotDate": "2024-01-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "isAvailable": true,
  "price": 500.00,
  "createdAt": "2024-01-15T10:35:00",
  "updatedAt": "2024-01-15T10:35:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/venues/1/slots \
  -H "Content-Type: application/json" \
  -d '{
    "slotDate": "2024-01-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "price": 500.00
  }'
```

---

### 6. Get Slots by Venue
**GET** `/venues/{venueId}/slots`

Retrieves all slots for a specific venue.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "venueId": 1,
    "venueName": "Central Football Ground",
    "slotDate": "2024-01-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "isAvailable": true,
    "price": 500.00,
    "createdAt": "2024-01-15T10:35:00",
    "updatedAt": "2024-01-15T10:35:00"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/venues/1/slots
```

---

### 7. Get Available Venues
**GET** `/venues/available?date=2024-01-20&startTime=10:00:00&endTime=11:00:00&sportCode=FOOTBALL`

Fetches available venues for a given time range and sport.

**Query Parameters:**
- `date` (required): Date in format `YYYY-MM-DD`
- `startTime` (required): Start time in format `HH:mm:ss`
- `endTime` (required): End time in format `HH:mm:ss`
- `sportCode` (required): Sport code from the sports API

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Central Football Ground",
    "location": "123 Sports Street, City",
    "sportCode": "FOOTBALL",
    "sportId": "1",
    "description": "Professional football ground with artificial turf",
    "capacity": 22,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/venues/available?date=2024-01-20&startTime=10:00:00&endTime=11:00:00&sportCode=FOOTBALL"
```

---

### 8. Create Booking
**POST** `/bookings`

Creates a booking for a slot. Uses pessimistic locking to prevent double booking.

**Request Body:**
```json
{
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890",
  "status": "CONFIRMED",
  "totalAmount": 500.00,
  "createdAt": "2024-01-15T10:40:00",
  "updatedAt": "2024-01-15T10:40:00",
  "cancelledAt": null
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+1234567890"
  }'
```

---

### 9. Get Booking by ID
**GET** `/bookings/{id}`

Retrieves a specific booking by ID.

**Response:** `200 OK`
```json
{
  "id": 1,
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890",
  "status": "CONFIRMED",
  "totalAmount": 500.00,
  "createdAt": "2024-01-15T10:40:00",
  "updatedAt": "2024-01-15T10:40:00",
  "cancelledAt": null
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/bookings/1
```

---

### 10. Get All Bookings
**GET** `/bookings`

Retrieves all bookings.

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "slotId": 1,
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+1234567890",
    "status": "CONFIRMED",
    "totalAmount": 500.00,
    "createdAt": "2024-01-15T10:40:00",
    "updatedAt": "2024-01-15T10:40:00",
    "cancelledAt": null
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/bookings
```

---

### 11. Cancel Booking
**PUT** `/bookings/{id}/cancel`

Cancels a booking and frees the slot immediately.

**Response:** `200 OK`
```json
{
  "id": 1,
  "slotId": 1,
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "customerPhone": "+1234567890",
  "status": "CANCELLED",
  "totalAmount": 500.00,
  "createdAt": "2024-01-15T10:40:00",
  "updatedAt": "2024-01-15T10:45:00",
  "cancelledAt": "2024-01-15T10:45:00"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/bookings/1/cancel
```

---

## Sports API Integration

The application validates sport codes against the public API:
```
GET https://stapubox.com/sportslist/
```

**Implementation:**
- `SportService` fetches sports from `https://stapubox.com/sportslist/`
- `DataInitializer` seeds all sports from the API at startup (no hardcoding)
- Sports are stored in the `sports` table with:
  - `code`: The `sport_code` from the API (e.g., "7020104", "7061509")
  - `name`: The `sport_name` from the API (e.g., "badminton", "football")
- When creating a venue, the system:
  1. Validates the `sportCode` against the external API
  2. Resolves the `sportCode` to a `Sport` entity in the database
  3. Creates a foreign key relationship (`venue.sport_id` → `sports.id`)
- The service stores both `sport_code` (from API) and `sport_id` (foreign key) in the database
- **No hardcoding**: All sports come exclusively from the external API

**Data Initialization:**
- Sports are automatically fetched from `https://stapubox.com/sportslist/` at application startup
- All sports from the API are seeded into the database (no hardcoding)
- Both `sport_code` and `sport_id` from the API are stored
- If the API is unavailable at startup, sports will be fetched and validated when creating venues

---

## Assumptions

1. **Booking Model**: One booking corresponds to exactly one slot. A booking cannot span multiple slots.

2. **Slot Immutability**: Once a slot is booked, its time (date, start time, end time) cannot be modified. This ensures data integrity and prevents conflicts.

3. **Cancellation Behavior**: When a booking is cancelled, the associated slot is immediately marked as available (`is_available = true`), making it available for new bookings.

4. **No Slot Overlaps**: The system prevents creating overlapping slots for the same venue on the same date. Overlap detection uses time range intersection logic:
   - Two slots overlap if: `start1 < end2 AND end1 > start2`
   - This covers all overlap scenarios (partial, complete, contained)

5. **Database**: Single MySQL instance is used. No external caching layer (Redis, etc.) is implemented.

6. **Concurrency Safety**: 
   - Pessimistic locking (`PESSIMISTIC_WRITE`) is used when booking a slot to prevent race conditions
   - Database-level unique constraint on `slot_id` in bookings table provides additional protection
   - Double-checking slot availability before creating booking

7. **Time Format**: All times are stored and processed in `HH:mm:ss` format (24-hour format).

8. **Date Format**: Dates are stored and processed in `YYYY-MM-DD` format.

9. **Availability Check**: The availability API checks for slots that:
   - Match the sport code
   - Are on the specified date
   - Are marked as available
   - Fully contain the requested time range (slot.startTime <= requested.startTime AND slot.endTime >= requested.endTime)

10. **Sport Entity Resolution**: Venues must resolve to a valid Sport entity. The system:
    - Validates sport codes against the external API
    - Resolves sportCode to Sport entity in database
    - Creates proper foreign key relationships
    - Ensures sportId is never null

---

## Error Handling

The API returns appropriate HTTP status codes:

- `200 OK`: Successful GET/PUT request
- `201 Created`: Successful POST request
- `204 No Content`: Successful DELETE request
- `400 Bad Request`: Validation errors or invalid input
- `404 Not Found`: Resource not found
- `409 Conflict`: Business logic conflicts (e.g., slot already booked, overlapping slots)
- `500 Internal Server Error`: Unexpected server errors

Error response format:
```json
{
  "status": 400,
  "message": "Error message here",
  "timestamp": "2024-01-15T10:30:00"
}
```

For validation errors:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "fieldName": "Error message for this field"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Concurrency Safety

The booking system implements multiple layers of concurrency protection:

1. **Pessimistic Locking**: When booking a slot, the system uses `PESSIMISTIC_WRITE` lock mode, which locks the slot row in the database until the transaction completes.

2. **Database Constraints**: A unique constraint on `slot_id` in the bookings table ensures that at the database level, only one booking can exist per slot.

3. **Application-Level Checks**: Before creating a booking, the application:
   - Checks if the slot exists and is available
   - Verifies no existing booking exists for the slot
   - Marks the slot as unavailable atomically within the same transaction

This multi-layered approach ensures that even under high concurrency, double bookings cannot occur.

---

## Testing the API

### Automated Testing

Run the automated test script to verify all endpoints:

```bash
./quick-test.sh
```

This script tests:
- ✅ Venue creation
- ✅ Get all venues
- ✅ Slot creation
- ✅ Overlap prevention
- ✅ Availability API
- ✅ Booking creation
- ✅ Double booking prevention
- ✅ Booking cancellation
- ✅ Slot availability after cancellation

### Sample Workflow

1. **Create a venue:**
```bash
curl -X POST http://localhost:8080/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Central Football Ground",
    "location": "123 Sports Street, City",
    "sportCode": "FOOTBALL",
    "description": "Professional football ground",
    "capacity": 22
  }'
```

2. **Create a slot:**
```bash
curl -X POST http://localhost:8080/venues/1/slots \
  -H "Content-Type: application/json" \
  -d '{
    "slotDate": "2024-01-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "price": 500.00
  }'
```

3. **Check availability:**
```bash
curl -X GET "http://localhost:8080/venues/available?date=2024-01-20&startTime=10:00:00&endTime=11:00:00&sportCode=FOOTBALL"
```

4. **Create a booking:**
```bash
curl -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "customerPhone": "+1234567890"
  }'
```

5. **Cancel a booking:**
```bash
curl -X PUT http://localhost:8080/bookings/1/cancel
```

---

## Local Development (Without Docker)

1. Ensure MySQL is running locally
2. Update `application.yml` with your local MySQL credentials
3. Build the project:
```bash
mvn clean package
```
4. Run the application:
```bash
java -jar target/sports-venue-booking-1.0.0.jar
```

---

## Project Structure

```
stapubox/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/stapubox/booking/
│       │       ├── SportsVenueBookingApplication.java
│       │       ├── config/
│       │       │   └── DataInitializer.java (seeds sports at startup)
│       │       ├── controller/
│       │       │   ├── VenueController.java
│       │       │   ├── SlotController.java
│       │       │   ├── BookingController.java
│       │       │   └── AvailabilityController.java
│       │       ├── service/
│       │       │   ├── VenueService.java
│       │       │   ├── SlotService.java
│       │       │   ├── BookingService.java
│       │       │   ├── AvailabilityService.java
│       │       │   └── SportService.java
│       │       ├── repository/
│       │       │   ├── VenueRepository.java
│       │       │   ├── SlotRepository.java
│       │       │   ├── BookingRepository.java
│       │       │   └── SportRepository.java
│       │       ├── model/
│       │       │   ├── Venue.java
│       │       │   ├── Slot.java
│       │       │   ├── Booking.java
│       │       │   └── Sport.java
│       │       ├── dto/
│       │       │   ├── VenueRequest.java
│       │       │   ├── VenueResponse.java
│       │       │   ├── SlotRequest.java
│       │       │   ├── SlotResponse.java
│       │       │   ├── BookingRequest.java
│       │       │   ├── BookingResponse.java
│       │       │   └── AvailabilityRequest.java
│       │       └── exception/
│       │           └── GlobalExceptionHandler.java
│       └── resources/
│           └── application.yml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── postman_collection.json
├── quick-test.sh
```

---

## Additional Resources

- **Postman Collection**: Import `postman_collection.json` into Postman for easy API testing

## Key Features

1. **Sport Entity Management**: Sports are automatically seeded at startup, venues resolve to Sport entities with proper foreign key relationships
2. **Overlap Prevention**: Time range intersection algorithm (`start1 < end2 AND end1 > start2`) prevents slot overlaps
3. **Concurrency Safety**: Three-layer protection (pessimistic locking, unique constraint, application-level checks) prevents double bookings
4. **External API Integration**: Validates sport codes against `https://stapubox.com/sportslist/`
5. **Clean JSON Responses**: Exception handlers sanitize output, ensuring valid JSON in all responses
6. **Comprehensive Documentation**: Complete README, Postman collection, and testing guides

