package com.Team10.ConsultLink.service;

import com.Team10.ConsultLink.model.*;

public class BankTransferStrategy implements PaymentStrategy {
    private BankTransfer bankTransfer;
    
    public BankTransferStrategy(BankTransfer bankTransfer) {
        this.bankTransfer = bankTransfer;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Processing payment through bank transfer...");
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return bankTransfer;
    }

    @Override
    public void simPaymentProcessing() {
        // Simulate payment processing for bank transfer
        System.out.println("Simulating bank transfer payment processing...");
    }

}
