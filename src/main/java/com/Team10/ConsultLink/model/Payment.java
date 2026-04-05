package com.Team10.ConsultLink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double price;
    private LocalDateTime paymentDate;
    private String status;

    // Default Constructor (Required by JPA)
    public Payment() {}

    public Payment(double price) {
        this.price = price;
        this.paymentDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}


/* 
public class Payment {

    private String paymentID;
    private double price;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private String transactionID;

    // Constructor
    public Payment(String paymentID, double price, PaymentStatus status, LocalDateTime timestamp, String transactionID) {
        this.paymentID = paymentID;
        this.price = price;
        this.status = status;
        this.timestamp = timestamp;
        this.transactionID = transactionID;
    }

    // Getters and Setters
    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentID='" + paymentID + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", transactionID='" + transactionID + '\'' +
                '}';
    }
}
*/