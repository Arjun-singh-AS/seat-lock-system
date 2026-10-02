package com.seatlock.event.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("events")
public class EventDocument {

    @Id
    private String id;
    private String screenId;
    private String title;
    private String description;
    private Instant startsAt;
    private Instant endsAt;

    public EventDocument() {
    }

    public EventDocument(String screenId, String title, String description, Instant startsAt, Instant endsAt) {
        this.screenId = screenId;
        this.title = title;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getScreenId() { return screenId; }
    public void setScreenId(String screenId) { this.screenId = screenId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }
}
