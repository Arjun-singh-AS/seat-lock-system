package com.seatlock.booking.application;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.seatlock.booking.domain.BookingDocument;
import com.seatlock.booking.domain.BookingSeatDocument;
import com.seatlock.booking.domain.BookingStatus;
import com.seatlock.booking.infrastructure.persistence.BookingRepository;
import com.seatlock.booking.infrastructure.persistence.BookingSeatRepository;

@Component
public class BookingReservationMigration implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BookingReservationMigration.class);

    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final Duration holdDuration;

    public BookingReservationMigration(
            BookingRepository bookings,
            BookingSeatRepository bookingSeats,
            @Value("${seatlock.booking.hold-duration:PT10M}") Duration holdDuration) {
        this.bookings = bookings;
        this.bookingSeats = bookingSeats;
        this.holdDuration = holdDuration;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<BookingSeatDocument> legacyReservations = bookingSeats.findByExpiresAtIsNull();
        int migrated = 0;
        int released = 0;
        for (BookingSeatDocument reservation : legacyReservations) {
            BookingDocument booking = bookings.findById(reservation.getBookingId()).orElse(null);
            if (booking == null || booking.getStatus() != BookingStatus.PENDING || booking.getCreatedAt() == null) {
                bookingSeats.delete(reservation);
                released++;
                continue;
            }

            Instant expiresAt = booking.getExpiresAt() == null
                    ? booking.getCreatedAt().plus(holdDuration)
                    : booking.getExpiresAt();
            booking.setExpiresAt(expiresAt);
            bookings.save(booking);
            reservation.setExpiresAt(expiresAt);
            bookingSeats.save(reservation);
            migrated++;
        }

        if (migrated > 0 || released > 0) {
            logger.info("Updated legacy booking reservations: {} migrated, {} released", migrated, released);
        }
    }
}
