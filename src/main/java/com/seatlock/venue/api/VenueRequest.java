package com.seatlock.venue.api;

import jakarta.validation.constraints.NotBlank;

public record VenueRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String city) {
}
