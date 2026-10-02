package com.seatlock.booking.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("booking_seats")
@CompoundIndex(name = "event_seat_unique", def = "{'eventId': 1, 'seatId': 1}", unique = true)
public class BookingSeatDocument {

    @Id
    private String id;
    private String eventId;
    private String seatId;
    private String bookingId;
    @Indexed(name = "booking_seat_expiration_ttl", expireAfter = "0s")
    private Instant expiresAt;

    public BookingSeatDocument() {
    }

    public BookingSeatDocument(String eventId, String seatId, String bookingId, Instant expiresAt) {
        this.eventId = eventId;
        this.seatId = seatId;
        this.bookingId = bookingId;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getSeatId() { return seatId; }
    public String getBookingId() { return bookingId; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
