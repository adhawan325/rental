package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing calculated metrics for a rental property analysis.
 * Contains computed values like cash-on-cash return, cap rate, ROI, etc.
 */
@JmixEntity
@Table(name = "CALCULATED_METRICS")
@Entity
public class CalculatedMetrics {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RENTAL_ANALYSIS_ID", nullable = false)
    private RentalAnalysis rentalAnalysis;

    @Column(name = "MONTHLY_CASH_FLOW", precision = 19, scale = 2)
    private BigDecimal monthlyCashFlow;

    @Column(name = "ANNUAL_CASH_FLOW", precision = 19, scale = 2)
    private BigDecimal annualCashFlow;

    @Column(name = "CASH_ON_CASH_RETURN")
    private Double cashOnCashReturn;

    @Column(name = "CAP_RATE")
    private Double capRate;

    @Column(name = "GROSS_YIELD")
    private Double grossYield;

    @Column(name = "NET_YIELD")
    private Double netYield;

    @Column(name = "TOTAL_ROI")
    private Double totalROI;

    @Column(name = "DEBT_SERVICE_COVERAGE_RATIO")
    private Double debtServiceCoverageRatio;

    @Column(name = "BREAK_EVEN_RATIO")
    private Double breakEvenRatio;

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

    public RentalAnalysis getRentalAnalysis() {
        return rentalAnalysis;
    }

    public void setRentalAnalysis(RentalAnalysis rentalAnalysis) {
        this.rentalAnalysis = rentalAnalysis;
    }

    public BigDecimal getMonthlyCashFlow() {
        return monthlyCashFlow;
    }

    public void setMonthlyCashFlow(BigDecimal monthlyCashFlow) {
        this.monthlyCashFlow = monthlyCashFlow;
    }

    public BigDecimal getAnnualCashFlow() {
        return annualCashFlow;
    }

    public void setAnnualCashFlow(BigDecimal annualCashFlow) {
        this.annualCashFlow = annualCashFlow;
    }

    public Double getCashOnCashReturn() {
        return cashOnCashReturn;
    }

    public void setCashOnCashReturn(Double cashOnCashReturn) {
        this.cashOnCashReturn = cashOnCashReturn;
    }

    public Double getCapRate() {
        return capRate;
    }

    public void setCapRate(Double capRate) {
        this.capRate = capRate;
    }

    public Double getGrossYield() {
        return grossYield;
    }

    public void setGrossYield(Double grossYield) {
        this.grossYield = grossYield;
    }

    public Double getNetYield() {
        return netYield;
    }

    public void setNetYield(Double netYield) {
        this.netYield = netYield;
    }

    public Double getTotalROI() {
        return totalROI;
    }

    public void setTotalROI(Double totalROI) {
        this.totalROI = totalROI;
    }

    public Double getDebtServiceCoverageRatio() {
        return debtServiceCoverageRatio;
    }

    public void setDebtServiceCoverageRatio(Double debtServiceCoverageRatio) {
        this.debtServiceCoverageRatio = debtServiceCoverageRatio;
    }

    public Double getBreakEvenRatio() {
        return breakEvenRatio;
    }

    public void setBreakEvenRatio(Double breakEvenRatio) {
        this.breakEvenRatio = breakEvenRatio;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @InstanceName
    @DependsOnProperties({"cashOnCashReturn", "capRate"})
    public String getInstanceName() {
        if (cashOnCashReturn != null && capRate != null) {
            return String.format("Metrics: CoC %.2f%%, Cap %.2f%%", cashOnCashReturn, capRate);
        } else if (cashOnCashReturn != null) {
            return String.format("Metrics: CoC %.2f%%", cashOnCashReturn);
        } else if (capRate != null) {
            return String.format("Metrics: Cap %.2f%%", capRate);
        }
        return "Calculated Metrics";
    }
}