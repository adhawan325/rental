package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing financing details for a rental property analysis.
 * Contains loan information and calculates monthly payments.
 */
@JmixEntity
@Table(name = "FINANCING_DETAILS")
@Entity
public class FinancingDetails {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RENTAL_ANALYSIS_ID", nullable = false)
    private RentalAnalysis rentalAnalysis;

    @Column(name = "LOAN_AMOUNT", precision = 19, scale = 2)
    private BigDecimal loanAmount;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "INTEREST_RATE", nullable = false)
    private Double interestRate;

    @NotNull
    @Min(value = 1)
    @Column(name = "LOAN_TERM_YEARS", nullable = false)
    private Integer loanTermYears;

    @DecimalMin(value = "0.0")
    @Column(name = "POINTS_PAID")
    private Double pointsPaid;

    @Column(name = "MONTHLY_PAYMENT", precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    // Helper methods for calculated fields

    /**
     * Calculates the loan amount based on purchase price and down payment.
     * Loan Amount = Purchase Price - Down Payment
     */
    public void calculateLoanAmount(BigDecimal purchasePrice, BigDecimal downPaymentAmount) {
        if (purchasePrice != null && downPaymentAmount != null) {
            loanAmount = purchasePrice.subtract(downPaymentAmount);
        } else if (purchasePrice != null) {
            loanAmount = purchasePrice;
        }
    }

    /**
     * Calculates the monthly mortgage payment using the standard amortization formula.
     * M = P * [r(1+r)^n] / [(1+r)^n - 1]
     * where:
     * M = Monthly payment
     * P = Principal (loan amount)
     * r = Monthly interest rate (annual rate / 12)
     * n = Number of payments (years * 12)
     */
    public void calculateMonthlyPayment() {
        if (loanAmount == null || interestRate == null || loanTermYears == null) {
            monthlyPayment = null;
            return;
        }

        if (loanAmount.compareTo(BigDecimal.ZERO) == 0) {
            monthlyPayment = BigDecimal.ZERO;
            return;
        }

        if (interestRate == 0.0) {
            // No interest - simple division
            int totalMonths = loanTermYears * 12;
            monthlyPayment = loanAmount.divide(BigDecimal.valueOf(totalMonths), 2, BigDecimal.ROUND_HALF_UP);
            return;
        }

        // Convert annual interest rate to monthly decimal rate
        double monthlyRate = interestRate / 100.0 / 12.0;
        int numberOfPayments = loanTermYears * 12;

        // Calculate (1 + r)^n
        double onePlusRatePowerN = Math.pow(1 + monthlyRate, numberOfPayments);

        // Calculate monthly payment
        double payment = loanAmount.doubleValue() * 
                (monthlyRate * onePlusRatePowerN) / 
                (onePlusRatePowerN - 1);

        monthlyPayment = BigDecimal.valueOf(payment).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculates the total cost of points paid.
     * Points are typically a percentage of the loan amount.
     */
    @Transient
    public BigDecimal getPointsCost() {
        if (pointsPaid != null && loanAmount != null) {
            return loanAmount.multiply(BigDecimal.valueOf(pointsPaid / 100.0));
        }
        return BigDecimal.ZERO;
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

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getLoanTermYears() {
        return loanTermYears;
    }

    public void setLoanTermYears(Integer loanTermYears) {
        this.loanTermYears = loanTermYears;
    }

    public Double getPointsPaid() {
        return pointsPaid;
    }

    public void setPointsPaid(Double pointsPaid) {
        this.pointsPaid = pointsPaid;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(BigDecimal monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @InstanceName
    @DependsOnProperties({"loanAmount", "interestRate", "loanTermYears"})
    public String getInstanceName() {
        if (loanAmount != null && interestRate != null && loanTermYears != null) {
            return String.format("Loan: $%s @ %.2f%% for %d years", 
                    loanAmount, interestRate, loanTermYears);
        }
        return "Financing Details";
    }
}