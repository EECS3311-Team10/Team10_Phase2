package com.Team10.ConsultLink.service;

import com.Team10.ConsultLink.model.*;

public interface BookingState {
	void handleRequest(Booking b);
    void handleConfirm(Booking b);
    void handleCancel(Booking b);
    void handlePayment(Booking b);
    void handleComplete(Booking b);
    void handleReject(Booking b);
}
