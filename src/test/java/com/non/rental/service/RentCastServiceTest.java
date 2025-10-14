package com.non.rental.service;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import com.non.rental.util.AddressNormalizer;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RentCastService.
 * These tests make actual API calls to RentCast using the test address: 3080 Morley Lane 95747
 */
@SpringBootTest
public class RentCastServiceTest {

    @Autowired
    private RentCastService rentCastService;

    @Autowired
    private AddressNormalizer addressNormalizer;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private static final String TEST_ADDRESS = "3080 Morley Lane 95747";

    /**
     * Test address normalization.
     */
    @Test
    public void testAddressNormalization() {
        String address1 = "3080 Morley Lane 95747";
        String address2 = "3080 Morley Ln 95747";
        String address3 = "3080 MORLEY LN 95747";

        String normalized1 = addressNormalizer.normalize(address1);
        String normalized2 = addressNormalizer.normalize(address2);
        String normalized3 = addressNormalizer.normalize(address3);

        assertEquals(normalized1, normalized2, "Lane and Ln should normalize to same value");
        assertEquals(normalized2, normalized3, "Case should not matter");
        assertTrue(addressNormalizer.areEquivalent(address1, address2));
        assertTrue(addressNormalizer.areEquivalent(address2, address3));
    }

    /**
     * Test fetching property data from RentCast API.
     */
    @Test
    public void testGetPropertyByAddress() {
        systemAuthenticator.runWithSystem(() -> {
            RentCastProperty property = rentCastService.getPropertyByAddress(TEST_ADDRESS);

            assertNotNull(property, "Property should not be null");
            assertNotNull(property.getId(), "Property ID should not be null");
            assertNotNull(property.getFormattedAddress(), "Formatted address should not be null");
            assertNotNull(property.getNormalizedAddress(), "Normalized address should not be null");
            assertNotNull(property.getCity(), "City should not be null");
            assertNotNull(property.getState(), "State should not be null");
            assertNotNull(property.getZipCode(), "Zip code should not be null");
            
            System.out.println("Property retrieved: " + property.getFormattedAddress());
            System.out.println("City: " + property.getCity() + ", State: " + property.getState());
            System.out.println("Bedrooms: " + property.getBedrooms() + ", Bathrooms: " + property.getBathrooms());
            System.out.println("Square Footage: " + property.getSquareFootage());
        });
    }

    /**
     * Test fetching value estimate from RentCast API.
     */
    @Test
    public void testGetValueEstimate() {
        systemAuthenticator.runWithSystem(() -> {
            RentCastValueEstimate estimate = rentCastService.getValueEstimate(TEST_ADDRESS);

            assertNotNull(estimate, "Value estimate should not be null");
            assertNotNull(estimate.getId(), "Estimate ID should not be null");
            assertNotNull(estimate.getPrice(), "Price should not be null");
            assertTrue(estimate.getPrice().doubleValue() > 0, "Price should be greater than 0");
            
            System.out.println("Value Estimate for " + TEST_ADDRESS);
            System.out.println("Price: $" + estimate.getPrice());
            if (estimate.getPriceLow() != null && estimate.getPriceHigh() != null) {
                System.out.println("Price Range: $" + estimate.getPriceLow() + " - $" + estimate.getPriceHigh());
            }
            
        });
    }

    /**
     * Test fetching rent estimate from RentCast API.
     */
    @Test
    public void testGetRentEstimate() {
        systemAuthenticator.runWithSystem(() -> {
            RentCastRentEstimate estimate = rentCastService.getRentEstimate(TEST_ADDRESS);

            assertNotNull(estimate, "Rent estimate should not be null");
            assertNotNull(estimate.getId(), "Estimate ID should not be null");
            assertNotNull(estimate.getRent(), "Rent should not be null");
            assertTrue(estimate.getRent().doubleValue() > 0, "Rent should be greater than 0");
            
            System.out.println("Rent Estimate for " + TEST_ADDRESS);
            System.out.println("Rent: $" + estimate.getRent());
            if (estimate.getRentLow() != null && estimate.getRentHigh() != null) {
                System.out.println("Rent Range: $" + estimate.getRentLow() + " - $" + estimate.getRentHigh());
            }
            
        });
    }

