package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a property value estimate from RentCast AVM API.
 * Stores estimated property values and comparable sales data.
 */
@JmixEntity
@Table(name = "RENTCAST_VALUE_ESTIMATE", indexes = {
        @Index(name = "IDX_RENTCAST_VALUE_ESTIMATE_PROPERTY", columnList = "PROPERTY_ID"),
        @Index(name = "IDX_RENTCAST_VALUE_ESTIMATE_ADDRESS", columnList = "NORMALIZED_ADDRESS")
})
@Entity
public class RentCastValueEstimate {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "PROPERTY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private RentCastProperty property;

    @NotNull
    @Column(name = "NORMALIZED_ADDRESS", nullable = false, length = 500)
    private String normalizedAddress;

    @InstanceName
    @Column(name = "FORMATTED_ADDRESS", length = 500)
    private String formattedAddress;

    @Column(name = "PRICE", precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "PRICE_LOW", precision = 19, scale = 2)
    private BigDecimal priceLow;

    @Column(name = "PRICE_HIGH", precision = 19, scale = 2)
    private BigDecimal priceHigh;

    @Column(name = "CONFIDENCE_SCORE", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Lob
    @Column(name = "COMPARABLES_JSON")
    private String comparablesJson;

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

    public RentCastProperty getProperty() {
        return property;
    }

    public void setProperty(RentCastProperty property) {
        this.property = property;
    }

    public String getNormalizedAddress() {
        return normalizedAddress;
    }

    public void setNormalizedAddress(String normalizedAddress) {
        this.normalizedAddress = normalizedAddress;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPriceLow() {
        return priceLow;
    }

    public void setPriceLow(BigDecimal priceLow) {
        this.priceLow = priceLow;
    }

    public BigDecimal getPriceHigh() {
        return priceHigh;
    }

    public void setPriceHigh(BigDecimal priceHigh) {
        this.priceHigh = priceHigh;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getComparablesJson() {
        return comparablesJson;
    }

    public void setComparablesJson(String comparablesJson) {
        this.comparablesJson = comparablesJson;
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