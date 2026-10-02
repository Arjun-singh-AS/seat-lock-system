package com.seatlock.venue.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.shared.exception.ResourceNotFoundException;
import com.seatlock.venue.api.VenueRequest;
import com.seatlock.venue.domain.VenueDocument;
import com.seatlock.venue.infrastructure.persistence.ScreenRepository;
import com.seatlock.venue.infrastructure.persistence.VenueRepository;

@Service
public class VenueService {

    private final VenueRepository venues;
    private final ScreenRepository screens;

    public VenueService(VenueRepository venues, ScreenRepository screens) {
        this.venues = venues;
        this.screens = screens;
    }

    public VenueDocument create(VenueRequest request) {
        return venues.save(new VenueDocument(request.name(), request.address(), request.city()));
    }

    public List<VenueDocument> findAll() {
        return venues.findAll();
    }

    public VenueDocument findById(String id) {
        return venues.findById(id).orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }

    public VenueDocument update(String id, VenueRequest request) {
        VenueDocument venue = findById(id);
        venue.setName(request.name());
        venue.setAddress(request.address());
        venue.setCity(request.city());
        return venues.save(venue);
    }

    public void delete(String id) {
        VenueDocument venue = findById(id);
        if (screens.existsByVenueId(id)) {
            throw new ResourceConflictException("Venue cannot be deleted while it has screens");
        }
        venues.delete(venue);
    }
}
