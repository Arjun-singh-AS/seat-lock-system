package com.seatlock.venue.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ScreenRequest(
        @NotBlank String venueId,
        @NotBlank String name,
        @Positive int capacity) {
}
