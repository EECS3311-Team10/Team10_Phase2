
package com.Team10.ConsultLink.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


//handle bookings

@RestController
@RequestMapping("/api")
public class BookingController {

    @GetMapping("/hello")
    public String sayHello() {
        return "ConsultLink API is running!";
    }
}