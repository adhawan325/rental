# RentCast API Integration - Quick Reference Guide

## 🚀 Getting Started

### Start the Application
```bash
cd /Users/adhawan/IdeaProjects/rental
./gradlew bootRun
```

The application will start on `http://localhost:8080`

---

## 📡 REST API Endpoints

### Base URL
```
http://localhost:8080/api/rentcast
```

### 1. Get Property Data
**Endpoint:** `GET /api/rentcast/property`

**Example:**
```bash
curl "http://localhost:8080/api/rentcast/property?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

**Response:** Full property data including address, attributes, owner info, tax history, etc.

---

### 2. Get Value Estimate
**Endpoint:** `GET /api/rentcast/value-estimate`

**Example:**
```bash
curl "http://localhost:8080/api/rentcast/value-estimate?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

**Response:** Property value estimate with price range and comparable properties

---

### 3. Get Rent Estimate
**Endpoint:** `GET /api/rentcast/rent-estimate`

**Example:**
```bash
curl "http://localhost:8080/api/rentcast/rent-estimate?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

**Response:** Rent estimate with rent range and comparable rentals

---

### 4. Get Cached Property (No API Call)
**Endpoint:** `GET /api/rentcast/property/cached`

**Example:**
```bash
curl "http://localhost:8080/api/rentcast/property/cached?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

**Response:** Cached property data or 404 if not found

---

### 5. Force Refresh Property
**Endpoint:** `POST /api/rentcast/property/refresh`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/rentcast/property/refresh?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

**Response:** Freshly fetched property data from API

---

### 6. Force Refresh Value Estimate
**Endpoint:** `POST /api/rentcast/value-estimate/refresh`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/rentcast/value-estimate/refresh?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

**Response:** Freshly fetched value estimate from API

---

### 7. Force Refresh Rent Estimate
**Endpoint:** `POST /api/rentcast/rent-estimate/refresh`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/rentcast/rent-estimate/refresh?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

**Response:** Freshly fetched rent estimate from API

---

### 8. Clear Expired Cache
**Endpoint:** `POST /api/rentcast/cache/clear-expired`

**Example:**
```bash
curl -X POST "http://localhost:8080/api/rentcast/cache/clear-expired"
```

**Response:**
```json
{
  "success": true,
  "clearedCount": 5,
  "message": "Cleared 5 expired cache entries"
}
```

---

## 💻 Java Service Usage

### Inject the Service
```java
@Autowired
private RentCastService rentCastService;
```

### Get Property Data
```java
// Uses cache if available, otherwise calls API
RentCastProperty property = rentCastService.getPropertyByAddress(
    "5500 Grand Lake Dr, San Antonio, TX 78244"
);

System.out.println("Property ID: " + property.getRentcastId());
System.out.println("Bedrooms: " + property.getBedrooms());
System.out.println("Bathrooms: " + property.getBathrooms());
System.out.println("Square Feet: " + property.getSquareFeet());
```

### Get Value Estimate
```java
RentCastValueEstimate estimate = rentCastService.getValueEstimate(
    "5500 Grand Lake Dr, San Antonio, TX 78244"
);

System.out.println("Estimated Price: $" + estimate.getPrice());
System.out.println("Price Range: $" + estimate.getPriceLow() + " - $" + estimate.getPriceHigh());
System.out.println("Confidence: " + estimate.getConfidenceScore());
```

### Get Rent Estimate
```java
RentCastRentEstimate estimate = rentCastService.getRentEstimate(
    "5500 Grand Lake Dr, San Antonio, TX 78244"
);

System.out.println("Estimated Rent: $" + estimate.getRent());
System.out.println("Rent Range: $" + estimate.getRentLow() + " - $" + estimate.getRentHigh());
System.out.println("Confidence: " + estimate.getConfidenceScore());
```

### Check Cache Only
```java
Optional<RentCastProperty> cached = rentCastService.getPropertyFromCache(
    "5500 Grand Lake Dr, San Antonio, TX 78244"
);

if (cached.isPresent()) {
    System.out.println("Found in cache!");
    RentCastProperty property = cached.get();
    // Use property...
} else {
    System.out.println("Not in cache or expired");
}
```

### Force Refresh
```java
// Bypass cache and fetch fresh data from API
RentCastProperty property = rentCastService.refreshPropertyData(
    "5500 Grand Lake Dr, San Antonio, TX 78244"
);

RentCastValueEstimate valueEstimate = rentCastService.refreshValueEstimate(
    "5500 Grand Lake Dr, San Antonio, TX 78244"
);

RentCastRentEstimate rentEstimate = rentCastService.refreshRentEstimate(
    "5500 Grand Lake Dr, San Antonio, TX 78244"
);
```

### Clear Expired Cache
```java
int clearedCount = rentCastService.clearExpiredCache();
System.out.println("Cleared " + clearedCount + " expired entries");
```

---

## 🔍 Address Normalization Examples

