package com.seatlock.venue.infrastructure.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.venue.domain.ScreenDocument;

public interface ScreenRepository extends MongoRepository<ScreenDocument, String> {
    List<ScreenDocument> findByVenueId(String venueId);
    boolean existsByVenueId(String venueId);
}
