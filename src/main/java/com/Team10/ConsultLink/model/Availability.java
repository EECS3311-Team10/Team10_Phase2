package com.Team10.ConsultLink.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "availability")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String consultantUserId;
    private String date;
    private String time;

    public Availability() {
    }

    public Availability(String consultantUserId, String date, String time) {
        this.consultantUserId = consultantUserId;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public String getConsultantUserId() {
        return consultantUserId;
    }

    public void setConsultantUserId(String consultantUserId) {
        this.consultantUserId = consultantUserId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}