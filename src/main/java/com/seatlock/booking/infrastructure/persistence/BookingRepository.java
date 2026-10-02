package com.seatlock.booking.infrastructure.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.booking.domain.BookingDocument;

public interface BookingRepository extends MongoRepository<BookingDocument, String> {
    List<BookingDocument> findByUserIdOrderByCreatedAtDesc(String userId);
    boolean existsByEventId(String eventId);
}
