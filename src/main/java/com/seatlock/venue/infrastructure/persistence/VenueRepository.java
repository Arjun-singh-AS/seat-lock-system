package com.seatlock.venue.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.venue.domain.VenueDocument;

public interface VenueRepository extends MongoRepository<VenueDocument, String> {
}
