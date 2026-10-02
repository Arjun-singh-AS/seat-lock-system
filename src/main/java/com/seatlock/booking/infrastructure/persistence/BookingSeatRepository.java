package com.seatlock.booking.infrastructure.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.booking.domain.BookingSeatDocument;

public interface BookingSeatRepository extends MongoRepository<BookingSeatDocument, String> {
    List<BookingSeatDocument> findByEventId(String eventId);
    void deleteByBookingId(String bookingId);
    boolean existsBySeatId(String seatId);
}
