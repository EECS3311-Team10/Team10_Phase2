package com.Team10.ConsultLink.service;
import com.Team10.ConsultLink.model.*;

public class ConfirmedState extends BaseBookingState {
    @Override
    public void handleCancel(Booking booking) {
        booking.setState(new CancelledState());
    }

    @Override
    public void handlePayment(Booking booking) {
        booking.setState(new PendingPaymentState());
    }
}
