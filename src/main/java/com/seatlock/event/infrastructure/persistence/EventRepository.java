package com.seatlock.event.infrastructure.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.seatlock.event.domain.EventDocument;

public interface EventRepository extends MongoRepository<EventDocument, String> {
    List<EventDocument> findByScreenId(String screenId);
    boolean existsByScreenId(String screenId);
}
