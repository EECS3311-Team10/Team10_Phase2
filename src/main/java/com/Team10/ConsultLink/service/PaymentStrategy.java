package com.Team10.ConsultLink.service;
import com.Team10.ConsultLink.model.*;

public interface PaymentStrategy {
	public void pay(double amount);
	public PaymentMethod getPaymentMethod();
	public void simPaymentProcessing();
}
