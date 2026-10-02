package com.seatlock.booking.api;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotBlank String userId,
        @NotBlank String eventId,
        @NotNull @NotEmpty List<@NotBlank String> seatIds) {
}
