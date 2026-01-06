#!/bin/bash

BASE_URL="http://localhost:8080"

echo "🚀 Quick Test - Sports Venue Booking API" >&2
echo "========================================" >&2
echo "" >&2

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'
check_server() {
    echo -n "Checking if server is running... " >&2
    if curl -s -f "$BASE_URL/venues" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Server is up${NC}" >&2
        return 0
    else
        echo -e "${RED}❌ Server is not responding${NC}" >&2
        echo "   Make sure you ran: docker-compose up --build" >&2
        return 1
    fi
}

test_create_venue() {
    echo -e "\n${YELLOW}Test 1: Creating venue...${NC}" >&2
    
    TMP_FILE=$(mktemp)
    
    curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/venues" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Football Ground",
            "location": "123 Test Street",
            "sportCode": "7061509",
            "description": "Test venue",
            "capacity": 22
        }' > "$TMP_FILE" 2>/dev/null
    
    HTTP_CODE=$(grep "HTTP_CODE:" "$TMP_FILE" | tail -n1 | sed 's/.*HTTP_CODE://' | tr -d '\r\n')
    BODY=$(sed '/HTTP_CODE:/,$d' "$TMP_FILE" | tr -d '\r' | sed 's/[[:cntrl:]]//g')
    
    rm -f "$TMP_FILE"
    
    if [ -n "$HTTP_CODE" ] && [ "$HTTP_CODE" -eq 201 ] 2>/dev/null; then
        VENUE_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -n1)
        echo -e "${GREEN}✅ Venue created with ID: $VENUE_ID${NC}" >&2
        echo "$VENUE_ID"
    else
        echo -e "${RED}❌ Failed to create venue (HTTP ${HTTP_CODE:-unknown})${NC}" >&2
        echo "$BODY" | head -c 200 >&2
        echo "" >&2
        echo ""
    fi
}

test_get_venues() {
    echo -e "\n${YELLOW}Test 2: Getting all venues...${NC}" >&2
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/venues")
    
    if [ -n "$HTTP_CODE" ] && [ "$HTTP_CODE" -eq 200 ] 2>/dev/null; then
        COUNT=$(curl -s -X GET "$BASE_URL/venues" | grep -o '"id"' | wc -l | tr -d ' ')
        echo -e "${GREEN}✅ Retrieved venues (Count: $COUNT)${NC}" >&2
    else
        echo -e "${RED}❌ Failed to get venues (HTTP ${HTTP_CODE:-unknown})${NC}" >&2
    fi
}

test_create_slot() {
    local VENUE_ID=$1
    echo -e "\n${YELLOW}Test 3: Creating slot for venue $VENUE_ID...${NC}" >&2
    
    TMP_FILE=$(mktemp)
    
    curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/venues/$VENUE_ID/slots" \
        -H "Content-Type: application/json" \
        -d '{
            "slotDate": "2024-01-20",
            "startTime": "10:00:00",
            "endTime": "11:00:00",
            "price": 500.00
        }' > "$TMP_FILE" 2>/dev/null
    
    HTTP_CODE=$(grep "HTTP_CODE:" "$TMP_FILE" | tail -n1 | sed 's/.*HTTP_CODE://' | tr -d '\r\n')
    BODY=$(sed '/HTTP_CODE:/,$d' "$TMP_FILE" | tr -d '\r' | sed 's/[[:cntrl:]]//g')
    
    rm -f "$TMP_FILE"
    
    if [ -n "$HTTP_CODE" ] && [ "$HTTP_CODE" -eq 201 ] 2>/dev/null; then
        SLOT_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -n1)
        echo -e "${GREEN}✅ Slot created with ID: $SLOT_ID${NC}" >&2
        echo "$SLOT_ID"
    else
        echo -e "${RED}❌ Failed to create slot (HTTP ${HTTP_CODE:-unknown})${NC}" >&2
        echo "$BODY" | head -c 200 >&2
        echo "" >&2
        echo ""
    fi
}

