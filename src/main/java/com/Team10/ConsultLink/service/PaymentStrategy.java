package com.Team10.ConsultLink.service;
import com.Team10.ConsultLink.model.*;

public interface PaymentStrategy {
	public void pay();
	public PaymentMethod getPaymentMethod();
	public void simPaymentProcessing();
}
