package com.seatlock.booking.application;

import java.time.Instant;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seatlock.booking.domain.BookingSeatDocument;
import com.seatlock.booking.infrastructure.persistence.BookingSeatRepository;
import com.seatlock.event.domain.EventDocument;
import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.venue.domain.SeatCategory;
import com.seatlock.venue.domain.SeatDocument;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatAvailabilityServiceTests {

    @Mock
    private EventRepository events;

    @Mock
    private SeatRepository seats;

    @Mock
    private BookingSeatRepository bookingSeats;

    private SeatAvailabilityService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new SeatAvailabilityService(events, seats, bookingSeats,
                Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), java.time.ZoneOffset.UTC));
    }

    @Test
    void returnsAvailabilityForEventScreenSeats() {
        EventDocument event = new EventDocument(
                "screen-1", "Show", null, Instant.parse("2026-10-15T19:00:00Z"),
                Instant.parse("2026-10-15T21:00:00Z"));
        event.setId("event-1");
        when(events.findById("event-1")).thenReturn(Optional.of(event));
        when(bookingSeats.findByEventIdAndExpiresAtAfter("event-1", Instant.parse("2026-10-01T00:00:00Z")))
                .thenReturn(List.of(new BookingSeatDocument(
                        "event-1", "seat-1", "booking-1", Instant.parse("2026-10-01T00:10:00Z"))));
        when(seats.findByScreenId("screen-1")).thenReturn(List.of(
                seat("seat-1", 1), seat("seat-2", 2)));

        var availability = service.findByEvent("event-1");

        assertEquals(2, availability.size());
        assertEquals("RESERVED", availability.get(0).status().name());
        assertEquals("AVAILABLE", availability.get(1).status().name());
    }

    private SeatDocument seat(String id, int number) {
        SeatDocument seat = new SeatDocument("screen-1", "A", number, SeatCategory.STANDARD);
        seat.setId(id);
        return seat;
    }
}
