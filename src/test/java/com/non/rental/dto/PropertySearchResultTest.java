package com.non.rental.dto;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PropertySearchResult DTO.
 */
@DisplayName("PropertySearchResult Tests")
class PropertySearchResultTest {

    private PropertySearchResult searchResult;
    private RentCastProperty property;
    private RentCastValueEstimate valueEstimate;
    private RentCastRentEstimate rentEstimate;

    @BeforeEach
    void setUp() {
        searchResult = new PropertySearchResult();
        
        property = new RentCastProperty();
        property.setFormattedAddress("123 Main St, San Francisco, CA 94102");
        property.setBedrooms(3);
        property.setBathrooms(2);
        property.setSquareFootage(1500);
        
        valueEstimate = new RentCastValueEstimate();
        valueEstimate.setFormattedAddress("123 Main St, San Francisco, CA 94102");
        valueEstimate.setPrice(new BigDecimal("500000.00"));
        valueEstimate.setPriceLow(new BigDecimal("450000.00"));
        valueEstimate.setPriceHigh(new BigDecimal("550000.00"));
        
        rentEstimate = new RentCastRentEstimate();
        rentEstimate.setFormattedAddress("123 Main St, San Francisco, CA 94102");
        rentEstimate.setRent(new BigDecimal("3000.00"));
        rentEstimate.setRentLow(new BigDecimal("2800.00"));
        rentEstimate.setRentHigh(new BigDecimal("3200.00"));
    }

    @Test
    @DisplayName("Should calculate gross yield correctly")
    void shouldCalculateGrossYieldCorrectly() {
        searchResult.setValueEstimate(valueEstimate);
        searchResult.setRentEstimate(rentEstimate);
        
        // $3,000/month × 12 = $36,000/year
        // $36,000 / $500,000 × 100 = 7.2%
        Double grossYield = searchResult.calculateGrossYield();
        
        assertNotNull(grossYield);
        assertEquals(7.2, grossYield, 0.01, "Gross yield should be 7.2%");
    }

    @Test
    @DisplayName("Should return null when value estimate is missing")
    void shouldReturnNullWhenValueEstimateIsMissing() {
        searchResult.setRentEstimate(rentEstimate);
        
        Double grossYield = searchResult.calculateGrossYield();
        
        assertNull(grossYield);
    }

    @Test
    @DisplayName("Should return null when rent estimate is missing")
    void shouldReturnNullWhenRentEstimateIsMissing() {
        searchResult.setValueEstimate(valueEstimate);
        
        Double grossYield = searchResult.calculateGrossYield();
        
        assertNull(grossYield);
    }

    @Test
    @DisplayName("Should return null when price is zero")
    void shouldReturnNullWhenPriceIsZero() {
        valueEstimate.setPrice(BigDecimal.ZERO);
        searchResult.setValueEstimate(valueEstimate);
        searchResult.setRentEstimate(rentEstimate);
        
        Double grossYield = searchResult.calculateGrossYield();
        
        assertNull(grossYield);
    }

    @Test
    @DisplayName("Should get formatted address from property")
    void shouldGetFormattedAddressFromProperty() {
        searchResult.setProperty(property);
        
        String address = searchResult.getFormattedAddress();
        
        assertEquals("123 Main St, San Francisco, CA 94102", address);
    }

    @Test
    @DisplayName("Should get formatted address from value estimate when property is null")
    void shouldGetFormattedAddressFromValueEstimate() {
        searchResult.setValueEstimate(valueEstimate);
        
        String address = searchResult.getFormattedAddress();
        
        assertEquals("123 Main St, San Francisco, CA 94102", address);
    }

    @Test
    @DisplayName("Should get formatted address from rent estimate when others are null")
    void shouldGetFormattedAddressFromRentEstimate() {
        searchResult.setRentEstimate(rentEstimate);
        
        String address = searchResult.getFormattedAddress();
        
        assertEquals("123 Main St, San Francisco, CA 94102", address);
    }

    @Test
    @DisplayName("Should return 'Unknown Address' when all are null")
    void shouldReturnUnknownAddressWhenAllAreNull() {
        String address = searchResult.getFormattedAddress();
        
        assertEquals("Unknown Address", address);
    }

    @Test
    @DisplayName("Should return true when has data")
    void shouldReturnTrueWhenHasData() {
        searchResult.setProperty(property);
        
        assertTrue(searchResult.hasData());
    }

    @Test
    @DisplayName("Should return false when has no data")
    void shouldReturnFalseWhenHasNoData() {
        assertFalse(searchResult.hasData());
    }

    @Test
    @DisplayName("Should return true when has property details")
    void shouldReturnTrueWhenHasPropertyDetails() {
        searchResult.setProperty(property);
        
        assertTrue(searchResult.hasPropertyDetails());
    }

    @Test
    @DisplayName("Should return false when has no property details")
    void shouldReturnFalseWhenHasNoPropertyDetails() {
        assertFalse(searchResult.hasPropertyDetails());
    }

    @Test
    @DisplayName("Should return true when has value estimate")
    void shouldReturnTrueWhenHasValueEstimate() {
        searchResult.setValueEstimate(valueEstimate);
        
        assertTrue(searchResult.hasValueEstimate());
    }

    @Test
    @DisplayName("Should return false when value estimate price is null")
    void shouldReturnFalseWhenValueEstimatePriceIsNull() {
        valueEstimate.setPrice(null);
        searchResult.setValueEstimate(valueEstimate);
        
        assertFalse(searchResult.hasValueEstimate());
    }

    @Test
    @DisplayName("Should return true when has rent estimate")
    void shouldReturnTrueWhenHasRentEstimate() {
        searchResult.setRentEstimate(rentEstimate);
        
        assertTrue(searchResult.hasRentEstimate());
    }

    @Test
    @DisplayName("Should return false when rent estimate rent is null")
    void shouldReturnFalseWhenRentEstimateRentIsNull() {
        rentEstimate.setRent(null);
        searchResult.setRentEstimate(rentEstimate);
        
        assertFalse(searchResult.hasRentEstimate());
    }

    @Test
    @DisplayName("Should calculate high yield correctly")
    void shouldCalculateHighYieldCorrectly() {
        // High rent, low value = high yield
        valueEstimate.setPrice(new BigDecimal("200000.00"));
        rentEstimate.setRent(new BigDecimal("2000.00"));
        
        searchResult.setValueEstimate(valueEstimate);
        searchResult.setRentEstimate(rentEstimate);
        
        // $2,000/month × 12 = $24,000/year
        // $24,000 / $200,000 × 100 = 12%
        Double grossYield = searchResult.calculateGrossYield();
        
        assertNotNull(grossYield);
        assertEquals(12.0, grossYield, 0.01, "Gross yield should be 12%");
    }

    @Test
    @DisplayName("Should calculate low yield correctly")
    void shouldCalculateLowYieldCorrectly() {
        // Low rent, high value = low yield
        valueEstimate.setPrice(new BigDecimal("1000000.00"));
        rentEstimate.setRent(new BigDecimal("2500.00"));
        
        searchResult.setValueEstimate(valueEstimate);
        searchResult.setRentEstimate(rentEstimate);
        
        // $2,500/month × 12 = $30,000/year
        // $30,000 / $1,000,000 × 100 = 3%
        Double grossYield = searchResult.calculateGrossYield();
        
        assertNotNull(grossYield);
        assertEquals(3.0, grossYield, 0.01, "Gross yield should be 3%");
    }
}