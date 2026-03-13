package com.Team10.ConsultLink.service;

import com.Team10.ConsultLink.model.*;


public interface PricingStrategy {
	double calculatePrice(Service service);
}
