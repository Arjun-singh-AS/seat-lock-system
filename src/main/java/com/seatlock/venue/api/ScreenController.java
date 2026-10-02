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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.seatlock.venue.application.ScreenService;
import com.seatlock.venue.domain.ScreenDocument;

@RestController
@RequestMapping("/api/screens")
public class ScreenController {

    private final ScreenService service;

    public ScreenController(ScreenService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScreenDocument create(@Valid @RequestBody ScreenRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ScreenDocument> findAll(@RequestParam(required = false) String venueId) {
        return service.findAll(venueId);
    }

    @GetMapping("/{id}")
    public ScreenDocument findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public ScreenDocument update(@PathVariable String id, @Valid @RequestBody ScreenRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