The system automatically normalizes addresses to prevent duplicate API calls:

| Original Address | Normalized Address | Result |
|-----------------|-------------------|--------|
| "3080 Morley Lane" | "3080 morley ln" | ✅ Same |
| "3080 Morley Ln" | "3080 morley ln" | ✅ Same |
| "3080 MORLEY LANE" | "3080 morley ln" | ✅ Same |
| "123 North Main Street" | "123 n main st" | ✅ Same |
| "123 N Main St" | "123 n main st" | ✅ Same |
| "456 Oak Avenue Apt 2B" | "456 oak ave apt 2b" | ✅ Same |
| "456 Oak Ave #2B" | "456 oak ave 2b" | ✅ Same |

**Supported Abbreviations:**
- **Streets:** Street→St, Avenue→Ave, Lane→Ln, Drive→Dr, Road→Rd, Boulevard→Blvd, Court→Ct, Circle→Cir, Way→Way
- **Directions:** North→N, South→S, East→E, West→W, Northeast→NE, Northwest→NW, Southeast→SE, Southwest→SW
- **Units:** Apartment→Apt, Suite→Ste, Unit→Unit, Building→Bldg, Floor→Fl

---

## 🗄️ Database Queries

### Check Cached Properties
```sql
SELECT * FROM RENT_CAST_PROPERTY 
WHERE normalized_address = '5500 grand lake dr san antonio tx 78244';
```

### Check Cache Expiry
```sql
SELECT normalized_address, created_date, cache_expiry_date 
FROM RENT_CAST_PROPERTY 
WHERE cache_expiry_date < CURRENT_TIMESTAMP;
```

### Count Cached Entries
```sql
SELECT 
    (SELECT COUNT(*) FROM RENT_CAST_PROPERTY) as properties,
    (SELECT COUNT(*) FROM RENT_CAST_VALUE_ESTIMATE) as value_estimates,
    (SELECT COUNT(*) FROM RENT_CAST_RENT_ESTIMATE) as rent_estimates;
```

### Find Properties Expiring Soon
```sql
SELECT normalized_address, cache_expiry_date 
FROM RENT_CAST_PROPERTY 
WHERE cache_expiry_date BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '7 days'
ORDER BY cache_expiry_date;
```

---

## 📊 Monitoring

### Check Logs
```bash
# View application logs
tail -f logs/application.log

# Filter RentCast logs
tail -f logs/application.log | grep "RentCast"

# Filter API calls
tail -f logs/application.log | grep "Calling RentCast API"

# Filter cache hits
tail -f logs/application.log | grep "Found in cache"
```

### Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.com.non.rental.service.rentcast=debug
logging.level.com.non.rental.client=debug
logging.level.com.non.rental.job=debug
```

---

## ⏰ Scheduled Jobs

The following jobs run automatically:

| Job | Schedule | Description |
|-----|----------|-------------|
| Clear Expired Cache | Daily at 1:00 AM | Removes expired cache entries |
| Refresh Expiring Properties | Daily at 2:00 AM | Refreshes properties expiring within 7 days |
| Refresh Expiring Value Estimates | Daily at 3:00 AM | Refreshes value estimates expiring within 7 days |
| Refresh Expiring Rent Estimates | Daily at 4:00 AM | Refreshes rent estimates expiring within 7 days |

**Check Job Execution:**
```bash
# View job logs
tail -f logs/application.log | grep "RentCastCacheRefreshJob"
```

---

## 🔧 Configuration

### Current Settings (application.properties)
```properties
# API Configuration
rentcast.api.key=34fb2dfe78fb4d67a8a3fd6446e9fd73
rentcast.api.base-url=https://api.rentcast.io/v1
rentcast.api.timeout=30000
rentcast.api.max-retries=3

# Cache Configuration
rentcast.cache.ttl-days=30
rentcast.cache.enabled=true

# Logging
logging.level.com.non.rental.service.rentcast=debug
```

### Adjust Cache TTL
Change `rentcast.cache.ttl-days` to desired number of days:
```properties
rentcast.cache.ttl-days=7   # 1 week
rentcast.cache.ttl-days=14  # 2 weeks
rentcast.cache.ttl-days=30  # 1 month (default)
rentcast.cache.ttl-days=90  # 3 months
```

### Disable Caching (Not Recommended)
```properties
rentcast.cache.enabled=false
```

---

## 🚨 Error Handling

### Common Errors

#### 1. API Authentication Error
**Error:** `401 Unauthorized`

**Solution:** Check API key in `application.properties`

#### 2. Property Not Found
**Error:** `404 Not Found`

**Solution:** Verify address is correct and exists in RentCast database

#### 3. Rate Limit Exceeded
**Error:** `429 Too Many Requests`

**Solution:** Wait for rate limit reset or upgrade RentCast plan

#### 4. Server Error
**Error:** `500 Internal Server Error`

**Solution:** Automatic retry will be attempted. Check RentCast API status.

### Exception Handling Example
```java
try {
    RentCastProperty property = rentCastService.getPropertyByAddress(address);
    // Use property...
} catch (RentCastApiException e) {
    log.error("RentCast API error: {}", e.getMessage());
    log.error("Status code: {}", e.getStatusCode());
    log.error("Response body: {}", e.getResponseBody());
    // Handle error...
}
```

---

## 🧪 Testing

### Run Integration Tests
```bash
./gradlew test --tests RentCastServiceTest
```

**Note:** Tests are disabled by default. Edit `RentCastServiceTest.java` and remove `@Disabled` annotation to run tests.

### Manual Testing with cURL
```bash
# Test property endpoint
curl -v "http://localhost:8080/api/rentcast/property?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"

