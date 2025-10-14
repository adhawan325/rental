package com.non.rental.service.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Data transfer object representing projected equity buildup for a single year.
 * Tracks how equity grows through mortgage principal paydown and property appreciation.
 * 
 * <p>This class is immutable and thread-safe.</p>
 * 
 * @author Rental Analysis System
 * @version 1.0
 */
public class EquityProjection {

    /**
     * The projection year (1 = first year, 2 = second year, etc.)
     */
    private final Integer year;

    /**
     * Total equity in the property at the end of this year
     * Equity = Property Value - Remaining Loan Balance
     */
    private final BigDecimal totalEquity;

    /**
     * Cumulative principal paid on the mortgage through this year
     * This represents forced savings through mortgage payments
     */
    private final BigDecimal principalPaid;

    /**
     * Cumulative property appreciation through this year
     * Based on the assumed annual appreciation rate
     */
    private final BigDecimal appreciation;

    /**
     * Projected property value at the end of this year
     * Original Purchase Price + Cumulative Appreciation
     */
    private final BigDecimal propertyValue;

    /**
     * Remaining loan balance at the end of this year
     * Original Loan Amount - Principal Paid
     */
    private final BigDecimal remainingLoanBalance;

    /**
     * Loan-to-value ratio at the end of this year
     * LTV = (Remaining Loan Balance / Property Value) × 100
     */
    private final Double loanToValueRatio;

    /**
     * Creates a new equity projection with all metrics.
     * 
     * @param year the projection year (must be positive)
     * @param totalEquity total equity at year end
     * @param principalPaid cumulative principal paid through this year
     * @param appreciation cumulative appreciation through this year
     * @param propertyValue property value at year end
     * @param remainingLoanBalance remaining loan balance at year end
     * @param loanToValueRatio LTV ratio at year end (as percentage)
     * @throws IllegalArgumentException if year is null or less than 1
     */
    public EquityProjection(Integer year, BigDecimal totalEquity, BigDecimal principalPaid,
                           BigDecimal appreciation, BigDecimal propertyValue,
                           BigDecimal remainingLoanBalance, Double loanToValueRatio) {
        if (year == null || year < 1) {
            throw new IllegalArgumentException("Year must be a positive integer");
        }
        this.year = year;
        this.totalEquity = totalEquity != null ? totalEquity : BigDecimal.ZERO;
        this.principalPaid = principalPaid != null ? principalPaid : BigDecimal.ZERO;
        this.appreciation = appreciation != null ? appreciation : BigDecimal.ZERO;
        this.propertyValue = propertyValue != null ? propertyValue : BigDecimal.ZERO;
        this.remainingLoanBalance = remainingLoanBalance != null ? remainingLoanBalance : BigDecimal.ZERO;
        this.loanToValueRatio = loanToValueRatio;
    }

    /**
     * Gets the projection year.
     * 
     * @return year number (1-based)
     */
    public Integer getYear() {
        return year;
    }

    /**
     * Gets the total equity at the end of this year.
     * 
     * @return total equity amount
     */
    public BigDecimal getTotalEquity() {
        return totalEquity;
    }

    /**
     * Gets the cumulative principal paid through this year.
     * 
     * @return principal paid amount
     */
    public BigDecimal getPrincipalPaid() {
        return principalPaid;
    }

    /**
     * Gets the cumulative appreciation through this year.
     * 
     * @return appreciation amount
     */
    public BigDecimal getAppreciation() {
        return appreciation;
    }

    /**
     * Gets the property value at the end of this year.
     * 
     * @return property value
     */
    public BigDecimal getPropertyValue() {
        return propertyValue;
    }

    /**
     * Gets the remaining loan balance at the end of this year.
     * 
     * @return remaining loan balance
     */
    public BigDecimal getRemainingLoanBalance() {
        return remainingLoanBalance;
    }

    /**
     * Gets the loan-to-value ratio at the end of this year.
     * 
     * @return LTV ratio as percentage (e.g., 75.5 for 75.5%), or null if no loan
     */
    public Double getLoanToValueRatio() {
        return loanToValueRatio;
    }

    /**
     * Calculates the equity percentage (equity / property value).
     * This is the inverse of LTV ratio.
     * 
     * @return equity percentage (e.g., 24.5 for 24.5%)
     */
    public Double getEquityPercentage() {
        if (propertyValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return totalEquity.divide(propertyValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculates the equity gain for this year (compared to initial equity).
     * 
     * @param initialEquity the initial equity (down payment)
     * @return equity gain amount
     */
    public BigDecimal getEquityGain(BigDecimal initialEquity) {
        if (initialEquity == null) {
            return totalEquity;
        }
        return totalEquity.subtract(initialEquity);
    }

    /**
     * Calculates the return on equity (ROE) for this year.
     * ROE = (Equity Gain / Initial Equity) × 100
     * 
     * @param initialEquity the initial equity (down payment)
     * @return ROE as percentage, or null if initial equity is zero
     */
    public Double getReturnOnEquity(BigDecimal initialEquity) {
        if (initialEquity == null || initialEquity.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal gain = getEquityGain(initialEquity);
        return gain.divide(initialEquity, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquityProjection that = (EquityProjection) o;
        return Objects.equals(year, that.year) &&
               Objects.equals(totalEquity, that.totalEquity) &&
               Objects.equals(principalPaid, that.principalPaid) &&
               Objects.equals(appreciation, that.appreciation) &&
               Objects.equals(propertyValue, that.propertyValue) &&
               Objects.equals(remainingLoanBalance, that.remainingLoanBalance) &&
               Objects.equals(loanToValueRatio, that.loanToValueRatio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, totalEquity, principalPaid, appreciation, propertyValue, 
                          remainingLoanBalance, loanToValueRatio);
    }

    @Override
    public String toString() {
        return String.format("EquityProjection{year=%d, equity=$%s, principalPaid=$%s, appreciation=$%s, " +
                           "propertyValue=$%s, loanBalance=$%s, LTV=%.2f%%}",
                year, totalEquity, principalPaid, appreciation, propertyValue, 
                remainingLoanBalance, loanToValueRatio != null ? loanToValueRatio : 0.0);
    }
}