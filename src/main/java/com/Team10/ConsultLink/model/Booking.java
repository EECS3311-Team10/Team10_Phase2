package com.Team10.ConsultLink.model;

import java.time.LocalDateTime;

import com.Team10.ConsultLink.service.BookingState;
import com.Team10.ConsultLink.service.RequestedState;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingID;

    // store client who created booking
    private String clientUserId;

    // store selected service
    private Long serviceId;
    private String serviceName;

    // persistable status
    private String status;

    private LocalDateTime requestedTime;
    private LocalDateTime scheduledTime;

    private double price;

    @Transient
    private BookingState state;

    public Booking() {}

    public Booking(String bookingID,
                   String clientUserId,
                   Long serviceId,
                   String serviceName,
                   LocalDateTime requestedTime,
                   LocalDateTime scheduledTime,
                   double price) {

        this.bookingID = bookingID;
        this.clientUserId = clientUserId;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.requestedTime = requestedTime;
        this.scheduledTime = scheduledTime;
        this.price = price;

        this.status = "REQUESTED";
        this.state = new RequestedState();
    }

    // ----- state logic -----
    public void setState(BookingState state) {
        this.state = state;
    }

    public BookingState getState() {
        return state;
    }

    public void request() { state.handleRequest(this); }
    public void confirm() { state.handleConfirm(this); }
    public void reject() { state.handleReject(this); }
    public void complete() { state.handleComplete(this); }
    public void cancel() { state.handleCancel(this); }
    public void processPayment() { state.handlePayment(this); }

    // ----- getters -----
    public Long getId() { return id; }
    public String getBookingID() { return bookingID; }
    public String getClientUserId() { return clientUserId; }
    public Long getServiceId() { return serviceId; }
    public String getServiceName() { return serviceName; }
    public String getStatus() { return status; }
    public LocalDateTime getRequestedTime() { return requestedTime; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public double getPrice() { return price; }

    // ----- setters -----
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingID='" + bookingID + '\'' +
                ", clientUserId='" + clientUserId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", status='" + status + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", price=" + price +
                '}';
    }
}

// package com.Team10.ConsultLink.model;

// import com.Team10.ConsultLink.service.*;
// import jakarta.persistence.*; // Required for JPA annotations
// import java.time.LocalDateTime;

// @Entity
// @Table(name = "bookings")
// public class Booking {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id; // Primary key for the database

//     private String bookingID; // Your business-logic ID

//     @Transient // We mark the State object as Transient because JPA cannot 
//                // easily store a State Pattern object in a single column.
//     private BookingState state;

//     private LocalDateTime requestedTime;
//     private LocalDateTime scheduledTime;
//     private double price;

//     // 1. Required Default Constructor for JPA
//     public Booking() {}

//     // 2. Your existing constructor
//     public Booking(String bookingID, LocalDateTime requestedTime, LocalDateTime scheduledTime, double price) {
//         this.bookingID = bookingID;
//         this.requestedTime = requestedTime;
//         this.scheduledTime = scheduledTime;
//         this.price = price;
//         this.state = new RequestedState();
//     }

//     // --- State Pattern Logic ---
//     public void setState(BookingState state) {
//         this.state = state;
//     }

//     public BookingState getState() {
//         return state;
//     }

//     public void request() { state.handleRequest(this); }
//     public void confirm() { state.handleConfirm(this); }
//     public void reject() { state.handleReject(this); }
//     public void complete() { state.handleComplete(this); }
//     public void cancel() { state.handleCancel(this); }
//     public void processPayment() { state.handlePayment(this); }

//     // --- Getters and Setters ---
//     public Long getId() { return id; }
//     public String getBookingID() { return bookingID; }
//     public LocalDateTime getRequestedTime() { return requestedTime; }
//     public LocalDateTime getScheduledTime() { return scheduledTime; }
//     public double getPrice() { return price; }

//     @Override
//     public String toString() {
//         return "Booking{" +
//                 "id=" + id +
//                 ", bookingID='" + bookingID + '\'' +
//                 ", state=" + (state != null ? state.getClass().getSimpleName() : "null") +
//                 ", requestedTime=" + requestedTime +
//                 ", scheduledTime=" + scheduledTime +
//                 ", price=" + price +
//                 '}';
//     }
// }
