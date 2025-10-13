# RentCast API Integration

This document describes the RentCast API integration implementation for the Rental application.

## Overview

The integration provides access to RentCast's property data, value estimates, and rent estimates with built-in caching and address normalization to minimize API calls.

## Architecture

### Components

1. **Configuration** (`com.non.rental.config`)
   - `RentCastProperties`: Configuration properties from application.properties
   - `CacheConfig`: Spring Cache configuration
   - `SchedulingConfig`: Enables scheduled jobs

2. **Entities** (`com.non.rental.entity`)
   - `RentCastProperty`: Stores property data
   - `RentCastValueEstimate`: Stores property value estimates
   - `RentCastRentEstimate`: Stores rent estimates

3. **DTOs** (`com.non.rental.dto`)
   - `RentCastPropertyDto`: Maps API property response
   - `RentCastValueEstimateDto`: Maps API value estimate response
   - `RentCastRentEstimateDto`: Maps API rent estimate response

4. **Client** (`com.non.rental.client`)
   - `RentCastClient`: HTTP client for RentCast API with retry logic

5. **Service** (`com.non.rental.service`)
   - `RentCastService`: Service interface
   - `RentCastServiceImpl`: Implementation with caching and persistence

6. **Utilities** (`com.non.rental.util`)
   - `AddressNormalizer`: Normalizes addresses to prevent duplicate API calls

7. **Jobs** (`com.non.rental.job`)
   - `RentCastCacheRefreshJob`: Scheduled jobs for cache maintenance

8. **Exceptions** (`com.non.rental.exception`)
   - `RentCastApiException`: Custom exception for API errors

9. **REST API** (`com.non.rental.controller`)
   - `RentCastController`: REST endpoints for accessing RentCast data

## Configuration

### Application Properties

```properties
# RentCast API Configuration
rentcast.api.key=34fb2dfe78fb4d67a8a3fd6446e9fd73
rentcast.api.base-url=https://api.rentcast.io/v1
rentcast.api.timeout=30000
rentcast.api.max-retries=3
rentcast.cache.ttl-days=30
rentcast.cache.enabled=true

# RentCast API logging
logging.level.com.non.rental.service.rentcast=debug
```

## Features

### 1. Address Normalization

The `AddressNormalizer` utility ensures that address variations are treated as the same property:

- **"3080 Morley Lane"** and **"3080 Morley Ln"** → Same normalized address
- Handles street type abbreviations (St, Ave, Dr, Ln, etc.)
- Handles directional abbreviations (N, S, E, W, NE, NW, SE, SW)
- Case-insensitive comparison
- Removes extra whitespace and special characters

### 2. Caching Strategy

**Two-Level Caching:**

1. **Spring Cache** (In-Memory)
   - Fast access for frequently requested data
   - Automatic cache key generation based on address

2. **Database Cache** (PostgreSQL)
   - Persistent cache with 30-day TTL
   - Survives application restarts
   - Indexed by normalized address for fast lookups

**Cache Expiration:**
- Default TTL: 30 days (configurable)
- Automatic expiry checking on each request
- Scheduled cleanup job runs daily at 1 AM

### 3. API Client Features

- **Retry Logic**: Automatic retry with exponential backoff for server errors (5xx)
- **Error Handling**: Proper exception handling for client errors (4xx)
- **Logging**: Comprehensive logging of API interactions
- **Authentication**: API key passed via X-Api-Key header

### 4. Scheduled Jobs

| Job | Schedule | Description |
|-----|----------|-------------|
| `clearExpiredCache` | Daily at 1 AM | Removes expired cache entries |
| `refreshExpiringProperties` | Daily at 2 AM | Refreshes properties expiring within 7 days |
| `refreshExpiringValueEstimates` | Daily at 3 AM | Refreshes value estimates expiring within 7 days |
| `refreshExpiringRentEstimates` | Daily at 4 AM | Refreshes rent estimates expiring within 7 days |

## Usage Examples

### REST API Endpoints

The integration provides REST endpoints for easy access to RentCast data:

#### Get Property Data
```bash
GET /api/rentcast/property?address=5500 Grand Lake Dr, San Antonio, TX 78244
```

#### Get Value Estimate
```bash
GET /api/rentcast/value-estimate?address=5500 Grand Lake Dr, San Antonio, TX 78244
```

#### Get Rent Estimate
```bash
GET /api/rentcast/rent-estimate?address=5500 Grand Lake Dr, San Antonio, TX 78244
```

#### Get Cached Property (No API Call)
```bash
GET /api/rentcast/property/cached?address=5500 Grand Lake Dr, San Antonio, TX 78244
```

#### Force Refresh Property
```bash
POST /api/rentcast/property/refresh?address=5500 Grand Lake Dr, San Antonio, TX 78244
```

#### Force Refresh Value Estimate
```bash
POST /api/rentcast/value-estimate/refresh?address=5500 Grand Lake Dr, San Antonio, TX 78244
```

