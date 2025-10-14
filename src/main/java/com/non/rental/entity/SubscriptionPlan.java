package com.non.rental.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * Enum representing available subscription plans.
 */
public enum SubscriptionPlan implements EnumClass<String> {
    FREE("FREE", "Free", 0.0, 0.0),
    PLUS("PLUS", "Plus", 9.99, 99.99),
    PRO("PRO", "Pro", 19.99, 199.99);

    private final String id;
    private final String displayName;
    private final Double monthlyPrice;
    private final Double annualPrice;

    SubscriptionPlan(String id, String displayName, Double monthlyPrice, Double annualPrice) {
        this.id = id;
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.annualPrice = annualPrice;
    }

    @Override
    public String getId() {
        return id;
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

    @Nullable
    public static SubscriptionPlan fromId(String id) {
        for (SubscriptionPlan plan : SubscriptionPlan.values()) {
            if (plan.getId().equals(id)) {
                return plan;
            }
        }
        return null;
    }
}