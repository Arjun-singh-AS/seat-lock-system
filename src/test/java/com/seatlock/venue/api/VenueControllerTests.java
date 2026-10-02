package com.seatlock.venue.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.seatlock.venue.application.VenueService;
import com.seatlock.venue.domain.VenueDocument;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
class VenueControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService service;

    @Test
    void createReturnsCreatedVenue() throws Exception {
        VenueDocument venue = new VenueDocument("Downtown Cinema", "10 Main Street", "Seattle");
        venue.setId("venue-1");
        when(service.create(any(VenueRequest.class))).thenReturn(venue);

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Downtown Cinema",
                                  "address": "10 Main Street",
                                  "city": "Seattle"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("venue-1"))
                .andExpect(jsonPath("$.name").value("Downtown Cinema"));
    }

    @Test
    void createRejectsBlankRequiredFields() throws Exception {
        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "address": "",
                                  "city": "Seattle"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.address").exists());

        verify(service, never()).create(any(VenueRequest.class));
    }
}
