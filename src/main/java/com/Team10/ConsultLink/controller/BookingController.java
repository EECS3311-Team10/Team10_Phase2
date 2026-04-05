package com.Team10.ConsultLink.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        if (booking.getStatus() == null || booking.getStatus().isEmpty()) {
            booking.setStatus("REQUESTED");
        }
        return bookingRepository.save(booking);
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @GetMapping("/client/{clientUserId}")
    public List<Booking> getBookingsByClient(@PathVariable String clientUserId) {
        return bookingRepository.findByClientUserId(clientUserId);
    }

    @PutMapping("/{id}/confirm")
    public Booking confirmBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return null;

        booking.setStatus("PENDING_PAYMENT");
        return bookingRepository.save(booking);
    }

    @PutMapping("/{id}/reject")
    public Booking rejectBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return null;

        booking.setStatus("REJECTED");
        return bookingRepository.save(booking);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return null;

        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }

    @PutMapping("/{id}/pay")
    public Booking markPaid(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return null;

        booking.setStatus("PAID");
        return bookingRepository.save(booking);
    }

    @PutMapping("/{id}/complete")
    public Booking completeBooking(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return null;

        booking.setStatus("COMPLETED");
        return bookingRepository.save(booking);
    }
}