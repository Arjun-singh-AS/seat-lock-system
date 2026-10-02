package com.seatlock.venue.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.shared.exception.ResourceNotFoundException;
import com.seatlock.venue.api.SeatRequest;
import com.seatlock.venue.domain.SeatDocument;
import com.seatlock.venue.domain.ScreenDocument;
import com.seatlock.venue.infrastructure.persistence.ScreenRepository;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;

@Service
public class SeatService {

    private final SeatRepository seats;
    private final ScreenRepository screens;

    public SeatService(SeatRepository seats, ScreenRepository screens) {
        this.seats = seats;
        this.screens = screens;
    }

    public SeatDocument create(SeatRequest request) {
        ScreenDocument screen = requireScreen(request.screenId());
        if (seats.countByScreenId(request.screenId()) >= screen.getCapacity()) {
            throw new ResourceConflictException("Screen has reached its configured seat capacity");
        }
        ensureSeatPositionAvailable(request, null);
        return seats.save(new SeatDocument(
                request.screenId(), request.rowLabel(), request.seatNumber(), request.category()));
    }

    public List<SeatDocument> findAll(String screenId) {
        if (screenId == null) {
            return seats.findAll();
        }
        requireScreen(screenId);
        return seats.findByScreenId(screenId);
    }

    public SeatDocument findById(String id) {
        return seats.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seat", id));
    }

    public SeatDocument update(String id, SeatRequest request) {
        SeatDocument seat = findById(id);
        ScreenDocument screen = requireScreen(request.screenId());
        long configuredSeats = seats.countByScreenId(request.screenId());
        if (!request.screenId().equals(seat.getScreenId()) && configuredSeats >= screen.getCapacity()) {
            throw new ResourceConflictException("Screen has reached its configured seat capacity");
        }
        ensureSeatPositionAvailable(request, id);
        seat.setScreenId(request.screenId());
        seat.setRowLabel(request.rowLabel());
        seat.setSeatNumber(request.seatNumber());
        seat.setCategory(request.category());
        return seats.save(seat);
    }

    public void delete(String id) {
        seats.delete(findById(id));
    }

    private ScreenDocument requireScreen(String screenId) {
        return screens.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", screenId));
    }

    private void ensureSeatPositionAvailable(SeatRequest request, String currentSeatId) {
        boolean occupied = seats.existsByScreenIdAndRowLabelAndSeatNumber(
                request.screenId(), request.rowLabel(), request.seatNumber());
        if (occupied && (currentSeatId == null || !seats.findById(currentSeatId)
                .map(seat -> seat.getScreenId().equals(request.screenId())
                        && seat.getRowLabel().equals(request.rowLabel())
                        && seat.getSeatNumber() == request.seatNumber())
                .orElse(false))) {
            throw new ResourceConflictException("That seat position is already configured for the screen");
        }
    }
}