test_overlap_prevention() {
    local VENUE_ID=$1
    echo -e "\n${YELLOW}Test 4: Testing overlap prevention...${NC}" >&2
    
    if [ -z "$VENUE_ID" ]; then
        echo -e "${YELLOW}⚠️  Skipping - no venue ID${NC}"
        return
    fi
    
    TMP_FILE=$(mktemp)
    curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/venues/$VENUE_ID/slots" \
        -H "Content-Type: application/json" \
        -d '{
            "slotDate": "2024-01-20",
            "startTime": "10:30:00",
            "endTime": "11:30:00",
            "price": 550.00
        }' > "$TMP_FILE" 2>/dev/null
    
    HTTP_CODE=$(grep "HTTP_CODE:" "$TMP_FILE" | tail -n1 | sed 's/.*HTTP_CODE://' | tr -d '\r\n')
    rm -f "$TMP_FILE"
    
    if [ -n "$HTTP_CODE" ] && [ "$HTTP_CODE" -eq 400 ] 2>/dev/null; then
        echo -e "${GREEN}✅ Overlap prevention works! (Correctly rejected)${NC}" >&2
    else
        echo -e "${RED}❌ Overlap prevention failed (HTTP ${HTTP_CODE:-unknown})${NC}" >&2
    fi
}

test_availability() {
    echo -e "\n${YELLOW}Test 5: Getting available venues...${NC}" >&2
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/venues/available?date=2024-01-20&startTime=10:00:00&endTime=11:00:00&sportCode=7061509")
    COUNT=$(echo "$RESPONSE" | grep -o '"id"' | wc -l)
    
    if [ "$COUNT" -gt 0 ]; then
        echo -e "${GREEN}✅ Found $COUNT available venue(s)${NC}" >&2
    else
        echo -e "${YELLOW}⚠️  No available venues (this might be expected)${NC}" >&2
    fi
}

test_create_booking() {
    local SLOT_ID=$1
    echo -e "\n${YELLOW}Test 6: Creating booking for slot $SLOT_ID...${NC}" >&2
    
    if [ -z "$SLOT_ID" ] || [ "$SLOT_ID" = "unknown" ]; then
        echo -e "${RED}❌ Cannot create booking - invalid slot ID${NC}" >&2
        echo ""
        return
    fi
    
    TMP_FILE=$(mktemp)
    
    curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/bookings" \
        -H "Content-Type: application/json" \
        -d "{
            \"slotId\": $SLOT_ID,
            \"customerName\": \"Test Customer\",
            \"customerEmail\": \"test@example.com\",
            \"customerPhone\": \"+1234567890\"
        }" > "$TMP_FILE" 2>/dev/null
    
    HTTP_CODE=$(grep "HTTP_CODE:" "$TMP_FILE" | tail -n1 | sed 's/.*HTTP_CODE://' | tr -d '\r\n')
    BODY=$(sed '/HTTP_CODE:/,$d' "$TMP_FILE" | tr -d '\r' | sed 's/[[:cntrl:]]//g')
    
    rm -f "$TMP_FILE"
    
    if [ -n "$HTTP_CODE" ] && [ "$HTTP_CODE" -eq 201 ] 2>/dev/null; then
        BOOKING_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -n1)
        echo -e "${GREEN}✅ Booking created with ID: $BOOKING_ID${NC}" >&2
        echo "$BOOKING_ID"
    else
        echo -e "${RED}❌ Failed to create booking (HTTP ${HTTP_CODE:-unknown})${NC}" >&2
        echo "$BODY" | head -c 200 >&2
        echo "" >&2
        echo ""
    fi
}

