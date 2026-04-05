package com.Team10.ConsultLink.service;

import com.Team10.ConsultLink.model.*;
import jakarta.persistence.Entity;

@Entity
public class BankTransfer extends PaymentMethod {

    private String accountNumber;
    private String address;
    private String accountName;
    private String routingNumber;

    public BankTransfer() { super(); }

    public BankTransfer(String accountNumber, String address, String accountName, String routingNumber) {
        super();
        this.setPaymentType("BANK_TRANSFER");
        this.accountNumber = accountNumber;
        this.address = address;
        this.accountName = accountName;
        this.routingNumber = routingNumber;
    }

    @Override
    public boolean validate() {
        return accountNumber != null && routingNumber != null;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public String getRoutingNumber() { return routingNumber; }
}
