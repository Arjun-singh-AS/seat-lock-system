package com.seatlock.booking.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.seatlock.booking.api.CreateBookingRequest;
import com.seatlock.booking.domain.BookingDocument;
import com.seatlock.booking.domain.BookingSeatDocument;
import com.seatlock.booking.domain.BookingStatus;
import com.seatlock.booking.infrastructure.persistence.BookingRepository;
import com.seatlock.booking.infrastructure.persistence.BookingSeatRepository;
import com.seatlock.event.domain.EventDocument;
import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.shared.exception.ResourceNotFoundException;
import com.seatlock.venue.domain.SeatDocument;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final EventRepository events;
    private final SeatRepository seats;

    public BookingService(BookingRepository bookings, BookingSeatRepository bookingSeats,
                          EventRepository events, SeatRepository seats) {
        this.bookings = bookings;
        this.bookingSeats = bookingSeats;
        this.events = events;
        this.seats = seats;
    }

    public BookingDocument create(CreateBookingRequest request) {
        EventDocument event = events.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.eventId()));
        List<String> seatIds = request.seatIds();
        if (new HashSet<>(seatIds).size() != seatIds.size()) {
            throw new ResourceConflictException("A seat may only be included once in a booking");
        }

        List<SeatDocument> requestedSeats = seats.findAllById(seatIds);
        if (requestedSeats.size() != seatIds.size()
                || requestedSeats.stream().anyMatch(seat -> !event.getScreenId().equals(seat.getScreenId()))) {
            throw new ResourceConflictException("Every requested seat must exist on the event's screen");
        }

        BookingDocument booking = new BookingDocument(request.userId(), event.getId(), seatIds);
        List<BookingSeatDocument> acquiredSeats = new ArrayList<>();
        try {
            for (String seatId : seatIds) {
                acquiredSeats.add(bookingSeats.save(new BookingSeatDocument(event.getId(), seatId, booking.getId())));
            }
            return bookings.insert(booking);
        } catch (DuplicateKeyException exception) {
            ResourceConflictException conflict =
                    new ResourceConflictException("One or more seats are already reserved for this event");
            releaseAcquiredSeats(acquiredSeats, conflict);
            throw conflict;
        } catch (RuntimeException exception) {
            releaseAcquiredSeats(acquiredSeats, exception);
            throw exception;
        }
    }

    public BookingDocument findById(String id) {
        return bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    public List<BookingDocument> findByUser(String userId) {
        return bookings.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void cancel(String id) {
        BookingDocument booking = findById(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookings.save(booking);
        bookingSeats.deleteByBookingId(id);
    }

    private void releaseAcquiredSeats(List<BookingSeatDocument> acquiredSeats, RuntimeException originalFailure) {
        if (!acquiredSeats.isEmpty()) {
            try {
                bookingSeats.deleteAll(acquiredSeats);
            } catch (RuntimeException cleanupFailure) {
                originalFailure.addSuppressed(cleanupFailure);
            }
        }
    }
}
