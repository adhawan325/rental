package com.non.rental.service;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;

import java.util.Optional;

/**
 * Service interface for RentCast API integration.
 * Provides methods to retrieve property data, value estimates, and rent estimates.
 */
public interface RentCastService {

    /**
     * Retrieves property data for a given address.
     * Uses cached data if available and not expired, otherwise fetches from API.
     *
     * @param address the property address
     * @return property entity
     */
    RentCastProperty getPropertyByAddress(String address);

    /**
     * Retrieves property data from cache only.
     *
     * @param address the property address
     * @return optional property entity
     */
    Optional<RentCastProperty> getPropertyFromCache(String address);

    /**
     * Retrieves value estimate for a property address.
     * Uses cached data if available and not expired, otherwise fetches from API.
     *
     * @param address the property address
     * @return value estimate entity
     */
    RentCastValueEstimate getValueEstimate(String address);

    /**
     * Retrieves value estimate from cache only.
     *
     * @param address the property address
     * @return optional value estimate entity
     */
    Optional<RentCastValueEstimate> getValueEstimateFromCache(String address);

    /**
     * Retrieves rent estimate for a property address.
     * Uses cached data if available and not expired, otherwise fetches from API.
     *
     * @param address the property address
     * @return rent estimate entity
     */
    RentCastRentEstimate getRentEstimate(String address);

    /**
     * Retrieves rent estimate from cache only.
     *
     * @param address the property address
     * @return optional rent estimate entity
     */
    Optional<RentCastRentEstimate> getRentEstimateFromCache(String address);

    /**
     * Forces a refresh of property data from the API, bypassing cache.
     *
     * @param address the property address
     * @return refreshed property entity
     */
    RentCastProperty refreshPropertyData(String address);

    /**
     * Forces a refresh of value estimate from the API, bypassing cache.
     *
     * @param address the property address
     * @return refreshed value estimate entity
     */
    RentCastValueEstimate refreshValueEstimate(String address);

    /**
     * Forces a refresh of rent estimate from the API, bypassing cache.
     *
     * @param address the property address
     * @return refreshed rent estimate entity
     */
    RentCastRentEstimate refreshRentEstimate(String address);

    /**
     * Clears expired cache entries.
     *
     * @return number of entries cleared
     */
    int clearExpiredCache();
}