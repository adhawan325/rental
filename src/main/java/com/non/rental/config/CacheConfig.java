package com.non.rental.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache configuration for RentCast API responses.
 * Configures Spring Cache with custom cache names.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String RENTCAST_PROPERTY_CACHE = "rentcastPropertyCache";
    public static final String RENTCAST_VALUE_ESTIMATE_CACHE = "rentcastValueEstimateCache";
    public static final String RENTCAST_RENT_ESTIMATE_CACHE = "rentcastRentEstimateCache";
    
    // Jmix security framework required caches
    public static final String RESOURCE_ROLES_CACHE = "resource-roles-cache";
    public static final String ROW_LEVEL_ROLES_CACHE = "row-level-roles-cache";
    public static final String JMIX_EL_QUERY_CACHE = "jmix-eclipselink-query-cache";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                // RentCast API caches
                new ConcurrentMapCache(RENTCAST_PROPERTY_CACHE),
                new ConcurrentMapCache(RENTCAST_VALUE_ESTIMATE_CACHE),
                new ConcurrentMapCache(RENTCAST_RENT_ESTIMATE_CACHE),
                // Jmix security caches
                new ConcurrentMapCache(RESOURCE_ROLES_CACHE),
                new ConcurrentMapCache(ROW_LEVEL_ROLES_CACHE),
                new ConcurrentMapCache(JMIX_EL_QUERY_CACHE)
        ));
        return cacheManager;
    }
}
