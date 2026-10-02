package com.seatlock.booking.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DuplicateKeyException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seatlock.booking.api.CreateBookingRequest;
import com.seatlock.booking.domain.BookingDocument;
import com.seatlock.booking.domain.BookingSeatDocument;
import com.seatlock.booking.domain.BookingStatus;
import com.seatlock.booking.infrastructure.persistence.BookingRepository;
import com.seatlock.booking.infrastructure.persistence.BookingSeatRepository;
import com.seatlock.event.domain.EventDocument;
import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.venue.domain.SeatCategory;
import com.seatlock.venue.domain.SeatDocument;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository bookings;

    @Mock
    private BookingSeatRepository bookingSeats;

    @Mock
    private EventRepository events;

    @Mock
    private SeatRepository seats;

    @InjectMocks
    private BookingService service;

    @Test
    void createReservesRequestedSeatsAndCreatesPendingBooking() {
        EventDocument event = event();
        SeatDocument seat = seat("seat-1", "screen-1");
        BookingSeatDocument reservation = new BookingSeatDocument("event-1", "seat-1", "booking-1");
        when(events.findById("event-1")).thenReturn(Optional.of(event));
        when(seats.findAllById(List.of("seat-1"))).thenReturn(List.of(seat));
        when(bookingSeats.save(any(BookingSeatDocument.class))).thenReturn(reservation);
        when(bookings.insert(any(BookingDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(new CreateBookingRequest("user-1", "event-1", List.of("seat-1")));

        assertEquals("event-1", created.getEventId());
        assertEquals(BookingStatus.PENDING, created.getStatus());
        assertEquals(List.of("seat-1"), created.getSeatIds());
        verify(bookingSeats).save(any(BookingSeatDocument.class));
        verify(bookings).insert(any(BookingDocument.class));
    }

    @Test
    void createReleasesPreviouslyReservedSeatsIfAnotherRequestedSeatIsTaken() {
        EventDocument event = event();
        when(events.findById("event-1")).thenReturn(Optional.of(event));
        when(seats.findAllById(List.of("seat-1", "seat-2")))
                .thenReturn(List.of(seat("seat-1", "screen-1"), seat("seat-2", "screen-1")));
        BookingSeatDocument firstReservation = new BookingSeatDocument("event-1", "seat-1", "booking-1");
        when(bookingSeats.save(any(BookingSeatDocument.class)))
                .thenReturn(firstReservation)
                .thenThrow(new DuplicateKeyException("duplicate"));

        assertThrows(ResourceConflictException.class, () -> service.create(
                new CreateBookingRequest("user-1", "event-1", List.of("seat-1", "seat-2"))));

        verify(bookingSeats).deleteAll(List.of(firstReservation));
        verify(bookings, never()).insert(any(BookingDocument.class));
    }

    @Test
    void createRejectsSeatsFromAnotherScreen() {
        when(events.findById("event-1")).thenReturn(Optional.of(event()));
        when(seats.findAllById(List.of("seat-2"))).thenReturn(List.of(seat("seat-2", "screen-2")));

        assertThrows(ResourceConflictException.class, () -> service.create(
                new CreateBookingRequest("user-1", "event-1", List.of("seat-2"))));

        verify(bookingSeats, never()).save(any(BookingSeatDocument.class));
    }

    @Test
    void cancelMarksBookingCancelledAndReleasesItsSeats() {
        var booking = new BookingDocument("user-1", "event-1", List.of("seat-1"));
        booking.setId("booking-1");
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));
        when(bookings.save(booking)).thenReturn(booking);

        service.cancel("booking-1");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookings).save(booking);
        verify(bookingSeats).deleteByBookingId("booking-1");
    }

    private EventDocument event() {
        EventDocument event = new EventDocument(
                "screen-1", "Show", null, Instant.parse("2026-10-15T19:00:00Z"),
                Instant.parse("2026-10-15T21:00:00Z"));
        event.setId("event-1");
        return event;
    }

    private SeatDocument seat(String id, String screenId) {
        SeatDocument seat = new SeatDocument(screenId, "A", 1, SeatCategory.STANDARD);
        seat.setId(id);
        return seat;
    }
}
