package com.seatlock.booking.infrastructure.persistence;

import java.util.List;
import java.time.Instant;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.booking.domain.BookingDocument;
import com.seatlock.booking.domain.BookingStatus;

public interface BookingRepository extends MongoRepository<BookingDocument, String> {
    List<BookingDocument> findByUserIdOrderByCreatedAtDesc(String userId);
    boolean existsByEventId(String eventId);
    List<BookingDocument> findByStatusAndExpiresAtLessThanEqual(BookingStatus status, Instant now);
}
