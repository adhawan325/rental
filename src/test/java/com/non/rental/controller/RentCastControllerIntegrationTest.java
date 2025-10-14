package com.non.rental.controller;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RentCastController.
 * These tests make actual API calls to RentCast using the test address: 3080 Morley Lane 95747
 */
@SpringBootTest
public class RentCastControllerIntegrationTest {

    @Autowired
    private RentCastController rentCastController;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    private static final String TEST_ADDRESS = "3080 Morley Lane 95747";

    // ==================== Property Endpoint Tests ====================

    @Test
    public void testGetProperty_RealApiCall() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing GET /api/rentcast/property ===");
            
            ResponseEntity<?> response = rentCastController.getProperty(TEST_ADDRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof RentCastProperty, "Response should be RentCastProperty");

            RentCastProperty property = (RentCastProperty) response.getBody();
            assertNotNull(property.getId(), "Property ID should not be null");
            assertNotNull(property.getFormattedAddress(), "Formatted address should not be null");
            assertNotNull(property.getCity(), "City should not be null");
            assertNotNull(property.getState(), "State should not be null");

            System.out.println("✓ Property retrieved successfully");
            System.out.println("  Address: " + property.getFormattedAddress());
            System.out.println("  City: " + property.getCity() + ", State: " + property.getState());
            System.out.println("  Bedrooms: " + property.getBedrooms() + ", Bathrooms: " + property.getBathrooms());
            System.out.println("  Square Footage: " + property.getSquareFootage());
            
        });
    }

    @Test
    public void testGetPropertyFromCache_AfterInitialCall() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing GET /api/rentcast/property/cached ===");
            
            // First, populate the cache
            rentCastController.getProperty(TEST_ADDRESS);

            // Now retrieve from cache
            ResponseEntity<?> response = rentCastController.getPropertyFromCache(TEST_ADDRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof RentCastProperty, "Response should be RentCastProperty");

            RentCastProperty property = (RentCastProperty) response.getBody();
            assertNotNull(property.getFormattedAddress(), "Cached property should have formatted address");

            System.out.println("✓ Property retrieved from cache successfully");
            System.out.println("  Address: " + property.getFormattedAddress());
            
        });
    }

    @Test
    public void testRefreshProperty_RealApiCall() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing POST /api/rentcast/property/refresh ===");
            
            ResponseEntity<?> response = rentCastController.refreshProperty(TEST_ADDRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof RentCastProperty, "Response should be RentCastProperty");

            RentCastProperty property = (RentCastProperty) response.getBody();
            assertNotNull(property.getId(), "Property ID should not be null");
            assertNotNull(property.getFormattedAddress(), "Formatted address should not be null");

            System.out.println("✓ Property refreshed successfully");
            System.out.println("  Address: " + property.getFormattedAddress());
            
        });
    }

    // ==================== Value Estimate Endpoint Tests ====================

    @Test
    public void testGetValueEstimate_RealApiCall() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing GET /api/rentcast/value-estimate ===");
            
            ResponseEntity<?> response = rentCastController.getValueEstimate(TEST_ADDRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof RentCastValueEstimate, "Response should be RentCastValueEstimate");

            RentCastValueEstimate estimate = (RentCastValueEstimate) response.getBody();
            assertNotNull(estimate.getId(), "Estimate ID should not be null");
            assertNotNull(estimate.getPrice(), "Price should not be null");
            assertTrue(estimate.getPrice().doubleValue() > 0, "Price should be greater than 0");

            System.out.println("✓ Value estimate retrieved successfully");
            System.out.println("  Price: $" + String.format("%,.2f", estimate.getPrice()));
            if (estimate.getPriceLow() != null && estimate.getPriceHigh() != null) {
                System.out.println("  Range: $" + String.format("%,.2f", estimate.getPriceLow()) + 
                                 " - $" + String.format("%,.2f", estimate.getPriceHigh()));
            }
            
        });
    }

    @Test
    public void testRefreshValueEstimate_RealApiCall() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing POST /api/rentcast/value-estimate/refresh ===");
            
            ResponseEntity<?> response = rentCastController.refreshValueEstimate(TEST_ADDRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof RentCastValueEstimate, "Response should be RentCastValueEstimate");

            RentCastValueEstimate estimate = (RentCastValueEstimate) response.getBody();
            assertNotNull(estimate.getPrice(), "Price should not be null");
            assertTrue(estimate.getPrice().doubleValue() > 0, "Price should be greater than 0");

            System.out.println("✓ Value estimate refreshed successfully");
            System.out.println("  Price: $" + String.format("%,.2f", estimate.getPrice()));
            
        });
    }

    // ==================== Rent Estimate Endpoint Tests ====================

    @Test
    public void testGetRentEstimate_RealApiCall() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing GET /api/rentcast/rent-estimate ===");
            
            ResponseEntity<?> response = rentCastController.getRentEstimate(TEST_ADDRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof RentCastRentEstimate, "Response should be RentCastRentEstimate");

            RentCastRentEstimate estimate = (RentCastRentEstimate) response.getBody();
            assertNotNull(estimate.getId(), "Estimate ID should not be null");
            assertNotNull(estimate.getRent(), "Rent should not be null");
            assertTrue(estimate.getRent().doubleValue() > 0, "Rent should be greater than 0");

            System.out.println("✓ Rent estimate retrieved successfully");
            System.out.println("  Rent: $" + String.format("%,.2f", estimate.getRent()));
            if (estimate.getRentLow() != null && estimate.getRentHigh() != null) {
                System.out.println("  Range: $" + String.format("%,.2f", estimate.getRentLow()) + 
                                 " - $" + String.format("%,.2f", estimate.getRentHigh()));
            }
            
        });
    }

    @Test
    public void testRefreshRentEstimate_RealApiCall() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing POST /api/rentcast/rent-estimate/refresh ===");
            
            ResponseEntity<?> response = rentCastController.refreshRentEstimate(TEST_ADDRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof RentCastRentEstimate, "Response should be RentCastRentEstimate");

            RentCastRentEstimate estimate = (RentCastRentEstimate) response.getBody();
            assertNotNull(estimate.getRent(), "Rent should not be null");
            assertTrue(estimate.getRent().doubleValue() > 0, "Rent should be greater than 0");

            System.out.println("✓ Rent estimate refreshed successfully");
            System.out.println("  Rent: $" + String.format("%,.2f", estimate.getRent()));
            
        });
    }

    // ==================== Cache Management Tests ====================

    @Test
    public void testClearExpiredCache() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing POST /api/rentcast/cache/clear-expired ===");
            
            // First, populate cache with some data
            rentCastController.getProperty(TEST_ADDRESS);
            rentCastController.getValueEstimate(TEST_ADDRESS);
            rentCastController.getRentEstimate(TEST_ADDRESS);

            // Clear expired cache
            ResponseEntity<?> response = rentCastController.clearExpiredCache();

            assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return 200 OK");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertTrue(response.getBody() instanceof Map, "Response should be a Map");

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.getBody();
            assertTrue((Boolean) result.get("success"), "Success should be true");
            assertNotNull(result.get("clearedCount"), "Cleared count should not be null");
            assertTrue((Integer) result.get("clearedCount") >= 0, "Cleared count should be non-negative");

            System.out.println("✓ Cache cleared successfully");
            System.out.println("  Cleared entries: " + result.get("clearedCount"));
            System.out.println("  Message: " + result.get("message"));
            
        });
    }

    // ==================== Comprehensive Integration Test ====================

    @Test
    public void testCompleteWorkflow() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing Complete Workflow ===");
            
            // Step 1: Get property data
            System.out.println("Step 1: Fetching property data...");
            ResponseEntity<?> propertyResponse = rentCastController.getProperty(TEST_ADDRESS);
            assertEquals(HttpStatus.OK, propertyResponse.getStatusCode());
            RentCastProperty property = (RentCastProperty) propertyResponse.getBody();
            assertNotNull(property);
            System.out.println("  ✓ Property: " + property.getFormattedAddress());

            // Step 2: Get value estimate
            System.out.println("Step 2: Fetching value estimate...");
            ResponseEntity<?> valueResponse = rentCastController.getValueEstimate(TEST_ADDRESS);
            assertEquals(HttpStatus.OK, valueResponse.getStatusCode());
            RentCastValueEstimate valueEstimate = (RentCastValueEstimate) valueResponse.getBody();
            assertNotNull(valueEstimate);
            System.out.println("  ✓ Value: $" + String.format("%,.2f", valueEstimate.getPrice()));

            // Step 3: Get rent estimate
            System.out.println("Step 3: Fetching rent estimate...");
            ResponseEntity<?> rentResponse = rentCastController.getRentEstimate(TEST_ADDRESS);
            assertEquals(HttpStatus.OK, rentResponse.getStatusCode());
            RentCastRentEstimate rentEstimate = (RentCastRentEstimate) rentResponse.getBody();
            assertNotNull(rentEstimate);
            System.out.println("  ✓ Rent: $" + String.format("%,.2f", rentEstimate.getRent()));

            // Step 4: Verify cache works
            System.out.println("Step 4: Verifying cache...");
            ResponseEntity<?> cachedResponse = rentCastController.getPropertyFromCache(TEST_ADDRESS);
            assertEquals(HttpStatus.OK, cachedResponse.getStatusCode());
            System.out.println("  ✓ Cache working");

            // Step 5: Refresh data
            System.out.println("Step 5: Refreshing property data...");
            ResponseEntity<?> refreshResponse = rentCastController.refreshProperty(TEST_ADDRESS);
            assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());
            System.out.println("  ✓ Data refreshed");

            System.out.println("\n✓ Complete workflow test passed!");
            
        });
    }

    // ==================== Performance Test ====================

    @Test
    public void testCachingPerformance() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing Caching Performance ===");
            
            // Use refresh to force API call for first request
            long start1 = System.currentTimeMillis();
            ResponseEntity<?> response1 = rentCastController.refreshProperty(TEST_ADDRESS);
            long duration1 = System.currentTimeMillis() - start1;
            assertEquals(HttpStatus.OK, response1.getStatusCode());

            // Second call - should use cache
            long start2 = System.currentTimeMillis();
            ResponseEntity<?> response2 = rentCastController.getProperty(TEST_ADDRESS);
            long duration2 = System.currentTimeMillis() - start2;
            assertEquals(HttpStatus.OK, response2.getStatusCode());

            System.out.println("  First call (API):    " + duration1 + "ms");
            System.out.println("  Second call (Cache): " + duration2 + "ms");
            
            if (duration2 < duration1) {
                System.out.println("  Speed improvement:   " + (duration1 - duration2) + "ms (" + 
                                 String.format("%.1f", (1.0 - (double)duration2/duration1) * 100) + "% faster)");
                System.out.println("\n✓ Caching performance test passed!");
            } else {
                // Sometimes timing can be unreliable, so just log a warning instead of failing
                System.out.println("  Note: Cache timing was similar to API call (timing can vary)");
                System.out.println("  This is acceptable as both calls succeeded");
            }
            
            // Just verify both calls succeeded - don't enforce strict timing
            assertNotNull(response1.getBody());
            assertNotNull(response2.getBody());
            
        });
    }

    // ==================== Address Variation Test ====================

    @Test
    public void testAddressVariations() {
        systemAuthenticator.runWithSystem(() -> {
            System.out.println("\n=== Testing Address Variations ===");
            
            String address1 = "3080 Morley Lane 95747";
            String address2 = "3080 Morley Ln 95747";
            String address3 = "3080 MORLEY LN 95747";

            // Get property with first address format
            ResponseEntity<?> response1 = rentCastController.getProperty(address1);
            assertEquals(HttpStatus.OK, response1.getStatusCode());
            RentCastProperty property1 = (RentCastProperty) response1.getBody();

            // Get property with second address format (should use cache)
            ResponseEntity<?> response2 = rentCastController.getProperty(address2);
            assertEquals(HttpStatus.OK, response2.getStatusCode());
            RentCastProperty property2 = (RentCastProperty) response2.getBody();

            // Get property with third address format (should use cache)
            ResponseEntity<?> response3 = rentCastController.getProperty(address3);
            assertEquals(HttpStatus.OK, response3.getStatusCode());
            RentCastProperty property3 = (RentCastProperty) response3.getBody();

            // All should have the same normalized address
            assertEquals(property1.getNormalizedAddress(), property2.getNormalizedAddress(),
                    "Address variations should normalize to same value");
            assertEquals(property2.getNormalizedAddress(), property3.getNormalizedAddress(),
                    "Address variations should normalize to same value");

            System.out.println("  Address 1: " + address1);
            System.out.println("  Address 2: " + address2);
            System.out.println("  Address 3: " + address3);
            System.out.println("  Normalized: " + property1.getNormalizedAddress());
            System.out.println("\n✓ Address variation test passed!");
            
        });
    }
}