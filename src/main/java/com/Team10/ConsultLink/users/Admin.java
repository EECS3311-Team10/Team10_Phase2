package com.Team10.ConsultLink.users;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {

    public Admin() { super(); }

    public Admin(String userId, String name, String email, String phone, String password) {
        super(userId, name, email, phone, "ADMIN", password);
    }

    public void defineSystemPolicies() {
        // Logic for system policies
    }
}
/* 

    // =========================================================
    // Consultant approval
    // =========================================================
    public boolean reviewConsultantRequest(Consultant consultant, boolean approve) {

        if (consultant == null) return false;

        if (approve) {
            consultant.approve();
            System.out.println("Consultant approved: " + consultant.getName());
        } else {
            consultant.reject();
            System.out.println("Consultant rejected: " + consultant.getName());
        }

        return approve;
    }

    // =========================================================
    // Define system policies
    // =========================================================
    public void defineSystemPolicies(PricingStrategy pricing,
                                     CancellationPolicy cancellation,
                                     RefundPolicy refund) {

        PolicyManager manager = PolicyManager.getInstance();

        manager.setPricingStrategy(pricing);
        manager.setCancellationPolicy(cancellation);
        manager.setRefundPolicy(refund);

        System.out.println("System policies updated by admin.");
    }
        
}*/