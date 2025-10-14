package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing purchase details for a rental property analysis.
 * Contains information about purchase price, down payment, closing costs, and renovation costs.
 */
@JmixEntity
@Table(name = "PURCHASE_DETAILS")
@Entity
public class PurchaseDetails {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RENTAL_ANALYSIS_ID", nullable = false)
    private RentalAnalysis rentalAnalysis;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "PURCHASE_PRICE", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "DOWN_PAYMENT_AMOUNT", precision = 19, scale = 2)
    private BigDecimal downPaymentAmount;

    @DecimalMin(value = "0.0")
    @Column(name = "DOWN_PAYMENT_PERCENT")
    private Double downPaymentPercent;

    @Column(name = "CLOSING_COSTS", precision = 19, scale = 2)
    private BigDecimal closingCosts;

    @DecimalMin(value = "0.0")
    @Column(name = "CLOSING_COSTS_PERCENT")
    private Double closingCostsPercent;

    @Column(name = "RENOVATION_COSTS", precision = 19, scale = 2)
    private BigDecimal renovationCosts;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    // Helper methods for calculated fields

    /**
     * Calculates the total cash invested in the property.
     * Total = Down Payment + Closing Costs + Renovation Costs
     */
    @Transient
    public BigDecimal getTotalCashInvested() {
        BigDecimal total = BigDecimal.ZERO;
        
        if (downPaymentAmount != null) {
            total = total.add(downPaymentAmount);
        }
        
        if (closingCosts != null) {
            total = total.add(closingCosts);
        }
        
        if (renovationCosts != null) {
            total = total.add(renovationCosts);
        }
        
        return total;
    }

    /**
     * Calculates down payment amount from percentage if not directly specified.
     */
    public void calculateDownPaymentAmount() {
        if (downPaymentAmount == null && downPaymentPercent != null && purchasePrice != null) {
            downPaymentAmount = purchasePrice.multiply(BigDecimal.valueOf(downPaymentPercent / 100.0));
        }
    }

    /**
     * Calculates down payment percentage from amount if not directly specified.
     */
    public void calculateDownPaymentPercent() {
        if (downPaymentPercent == null && downPaymentAmount != null && purchasePrice != null 
                && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            downPaymentPercent = downPaymentAmount.divide(purchasePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0)).doubleValue();
        }
    }

    /**
     * Calculates closing costs from percentage if not directly specified.
     */
    public void calculateClosingCosts() {
        if (closingCosts == null && closingCostsPercent != null && purchasePrice != null) {
            closingCosts = purchasePrice.multiply(BigDecimal.valueOf(closingCostsPercent / 100.0));
        }
    }

    /**
     * Calculates closing costs percentage from amount if not directly specified.
     */
    public void calculateClosingCostsPercent() {
        if (closingCostsPercent == null && closingCosts != null && purchasePrice != null 
                && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            closingCostsPercent = closingCosts.divide(purchasePrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0)).doubleValue();
        }
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RentalAnalysis getRentalAnalysis() {
        return rentalAnalysis;
    }

    public void setRentalAnalysis(RentalAnalysis rentalAnalysis) {
        this.rentalAnalysis = rentalAnalysis;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getDownPaymentAmount() {
        return downPaymentAmount;
    }

    public void setDownPaymentAmount(BigDecimal downPaymentAmount) {
        this.downPaymentAmount = downPaymentAmount;
    }

    public Double getDownPaymentPercent() {
        return downPaymentPercent;
    }

    public void setDownPaymentPercent(Double downPaymentPercent) {
        this.downPaymentPercent = downPaymentPercent;
    }

    public BigDecimal getClosingCosts() {
        return closingCosts;
    }

    public void setClosingCosts(BigDecimal closingCosts) {
        this.closingCosts = closingCosts;
    }

    public Double getClosingCostsPercent() {
        return closingCostsPercent;
    }

    public void setClosingCostsPercent(Double closingCostsPercent) {
        this.closingCostsPercent = closingCostsPercent;
    }

    public BigDecimal getRenovationCosts() {
        return renovationCosts;
    }

    public void setRenovationCosts(BigDecimal renovationCosts) {
        this.renovationCosts = renovationCosts;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @InstanceName
    @DependsOnProperties({"purchasePrice"})
    public String getInstanceName() {
        return purchasePrice != null ? "Purchase: $" + purchasePrice : "Purchase Details";
    }
}