package com.non.rental.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.non.rental.client.RentCastClient;
import com.non.rental.config.RentCastProperties;
import com.non.rental.dto.RentCastPropertyDto;
import com.non.rental.dto.RentCastRentEstimateDto;
import com.non.rental.dto.RentCastValueEstimateDto;
import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import com.non.rental.exception.RentCastApiException;
import com.non.rental.util.AddressNormalizer;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.non.rental.config.CacheConfig.*;

/**
 * Implementation of RentCastService.
 * Handles API integration, caching, and data persistence.
 */
@Service
public class RentCastServiceImpl implements RentCastService {

    private static final Logger log = LoggerFactory.getLogger(RentCastServiceImpl.class);

    private final RentCastClient rentCastClient;
    private final DataManager dataManager;
    private final AddressNormalizer addressNormalizer;
    private final RentCastProperties properties;
    private final ObjectMapper objectMapper;

    public RentCastServiceImpl(
            RentCastClient rentCastClient,
            DataManager dataManager,
            AddressNormalizer addressNormalizer,
            RentCastProperties properties,
            ObjectMapper objectMapper) {
        this.rentCastClient = rentCastClient;
        this.dataManager = dataManager;
        this.addressNormalizer = addressNormalizer;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    @Cacheable(value = RENTCAST_PROPERTY_CACHE, key = "#address", unless = "!#result")
    public RentCastProperty getPropertyByAddress(String address) {
        log.info("Getting property data for address: {}", address);

        String normalizedAddress = addressNormalizer.normalize(address);

        // Check database cache first
        Optional<RentCastProperty> cached = findPropertyByNormalizedAddress(normalizedAddress);
        if (cached.isPresent() && !isCacheExpired(cached.get().getCacheExpiryDate())) {
            log.debug("Returning cached property data");
            return cached.get();
        }

        // Fetch from API
        log.debug("Fetching property data from API");
        RentCastPropertyDto dto = rentCastClient.getPropertyByAddress(address);

        // Save to database
        RentCastProperty property = cached.orElse(dataManager.create(RentCastProperty.class));
        mapDtoToEntity(dto, property, normalizedAddress);
        property.setCacheExpiryDate(calculateCacheExpiry());

        return dataManager.save(property);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentCastProperty> getPropertyFromCache(String address) {
        String normalizedAddress = addressNormalizer.normalize(address);
        Optional<RentCastProperty> cached = findPropertyByNormalizedAddress(normalizedAddress);

        if (cached.isPresent() && !isCacheExpired(cached.get().getCacheExpiryDate())) {
            return cached;
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    @Cacheable(value = RENTCAST_VALUE_ESTIMATE_CACHE, key = "#address", unless = "!#result")
    public RentCastValueEstimate getValueEstimate(String address) {
        log.info("Getting value estimate for address: {}", address);

        String normalizedAddress = addressNormalizer.normalize(address);

        // Check database cache first
        Optional<RentCastValueEstimate> cached = findValueEstimateByNormalizedAddress(normalizedAddress);
        if (cached.isPresent() && !isCacheExpired(cached.get().getCacheExpiryDate())) {
            log.debug("Returning cached value estimate");
            return cached.get();
        }

        // Fetch from API
        log.debug("Fetching value estimate from API");
        RentCastValueEstimateDto dto = rentCastClient.getValueEstimate(address);

        // Save to database
        RentCastValueEstimate estimate = cached.orElse(dataManager.create(RentCastValueEstimate.class));
        mapValueEstimateDtoToEntity(dto, estimate, address, normalizedAddress);
        estimate.setCacheExpiryDate(calculateCacheExpiry());

        return dataManager.save(estimate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentCastValueEstimate> getValueEstimateFromCache(String address) {
        String normalizedAddress = addressNormalizer.normalize(address);
        Optional<RentCastValueEstimate> cached = findValueEstimateByNormalizedAddress(normalizedAddress);

        if (cached.isPresent() && !isCacheExpired(cached.get().getCacheExpiryDate())) {
            return cached;
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    @Cacheable(value = RENTCAST_RENT_ESTIMATE_CACHE, key = "#address", unless = "!#result")
    public RentCastRentEstimate getRentEstimate(String address) {
        log.info("Getting rent estimate for address: {}", address);

        String normalizedAddress = addressNormalizer.normalize(address);

        // Check database cache first
        Optional<RentCastRentEstimate> cached = findRentEstimateByNormalizedAddress(normalizedAddress);
        if (cached.isPresent() && !isCacheExpired(cached.get().getCacheExpiryDate())) {
            log.debug("Returning cached rent estimate");
            return cached.get();
        }

        // Fetch from API
        log.debug("Fetching rent estimate from API");
        RentCastRentEstimateDto dto = rentCastClient.getRentEstimate(address);

        // Save to database
        RentCastRentEstimate estimate = cached.orElse(dataManager.create(RentCastRentEstimate.class));
        mapRentEstimateDtoToEntity(dto, estimate, address, normalizedAddress);
        estimate.setCacheExpiryDate(calculateCacheExpiry());

        return dataManager.save(estimate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentCastRentEstimate> getRentEstimateFromCache(String address) {
        String normalizedAddress = addressNormalizer.normalize(address);
        Optional<RentCastRentEstimate> cached = findRentEstimateByNormalizedAddress(normalizedAddress);

        if (cached.isPresent() && !isCacheExpired(cached.get().getCacheExpiryDate())) {
            return cached;
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    @CacheEvict(value = RENTCAST_PROPERTY_CACHE, key = "#address")
    public RentCastProperty refreshPropertyData(String address) {
        log.info("Forcing refresh of property data for address: {}", address);

        String normalizedAddress = addressNormalizer.normalize(address);

        // Fetch from API
        RentCastPropertyDto dto = rentCastClient.getPropertyByAddress(address);

        // Find existing or create new
        Optional<RentCastProperty> existing = findPropertyByNormalizedAddress(normalizedAddress);
        RentCastProperty property = existing.orElse(dataManager.create(RentCastProperty.class));

        mapDtoToEntity(dto, property, normalizedAddress);
        property.setCacheExpiryDate(calculateCacheExpiry());

        return dataManager.save(property);
    }

    @Override
    @Transactional
    @CacheEvict(value = RENTCAST_VALUE_ESTIMATE_CACHE, key = "#address")
    public RentCastValueEstimate refreshValueEstimate(String address) {
        log.info("Forcing refresh of value estimate for address: {}", address);

        String normalizedAddress = addressNormalizer.normalize(address);

        // Fetch from API
        RentCastValueEstimateDto dto = rentCastClient.getValueEstimate(address);

        // Find existing or create new
        Optional<RentCastValueEstimate> existing = findValueEstimateByNormalizedAddress(normalizedAddress);
        RentCastValueEstimate estimate = existing.orElse(dataManager.create(RentCastValueEstimate.class));

        mapValueEstimateDtoToEntity(dto, estimate, address, normalizedAddress);
        estimate.setCacheExpiryDate(calculateCacheExpiry());

        return dataManager.save(estimate);
    }

    @Override
    @Transactional
    @CacheEvict(value = RENTCAST_RENT_ESTIMATE_CACHE, key = "#address")
    public RentCastRentEstimate refreshRentEstimate(String address) {
        log.info("Forcing refresh of rent estimate for address: {}", address);

        String normalizedAddress = addressNormalizer.normalize(address);

        // Fetch from API
        RentCastRentEstimateDto dto = rentCastClient.getRentEstimate(address);

        // Find existing or create new
        Optional<RentCastRentEstimate> existing = findRentEstimateByNormalizedAddress(normalizedAddress);
        RentCastRentEstimate estimate = existing.orElse(dataManager.create(RentCastRentEstimate.class));

        mapRentEstimateDtoToEntity(dto, estimate, address, normalizedAddress);
        estimate.setCacheExpiryDate(calculateCacheExpiry());

        return dataManager.save(estimate);
    }

    @Override
    @Transactional
    public int clearExpiredCache() {
        log.info("Clearing expired cache entries");

        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        // Clear expired properties
        List<RentCastProperty> expiredProperties = dataManager.load(RentCastProperty.class)
                .query("select p from RentCastProperty p where p.cacheExpiryDate < :now")
                .parameter("now", now)
                .list();
        count += expiredProperties.size();
        expiredProperties.forEach(dataManager::remove);

        // Clear expired value estimates
        List<RentCastValueEstimate> expiredValueEstimates = dataManager.load(RentCastValueEstimate.class)
                .query("select v from RentCastValueEstimate v where v.cacheExpiryDate < :now")
                .parameter("now", now)
                .list();
        count += expiredValueEstimates.size();
        expiredValueEstimates.forEach(dataManager::remove);

        // Clear expired rent estimates
        List<RentCastRentEstimate> expiredRentEstimates = dataManager.load(RentCastRentEstimate.class)
                .query("select r from RentCastRentEstimate r where r.cacheExpiryDate < :now")
                .parameter("now", now)
                .list();
        count += expiredRentEstimates.size();
        expiredRentEstimates.forEach(dataManager::remove);

        log.info("Cleared {} expired cache entries", count);
        return count;
    }

    // Helper methods

    private Optional<RentCastProperty> findPropertyByNormalizedAddress(String normalizedAddress) {
        return dataManager.load(RentCastProperty.class)
                .query("select p from RentCastProperty p where p.normalizedAddress = :address")
                .parameter("address", normalizedAddress)
                .optional();
    }

    private Optional<RentCastValueEstimate> findValueEstimateByNormalizedAddress(String normalizedAddress) {
        return dataManager.load(RentCastValueEstimate.class)
                .query("select v from RentCastValueEstimate v where v.normalizedAddress = :address")
                .parameter("address", normalizedAddress)
                .optional();
    }

    private Optional<RentCastRentEstimate> findRentEstimateByNormalizedAddress(String normalizedAddress) {
        return dataManager.load(RentCastRentEstimate.class)
                .query("select r from RentCastRentEstimate r where r.normalizedAddress = :address")
                .parameter("address", normalizedAddress)
                .optional();
    }

    private boolean isCacheExpired(LocalDateTime cacheExpiryDate) {
        if (cacheExpiryDate == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(cacheExpiryDate);
    }

    private LocalDateTime calculateCacheExpiry() {
        return LocalDateTime.now().plusDays(properties.getCache().getTtlDays());
    }

    private void mapDtoToEntity(RentCastPropertyDto dto, RentCastProperty entity, String normalizedAddress) {
        entity.setRentcastId(dto.getId());
        entity.setFormattedAddress(dto.getFormattedAddress());
        entity.setNormalizedAddress(normalizedAddress);
        entity.setAddressLine1(dto.getAddressLine1());
        entity.setAddressLine2(dto.getAddressLine2());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setStateFips(dto.getStateFips());
        entity.setZipCode(dto.getZipCode());
        entity.setCounty(dto.getCounty());
        entity.setCountyFips(dto.getCountyFips());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setPropertyType(dto.getPropertyType());
        entity.setBedrooms(dto.getBedrooms());
        entity.setBathrooms(dto.getBathrooms());
        entity.setSquareFootage(dto.getSquareFootage());
        entity.setLotSize(dto.getLotSize());
        entity.setYearBuilt(dto.getYearBuilt());
        entity.setAssessorId(dto.getAssessorID());
        entity.setLegalDescription(dto.getLegalDescription());
        entity.setSubdivision(dto.getSubdivision());
        entity.setZoning(dto.getZoning());
        entity.setLastSaleDate(dto.getLastSaleDate());
        entity.setLastSalePrice(dto.getLastSalePrice());
        entity.setOwnerOccupied(dto.getOwnerOccupied());

        // HOA
        if (dto.getHoa() != null) {
            entity.setHoaFee(dto.getHoa().getFee());
        }

        // Owner
        if (dto.getOwner() != null) {
            entity.setOwnerType(dto.getOwner().getType());
            if (dto.getOwner().getNames() != null) {
                entity.setOwnerNames(String.join(", ", dto.getOwner().getNames()));
            }
            if (dto.getOwner().getMailingAddress() != null) {
                entity.setOwnerMailingAddress(dto.getOwner().getMailingAddress().getFormattedAddress());
            }
        }

        // Store complex objects as JSON
        try {
            if (dto.getFeatures() != null) {
                entity.setFeaturesJson(objectMapper.writeValueAsString(dto.getFeatures()));
            }
            if (dto.getTaxAssessments() != null) {
                entity.setTaxAssessmentsJson(objectMapper.writeValueAsString(dto.getTaxAssessments()));
            }
            if (dto.getPropertyTaxes() != null) {
                entity.setPropertyTaxesJson(objectMapper.writeValueAsString(dto.getPropertyTaxes()));
            }
            if (dto.getHistory() != null) {
                entity.setHistoryJson(objectMapper.writeValueAsString(dto.getHistory()));
            }
            entity.setRawApiResponse(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JSON fields", e);
            throw new RentCastApiException("Failed to serialize property data", e);
        }
    }

    private void mapValueEstimateDtoToEntity(
            RentCastValueEstimateDto dto,
            RentCastValueEstimate entity,
            String address,
            String normalizedAddress) {

        entity.setFormattedAddress(address);
        entity.setNormalizedAddress(normalizedAddress);
        entity.setPrice(dto.getPrice());
        entity.setPriceLow(dto.getPriceRangeLow());
        entity.setPriceHigh(dto.getPriceRangeHigh());
        entity.setConfidenceScore(dto.getConfidenceScore());

        try {
            if (dto.getComparables() != null) {
                entity.setComparablesJson(objectMapper.writeValueAsString(dto.getComparables()));
            }
            entity.setRawApiResponse(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JSON fields", e);
            throw new RentCastApiException("Failed to serialize value estimate data", e);
        }
    }

    private void mapRentEstimateDtoToEntity(
            RentCastRentEstimateDto dto,
            RentCastRentEstimate entity,
            String address,
            String normalizedAddress) {

        entity.setFormattedAddress(address);
        entity.setNormalizedAddress(normalizedAddress);
        entity.setRent(dto.getRent());
        entity.setRentLow(dto.getRentRangeLow());
        entity.setRentHigh(dto.getRentRangeHigh());
        entity.setConfidenceScore(dto.getConfidenceScore());

        try {
            if (dto.getComparables() != null) {
                entity.setComparablesJson(objectMapper.writeValueAsString(dto.getComparables()));
            }
            entity.setRawApiResponse(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JSON fields", e);
            throw new RentCastApiException("Failed to serialize rent estimate data", e);
        }
    }
}