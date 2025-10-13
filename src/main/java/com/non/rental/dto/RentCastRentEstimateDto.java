package com.non.rental.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for RentCast Rent Estimate API response.
 * Maps JSON response from /avm/rent endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RentCastRentEstimateDto {

    private BigDecimal rent;
    private BigDecimal rentRangeLow;
    private BigDecimal rentRangeHigh;
    private BigDecimal confidenceScore;
    private List<ComparableDto> comparables;

    public BigDecimal getRent() {
        return rent;
    }

    public void setRent(BigDecimal rent) {
        this.rent = rent;
    }

    public BigDecimal getRentRangeLow() {
        return rentRangeLow;
    }

    public void setRentRangeLow(BigDecimal rentRangeLow) {
        this.rentRangeLow = rentRangeLow;
    }

    public BigDecimal getRentRangeHigh() {
        return rentRangeHigh;
    }

    public void setRentRangeHigh(BigDecimal rentRangeHigh) {
        this.rentRangeHigh = rentRangeHigh;
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
        private BigDecimal rent;
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

        public BigDecimal getRent() {
            return rent;
        }

        public void setRent(BigDecimal rent) {
            this.rent = rent;
        }

        public BigDecimal getDistance() {
            return distance;
        }

        public void setDistance(BigDecimal distance) {
            this.distance = distance;
        }
    }
}