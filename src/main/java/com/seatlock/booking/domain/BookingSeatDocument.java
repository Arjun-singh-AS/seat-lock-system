package com.seatlock.booking.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("booking_seats")
@CompoundIndex(name = "event_seat_unique", def = "{'eventId': 1, 'seatId': 1}", unique = true)
public class BookingSeatDocument {

    @Id
    private String id;
    private String eventId;
    private String seatId;
    private String bookingId;

    public BookingSeatDocument() {
    }

    public BookingSeatDocument(String eventId, String seatId, String bookingId) {
        this.eventId = eventId;
        this.seatId = seatId;
        this.bookingId = bookingId;
    }

    public String getId() { return id; }
    public String getEventId() { return eventId; }
    public String getSeatId() { return seatId; }
    public String getBookingId() { return bookingId; }
}
