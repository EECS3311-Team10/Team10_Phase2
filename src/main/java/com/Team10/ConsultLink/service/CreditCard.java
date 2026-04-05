package com.Team10.ConsultLink.service;

import jakarta.persistence.Entity;
import com.Team10.ConsultLink.model.*;
import java.time.YearMonth;

@Entity
public class CreditCard extends PaymentMethod {

    private String cardNumber;
    private String cardHolderName;
    private String expiryDate; // Stored as String for easier DB compatibility
    private String cvv;

    public CreditCard() { super(); }

    public CreditCard(String cardNumber, String cardHolderName, YearMonth expiryDate, String cvv) {
        super();
        this.setPaymentType("CREDIT");
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate.toString();
        this.cvv = cvv;
    }

    @Override
    public boolean validate() {
        // Basic check: 16 digits and 3 digit CVV
        return cardNumber != null && cardNumber.length() == 16 && cvv.length() == 3;
    }

    // Getters and Setters
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
}


/*package com.Team10.ConsultLink.service;
import com.Team10.ConsultLink.model.PaymentMethod;

import java.time.*;



public class CreditCard extends PaymentMethod {

    private String cardNumber;
    private String cardHolderName;
    private YearMonth expiryDate;
    private final String CVV;

    public CreditCard(String cardNumber, String cardHolderName, YearMonth expiryDate, String CVV) {
        super("Credit Card");
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.CVV = CVV;
    }

    @Override
    public boolean validate() {
        // Validation logic for credit card

        boolean validated = true;
        // Verify card number length = 16
        String s = String.valueOf(this.cardNumber);
        if (s.length() != 16) {
            validated = false;
        }
        // Verify expiryDate >> current date
        if (expiryDate.isBefore(YearMonth.now())) {
            validated = false;
        }

        return validated;
    }

    @Override
    public String getPaymentDetails() {
        return "Credit Card: " + cardNumber;
    }
}
*/