    /**
     * Test caching behavior - second call should be faster.
     */
    @Test
    public void testCaching() {
        systemAuthenticator.runWithSystem(() -> {
            // First call - should fetch from API
            long startTime1 = System.currentTimeMillis();
            RentCastProperty property1 = rentCastService.getPropertyByAddress(TEST_ADDRESS);
            long duration1 = System.currentTimeMillis() - startTime1;

            // Second call - should use cache
            long startTime2 = System.currentTimeMillis();
            RentCastProperty property2 = rentCastService.getPropertyByAddress(TEST_ADDRESS);
            long duration2 = System.currentTimeMillis() - startTime2;

            assertNotNull(property1, "First property should not be null");
            assertNotNull(property2, "Second property should not be null");
            assertEquals(property1.getId(), property2.getId(), "Both calls should return the same property");

            System.out.println("First call (API): " + duration1 + "ms");
            System.out.println("Second call (Cache): " + duration2 + "ms");
            
            // Cached call should be significantly faster
            assertTrue(duration2 < duration1, 
                    "Cached call should be faster. First: " + duration1 + "ms, Second: " + duration2 + "ms");
            
        });
    }

    /**
     * Test address variation handling - different formats should return same cached data.
     */
    @Test
    public void testAddressVariations() {
        systemAuthenticator.runWithSystem(() -> {
            String address1 = "3080 Morley Lane 95747";
            String address2 = "3080 Morley Ln 95747";

            RentCastProperty property1 = rentCastService.getPropertyByAddress(address1);
            RentCastProperty property2 = rentCastService.getPropertyByAddress(address2);

            assertNotNull(property1, "Property from address1 should not be null");
            assertNotNull(property2, "Property from address2 should not be null");

            // Should return the same cached entity due to address normalization
            assertEquals(property1.getNormalizedAddress(), property2.getNormalizedAddress(),
                    "Different address formats should normalize to the same value");
            
            System.out.println("Address 1: " + address1);
            System.out.println("Address 2: " + address2);
            System.out.println("Normalized: " + property1.getNormalizedAddress());
            
        });
    }

    /**
     * Test cache-only retrieval.
     */
    @Test
    public void testGetPropertyFromCache() {
        systemAuthenticator.runWithSystem(() -> {
            // First, ensure data is in cache
            rentCastService.getPropertyByAddress(TEST_ADDRESS);

            // Now retrieve from cache only
            var cachedProperty = rentCastService.getPropertyFromCache(TEST_ADDRESS);

            assertTrue(cachedProperty.isPresent(), "Property should be present in cache");
            assertNotNull(cachedProperty.get().getFormattedAddress(), "Cached property should have formatted address");
            
            System.out.println("Retrieved from cache: " + cachedProperty.get().getFormattedAddress());
            
        });
    }

    /**
     * Test refresh functionality - should force new API call.
     */
    @Test
    public void testRefreshPropertyData() {
        systemAuthenticator.runWithSystem(() -> {
            // First call to populate cache
            RentCastProperty property1 = rentCastService.getPropertyByAddress(TEST_ADDRESS);
            
            // Refresh - should make new API call
            RentCastProperty property2 = rentCastService.refreshPropertyData(TEST_ADDRESS);

            assertNotNull(property1, "Initial property should not be null");
            assertNotNull(property2, "Refreshed property should not be null");
            assertEquals(property1.getNormalizedAddress(), property2.getNormalizedAddress(),
                    "Both should have same normalized address");
            
            System.out.println("Property refreshed successfully");
            
        });
    }

    /**
     * Test clearing expired cache entries.
     */
    @Test
    public void testClearExpiredCache() {
        systemAuthenticator.runWithSystem(() -> {
            // Ensure some data is in cache
            rentCastService.getPropertyByAddress(TEST_ADDRESS);
            rentCastService.getValueEstimate(TEST_ADDRESS);
            rentCastService.getRentEstimate(TEST_ADDRESS);

            // Clear expired cache (should be 0 since we just added them)
            int clearedCount = rentCastService.clearExpiredCache();

            assertTrue(clearedCount >= 0, "Cleared count should be non-negative");
            System.out.println("Cleared " + clearedCount + " expired cache entries");
            
        });
    }
}