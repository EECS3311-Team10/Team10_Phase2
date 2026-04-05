package com.Team10.ConsultLink.service; // Ensure this matches your folder!

import com.Team10.ConsultLink.model.*;
import jakarta.persistence.Entity;

@Entity
public class PayPal extends PaymentMethod {

    private String email;

    public PayPal() { super(); }

    public PayPal(String email) {
        super();
        this.setPaymentType("PAYPAL");
        this.email = email;
    }

    // THIS FIXES THE ERROR: Matches the UML method signature
    public String getPaymentDetails() {
        return "PayPal Account: " + this.email;
    }

    @Override
    public boolean validate() {
        return email != null && email.contains("@");
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}