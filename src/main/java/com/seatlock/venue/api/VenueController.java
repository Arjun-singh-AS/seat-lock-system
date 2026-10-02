package com.seatlock.venue.api;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.seatlock.venue.application.VenueService;
import com.seatlock.venue.domain.VenueDocument;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService service;

    public VenueController(VenueService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VenueDocument create(@Valid @RequestBody VenueRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<VenueDocument> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public VenueDocument findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public VenueDocument update(@PathVariable String id, @Valid @RequestBody VenueRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
