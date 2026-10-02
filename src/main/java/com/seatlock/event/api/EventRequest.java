package com.seatlock.event.api;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @NotBlank String screenId,
        @NotBlank String title,
        String description,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt) {
}
