package com.Team10.ConsultLink.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies")
@CrossOrigin(origins = "*")
public class PolicyController {

    private final Map<String, String> policies = new HashMap<>();

    public PolicyController() {
        policies.put("cancellationRule", "24-hour notice required");
        policies.put("refundPolicy", "Full refund");
        policies.put("notificationSetting", "Email notifications enabled");
        policies.put("pricingStrategy", "Standard pricing");
    }

    @GetMapping
    public Map<String, String> getPolicies() {
        return policies;
    }

    @PutMapping
    public Map<String, String> updatePolicies(@RequestBody Map<String, String> updatedPolicies) {
        policies.put("cancellationRule", updatedPolicies.getOrDefault("cancellationRule", policies.get("cancellationRule")));
        policies.put("refundPolicy", updatedPolicies.getOrDefault("refundPolicy", policies.get("refundPolicy")));
        policies.put("notificationSetting", updatedPolicies.getOrDefault("notificationSetting", policies.get("notificationSetting")));
        policies.put("pricingStrategy", updatedPolicies.getOrDefault("pricingStrategy", policies.get("pricingStrategy")));
        return policies;
    }
}