# Test with different address format (should use same cache)
curl -v "http://localhost:8080/api/rentcast/property?address=5500%20Grand%20Lake%20Drive,%20San%20Antonio,%20TX%2078244"

# Check cache
curl -v "http://localhost:8080/api/rentcast/property/cached?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"

# Force refresh
curl -v -X POST "http://localhost:8080/api/rentcast/property/refresh?address=5500%20Grand%20Lake%20Dr,%20San%20Antonio,%20TX%2078244"
```

---

## 📦 Project Structure

```
src/main/java/com/non/rental/
├── client/
│   └── RentCastClient.java              # HTTP client for API calls
├── config/
│   ├── CacheConfig.java                 # Spring Cache configuration
│   ├── RentCastProperties.java          # Configuration properties
│   └── SchedulingConfig.java            # Scheduled job configuration
├── controller/
│   └── RentCastController.java          # REST API endpoints
├── dto/
│   ├── RentCastPropertyDto.java         # Property DTO
│   ├── RentCastRentEstimateDto.java     # Rent estimate DTO
│   └── RentCastValueEstimateDto.java    # Value estimate DTO
├── entity/
│   ├── RentCastProperty.java            # Property entity
│   ├── RentCastRentEstimate.java        # Rent estimate entity
│   └── RentCastValueEstimate.java       # Value estimate entity
├── exception/
│   └── RentCastApiException.java        # Custom exception
├── job/
│   └── RentCastCacheRefreshJob.java     # Scheduled jobs
├── service/
│   ├── RentCastService.java             # Service interface
│   └── RentCastServiceImpl.java         # Service implementation
└── util/
    └── AddressNormalizer.java           # Address normalization utility
```

---

## 🎯 Common Use Cases

### Use Case 1: Property Valuation
```java
// Get property data and value estimate
RentCastProperty property = rentCastService.getPropertyByAddress(address);
RentCastValueEstimate estimate = rentCastService.getValueEstimate(address);

System.out.println("Property: " + property.getFormattedAddress());
System.out.println("Estimated Value: $" + estimate.getPrice());
System.out.println("Value Range: $" + estimate.getPriceLow() + " - $" + estimate.getPriceHigh());
```

### Use Case 2: Rental Analysis
```java
// Get property data and rent estimate
RentCastProperty property = rentCastService.getPropertyByAddress(address);
RentCastRentEstimate estimate = rentCastService.getRentEstimate(address);

System.out.println("Property: " + property.getFormattedAddress());
System.out.println("Estimated Rent: $" + estimate.getRent() + "/month");
System.out.println("Rent Range: $" + estimate.getRentLow() + " - $" + estimate.getRentHigh());
```

### Use Case 3: Bulk Property Analysis
```java
List<String> addresses = Arrays.asList(
    "5500 Grand Lake Dr, San Antonio, TX 78244",
    "123 Main St, Austin, TX 78701",
    "456 Oak Ave, Dallas, TX 75201"
);

for (String address : addresses) {
    try {
        RentCastProperty property = rentCastService.getPropertyByAddress(address);
        RentCastValueEstimate value = rentCastService.getValueEstimate(address);
        RentCastRentEstimate rent = rentCastService.getRentEstimate(address);
        
        System.out.println("Address: " + property.getFormattedAddress());
        System.out.println("Value: $" + value.getPrice());
        System.out.println("Rent: $" + rent.getRent());
        System.out.println("---");
    } catch (RentCastApiException e) {
        log.error("Error processing address: " + address, e);
    }
}
```

---

## 📞 Support & Resources

- **Full Documentation:** `RENTCAST_INTEGRATION.md`
- **Implementation Summary:** `RENTCAST_INTEGRATION_SUMMARY.md`
- **RentCast API Docs:** https://developers.rentcast.io/reference/introduction
- **Jmix Documentation:** https://docs.jmix.io/

---

## ✅ Quick Checklist

- [x] Configuration in `application.properties`
- [x] Database tables created via Liquibase
- [x] Spring Cache configured
- [x] Scheduled jobs enabled
- [x] REST endpoints available
- [x] Address normalization working
- [x] Two-level caching implemented
- [x] Error handling in place
- [x] Logging configured

**You're all set! Start using the RentCast integration! 🎉**