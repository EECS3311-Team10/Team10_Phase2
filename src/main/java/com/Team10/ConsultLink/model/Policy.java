package com.Team10.ConsultLink.model;

import jakarta.persistence.*;

@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cancellationRule;
    private String refundPolicy;
    private String notificationSetting;
    private String pricingStrategy;

    public Policy() {}

    public Policy(String cancellationRule, String refundPolicy,
                  String notificationSetting, String pricingStrategy) {
        this.cancellationRule = cancellationRule;
        this.refundPolicy = refundPolicy;
        this.notificationSetting = notificationSetting;
        this.pricingStrategy = pricingStrategy;
    }

    public Long getId() { return id; }

    public String getCancellationRule() { return cancellationRule; }
    public void setCancellationRule(String cancellationRule) { this.cancellationRule = cancellationRule; }

    public String getRefundPolicy() { return refundPolicy; }
    public void setRefundPolicy(String refundPolicy) { this.refundPolicy = refundPolicy; }

    public String getNotificationSetting() { return notificationSetting; }
    public void setNotificationSetting(String notificationSetting) { this.notificationSetting = notificationSetting; }

    public String getPricingStrategy() { return pricingStrategy; }
    public void setPricingStrategy(String pricingStrategy) { this.pricingStrategy = pricingStrategy; }
}