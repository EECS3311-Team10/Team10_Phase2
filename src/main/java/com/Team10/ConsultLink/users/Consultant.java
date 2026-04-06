package com.Team10.ConsultLink.users;

import java.util.ArrayList;
import java.util.List;

import com.Team10.ConsultLink.model.Booking;
import com.Team10.ConsultLink.model.Service;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
public class Consultant extends User {

    private boolean isApproved;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @ManyToMany
    private List<Service> services = new ArrayList<>();

    public Consultant() { 
        super(); 
    }

    public Consultant(String userId, String name, String email, String phone, String password) {
        super(userId, name, email, phone, "CONSULTANT", password);
        this.isApproved = true;
    }

    // -------- REQUIRED FOR APPROVE PAGE --------
    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        this.isApproved = approved;
    }

    // -------- OPTIONAL BUT GOOD --------
    public List<Booking> getBookings() {
        return bookings;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
