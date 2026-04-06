package com.Team10.ConsultLink.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentID;
    private String bookingID;
    private Long bookingDbId;
    private String clientUserId;
    private String serviceName;
    private double price;
    private String method;
    private String status;
    private LocalDateTime paymentDate;
    private String transactionID;

    public Payment() {}

    public Payment(String paymentID,
                   String bookingID,
                   Long bookingDbId,
                   String clientUserId,
                   String serviceName,
                   double price,
                   String method,
                   String status,
                   LocalDateTime paymentDate,
                   String transactionID) {
        this.paymentID = paymentID;
        this.bookingID = bookingID;
        this.bookingDbId = bookingDbId;
        this.clientUserId = clientUserId;
        this.serviceName = serviceName;
        this.price = price;
        this.method = method;
        this.status = status;
        this.paymentDate = paymentDate;
        this.transactionID = transactionID;
    }

    public Long getId() { return id; }

    public String getPaymentID() { return paymentID; }
    public void setPaymentID(String paymentID) { this.paymentID = paymentID; }

    public String getBookingID() { return bookingID; }
    public void setBookingID(String bookingID) { this.bookingID = bookingID; }

    public Long getBookingDbId() { return bookingDbId; }
    public void setBookingDbId(Long bookingDbId) { this.bookingDbId = bookingDbId; }

    public String getClientUserId() { return clientUserId; }
    public void setClientUserId(String clientUserId) { this.clientUserId = clientUserId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getTransactionID() { return transactionID; }
    public void setTransactionID(String transactionID) { this.transactionID = transactionID; }
}
