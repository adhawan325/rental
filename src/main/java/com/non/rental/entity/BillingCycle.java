package com.non.rental.entity;

/**
 * Enum representing billing cycle options.
 */
public enum BillingCycle {
    MONTHLY("Monthly"),
    ANNUAL("Annual");

    private final String displayName;

    BillingCycle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}