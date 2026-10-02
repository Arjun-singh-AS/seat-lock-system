package com.seatlock.booking.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.Clock;

import org.springframework.stereotype.Service;

import com.seatlock.booking.api.SeatAvailabilityResponse;
import com.seatlock.booking.infrastructure.persistence.BookingSeatRepository;
import com.seatlock.event.domain.EventDocument;
import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.shared.exception.ResourceNotFoundException;
import com.seatlock.venue.domain.SeatDocument;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;

@Service
public class SeatAvailabilityService {

    private final EventRepository events;
    private final SeatRepository seats;
    private final BookingSeatRepository bookingSeats;
    private final Clock clock;

    public SeatAvailabilityService(EventRepository events, SeatRepository seats,
                                   BookingSeatRepository bookingSeats, Clock clock) {
        this.events = events;
        this.seats = seats;
        this.bookingSeats = bookingSeats;
        this.clock = clock;
    }

    public List<SeatAvailabilityResponse> findByEvent(String eventId) {
        EventDocument event = events.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
        Set<String> reservedSeatIds = new HashSet<>();
        bookingSeats.findByEventIdAndExpiresAtAfter(eventId, clock.instant())
                .forEach(reservation -> reservedSeatIds.add(reservation.getSeatId()));

        return seats.findByScreenId(event.getScreenId()).stream()
                .map(seat -> toResponse(seat, reservedSeatIds))
                .toList();
    }

    private SeatAvailabilityResponse toResponse(SeatDocument seat, Set<String> reservedSeatIds) {
        SeatAvailabilityResponse.Status status = reservedSeatIds.contains(seat.getId())
                ? SeatAvailabilityResponse.Status.RESERVED
                : SeatAvailabilityResponse.Status.AVAILABLE;
        return new SeatAvailabilityResponse(
                seat.getId(), seat.getRowLabel(), seat.getSeatNumber(), seat.getCategory(), status);
    }
}
