package com.seatlock.booking.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("bookings")
public class BookingDocument {

    @Id
    private String id;
    private String userId;
    private String eventId;
    private List<String> seatIds;
    private BookingStatus status;
    private Instant createdAt;

    public BookingDocument() {
    }

    public BookingDocument(String userId, String eventId, List<String> seatIds) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.eventId = eventId;
        this.seatIds = List.copyOf(seatIds);
        this.status = BookingStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public List<String> getSeatIds() { return seatIds; }
    public void setSeatIds(List<String> seatIds) { this.seatIds = List.copyOf(seatIds); }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
