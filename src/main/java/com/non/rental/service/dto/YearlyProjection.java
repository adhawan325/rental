package com.non.rental.service.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Data transfer object representing projected financial metrics for a single year.
 * Used in multi-year cash flow projections to show how income, expenses, and cash flow
 * evolve over time.
 * 
 * <p>This class is immutable and thread-safe.</p>
 * 
 * @author Rental Analysis System
 * @version 1.0
 */
public class YearlyProjection {

    /**
     * The projection year (1 = first year, 2 = second year, etc.)
     */
    private final Integer year;

    /**
     * Projected annual rental income for this year (after vacancy adjustment)
     */
    private final BigDecimal annualRent;

    /**
     * Projected annual operating expenses for this year
     */
    private final BigDecimal annualExpenses;

    /**
     * Annual mortgage payment for this year (principal + interest)
     * Will be zero if property is purchased with cash
     */
    private final BigDecimal annualMortgage;

    /**
     * Net cash flow for this year (income - expenses - mortgage)
     * Positive values indicate profit; negative values indicate loss
     */
    private final BigDecimal cashFlow;

    /**
     * Cumulative cash flow from year 1 through this year
     * Useful for tracking total profit/loss over the investment period
     */
    private final BigDecimal cumulativeCashFlow;

    /**
     * Creates a new yearly projection with all financial metrics.
     * 
     * @param year the projection year (must be positive)
     * @param annualRent projected annual rental income
     * @param annualExpenses projected annual operating expenses
     * @param annualMortgage annual mortgage payment
     * @param cashFlow net cash flow for the year
     * @param cumulativeCashFlow cumulative cash flow through this year
     * @throws IllegalArgumentException if year is null or less than 1
     */
    public YearlyProjection(Integer year, BigDecimal annualRent, BigDecimal annualExpenses,
                           BigDecimal annualMortgage, BigDecimal cashFlow, BigDecimal cumulativeCashFlow) {
        if (year == null || year < 1) {
            throw new IllegalArgumentException("Year must be a positive integer");
        }
        this.year = year;
        this.annualRent = annualRent != null ? annualRent : BigDecimal.ZERO;
        this.annualExpenses = annualExpenses != null ? annualExpenses : BigDecimal.ZERO;
        this.annualMortgage = annualMortgage != null ? annualMortgage : BigDecimal.ZERO;
        this.cashFlow = cashFlow != null ? cashFlow : BigDecimal.ZERO;
        this.cumulativeCashFlow = cumulativeCashFlow != null ? cumulativeCashFlow : BigDecimal.ZERO;
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
     * Gets the projected annual rental income.
     * 
     * @return annual rent amount
     */
    public BigDecimal getAnnualRent() {
        return annualRent;
    }

    /**
     * Gets the projected annual operating expenses.
     * 
     * @return annual expenses amount
     */
    public BigDecimal getAnnualExpenses() {
        return annualExpenses;
    }

    /**
     * Gets the annual mortgage payment.
     * 
     * @return annual mortgage amount (zero if cash purchase)
     */
    public BigDecimal getAnnualMortgage() {
        return annualMortgage;
    }

    /**
     * Gets the net cash flow for this year.
     * 
     * @return cash flow amount (positive = profit, negative = loss)
     */
    public BigDecimal getCashFlow() {
        return cashFlow;
    }

    /**
     * Gets the cumulative cash flow through this year.
     * 
     * @return cumulative cash flow amount
     */
    public BigDecimal getCumulativeCashFlow() {
        return cumulativeCashFlow;
    }

    /**
     * Calculates the net operating income (NOI) for this year.
     * NOI = Annual Rent - Annual Expenses (excludes mortgage)
     * 
     * @return net operating income
     */
    public BigDecimal getNetOperatingIncome() {
        return annualRent.subtract(annualExpenses);
    }

    /**
     * Calculates the total annual debt service (mortgage payments).
     * 
     * @return annual mortgage payment
     */
    public BigDecimal getAnnualDebtService() {
        return annualMortgage;
    }

    /**
     * Calculates the debt service coverage ratio (DSCR) for this year.
     * DSCR = NOI / Annual Debt Service
     * 
     * <p>DSCR indicates how well the property's income covers its debt obligations:</p>
     * <ul>
     *   <li>DSCR > 1.25: Excellent (strong cash flow cushion)</li>
     *   <li>DSCR = 1.0-1.25: Acceptable (income covers debt)</li>
     *   <li>DSCR < 1.0: Poor (income doesn't cover debt)</li>
     * </ul>
     * 
     * @return DSCR ratio, or null if there's no mortgage
     */
    public Double getDebtServiceCoverageRatio() {
        if (annualMortgage.compareTo(BigDecimal.ZERO) == 0) {
            return null; // No debt, DSCR not applicable
        }
        BigDecimal noi = getNetOperatingIncome();
        return noi.divide(annualMortgage, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YearlyProjection that = (YearlyProjection) o;
        return Objects.equals(year, that.year) &&
               Objects.equals(annualRent, that.annualRent) &&
               Objects.equals(annualExpenses, that.annualExpenses) &&
               Objects.equals(annualMortgage, that.annualMortgage) &&
               Objects.equals(cashFlow, that.cashFlow) &&
               Objects.equals(cumulativeCashFlow, that.cumulativeCashFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, annualRent, annualExpenses, annualMortgage, cashFlow, cumulativeCashFlow);
    }

    @Override
    public String toString() {
        return String.format("YearlyProjection{year=%d, rent=$%s, expenses=$%s, mortgage=$%s, cashFlow=$%s, cumulative=$%s}",
                year, annualRent, annualExpenses, annualMortgage, cashFlow, cumulativeCashFlow);
    }
}