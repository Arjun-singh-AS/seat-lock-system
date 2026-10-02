package com.seatlock.venue.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.shared.exception.ResourceNotFoundException;
import com.seatlock.venue.api.ScreenRequest;
import com.seatlock.venue.domain.ScreenDocument;
import com.seatlock.venue.infrastructure.persistence.ScreenRepository;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;
import com.seatlock.venue.infrastructure.persistence.VenueRepository;

@Service
public class ScreenService {

    private final ScreenRepository screens;
    private final VenueRepository venues;
    private final SeatRepository seats;
    private final EventRepository events;

    public ScreenService(ScreenRepository screens, VenueRepository venues,
                         SeatRepository seats, EventRepository events) {
        this.screens = screens;
        this.venues = venues;
        this.seats = seats;
        this.events = events;
    }

    public ScreenDocument create(ScreenRequest request) {
        requireVenue(request.venueId());
        return screens.save(new ScreenDocument(request.venueId(), request.name(), request.capacity()));
    }

    public List<ScreenDocument> findAll(String venueId) {
        if (venueId == null) {
            return screens.findAll();
        }
        requireVenue(venueId);
        return screens.findByVenueId(venueId);
    }

    public ScreenDocument findById(String id) {
        return screens.findById(id).orElseThrow(() -> new ResourceNotFoundException("Screen", id));
    }

    public ScreenDocument update(String id, ScreenRequest request) {
        ScreenDocument screen = findById(id);
        requireVenue(request.venueId());
        if (seats.countByScreenId(id) > request.capacity()) {
            throw new ResourceConflictException("Screen capacity cannot be lower than its configured seat count");
        }
        screen.setVenueId(request.venueId());
        screen.setName(request.name());
        screen.setCapacity(request.capacity());
        return screens.save(screen);
    }

    public void delete(String id) {
        ScreenDocument screen = findById(id);
        if (seats.existsByScreenId(id) || events.existsByScreenId(id)) {
            throw new ResourceConflictException("Screen cannot be deleted while it has seats or events");
        }
        screens.delete(screen);
    }

    private void requireVenue(String venueId) {
        if (!venues.existsById(venueId)) {
            throw new ResourceNotFoundException("Venue", venueId);
        }
    }
}
