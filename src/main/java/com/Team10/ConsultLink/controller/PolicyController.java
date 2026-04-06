package com.Team10.ConsultLink.controller;

import com.Team10.ConsultLink.model.Policy;
import com.Team10.ConsultLink.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@CrossOrigin(origins = "*")
public class PolicyController {

    @Autowired
    private PolicyRepository policyRepository;

    @GetMapping
    public Policy getPolicies() {
        List<Policy> policies = policyRepository.findAll();

        if (policies.isEmpty()) {
            Policy defaultPolicy = new Policy(
                    "24-hour notice required",
                    "Full refund",
                    "Email notifications enabled",
                    "Standard pricing"
            );
            return policyRepository.save(defaultPolicy);
        }

        return policies.get(0);
    }

    @PutMapping
    public Policy updatePolicies(@RequestBody Policy updatedPolicy) {
        List<Policy> policies = policyRepository.findAll();

        Policy policy;
        if (policies.isEmpty()) {
            policy = new Policy();
        } else {
            policy = policies.get(0);
        }

        policy.setCancellationRule(updatedPolicy.getCancellationRule());
        policy.setRefundPolicy(updatedPolicy.getRefundPolicy());
        policy.setNotificationSetting(updatedPolicy.getNotificationSetting());
        policy.setPricingStrategy(updatedPolicy.getPricingStrategy());

        return policyRepository.save(policy);
    }
}