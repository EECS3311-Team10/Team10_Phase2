package com.Team10.ConsultLink.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.Team10.ConsultLink.model.Availability;
import com.Team10.ConsultLink.repository.AvailabilityRepository;

@RestController
@RequestMapping("/api/availability")
@CrossOrigin(origins = "*")
public class AvailabilityController {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @PostMapping
    public Availability createAvailability(@RequestBody Availability availability) {
        return availabilityRepository.save(availability);
    }

    @GetMapping
    public List<Availability> getAllAvailability() {
        return availabilityRepository.findAll();
    }

    @GetMapping("/consultant/{consultantUserId}")
    public List<Availability> getAvailabilityByConsultant(@PathVariable String consultantUserId) {
        return availabilityRepository.findByConsultantUserId(consultantUserId);
    }

    @DeleteMapping("/{id}")
    public void deleteAvailability(@PathVariable Long id) {
        availabilityRepository.deleteById(id);
    }
}