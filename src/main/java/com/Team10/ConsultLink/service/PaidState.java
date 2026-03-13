package com.Team10.ConsultLink.service;
import com.Team10.ConsultLink.model.*;

public class PaidState extends BaseBookingState {
    @Override
    public void handleCancel(Booking booking) {
        booking.setState(new CancelledState());
    }

    @Override
    public void handleComplete(Booking booking) {
        booking.setState(new CompletedState());
    }
}
