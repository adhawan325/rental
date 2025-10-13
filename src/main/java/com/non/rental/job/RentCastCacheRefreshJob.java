package com.non.rental.job;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import com.non.rental.service.RentCastService;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to refresh cached RentCast data before expiration.
 * Runs daily to check for entries expiring within 7 days and refreshes them.
 */
@Component
public class RentCastCacheRefreshJob {

    private static final Logger log = LoggerFactory.getLogger(RentCastCacheRefreshJob.class);

    private final RentCastService rentCastService;
    private final DataManager dataManager;

    public RentCastCacheRefreshJob(RentCastService rentCastService, DataManager dataManager) {
        this.rentCastService = rentCastService;
        this.dataManager = dataManager;
    }

    /**
     * Refreshes property data that will expire within 7 days.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshExpiringProperties() {
        log.info("Starting scheduled refresh of expiring property data");

        try {
            LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(7);

            List<RentCastProperty> expiringProperties = dataManager.load(RentCastProperty.class)
                    .query("select p from RentCastProperty p where p.cacheExpiryDate < :threshold")
                    .parameter("threshold", expiryThreshold)
                    .list();

            log.info("Found {} properties expiring within 7 days", expiringProperties.size());

            int successCount = 0;
            int failureCount = 0;

            for (RentCastProperty property : expiringProperties) {
                try {
                    rentCastService.refreshPropertyData(property.getFormattedAddress());
                    successCount++;
                    log.debug("Refreshed property: {}", property.getFormattedAddress());
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to refresh property: {}", property.getFormattedAddress(), e);
                }
            }

            log.info("Property refresh completed. Success: {}, Failures: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("Error during scheduled property refresh", e);
        }
    }

    /**
     * Refreshes value estimates that will expire within 7 days.
     * Runs daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void refreshExpiringValueEstimates() {
        log.info("Starting scheduled refresh of expiring value estimates");

        try {
            LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(7);

            List<RentCastValueEstimate> expiringEstimates = dataManager.load(RentCastValueEstimate.class)
                    .query("select v from RentCastValueEstimate v where v.cacheExpiryDate < :threshold")
                    .parameter("threshold", expiryThreshold)
                    .list();

            log.info("Found {} value estimates expiring within 7 days", expiringEstimates.size());

            int successCount = 0;
            int failureCount = 0;

            for (RentCastValueEstimate estimate : expiringEstimates) {
                try {
                    rentCastService.refreshValueEstimate(estimate.getFormattedAddress());
                    successCount++;
                    log.debug("Refreshed value estimate: {}", estimate.getFormattedAddress());
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to refresh value estimate: {}", estimate.getFormattedAddress(), e);
                }
            }

            log.info("Value estimate refresh completed. Success: {}, Failures: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("Error during scheduled value estimate refresh", e);
        }
    }

    /**
     * Refreshes rent estimates that will expire within 7 days.
     * Runs daily at 4 AM.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void refreshExpiringRentEstimates() {
        log.info("Starting scheduled refresh of expiring rent estimates");

        try {
            LocalDateTime expiryThreshold = LocalDateTime.now().plusDays(7);

            List<RentCastRentEstimate> expiringEstimates = dataManager.load(RentCastRentEstimate.class)
                    .query("select r from RentCastRentEstimate r where r.cacheExpiryDate < :threshold")
                    .parameter("threshold", expiryThreshold)
                    .list();

            log.info("Found {} rent estimates expiring within 7 days", expiringEstimates.size());

            int successCount = 0;
            int failureCount = 0;

            for (RentCastRentEstimate estimate : expiringEstimates) {
                try {
                    rentCastService.refreshRentEstimate(estimate.getFormattedAddress());
                    successCount++;
                    log.debug("Refreshed rent estimate: {}", estimate.getFormattedAddress());
                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to refresh rent estimate: {}", estimate.getFormattedAddress(), e);
                }
            }

            log.info("Rent estimate refresh completed. Success: {}, Failures: {}", successCount, failureCount);

        } catch (Exception e) {
            log.error("Error during scheduled rent estimate refresh", e);
        }
    }

    /**
     * Clears expired cache entries.
     * Runs daily at 1 AM.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void clearExpiredCache() {
        log.info("Starting scheduled cleanup of expired cache entries");

        try {
            int clearedCount = rentCastService.clearExpiredCache();
            log.info("Cleared {} expired cache entries", clearedCount);
        } catch (Exception e) {
            log.error("Error during scheduled cache cleanup", e);
        }
    }
}