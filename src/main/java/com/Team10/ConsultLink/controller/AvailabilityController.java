package com.Team10.ConsultLink.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/consultant/{consultantUserId}")
    public List<Availability> getAvailabilityByConsultant(@PathVariable String consultantUserId) {
        return availabilityRepository.findByConsultantUserId(consultantUserId);
    }

    @DeleteMapping("/{id}")
    public void deleteAvailability(@PathVariable Long id) {
        availabilityRepository.deleteById(id);
    }
}