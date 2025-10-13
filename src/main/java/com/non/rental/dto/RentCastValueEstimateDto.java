package com.non.rental.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for RentCast Value Estimate API response.
 * Maps JSON response from /avm/value endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RentCastValueEstimateDto {

    private BigDecimal price;
    private BigDecimal priceRangeLow;
    private BigDecimal priceRangeHigh;
    private BigDecimal confidenceScore;
    private List<ComparableDto> comparables;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceRangeLow() {
        return priceRangeLow;
    }

    public void setPriceRangeLow(BigDecimal priceRangeLow) {
        this.priceRangeLow = priceRangeLow;
    }

    public BigDecimal getPriceRangeHigh() {
        return priceRangeHigh;
    }

    public void setPriceRangeHigh(BigDecimal priceRangeHigh) {
        this.priceRangeHigh = priceRangeHigh;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public List<ComparableDto> getComparables() {
        return comparables;
    }

    public void setComparables(List<ComparableDto> comparables) {
        this.comparables = comparables;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComparableDto {
        private String id;
        private String formattedAddress;
        private String propertyType;
        private Integer bedrooms;
        private Integer bathrooms;
        private Integer squareFootage;
        private BigDecimal price;
        private BigDecimal distance;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public void setFormattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public String getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(String propertyType) {
            this.propertyType = propertyType;
        }

        public Integer getBedrooms() {
            return bedrooms;
        }

        public void setBedrooms(Integer bedrooms) {
            this.bedrooms = bedrooms;
        }

        public Integer getBathrooms() {
            return bathrooms;
        }

        public void setBathrooms(Integer bathrooms) {
            this.bathrooms = bathrooms;
        }

        public Integer getSquareFootage() {
            return squareFootage;
        }

        public void setSquareFootage(Integer squareFootage) {
            this.squareFootage = squareFootage;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getDistance() {
            return distance;
        }

        public void setDistance(BigDecimal distance) {
            this.distance = distance;
        }
    }
}