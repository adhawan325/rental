package com.non.rental.dto;

/**
 * DTO representing a subscription plan feature.
 */
public class PlanFeature {
    private String name;
    private String description;
    private boolean included;

    public PlanFeature(String name, String description, boolean included) {
        this.name = name;
        this.description = description;
        this.included = included;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }
}