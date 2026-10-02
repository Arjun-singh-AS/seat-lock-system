package com.seatlock.booking.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seatlock.booking.application.SeatAvailabilityService;

@RestController
@RequestMapping("/api/events/{eventId}/seats")
public class SeatAvailabilityController {

    private final SeatAvailabilityService service;

    public SeatAvailabilityController(SeatAvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public List<SeatAvailabilityResponse> findByEvent(@PathVariable String eventId) {
        return service.findByEvent(eventId);
    }
}
