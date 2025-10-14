package com.non.rental.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.non.rental.config.RentCastProperties;
import com.non.rental.dto.RentCastPropertyDto;
import com.non.rental.dto.RentCastRentEstimateDto;
import com.non.rental.dto.RentCastValueEstimateDto;
import com.non.rental.exception.RentCastApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for making HTTP requests to RentCast API.
 * Handles authentication, error handling, and retry logic.
 */
@Component
public class RentCastClient {

    private static final Logger log = LoggerFactory.getLogger(RentCastClient.class);

    private final RestTemplate restTemplate;
    private final RentCastProperties properties;
    private final ObjectMapper objectMapper;

    public RentCastClient(RentCastProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Retrieves property data for a specific address.
     *
     * @param address the property address
     * @return property data DTO
     * @throws RentCastApiException if API call fails or no properties found
     */
    public RentCastPropertyDto getPropertyByAddress(String address) {
        log.debug("Fetching property data for address: {}", address);

        Map<String, String> params = new HashMap<>();
        params.put("address", address);

        String endpoint = "/properties";
        String response = executeGetRequest(endpoint, params);

        try {
            // RentCast API returns an array of properties
            RentCastPropertyDto[] properties = objectMapper.readValue(response, RentCastPropertyDto[].class);
            
            if (properties == null || properties.length == 0) {
                log.warn("No properties found for address: {}", address);
                throw new RentCastApiException("No properties found for address: " + address);
            }
            
            log.debug("Found {} property(ies) for address: {}", properties.length, address);
            return properties[0]; // Return the first matching property
            
        } catch (Exception e) {
            log.error("Failed to parse property response", e);
            throw new RentCastApiException("Failed to parse property response", e);
        }
    }

    /**
     * Retrieves value estimate for a property address.
     *
     * @param address the property address
     * @return value estimate DTO
     * @throws RentCastApiException if API call fails
     */
    public RentCastValueEstimateDto getValueEstimate(String address) {
        log.debug("Fetching value estimate for address: {}", address);

        Map<String, String> params = new HashMap<>();
        params.put("address", address);

        String endpoint = "/avm/value";
        String response = executeGetRequest(endpoint, params);

        try {
            return objectMapper.readValue(response, RentCastValueEstimateDto.class);
        } catch (Exception e) {
            log.error("Failed to parse value estimate response", e);
            throw new RentCastApiException("Failed to parse value estimate response", e);
        }
    }

    /**
     * Retrieves rent estimate for a property address.
     *
     * @param address the property address
     * @return rent estimate DTO
     * @throws RentCastApiException if API call fails
     */
    public RentCastRentEstimateDto getRentEstimate(String address) {
        log.debug("Fetching rent estimate for address: {}", address);

        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        params.put("compCount", "5"); // Number of comparable properties to use

        String endpoint = "/avm/rent/long-term";
        String response = executeGetRequest(endpoint, params);

        try {
            return objectMapper.readValue(response, RentCastRentEstimateDto.class);
        } catch (Exception e) {
            log.error("Failed to parse rent estimate response", e);
            throw new RentCastApiException("Failed to parse rent estimate response", e);
        }
    }

    /**
     * Executes a GET request to RentCast API with retry logic.
     *
     * @param endpoint the API endpoint
     * @param params query parameters
     * @return response body as string
     * @throws RentCastApiException if all retry attempts fail
     */
    private String executeGetRequest(String endpoint, Map<String, String> params) {
        int maxRetries = properties.getApi().getMaxRetries();
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                log.debug("API request attempt {} of {}", attempt, maxRetries);

                URI uri = buildUri(endpoint, params);
                HttpHeaders headers = createHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);

                log.debug("Calling RentCast API: {}", uri);
                ResponseEntity<String> response = restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.debug("API request successful");
                    return response.getBody();
                } else {
                    log.warn("API returned non-success status: {}", response.getStatusCode());
                    throw new RentCastApiException(
                            "API returned status: " + response.getStatusCode(),
                            response.getStatusCode().value(),
                            response.getBody()
                    );
                }

            } catch (HttpClientErrorException e) {
                log.error("Client error on attempt {}: {} - {}", attempt, e.getStatusCode(), e.getResponseBodyAsString());
                
                // Don't retry on client errors (4xx)
                throw new RentCastApiException(
                        "RentCast API client error: " + e.getMessage(),
                        e.getStatusCode().value(),
                        e.getResponseBodyAsString(),
                        e
                );

            } catch (HttpServerErrorException e) {
                log.error("Server error on attempt {}: {} - {}", attempt, e.getStatusCode(), e.getResponseBodyAsString());
                lastException = e;

                // Retry on server errors (5xx)
                if (attempt < maxRetries) {
                    try {
                        long waitTime = (long) Math.pow(2, attempt) * 1000; // Exponential backoff
                        log.debug("Waiting {} ms before retry", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RentCastApiException("Request interrupted", ie);
                    }
                }

            } catch (Exception e) {
                log.error("Unexpected error on attempt {}: {}", attempt, e.getMessage(), e);
                lastException = e;

                if (attempt < maxRetries) {
                    try {
                        long waitTime = (long) Math.pow(2, attempt) * 1000;
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RentCastApiException("Request interrupted", ie);
                    }
                }
            }
        }

        // All retries exhausted
        throw new RentCastApiException(
                "RentCast API request failed after " + maxRetries + " attempts",
                lastException
        );
    }

    /**
     * Builds the complete URI for an API request.
     *
     * @param endpoint the API endpoint
     * @param params query parameters
     * @return complete URI
     */
    private URI buildUri(String endpoint, Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(properties.getApi().getBaseUrl() + endpoint);

        if (params != null) {
            params.forEach(builder::queryParam);
        }

        return builder.build().encode().toUri();
    }

    /**
     * Creates HTTP headers with API key authentication.
     *
     * @return HTTP headers
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", properties.getApi().getKey());
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}