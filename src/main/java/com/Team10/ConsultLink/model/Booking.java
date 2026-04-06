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
    public void setBookingID(String bookingID) { this.bookingID = bookingID; }
    public void setClientUserId(String clientUserId) { this.clientUserId = clientUserId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setStatus(String status) { this.status = status; }
    public void setRequestedTime(LocalDateTime requestedTime) { this.requestedTime = requestedTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public void setPrice(double price) { this.price = price; }
            
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

