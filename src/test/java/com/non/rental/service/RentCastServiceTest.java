package com.non.rental.service;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import com.non.rental.util.AddressNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RentCastService.
 * Note: These tests make actual API calls and require a valid API key.
 */
@SpringBootTest
public class RentCastServiceTest {

    @Autowired
    private RentCastService rentCastService;

    @Autowired
    private AddressNormalizer addressNormalizer;

    /**
     * Test address normalization.
     */
    @Test
    public void testAddressNormalization() {
        String address1 = "3080 Morley Lane, San Antonio, TX";
        String address2 = "3080 Morley Ln, San Antonio, TX";
        String address3 = "3080 MORLEY LN, SAN ANTONIO, TX";

        String normalized1 = addressNormalizer.normalize(address1);
        String normalized2 = addressNormalizer.normalize(address2);
        String normalized3 = addressNormalizer.normalize(address3);

        assertEquals(normalized1, normalized2, "Lane and Ln should normalize to same value");
        assertEquals(normalized2, normalized3, "Case should not matter");
        assertTrue(addressNormalizer.areEquivalent(address1, address2));
        assertTrue(addressNormalizer.areEquivalent(address2, address3));
    }

    /**
     * Test fetching property data.
     * This test is disabled by default to avoid unnecessary API calls.
     * Remove @Disabled annotation to run.
     */
    @Test
    @org.junit.jupiter.api.Disabled("Requires valid API key and makes actual API call")
    public void testGetPropertyByAddress() {
        String address = "5500 Grand Lake Dr, San Antonio, TX 78244";

        RentCastProperty property = rentCastService.getPropertyByAddress(address);

        assertNotNull(property);
        assertNotNull(property.getId());
        assertNotNull(property.getFormattedAddress());
        assertNotNull(property.getNormalizedAddress());
        assertNotNull(property.getCity());
        assertNotNull(property.getState());
        assertEquals("San Antonio", property.getCity());
        assertEquals("TX", property.getState());
    }

    /**
     * Test fetching value estimate.
     * This test is disabled by default to avoid unnecessary API calls.
     * Remove @Disabled annotation to run.
     */
    @Test
    @org.junit.jupiter.api.Disabled("Requires valid API key and makes actual API call")
    public void testGetValueEstimate() {
        String address = "5500 Grand Lake Dr, San Antonio, TX 78244";

        RentCastValueEstimate estimate = rentCastService.getValueEstimate(address);

        assertNotNull(estimate);
        assertNotNull(estimate.getId());
        assertNotNull(estimate.getPrice());
        assertTrue(estimate.getPrice().doubleValue() > 0);
    }

    /**
     * Test fetching rent estimate.
     * This test is disabled by default to avoid unnecessary API calls.
     * Remove @Disabled annotation to run.
     */
    @Test
    @org.junit.jupiter.api.Disabled("Requires valid API key and makes actual API call")
    public void testGetRentEstimate() {
        String address = "5500 Grand Lake Dr, San Antonio, TX 78244";

        RentCastRentEstimate estimate = rentCastService.getRentEstimate(address);

        assertNotNull(estimate);
        assertNotNull(estimate.getId());
        assertNotNull(estimate.getRent());
        assertTrue(estimate.getRent().doubleValue() > 0);
    }

    /**
     * Test caching behavior.
     * This test is disabled by default to avoid unnecessary API calls.
     * Remove @Disabled annotation to run.
     */
    @Test
    @org.junit.jupiter.api.Disabled("Requires valid API key and makes actual API call")
    public void testCaching() {
        String address = "5500 Grand Lake Dr, San Antonio, TX 78244";

        // First call - should fetch from API
        long startTime1 = System.currentTimeMillis();
        RentCastProperty property1 = rentCastService.getPropertyByAddress(address);
        long duration1 = System.currentTimeMillis() - startTime1;

        // Second call - should use cache
        long startTime2 = System.currentTimeMillis();
        RentCastProperty property2 = rentCastService.getPropertyByAddress(address);
        long duration2 = System.currentTimeMillis() - startTime2;

        assertNotNull(property1);
        assertNotNull(property2);
        assertEquals(property1.getId(), property2.getId());

        // Cached call should be significantly faster
        assertTrue(duration2 < duration1, 
                "Cached call should be faster. First: " + duration1 + "ms, Second: " + duration2 + "ms");
    }

    /**
     * Test address variation handling.
     * This test is disabled by default to avoid unnecessary API calls.
     * Remove @Disabled annotation to run.
     */
    @Test
    @org.junit.jupiter.api.Disabled("Requires valid API key and makes actual API call")
    public void testAddressVariations() {
        String address1 = "5500 Grand Lake Drive, San Antonio, TX 78244";
        String address2 = "5500 Grand Lake Dr, San Antonio, TX 78244";

        RentCastProperty property1 = rentCastService.getPropertyByAddress(address1);
        RentCastProperty property2 = rentCastService.getPropertyByAddress(address2);

        assertNotNull(property1);
        assertNotNull(property2);

        // Should return the same cached entity due to address normalization
        assertEquals(property1.getNormalizedAddress(), property2.getNormalizedAddress());
    }
}