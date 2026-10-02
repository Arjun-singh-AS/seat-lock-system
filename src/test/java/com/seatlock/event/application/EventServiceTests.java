package com.seatlock.event.application;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.seatlock.event.api.EventRequest;
import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.venue.infrastructure.persistence.ScreenRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTests {

    @Mock
    private EventRepository events;

    @Mock
    private ScreenRepository screens;

    @InjectMocks
    private EventService service;

    @Test
    void createRejectsEventWhoseEndIsNotAfterItsStart() {
        when(screens.existsById("screen-1")).thenReturn(true);
        Instant startsAt = Instant.parse("2026-10-15T19:00:00Z");
        EventRequest request = new EventRequest(
                "screen-1", "Evening Show", null, startsAt, startsAt);

        assertThrows(ResourceConflictException.class, () -> service.create(request));

        verify(events, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
