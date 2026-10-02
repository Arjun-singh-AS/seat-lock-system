package com.seatlock.booking.api;

import com.seatlock.venue.domain.SeatCategory;

public record SeatAvailabilityResponse(
        String seatId,
        String rowLabel,
        int seatNumber,
        SeatCategory category,
        Status status) {

    public enum Status {
        AVAILABLE,
        RESERVED
    }
}
