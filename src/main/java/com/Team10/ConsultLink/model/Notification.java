package com.Team10.ConsultLink.model;

import com.Team10.ConsultLink.users.*;
import jakarta.persistence.*; // Required for JPA
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId; // Primary key for database indexing

    private String id; // Your business logic ID (e.g., UUID)
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    private LocalDateTime timestamp;
    private int priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User recipient;

    private boolean readStatus; // Changed name from 'read' as it's a SQL reserved word

    // 1. Required Default Constructor
    public Notification() {}

    // 2. Updated Constructor (removed 'final' constraints)
    public Notification(String id, String message, LocalDateTime timestamp, int priority, User recipient) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.priority = priority;
        this.recipient = recipient;
        this.readStatus = false;
    }

    // --- Getters and Setters ---
    public Long getDbId() { return dbId; }
    
    public String getId() { return id; }
    
    public String getMessage() { return message; }
    
    public LocalDateTime getTimestamp() { return timestamp; }

    public int getPriority() { return priority; }

    public User getRecipient() { return recipient; }

    public boolean isRead() { return readStatus; }

    public void markAsRead() {
        this.readStatus = true;
    }
}


/*package com.Team10.ConsultLink.model;

import com.Team10.ConsultLink.users.*;

import java.time.LocalDateTime;

public class Notification {

    private final String id;
    private final String message;
    private final LocalDateTime timestamp;
    private final int priority;
    private final User recipient;

    private boolean read;

    public Notification(String id, String message, LocalDateTime timestamp, int priority, User recipient) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.priority = priority;
        this.recipient = recipient;
        this.read = false;
    }

    public String getId() { return id; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public int getPriority() { return priority; }

    public User getRecipient() { return recipient; }

    public boolean isRead() { return read; }

    public void markAsRead() {
        read = true;
    }
}
    */