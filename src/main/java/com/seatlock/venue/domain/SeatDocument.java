package com.seatlock.venue.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("seats")
@CompoundIndex(name = "screen_row_seat_unique", def = "{'screenId': 1, 'rowLabel': 1, 'seatNumber': 1}", unique = true)
public class SeatDocument {

    @Id
    private String id;
    private String screenId;
    private String rowLabel;
    private int seatNumber;
    private SeatCategory category;

    public SeatDocument() {
    }

    public SeatDocument(String screenId, String rowLabel, int seatNumber, SeatCategory category) {
        this.screenId = screenId;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getScreenId() { return screenId; }
    public void setScreenId(String screenId) { this.screenId = screenId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public SeatCategory getCategory() { return category; }
    public void setCategory(SeatCategory category) { this.category = category; }
}
