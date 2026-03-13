package com.Team10.ConsultLink.service;

import com.Team10.ConsultLink.model.*;

public interface CancellationPolicy {
	boolean isAllowed(Booking booking);
}
