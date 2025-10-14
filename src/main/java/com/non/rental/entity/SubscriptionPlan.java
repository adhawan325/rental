package com.non.rental.entity;

/**
 * Enum representing available subscription plans.
 */
public enum SubscriptionPlan {
    FREE("Free", 0.0, 0.0),
    PLUS("Plus", 9.99, 99.99),
    PRO("Pro", 19.99, 199.99);

    private final String displayName;
    private final Double monthlyPrice;
    private final Double annualPrice;

    SubscriptionPlan(String displayName, Double monthlyPrice, Double annualPrice) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.annualPrice = annualPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Double getMonthlyPrice() {
        return monthlyPrice;
    }

    public Double getAnnualPrice() {
        return annualPrice;
    }

    public boolean isFree() {
        return this == FREE;
    }
}