#### Force Refresh Rent Estimate
```bash
POST /api/rentcast/rent-estimate/refresh?address=5500 Grand Lake Dr, San Antonio, TX 78244
```

#### Clear Expired Cache
```bash
POST /api/rentcast/cache/clear-expired
```

### Programmatic Usage (Service Layer)

```java
@Autowired
private RentCastService rentCastService;

// Get property data (uses cache if available)
RentCastProperty property = rentCastService.getPropertyByAddress("5500 Grand Lake Dr, San Antonio, TX 78244");

// Get value estimate
RentCastValueEstimate valueEstimate = rentCastService.getValueEstimate("5500 Grand Lake Dr, San Antonio, TX 78244");

// Get rent estimate
RentCastRentEstimate rentEstimate = rentCastService.getRentEstimate("5500 Grand Lake Dr, San Antonio, TX 78244");
```

### Force Refresh

```java
// Force refresh from API, bypassing cache
RentCastProperty property = rentCastService.refreshPropertyData("5500 Grand Lake Dr, San Antonio, TX 78244");
```

### Check Cache Only

```java
// Check if data exists in cache without calling API
Optional<RentCastProperty> cached = rentCastService.getPropertyFromCache("5500 Grand Lake Dr, San Antonio, TX 78244");

if (cached.isPresent()) {
    // Use cached data
} else {
    // Data not in cache or expired
}
```

### Manual Cache Cleanup

```java
// Clear all expired cache entries
int clearedCount = rentCastService.clearExpiredCache();
```

## Database Schema

### RentCastProperty Table

Stores comprehensive property data including:
- Address information (normalized and formatted)
- Property attributes (bedrooms, bathrooms, square footage, etc.)
- Owner information
- Tax assessment history (JSON)
- Property tax history (JSON)
- Sale history (JSON)
- Property features (JSON)
- Cache metadata (created date, expiry date)

**Indexes:**
- `normalized_address` - Fast lookup by normalized address
- `rentcast_id` - Fast lookup by RentCast property ID

### RentCastValueEstimate Table

Stores property value estimates:
- Estimated price
- Price range (low/high)
- Confidence score
- Comparable properties (JSON)
- Cache metadata

### RentCastRentEstimate Table

Stores rent estimates:
- Estimated rent
- Rent range (low/high)
- Confidence score
- Comparable rentals (JSON)
- Cache metadata

## Error Handling

### API Errors

- **4xx Client Errors**: No retry, immediate exception
- **5xx Server Errors**: Automatic retry with exponential backoff
- **Network Errors**: Automatic retry with exponential backoff

### Exception Types

```java
try {
    RentCastProperty property = rentCastService.getPropertyByAddress(address);
} catch (RentCastApiException e) {
    // Handle API error
    int statusCode = e.getStatusCode();
    String responseBody = e.getResponseBody();
    log.error("RentCast API error: {}", e.getMessage());
}
```

## Best Practices

1. **Always use normalized addresses** when storing or comparing addresses
2. **Use cache-first approach** - check cache before calling API
3. **Handle exceptions gracefully** - API calls may fail
4. **Monitor cache hit rates** - adjust TTL if needed
5. **Review scheduled job logs** - ensure cache refresh is working

## API Rate Limits

RentCast API has rate limits based on your subscription plan:
- Free plan: 50 requests/month
- Paid plans: Higher limits

The caching strategy helps minimize API calls and stay within limits.

## Monitoring

### Logging

Enable debug logging to monitor API interactions:

```properties
logging.level.com.non.rental.service.rentcast=debug
logging.level.com.non.rental.client=debug
```

### Key Metrics to Monitor

- Cache hit rate
- API call frequency
- Failed API requests
- Cache expiry and refresh rates
- Scheduled job execution

## Troubleshooting

### Issue: Too many API calls

**Solution:** 
- Check if address normalization is working correctly
- Verify cache TTL is appropriate
- Review application logs for cache misses

### Issue: Stale data

**Solution:**
- Reduce cache TTL in application.properties
- Use `refresh*` methods to force updates
- Check scheduled job execution

### Issue: API authentication errors

**Solution:**
- Verify API key in application.properties
- Check RentCast account status
- Review API usage limits

## Future Enhancements

Potential improvements:

1. **UI Screens**: Create Jmix Flow UI screens for property search and display
2. **Bulk Import**: Add support for bulk property data import
3. **Market Data**: Integrate RentCast market data endpoints
4. **Listings**: Add support for active sale and rental listings
5. **Analytics**: Add reporting and analytics features
6. **Webhooks**: Implement webhook support for real-time updates
7. **Advanced Caching**: Implement Redis for distributed caching

## References

- [RentCast API Documentation](https://developers.rentcast.io/reference/introduction)
- [RentCast Property Data](https://developers.rentcast.io/reference/property-data)
- [RentCast AVM Endpoints](https://developers.rentcast.io/reference/property-valuation)
- [Jmix Documentation](https://docs.jmix.io/)