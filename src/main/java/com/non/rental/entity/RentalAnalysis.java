package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Main entity representing a rental property analysis.
 * Contains all the details needed to calculate rental property yields and metrics.
 */
@JmixEntity
@Table(name = "RENTAL_ANALYSIS", indexes = {
        @Index(name = "IDX_RENTAL_ANALYSIS_USER", columnList = "USER_ID"),
        @Index(name = "IDX_RENTAL_ANALYSIS_PROPERTY_ID", columnList = "PROPERTY_ID"),
        @Index(name = "IDX_RENTAL_ANALYSIS_STATUS", columnList = "STATUS")
})
@Entity
public class RentalAnalysis {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @NotNull
    @Column(name = "PROPERTY_ADDRESS", nullable = false, length = 500)
    private String propertyAddress;

    @Column(name = "PROPERTY_ID", length = 500)
    private String propertyId;

    @InstanceName
    @NotNull
    @Column(name = "ANALYSIS_NAME", nullable = false, length = 255)
    private String analysisName;

    @NotNull
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @NotNull
    @Column(name = "LAST_MODIFIED_DATE", nullable = false)
    private LocalDateTime lastModifiedDate;

    @NotNull
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "rentalAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private PurchaseDetails purchaseDetails;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "rentalAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private FinancingDetails financingDetails;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "rentalAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private IncomeProjection incomeProjection;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "rentalAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private ExpenseProjection expenseProjection;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "rentalAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private CalculatedMetrics calculatedMetrics;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) {
            createdDate = now;
        }
        if (lastModifiedDate == null) {
            lastModifiedDate = now;
        }
        if (status == null) {
            status = AnalysisStatus.DRAFT.getId();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getAnalysisName() {
        return analysisName;
    }

    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public AnalysisStatus getStatus() {
        return status == null ? null : AnalysisStatus.fromId(status);
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public PurchaseDetails getPurchaseDetails() {
        return purchaseDetails;
    }

    public void setPurchaseDetails(PurchaseDetails purchaseDetails) {
        this.purchaseDetails = purchaseDetails;
    }

    public FinancingDetails getFinancingDetails() {
        return financingDetails;
    }

    public void setFinancingDetails(FinancingDetails financingDetails) {
        this.financingDetails = financingDetails;
    }

    public IncomeProjection getIncomeProjection() {
        return incomeProjection;
    }

    public void setIncomeProjection(IncomeProjection incomeProjection) {
        this.incomeProjection = incomeProjection;
    }

    public ExpenseProjection getExpenseProjection() {
        return expenseProjection;
    }

    public void setExpenseProjection(ExpenseProjection expenseProjection) {
        this.expenseProjection = expenseProjection;
    }

    public CalculatedMetrics getCalculatedMetrics() {
        return calculatedMetrics;
    }

    public void setCalculatedMetrics(CalculatedMetrics calculatedMetrics) {
        this.calculatedMetrics = calculatedMetrics;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}