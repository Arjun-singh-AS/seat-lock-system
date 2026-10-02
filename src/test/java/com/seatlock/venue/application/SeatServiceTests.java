package com.seatlock.venue.application;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.venue.api.SeatRequest;
import com.seatlock.venue.domain.ScreenDocument;
import com.seatlock.venue.domain.SeatCategory;
import com.seatlock.booking.infrastructure.persistence.BookingSeatRepository;
import com.seatlock.venue.infrastructure.persistence.ScreenRepository;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatServiceTests {

    @Mock
    private SeatRepository seats;

    @Mock
    private ScreenRepository screens;

    @Mock
    private BookingSeatRepository bookingSeats;

    @InjectMocks
    private SeatService service;

    @Test
    void createRejectsSeatPositionAlreadyConfiguredOnScreen() {
        when(screens.findById("screen-1"))
                .thenReturn(Optional.of(new ScreenDocument("venue-1", "Screen 1", 10)));
        when(seats.countByScreenId("screen-1")).thenReturn(1L);
        when(seats.existsByScreenIdAndRowLabelAndSeatNumber("screen-1", "A", 1)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> service.create(
                new SeatRequest("screen-1", "A", 1, SeatCategory.STANDARD)));

        verify(seats, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createRejectsSeatsBeyondScreenCapacity() {
        when(screens.findById("screen-1"))
                .thenReturn(Optional.of(new ScreenDocument("venue-1", "Screen 1", 1)));
        when(seats.countByScreenId("screen-1")).thenReturn(1L);

        assertThrows(ResourceConflictException.class, () -> service.create(
                new SeatRequest("screen-1", "A", 2, SeatCategory.STANDARD)));

        verify(seats, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
