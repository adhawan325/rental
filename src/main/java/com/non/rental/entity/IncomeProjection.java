package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing income projections for a rental property analysis.
 * Contains rental income, other income sources, and vacancy calculations.
 */
@JmixEntity
@Table(name = "INCOME_PROJECTION")
@Entity
public class IncomeProjection {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RENTAL_ANALYSIS_ID", nullable = false)
    private RentalAnalysis rentalAnalysis;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "MONTHLY_RENT", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "OTHER_MONTHLY_INCOME", precision = 19, scale = 2)
    private BigDecimal otherMonthlyIncome;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "VACANCY_RATE", nullable = false)
    private Double vacancyRate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    // Helper methods for calculated fields

    /**
     * Calculates the gross monthly income (before vacancy).
     * Gross Income = Monthly Rent + Other Monthly Income
     */
    @Transient
    public BigDecimal getGrossMonthlyIncome() {
        BigDecimal gross = monthlyRent != null ? monthlyRent : BigDecimal.ZERO;
        
        if (otherMonthlyIncome != null) {
            gross = gross.add(otherMonthlyIncome);
        }
        
        return gross;
    }

    /**
     * Calculates the effective monthly income (after accounting for vacancy).
     * Effective Income = Gross Income * (1 - Vacancy Rate)
     */
    @Transient
    public BigDecimal getEffectiveMonthlyIncome() {
        BigDecimal gross = getGrossMonthlyIncome();
        
        if (vacancyRate != null && vacancyRate > 0) {
            double occupancyRate = 1.0 - (vacancyRate / 100.0);
            return gross.multiply(BigDecimal.valueOf(occupancyRate))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        
        return gross;
    }

    /**
     * Calculates the annual effective income.
     */
    @Transient
    public BigDecimal getAnnualEffectiveIncome() {
        return getEffectiveMonthlyIncome().multiply(BigDecimal.valueOf(12));
    }

    /**
     * Calculates the monthly vacancy loss.
     */
    @Transient
    public BigDecimal getMonthlyVacancyLoss() {
        BigDecimal gross = getGrossMonthlyIncome();
        return gross.subtract(getEffectiveMonthlyIncome());
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

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public BigDecimal getOtherMonthlyIncome() {
        return otherMonthlyIncome;
    }

    public void setOtherMonthlyIncome(BigDecimal otherMonthlyIncome) {
        this.otherMonthlyIncome = otherMonthlyIncome;
    }

    public Double getVacancyRate() {
        return vacancyRate;
    }

    public void setVacancyRate(Double vacancyRate) {
        this.vacancyRate = vacancyRate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @InstanceName
    @DependsOnProperties({"monthlyRent"})
    public String getInstanceName() {
        return monthlyRent != null ? "Income: $" + monthlyRent + "/month" : "Income Projection";
    }
}