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

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(RENTCAST_PROPERTY_CACHE),
                new ConcurrentMapCache(RENTCAST_VALUE_ESTIMATE_CACHE),
                new ConcurrentMapCache(RENTCAST_RENT_ESTIMATE_CACHE)
        ));
        return cacheManager;
    }
}