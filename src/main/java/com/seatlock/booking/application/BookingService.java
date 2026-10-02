package com.seatlock.booking.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

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
    private final SeatRequestLock seatRequestLock;
    private final Clock clock;
    private final Duration holdDuration;

    public BookingService(BookingRepository bookings, BookingSeatRepository bookingSeats,
                          EventRepository events, SeatRepository seats, SeatRequestLock seatRequestLock, Clock clock,
                          @Value("${seatlock.booking.hold-duration:PT10M}") Duration holdDuration) {
        if (holdDuration.isZero() || holdDuration.isNegative()) {
            throw new IllegalArgumentException("Booking hold duration must be greater than zero");
        }
        this.bookings = bookings;
        this.bookingSeats = bookingSeats;
        this.events = events;
        this.seats = seats;
        this.seatRequestLock = seatRequestLock;
        this.clock = clock;
        this.holdDuration = holdDuration;
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

        return seatRequestLock.withLocks(event.getId(), seatIds, () -> reserveSeats(request, event, seatIds));
    }

    private BookingDocument reserveSeats(CreateBookingRequest request, EventDocument event, List<String> seatIds) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(holdDuration);
        BookingDocument booking = new BookingDocument(
                request.userId(), event.getId(), seatIds, now, expiresAt);
        List<BookingSeatDocument> acquiredSeats = new ArrayList<>();
        try {
            for (String seatId : seatIds) {
                bookingSeats.deleteByEventIdAndSeatIdAndExpiresAtLessThanEqual(event.getId(), seatId, now);
                acquiredSeats.add(bookingSeats.save(
                        new BookingSeatDocument(event.getId(), seatId, booking.getId(), expiresAt)));
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
        BookingDocument booking = bookings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        return expireIfNeeded(booking);
    }

    public List<BookingDocument> findByUser(String userId) {
        return bookings.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::expireIfNeeded)
                .toList();
    }

    public void cancel(String id) {
        BookingDocument booking = findById(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            return;
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookings.save(booking);
        bookingSeats.deleteByBookingId(id);
    }

    @Scheduled(fixedDelayString = "${seatlock.booking.expiration-scan-interval-ms:30000}")
    public void expirePendingBookings() {
        List<BookingDocument> expired = bookings.findByStatusAndExpiresAtLessThanEqual(
                BookingStatus.PENDING, clock.instant());
        expired.forEach(this::expireIfNeeded);
    }

    private BookingDocument expireIfNeeded(BookingDocument booking) {
        if (booking.getStatus() == BookingStatus.PENDING && !booking.getExpiresAt().isAfter(clock.instant())) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookings.save(booking);
            bookingSeats.deleteByBookingId(booking.getId());
        }
        return booking;
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
