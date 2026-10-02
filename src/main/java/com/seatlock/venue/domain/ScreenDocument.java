package com.seatlock.venue.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("screens")
public class ScreenDocument {

    @Id
    private String id;
    private String venueId;
    private String name;
    private int capacity;

    public ScreenDocument() {
    }

    public ScreenDocument(String venueId, String name, int capacity) {
        this.venueId = venueId;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVenueId() { return venueId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
