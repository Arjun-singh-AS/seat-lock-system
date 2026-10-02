package com.seatlock.venue.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.seatlock.venue.domain.SeatCategory;

public record SeatRequest(
        @NotBlank String screenId,
        @NotBlank String rowLabel,
        @Positive int seatNumber,
        @NotNull SeatCategory category) {
}
