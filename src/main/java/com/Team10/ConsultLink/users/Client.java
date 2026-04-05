package com.Team10.ConsultLink.users;

import com.Team10.ConsultLink.model.*;
import com.Team10.ConsultLink.service.*;
import jakarta.persistence.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client extends User {

    // One Client can have many Bookings
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private List<Booking> bookings = new ArrayList<>();

    // One Client can have many Payment Methods
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    // One Client has a history of many Payments
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private List<Payment> paymentHistory = new ArrayList<>();

    public Client() { super(); }

    public Client(String userId, String name, String email, String phone, String password) {
        super(userId, name, email, phone, "CLIENT", password);
    }

    // --- UML Methods ---

    public void requestBooking(Booking booking) {
        this.bookings.add(booking);
    }

    public void cancelBooking(Booking booking) {
        this.bookings.remove(booking);
    }

    public List<Booking> viewBookingHistory() {
        return this.bookings;
    }

    // Logic for Strategy Pattern
    public void processPayment(Payment payment, PaymentStrategy strategy) {
        strategy.pay(payment.getPrice()); 
        addPayment(payment);
    }

    public List<PaymentMethod> getPaymentMethods() {
        return this.paymentMethods;
    }

    // --- Payment Method Overloads ---

    public boolean addPaymentMethod(String paymentType, String cardNumber, YearMonth expiryDate,
                                 String cvv, String cardHolderName) {
        if (paymentType.equalsIgnoreCase("CREDIT")) {
            CreditCard card = new CreditCard(cardNumber, cardHolderName, expiryDate, cvv);
            if (card.validate()) {
                paymentMethods.add(card);
                return true;
            }
        } else if (paymentType.equalsIgnoreCase("DEBIT")) {
            DebitCard card = new DebitCard(cardNumber, cardHolderName, expiryDate, cvv);
            if (card.validate()) {
                paymentMethods.add(card);
                return true;
            }
        }
        return false;
    }

    public boolean addPaymentMethod(String accountNumber, String address, String accountName, String routingNumber) {
        BankTransfer bt = new BankTransfer(accountNumber, address, accountName, routingNumber);
        if (bt.validate()) {
            paymentMethods.add(bt);
            return true;
        }
        return false;
    }

    public boolean addPaymentMethod(String email) {
        PayPal pp = new PayPal(email);
        if (pp.validate()) {
            paymentMethods.add(pp);
            return true;
        }
        return false;
    }

    public void addPayment(Payment payment) {
        this.paymentHistory.add(payment);
    }

    public List<Payment> viewPaymentHistory() {
        return this.paymentHistory;
    }
}