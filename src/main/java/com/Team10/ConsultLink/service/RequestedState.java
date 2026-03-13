package com.Team10.ConsultLink.service;
import com.Team10.ConsultLink.model.*;

public class RequestedState extends BaseBookingState {
    @Override
    public void handleConfirm(Booking booking) {
        booking.setState(new ConfirmedState());
    }

    @Override
    public void handleReject(Booking booking) {
        booking.setState(new RejectedState());
    }

    @Override
    public void handleCancel(Booking booking) {
        booking.setState(new CancelledState());
    }

}
