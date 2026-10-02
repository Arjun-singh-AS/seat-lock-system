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

import com.seatlock.venue.application.SeatService;
import com.seatlock.venue.domain.SeatDocument;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService service;

    public SeatController(SeatService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SeatDocument create(@Valid @RequestBody SeatRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<SeatDocument> findAll(@RequestParam(required = false) String screenId) {
        return service.findAll(screenId);
    }

    @GetMapping("/{id}")
    public SeatDocument findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public SeatDocument update(@PathVariable String id, @Valid @RequestBody SeatRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
