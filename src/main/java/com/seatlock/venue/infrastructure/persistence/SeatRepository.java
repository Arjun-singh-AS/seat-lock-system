package com.seatlock.venue.infrastructure.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.venue.domain.SeatDocument;

public interface SeatRepository extends MongoRepository<SeatDocument, String> {
    List<SeatDocument> findByScreenId(String screenId);
    boolean existsByScreenId(String screenId);
    boolean existsByScreenIdAndRowLabelAndSeatNumber(String screenId, String rowLabel, int seatNumber);
    long countByScreenId(String screenId);
}
