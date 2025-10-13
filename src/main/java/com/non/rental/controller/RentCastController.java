package com.non.rental.controller;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import com.non.rental.exception.RentCastApiException;
import com.non.rental.service.RentCastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for RentCast API operations.
 * Provides endpoints to retrieve property data, value estimates, and rent estimates.
 */
@RestController
@RequestMapping("/api/rentcast")
public class RentCastController {

    private static final Logger log = LoggerFactory.getLogger(RentCastController.class);

    private final RentCastService rentCastService;

    public RentCastController(RentCastService rentCastService) {
        this.rentCastService = rentCastService;
    }

    /**
     * Get property data for an address.
     * 
     * @param address the property address
     * @return property data
     */
    @GetMapping("/property")
    public ResponseEntity<?> getProperty(@RequestParam String address) {
        try {
            log.info("REST request to get property for address: {}", address);
            RentCastProperty property = rentCastService.getPropertyByAddress(address);
            return ResponseEntity.ok(property);
        } catch (RentCastApiException e) {
            log.error("Error fetching property data", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching property data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Get property data from cache only.
     * 
     * @param address the property address
     * @return cached property data or 404 if not found
     */
    @GetMapping("/property/cached")
    public ResponseEntity<?> getPropertyFromCache(@RequestParam String address) {
        try {
            log.info("REST request to get cached property for address: {}", address);
            Optional<RentCastProperty> property = rentCastService.getPropertyFromCache(address);
            
            if (property.isPresent()) {
                return ResponseEntity.ok(property.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Property not found in cache"));
            }
        } catch (Exception e) {
            log.error("Error fetching cached property data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Get value estimate for an address.
     * 
     * @param address the property address
     * @return value estimate
     */
    @GetMapping("/value-estimate")
    public ResponseEntity<?> getValueEstimate(@RequestParam String address) {
        try {
            log.info("REST request to get value estimate for address: {}", address);
            RentCastValueEstimate estimate = rentCastService.getValueEstimate(address);
            return ResponseEntity.ok(estimate);
        } catch (RentCastApiException e) {
            log.error("Error fetching value estimate", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching value estimate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Get rent estimate for an address.
     * 
     * @param address the property address
     * @return rent estimate
     */
    @GetMapping("/rent-estimate")
    public ResponseEntity<?> getRentEstimate(@RequestParam String address) {
        try {
            log.info("REST request to get rent estimate for address: {}", address);
            RentCastRentEstimate estimate = rentCastService.getRentEstimate(address);
            return ResponseEntity.ok(estimate);
        } catch (RentCastApiException e) {
            log.error("Error fetching rent estimate", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching rent estimate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Force refresh property data from API.
     * 
     * @param address the property address
     * @return refreshed property data
     */
    @PostMapping("/property/refresh")
    public ResponseEntity<?> refreshProperty(@RequestParam String address) {
        try {
            log.info("REST request to refresh property for address: {}", address);
            RentCastProperty property = rentCastService.refreshPropertyData(address);
            return ResponseEntity.ok(property);
        } catch (RentCastApiException e) {
            log.error("Error refreshing property data", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error refreshing property data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Force refresh value estimate from API.
     * 
     * @param address the property address
     * @return refreshed value estimate
     */
    @PostMapping("/value-estimate/refresh")
    public ResponseEntity<?> refreshValueEstimate(@RequestParam String address) {
        try {
            log.info("REST request to refresh value estimate for address: {}", address);
            RentCastValueEstimate estimate = rentCastService.refreshValueEstimate(address);
            return ResponseEntity.ok(estimate);
        } catch (RentCastApiException e) {
            log.error("Error refreshing value estimate", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error refreshing value estimate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Force refresh rent estimate from API.
     * 
     * @param address the property address
     * @return refreshed rent estimate
     */
    @PostMapping("/rent-estimate/refresh")
    public ResponseEntity<?> refreshRentEstimate(@RequestParam String address) {
        try {
            log.info("REST request to refresh rent estimate for address: {}", address);
            RentCastRentEstimate estimate = rentCastService.refreshRentEstimate(address);
            return ResponseEntity.ok(estimate);
        } catch (RentCastApiException e) {
            log.error("Error refreshing rent estimate", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error refreshing rent estimate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * Clear expired cache entries.
     * 
     * @return number of entries cleared
     */
    @PostMapping("/cache/clear-expired")
    public ResponseEntity<?> clearExpiredCache() {
        try {
            log.info("REST request to clear expired cache");
            int clearedCount = rentCastService.clearExpiredCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("clearedCount", clearedCount);
            response.put("message", "Cleared " + clearedCount + " expired cache entries");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing expired cache", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error"));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}