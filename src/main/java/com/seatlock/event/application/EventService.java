package com.seatlock.event.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.seatlock.booking.infrastructure.persistence.BookingRepository;
import com.seatlock.event.api.EventRequest;
import com.seatlock.event.domain.EventDocument;
import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.shared.exception.ResourceNotFoundException;
import com.seatlock.venue.infrastructure.persistence.ScreenRepository;

@Service
public class EventService {

    private final EventRepository events;
    private final ScreenRepository screens;
    private final BookingRepository bookings;

    public EventService(EventRepository events, ScreenRepository screens, BookingRepository bookings) {
        this.events = events;
        this.screens = screens;
        this.bookings = bookings;
    }

    public EventDocument create(EventRequest request) {
        validate(request);
        return events.save(new EventDocument(
                request.screenId(), request.title(), request.description(), request.startsAt(), request.endsAt()));
    }

    public List<EventDocument> findAll(String screenId) {
        if (screenId == null) {
            return events.findAll();
        }
        requireScreen(screenId);
        return events.findByScreenId(screenId);
    }

    public EventDocument findById(String id) {
        return events.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    public EventDocument update(String id, EventRequest request) {
        EventDocument event = findById(id);
        validate(request);
        if (!event.getScreenId().equals(request.screenId()) && bookings.existsByEventId(id)) {
            throw new ResourceConflictException("Event screen cannot change after bookings have been created");
        }
        event.setScreenId(request.screenId());
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setStartsAt(request.startsAt());
        event.setEndsAt(request.endsAt());
        return events.save(event);
    }

    public void delete(String id) {
        EventDocument event = findById(id);
        if (bookings.existsByEventId(id)) {
            throw new ResourceConflictException("Event cannot be deleted after bookings have been created");
        }
        events.delete(event);
    }

    private void validate(EventRequest request) {
        requireScreen(request.screenId());
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new ResourceConflictException("Event end time must be after its start time");
        }
    }

    private void requireScreen(String screenId) {
        if (!screens.existsById(screenId)) {
            throw new ResourceNotFoundException("Screen", screenId);
        }
    }
}
