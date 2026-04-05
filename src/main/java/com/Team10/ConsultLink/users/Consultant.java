package com.Team10.ConsultLink.users;

import com.Team10.ConsultLink.model.*; // Import Booking and Service
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List; // THIS FIXES THE LIST ERROR

@Entity
public class Consultant extends User {

    private boolean isApproved;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    @ManyToMany
    private List<Service> services = new ArrayList<>();

    public Consultant() { super(); }
    
 
    public Consultant(String userId, String name, String email, String phone, String password) {
    
    super(userId, name, email, phone, "CONSULTANT", password); 
    this.isApproved = true; // Every dummy consultant starts as "Verified"
}
    
    // ... rest of your methods (acceptBooking, rejectBooking, etc.)
}
    /* 
  public void manageAvailability(){

  }
     
    public List<Booking> getPendingBookingRequests() {
        return null;
    }

    // =========================================
    // Booking management
    // =========================================
    public void acceptBooking(Booking booking) {

        if (booking == null) {
            return;
        }

        booking.confirm();  // State pattern transition
        bookings.add(booking);

        System.out.println("Booking accepted: " + booking.getBookingID());
    }

    public void rejectBooking(Booking booking) {

        if (booking == null) {
            return;
        }

        booking.reject();

        System.out.println("Booking rejected: " + booking.getBookingID());
    }

    public void completeBooking(Booking booking) {

        if (booking == null) {
            return;
        }

        booking.complete();

        System.out.println("Booking completed: " + booking.getBookingID());
    }

    // =========================================
    // Registration
    // =========================================
    public void register() {
        this.isApproved = false;
        System.out.println("Consultant registration submitted.");
    }

    // called by Admin
    public void approve() {
        this.isApproved = true;
        System.out.println("Consultant approved.");
    }

    // called by Admin
    public void reject() {
        this.isApproved = false;
        System.out.println("Consultant rejected.");
    }

    public boolean checkApproval() {
        return isApproved;
    }

    public List<Service> getServices() {
        return this.services;
    }

}
*/