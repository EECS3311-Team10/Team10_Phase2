package com.Team10.ConsultLink.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Team10.ConsultLink.model.Booking;
import com.Team10.ConsultLink.repository.BookingRepository;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/hello")
    public String sayHello() {
        return "ConsultLink Booking API is running!";
    }

    // CREATE BOOKING
    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingRepository.save(booking);
    }

    // GET ALL BOOKINGS
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // GET BOOKINGS BY CLIENT
    @GetMapping("/client/{clientUserId}")
    public List<Booking> getBookingsByClient(@PathVariable String clientUserId) {
        return bookingRepository.findByClientUserId(clientUserId);
    }

    // CONSULTANT ACCEPT BOOKING
    @PutMapping("/{id}/confirm")
    public Booking confirmBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking != null) {
            booking.confirm();   // uses your state pattern
            return bookingRepository.save(booking);
        }

        return null;
    }

    // CONSULTANT REJECT BOOKING
    @PutMapping("/{id}/reject")
    public Booking rejectBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking != null) {
            booking.reject();   // uses your state pattern
            return bookingRepository.save(booking);
        }

        return null;
    }

    // CONSULTANT COMPLETE BOOKING
    @PutMapping("/{id}/complete")
    public Booking completeBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking != null) {
            booking.complete();
            return bookingRepository.save(booking);
        }

        return null;
    }
}