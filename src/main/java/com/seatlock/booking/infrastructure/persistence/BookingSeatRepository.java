package com.seatlock.booking.infrastructure.persistence;

import java.util.List;
import java.time.Instant;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.booking.domain.BookingSeatDocument;

public interface BookingSeatRepository extends MongoRepository<BookingSeatDocument, String> {
    List<BookingSeatDocument> findByEventIdAndExpiresAtAfter(String eventId, Instant now);
    List<BookingSeatDocument> findByExpiresAtIsNull();
    void deleteByBookingId(String bookingId);
    boolean existsBySeatId(String seatId);
    long countByEventId(String eventId);
    void deleteByEventIdAndSeatIdAndExpiresAtLessThanEqual(String eventId, String seatId, Instant now);
}
