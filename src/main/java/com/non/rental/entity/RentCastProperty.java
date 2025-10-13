package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a property from RentCast API.
 * Stores property data including location, attributes, and owner information.
 */
@JmixEntity
@Table(name = "RENTCAST_PROPERTY", indexes = {
        @Index(name = "IDX_RENTCAST_PROPERTY_NORMALIZED_ADDRESS", columnList = "NORMALIZED_ADDRESS"),
        @Index(name = "IDX_RENTCAST_PROPERTY_RENTCAST_ID", columnList = "RENTCAST_ID")
})
@Entity
public class RentCastProperty {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "RENTCAST_ID", length = 500)
    private String rentcastId;

    @InstanceName
    @Column(name = "FORMATTED_ADDRESS", length = 500)
    private String formattedAddress;

    @NotNull
    @Column(name = "NORMALIZED_ADDRESS", nullable = false, length = 500)
    private String normalizedAddress;

    @Column(name = "ADDRESS_LINE1", length = 255)
    private String addressLine1;

    @Column(name = "ADDRESS_LINE2", length = 255)
    private String addressLine2;

    @Column(name = "CITY", length = 100)
    private String city;

    @Column(name = "STATE", length = 2)
    private String state;

    @Column(name = "STATE_FIPS", length = 2)
    private String stateFips;

    @Column(name = "ZIP_CODE", length = 10)
    private String zipCode;

    @Column(name = "COUNTY", length = 100)
    private String county;

    @Column(name = "COUNTY_FIPS", length = 3)
    private String countyFips;

    @Column(name = "LATITUDE", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "PROPERTY_TYPE", length = 50)
    private String propertyType;

    @Column(name = "BEDROOMS")
    private Integer bedrooms;

    @Column(name = "BATHROOMS")
    private Integer bathrooms;

    @Column(name = "SQUARE_FOOTAGE")
    private Integer squareFootage;

    @Column(name = "LOT_SIZE")
    private Integer lotSize;

    @Column(name = "YEAR_BUILT")
    private Integer yearBuilt;

    @Column(name = "ASSESSOR_ID", length = 100)
    private String assessorId;

    @Column(name = "LEGAL_DESCRIPTION", length = 500)
    private String legalDescription;

    @Column(name = "SUBDIVISION", length = 255)
    private String subdivision;

    @Column(name = "ZONING", length = 50)
    private String zoning;

    @Column(name = "LAST_SALE_DATE")
    private LocalDate lastSaleDate;

    @Column(name = "LAST_SALE_PRICE", precision = 19, scale = 2)
    private BigDecimal lastSalePrice;

    @Column(name = "HOA_FEE", precision = 19, scale = 2)
    private BigDecimal hoaFee;

    @Column(name = "OWNER_OCCUPIED")
    private Boolean ownerOccupied;

    @Column(name = "OWNER_NAMES", length = 500)
    private String ownerNames;

    @Column(name = "OWNER_TYPE", length = 50)
    private String ownerType;

    @Column(name = "OWNER_MAILING_ADDRESS", length = 500)
    private String ownerMailingAddress;

    @Lob
    @Column(name = "FEATURES_JSON")
    private String featuresJson;

    @Lob
    @Column(name = "TAX_ASSESSMENTS_JSON")
    private String taxAssessmentsJson;

    @Lob
    @Column(name = "PROPERTY_TAXES_JSON")
    private String propertyTaxesJson;

    @Lob
    @Column(name = "HISTORY_JSON")
    private String historyJson;

    @Lob
    @Column(name = "RAW_API_RESPONSE")
    private String rawApiResponse;

    @NotNull
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @NotNull
    @Column(name = "LAST_UPDATED_DATE", nullable = false)
    private LocalDateTime lastUpdatedDate;

    @Column(name = "CACHE_EXPIRY_DATE")
    private LocalDateTime cacheExpiryDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRentcastId() {
        return rentcastId;
    }

    public void setRentcastId(String rentcastId) {
        this.rentcastId = rentcastId;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public String getNormalizedAddress() {
        return normalizedAddress;
    }

    public void setNormalizedAddress(String normalizedAddress) {
        this.normalizedAddress = normalizedAddress;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateFips() {
        return stateFips;
    }

    public void setStateFips(String stateFips) {
        this.stateFips = stateFips;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCountyFips() {
        return countyFips;
    }

    public void setCountyFips(String countyFips) {
        this.countyFips = countyFips;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getSquareFootage() {
        return squareFootage;
    }

    public void setSquareFootage(Integer squareFootage) {
        this.squareFootage = squareFootage;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public Integer getYearBuilt() {
        return yearBuilt;
    }

    public void setYearBuilt(Integer yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    public String getAssessorId() {
        return assessorId;
    }

    public void setAssessorId(String assessorId) {
        this.assessorId = assessorId;
    }

    public String getLegalDescription() {
        return legalDescription;
    }

    public void setLegalDescription(String legalDescription) {
        this.legalDescription = legalDescription;
    }

    public String getSubdivision() {
        return subdivision;
    }

    public void setSubdivision(String subdivision) {
        this.subdivision = subdivision;
    }

    public String getZoning() {
        return zoning;
    }

    public void setZoning(String zoning) {
        this.zoning = zoning;
    }

    public LocalDate getLastSaleDate() {
        return lastSaleDate;
    }

    public void setLastSaleDate(LocalDate lastSaleDate) {
        this.lastSaleDate = lastSaleDate;
    }

    public BigDecimal getLastSalePrice() {
        return lastSalePrice;
    }

    public void setLastSalePrice(BigDecimal lastSalePrice) {
        this.lastSalePrice = lastSalePrice;
    }

    public BigDecimal getHoaFee() {
        return hoaFee;
    }

    public void setHoaFee(BigDecimal hoaFee) {
        this.hoaFee = hoaFee;
    }

    public Boolean getOwnerOccupied() {
        return ownerOccupied;
    }

    public void setOwnerOccupied(Boolean ownerOccupied) {
        this.ownerOccupied = ownerOccupied;
    }

    public String getOwnerNames() {
        return ownerNames;
    }

    public void setOwnerNames(String ownerNames) {
        this.ownerNames = ownerNames;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerMailingAddress() {
        return ownerMailingAddress;
    }

    public void setOwnerMailingAddress(String ownerMailingAddress) {
        this.ownerMailingAddress = ownerMailingAddress;
    }

    public String getFeaturesJson() {
        return featuresJson;
    }

    public void setFeaturesJson(String featuresJson) {
        this.featuresJson = featuresJson;
    }

    public String getTaxAssessmentsJson() {
        return taxAssessmentsJson;
    }

    public void setTaxAssessmentsJson(String taxAssessmentsJson) {
        this.taxAssessmentsJson = taxAssessmentsJson;
    }

    public String getPropertyTaxesJson() {
        return propertyTaxesJson;
    }

    public void setPropertyTaxesJson(String propertyTaxesJson) {
        this.propertyTaxesJson = propertyTaxesJson;
    }

    public String getHistoryJson() {
        return historyJson;
    }

    public void setHistoryJson(String historyJson) {
        this.historyJson = historyJson;
    }

    public String getRawApiResponse() {
        return rawApiResponse;
    }

    public void setRawApiResponse(String rawApiResponse) {
        this.rawApiResponse = rawApiResponse;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public LocalDateTime getCacheExpiryDate() {
        return cacheExpiryDate;
    }

    public void setCacheExpiryDate(LocalDateTime cacheExpiryDate) {
        this.cacheExpiryDate = cacheExpiryDate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
        lastUpdatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedDate = LocalDateTime.now();
    }
}