package com.Team10.ConsultLink.service;

import com.Team10.ConsultLink.model.*;

import jakarta.persistence.Entity;
import java.time.YearMonth;

@Entity
public class DebitCard extends PaymentMethod {

    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    public DebitCard() { super(); }

    public DebitCard(String cardNumber, String cardHolderName, YearMonth expiryDate, String cvv) {
        super();
        this.setPaymentType("DEBIT");
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate.toString();
        this.cvv = cvv;
    }

    @Override
    public boolean validate() {
        return cardNumber != null && cardNumber.length() >= 13;
    }
}