package com.non.rental.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * Enum representing billing cycle options.
 */
public enum BillingCycle implements EnumClass<String> {
    MONTHLY("MONTHLY", "Monthly"),
    ANNUAL("ANNUAL", "Annual");

    private final String id;
    private final String displayName;

    BillingCycle(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Nullable
    public static BillingCycle fromId(String id) {
        for (BillingCycle cycle : BillingCycle.values()) {
            if (cycle.getId().equals(id)) {
                return cycle;
            }
        }
        return null;
    }
}