test_double_booking() {
    local SLOT_ID=$1
    echo -e "\n${YELLOW}Test 7: Testing double booking prevention...${NC}" >&2
    
    if [ -z "$SLOT_ID" ] || [ "$SLOT_ID" = "unknown" ]; then
        echo -e "${YELLOW}⚠️  Skipping - no valid slot ID${NC}" >&2
        return
    fi
    
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/bookings" \
        -H "Content-Type: application/json" \
        -d "{
            \"slotId\": $SLOT_ID,
            \"customerName\": \"Another Customer\",
            \"customerEmail\": \"another@example.com\"
        }")
    
    if [ -n "$HTTP_CODE" ] && ([ "$HTTP_CODE" -eq 400 ] || [ "$HTTP_CODE" -eq 409 ]) 2>/dev/null; then
        echo -e "${GREEN}✅ Double booking prevention works! (Correctly rejected)${NC}" >&2
    else
        echo -e "${RED}❌ Double booking prevention failed (HTTP ${HTTP_CODE:-unknown})${NC}" >&2
    fi
}

test_cancel_booking() {
    local BOOKING_ID=$1
    echo -e "\n${YELLOW}Test 8: Cancelling booking $BOOKING_ID...${NC}" >&2
    
    if [ -z "$BOOKING_ID" ] || [ "$BOOKING_ID" = "unknown" ]; then
        echo -e "${YELLOW}⚠️  Skipping - no valid booking ID${NC}" >&2
        return
    fi
    
    TMP_FILE=$(mktemp)
    curl -s -w "\nHTTP_CODE:%{http_code}" -X PUT "$BASE_URL/bookings/$BOOKING_ID/cancel" > "$TMP_FILE" 2>/dev/null
    
    HTTP_CODE=$(grep "HTTP_CODE:" "$TMP_FILE" | tail -n1 | sed 's/.*HTTP_CODE://' | tr -d '\r\n')
    rm -f "$TMP_FILE"
    
    if [ -n "$HTTP_CODE" ] && [ "$HTTP_CODE" -eq 200 ] 2>/dev/null; then
        echo -e "${GREEN}✅ Booking cancelled successfully${NC}" >&2
    else
        echo -e "${RED}❌ Failed to cancel booking (HTTP ${HTTP_CODE:-unknown})${NC}" >&2
    fi
}

test_slot_available_again() {
    local VENUE_ID=$1
    echo -e "\n${YELLOW}Test 9: Verifying slot is available again...${NC}" >&2
    
    RESPONSE=$(curl -s -X GET "$BASE_URL/venues/$VENUE_ID/slots")
    IS_AVAILABLE=$(echo "$RESPONSE" | grep -o '"isAvailable":true' | head -n1)
    
    if [ -n "$IS_AVAILABLE" ]; then
        echo -e "${GREEN}✅ Slot is available again (cancellation worked!)${NC}" >&2
    else
        echo -e "${YELLOW}⚠️  Slot might still be booked${NC}" >&2
    fi
}

main() {
    if ! check_server; then
        exit 1
    fi
    
    echo "" >&2
    echo "Starting tests..." >&2
    echo "" >&2
    
    VENUE_ID=$(test_create_venue)
    if [ -z "$VENUE_ID" ]; then
        echo -e "${RED}❌ Cannot continue without venue${NC}" >&2
        exit 1
    fi
    
    test_get_venues
    SLOT_ID=$(test_create_slot "$VENUE_ID")
    if [ -z "$SLOT_ID" ]; then
        echo -e "${YELLOW}⚠️  Continuing without slot ID${NC}" >&2
    fi
    
    test_overlap_prevention "$VENUE_ID"
    test_availability
    
    if [ -n "$SLOT_ID" ]; then
        BOOKING_ID=$(test_create_booking "$SLOT_ID")
        if [ -n "$BOOKING_ID" ]; then
            test_double_booking "$SLOT_ID"
            test_cancel_booking "$BOOKING_ID"
            test_slot_available_again "$VENUE_ID"
        else
            echo -e "${YELLOW}⚠️  Skipping booking tests - booking creation failed${NC}" >&2
        fi
    else
        echo -e "${YELLOW}⚠️  Skipping booking tests - no slot ID${NC}" >&2
    fi
    
    echo "" >&2
    echo "========================================" >&2
    echo -e "${GREEN}✅ All tests completed!${NC}" >&2
    echo "" >&2
    echo "Check TESTING_GUIDE.md for detailed testing instructions" >&2
}

main

