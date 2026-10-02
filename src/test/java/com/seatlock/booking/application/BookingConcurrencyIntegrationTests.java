package com.seatlock.booking.application;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.seatlock.booking.api.CreateBookingRequest;
import com.seatlock.booking.infrastructure.persistence.BookingRepository;
import com.seatlock.booking.infrastructure.persistence.BookingSeatRepository;
import com.seatlock.event.domain.EventDocument;
import com.seatlock.event.infrastructure.persistence.EventRepository;
import com.seatlock.shared.exception.ResourceConflictException;
import com.seatlock.venue.domain.SeatCategory;
import com.seatlock.venue.domain.SeatDocument;
import com.seatlock.venue.infrastructure.persistence.SeatRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "MONGODB_TEST_URI", matches = ".+")
class BookingConcurrencyIntegrationTests {

    @Autowired
    private BookingService bookings;

    @Autowired
    private EventRepository events;

    @Autowired
    private SeatRepository seats;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @DynamicPropertySource
    static void configureMongo(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> System.getenv("MONGODB_TEST_URI"));
    }

    @BeforeEach
    void prepareIsolatedDatabase() {
        bookingSeatRepository.deleteAll();
        bookingRepository.deleteAll();
        events.deleteAll();
        seats.deleteAll();

        EventDocument event = new EventDocument(
                "screen-concurrency-test", "Concurrency Test", null,
                Instant.parse("2026-10-15T19:00:00Z"), Instant.parse("2026-10-15T21:00:00Z"));
        event.setId("event-concurrency-test");
        events.save(event);

        SeatDocument seat = new SeatDocument(
                "screen-concurrency-test", "A", 1, SeatCategory.STANDARD);
        seat.setId("seat-concurrency-test");
        seats.save(seat);
    }

    @Test
    void onlyOneOfOneHundredConcurrentRequestsCanReserveTheSameSeat() throws Exception {
        int requestCount = 100;
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        try {
            List<Future<Boolean>> results = java.util.stream.IntStream.range(0, requestCount)
                    .mapToObj(index -> executor.submit(() -> {
                        ready.countDown();
                        if (!start.await(10, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Timed out waiting to start concurrent booking requests");
                        }
                        try {
                            bookings.create(new CreateBookingRequest(
                                    "user-" + index, "event-concurrency-test", List.of("seat-concurrency-test")));
                            return true;
                        } catch (ResourceConflictException exception) {
                            return false;
                        }
                    }))
                    .toList();

            assertTrue(ready.await(10, TimeUnit.SECONDS), "All booking requests should be ready");
            start.countDown();
            long successfulRequests = 0;
            for (var result : results) {
                if (result.get(30, TimeUnit.SECONDS)) {
                    successfulRequests++;
                }
            }

            assertEquals(1, successfulRequests);
            assertEquals(1, bookingRepository.count());
            assertEquals(1, bookingSeatRepository.countByEventId("event-concurrency-test"));
        } finally {
            executor.shutdownNow();
        }
    }
}
