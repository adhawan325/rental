package com.non.rental.dto;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO to hold combined property search results from RentCast API.
 * Combines property data, value estimate, and rent estimate.
 */
public class PropertySearchResult implements Serializable {
    
    private RentCastProperty property;
    private RentCastValueEstimate valueEstimate;
    private RentCastRentEstimate rentEstimate;
    
    public PropertySearchResult() {
    }
    
    public PropertySearchResult(RentCastProperty property, 
                               RentCastValueEstimate valueEstimate, 
                               RentCastRentEstimate rentEstimate) {
        this.property = property;
        this.valueEstimate = valueEstimate;
        this.rentEstimate = rentEstimate;
    }
    
    // Getters and Setters
    
    public RentCastProperty getProperty() {
        return property;
    }
    
    public void setProperty(RentCastProperty property) {
        this.property = property;
    }
    
    public RentCastValueEstimate getValueEstimate() {
        return valueEstimate;
    }
    
    public void setValueEstimate(RentCastValueEstimate valueEstimate) {
        this.valueEstimate = valueEstimate;
    }
    
    public RentCastRentEstimate getRentEstimate() {
        return rentEstimate;
    }
    
    public void setRentEstimate(RentCastRentEstimate rentEstimate) {
        this.rentEstimate = rentEstimate;
    }
    
    // Convenience methods
    
    /**
     * Calculates estimated gross yield percentage.
     * Formula: (Annual Rent / Property Value) × 100
     */
    public Double calculateGrossYield() {
        if (rentEstimate != null && rentEstimate.getRent() != null 
                && valueEstimate != null && valueEstimate.getPrice() != null
                && valueEstimate.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal annualRent = rentEstimate.getRent().multiply(BigDecimal.valueOf(12));
            BigDecimal yield = annualRent
                    .divide(valueEstimate.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            return yield.doubleValue();
        }
        return null;
    }
    
    /**
     * Gets the formatted address from property or value estimate.
     */
    public String getFormattedAddress() {
        if (property != null && property.getFormattedAddress() != null) {
            return property.getFormattedAddress();
        }
        if (valueEstimate != null && valueEstimate.getFormattedAddress() != null) {
            return valueEstimate.getFormattedAddress();
        }
        if (rentEstimate != null && rentEstimate.getFormattedAddress() != null) {
            return rentEstimate.getFormattedAddress();
        }
        return "Unknown Address";
    }
    
    /**
     * Checks if the search result has valid data.
     */
    public boolean hasData() {
        return property != null || valueEstimate != null || rentEstimate != null;
    }
    
    /**
     * Checks if the result has property details.
     */
    public boolean hasPropertyDetails() {
        return property != null;
    }
    
    /**
     * Checks if the result has value estimate.
     */
    public boolean hasValueEstimate() {
        return valueEstimate != null && valueEstimate.getPrice() != null;
    }
    
    /**
     * Checks if the result has rent estimate.
     */
    public boolean hasRentEstimate() {
        return rentEstimate != null && rentEstimate.getRent() != null;
    }
}