package com.seatlock.venue.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("venues")
public class VenueDocument {

    @Id
    private String id;
    private String name;
    private String address;
    private String city;

    public VenueDocument() {
    }

    public VenueDocument(String name, String address, String city) {
        this.name = name;
        this.address = address;
        this.city = city;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
