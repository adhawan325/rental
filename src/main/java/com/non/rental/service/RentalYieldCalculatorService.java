package com.non.rental.service;

import com.non.rental.entity.*;
import com.non.rental.service.dto.EquityProjection;
import com.non.rental.service.dto.ValidationResult;
import com.non.rental.service.dto.YearlyProjection;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for rental property yield calculations.
 * Provides comprehensive financial analysis methods for rental properties including
 * basic metrics (free tier), advanced metrics (plus/pro tier), and projections.
 * 
 * <p>All calculations follow standard real estate investment formulas and best practices.
 * Methods are organized by complexity and subscription tier requirements.</p>
 * 
 * @author Rental Analysis System
 * @version 1.0
 */
public interface RentalYieldCalculatorService {

    // ========================================
    // Basic Calculation Methods (Free Tier)
    // ========================================

    /**
     * Calculates the gross rental yield for a property.
     * 
     * <p><b>Formula:</b> Gross Yield = (Annual Rental Income / Property Value) × 100</p>
     * 
     * <p>Gross yield provides a simple measure of rental return without considering expenses.
     * It's useful for quick comparisons between properties but doesn't reflect actual profitability.</p>
     * 
     * <p><b>Example:</b> A property worth $300,000 generating $24,000/year in rent has a gross yield of 8%.</p>
     * 
     * @param analysis the rental analysis containing property and income details
     * @return gross yield as a percentage (e.g., 8.5 for 8.5%), or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    Double calculateGrossYield(RentalAnalysis analysis);

    /**
     * Calculates the net rental yield for a property.
     * 
     * <p><b>Formula:</b> Net Yield = (Annual Net Income / Property Value) × 100</p>
     * <p>Where: Annual Net Income = Annual Rental Income - Annual Operating Expenses</p>
     * 
     * <p>Net yield accounts for operating expenses (taxes, insurance, maintenance, etc.) but excludes
     * mortgage payments. It provides a more realistic picture of rental profitability than gross yield.</p>
     * 
     * <p><b>Example:</b> A property worth $300,000 with $24,000 annual rent and $8,000 expenses 
     * has a net yield of 5.33%.</p>
     * 
     * @param analysis the rental analysis containing property, income, and expense details
     * @return net yield as a percentage (e.g., 5.33 for 5.33%), or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    Double calculateNetYield(RentalAnalysis analysis);

    /**
     * Calculates the monthly cash flow for a rental property.
     * 
     * <p><b>Formula:</b> Monthly Cash Flow = Effective Monthly Income - Total Monthly Expenses - Monthly Mortgage Payment</p>
     * 
     * <p>Cash flow represents the actual money left over each month after all expenses and mortgage
     * payments. Positive cash flow means the property generates income; negative means it costs money monthly.</p>
     * 
     * <p><b>Components:</b></p>
     * <ul>
     *   <li>Effective Monthly Income: Rental income adjusted for vacancy rate</li>
     *   <li>Total Monthly Expenses: Operating expenses (taxes, insurance, maintenance, management, etc.)</li>
     *   <li>Monthly Mortgage Payment: Principal and interest payment (if financed)</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing all financial details
     * @return monthly cash flow amount (positive or negative), or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    BigDecimal calculateMonthlyCashFlow(RentalAnalysis analysis);

    /**
     * Calculates the annual cash flow for a rental property.
     * 
     * <p><b>Formula:</b> Annual Cash Flow = Monthly Cash Flow × 12</p>
     * 
     * <p>Annual cash flow provides a yearly view of property profitability. This is useful for
     * tax planning and comparing against other annual investment returns.</p>
     * 
     * @param analysis the rental analysis containing all financial details
     * @return annual cash flow amount (positive or negative), or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    BigDecimal calculateAnnualCashFlow(RentalAnalysis analysis);

    /**
     * Calculates the monthly mortgage payment using standard amortization formula.
     * 
     * <p><b>Formula:</b> M = P × [r(1+r)^n] / [(1+r)^n - 1]</p>
     * <p>Where:</p>
     * <ul>
     *   <li>M = Monthly payment</li>
     *   <li>P = Principal (loan amount)</li>
     *   <li>r = Monthly interest rate (annual rate / 12 / 100)</li>
     *   <li>n = Number of payments (years × 12)</li>
     * </ul>
     * 
     * <p><b>Special Cases:</b></p>
     * <ul>
     *   <li>If interest rate is 0%, returns principal / total months</li>
     *   <li>If principal is 0, returns 0</li>
     * </ul>
     * 
     * <p><b>Example:</b> $240,000 loan at 6.5% for 30 years = $1,517.05/month</p>
     * 
     * @param principal the loan amount
     * @param annualRate the annual interest rate as a percentage (e.g., 6.5 for 6.5%)
     * @param years the loan term in years
     * @return monthly mortgage payment, or null if any parameter is null
     * @throws IllegalArgumentException if principal is negative, annualRate is negative, or years is less than 1
     */
    BigDecimal calculateMonthlyMortgagePayment(BigDecimal principal, Double annualRate, Integer years);

    // ========================================
    // Advanced Calculation Methods (Plus/Pro Tier)
    // ========================================

    /**
     * Calculates the capitalization rate (cap rate) for a property.
     * 
     * <p><b>Formula:</b> Cap Rate = (Net Operating Income / Property Value) × 100</p>
     * 
     * <p>Cap rate measures the rate of return on a real estate investment based on the income
     * the property generates, independent of financing. It's one of the most important metrics
     * for comparing investment properties.</p>
     * 
     * <p><b>Key Points:</b></p>
     * <ul>
     *   <li>Higher cap rate = Higher potential return (but possibly higher risk)</li>
     *   <li>Lower cap rate = Lower return (but possibly more stable/desirable area)</li>
     *   <li>Typical range: 4-10% depending on market and property type</li>
     *   <li>Does NOT include mortgage payments</li>
     * </ul>
     * 
     * <p><b>Example:</b> Property worth $300,000 with $18,000 NOI has a cap rate of 6%.</p>
     * 
     * @param analysis the rental analysis containing property, income, and expense details
     * @return cap rate as a percentage (e.g., 6.0 for 6%), or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    Double calculateCapRate(RentalAnalysis analysis);

    /**
     * Calculates the cash-on-cash return for a property.
     * 
     * <p><b>Formula:</b> Cash-on-Cash Return = (Annual Cash Flow / Total Cash Invested) × 100</p>
     * 
     * <p>Cash-on-cash return measures the annual return on the actual cash invested in the property.
     * Unlike cap rate, it accounts for financing and shows the return on your out-of-pocket investment.</p>
     * 
     * <p><b>Total Cash Invested includes:</b></p>
     * <ul>
     *   <li>Down payment</li>
     *   <li>Closing costs</li>
     *   <li>Renovation costs</li>
     *   <li>Any other upfront cash expenses</li>
     * </ul>
     * 
     * <p><b>Example:</b> $60,000 invested generating $4,800/year cash flow = 8% CoC return.</p>
     * 
     * @param analysis the rental analysis containing all financial details
     * @return cash-on-cash return as a percentage (e.g., 8.0 for 8%), or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    Double calculateCashOnCashReturn(RentalAnalysis analysis);

    /**
     * Calculates the total return on investment (ROI) over a specified holding period.
     * 
     * <p><b>Formula:</b> ROI = [(Total Profit / Total Investment) × 100] / Years</p>
     * <p>Where: Total Profit = Cumulative Cash Flow + Equity Gained + Appreciation</p>
     * 
     * <p>ROI provides a comprehensive view of investment performance including:</p>
     * <ul>
     *   <li>Cash flow over the holding period</li>
     *   <li>Principal paydown (equity buildup)</li>
     *   <li>Property appreciation (assumed at market rate)</li>
     * </ul>
     * 
     * <p><b>Assumptions:</b></p>
     * <ul>
     *   <li>Property appreciates at 3% annually (or custom rate if specified)</li>
     *   <li>Rental income and expenses remain constant (or use projection methods for accuracy)</li>
     *   <li>No major capital improvements during holding period</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing all financial details
     * @param years the holding period in years
     * @param appreciationRate the annual property appreciation rate (default 3% if null)
     * @return annualized ROI as a percentage (e.g., 12.5 for 12.5%), or null if required data is missing
     * @throws IllegalArgumentException if analysis is null or years is less than 1
     */
    Double calculateROI(RentalAnalysis analysis, Integer years, Double appreciationRate);

    /**
     * Calculates the internal rate of return (IRR) for a property investment.
     * 
     * <p><b>Definition:</b> IRR is the discount rate that makes the net present value (NPV) of all
     * cash flows equal to zero. It represents the annualized effective compounded return rate.</p>
     * 
     * <p><b>Formula:</b> NPV = 0 = Initial Investment + Σ(Cash Flow_t / (1+IRR)^t) + Exit Value / (1+IRR)^n</p>
     * 
     * <p>IRR is considered one of the most sophisticated investment metrics because it:</p>
     * <ul>
     *   <li>Accounts for the time value of money</li>
     *   <li>Considers all cash flows over the investment period</li>
     *   <li>Includes the exit value (sale proceeds)</li>
     *   <li>Provides a single percentage for easy comparison with other investments</li>
     * </ul>
     * 
     * <p><b>Calculation Method:</b> Uses Newton-Raphson iterative method to solve for IRR.</p>
     * 
     * <p><b>Example:</b> $60,000 invested, $4,800/year cash flow for 10 years, $350,000 exit value
     * might yield an IRR of 14.2%.</p>
     * 
     * @param analysis the rental analysis containing all financial details
     * @param years the holding period in years
     * @param exitValue the expected sale price at the end of the holding period (after selling costs, can be null to auto-calculate)
     * @param appreciationRate the annual property appreciation rate (default 3% if null, used when exitValue is null)
     * @return IRR as a percentage (e.g., 14.2 for 14.2%), or null if calculation fails or data is missing
     * @throws IllegalArgumentException if analysis is null, years is less than 1, or exitValue is negative
     */
    Double calculateIRR(RentalAnalysis analysis, Integer years, BigDecimal exitValue, Double appreciationRate);

    // ========================================
    // Helper Calculation Methods
    // ========================================

    /**
     * Calculates the net operating income (NOI) for a property.
     * 
     * <p><b>Formula:</b> NOI = Effective Annual Income - Annual Operating Expenses</p>
     * 
     * <p>NOI is a fundamental metric in real estate that represents the income generated by a property
     * before debt service (mortgage payments). It's used in calculating cap rate and property valuation.</p>
     * 
     * <p><b>Includes:</b></p>
     * <ul>
     *   <li>Rental income (adjusted for vacancy)</li>
     *   <li>Other income (parking, laundry, etc.)</li>
     * </ul>
     * 
     * <p><b>Excludes:</b></p>
     * <ul>
     *   <li>Mortgage payments (principal and interest)</li>
     *   <li>Capital expenditures (major improvements)</li>
     *   <li>Depreciation</li>
     *   <li>Income taxes</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing income and expense details
     * @return net operating income, or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    BigDecimal calculateNetOperatingIncome(RentalAnalysis analysis);

    /**
     * Calculates the total monthly operating expenses for a property.
     * 
     * <p><b>Components:</b></p>
     * <ul>
     *   <li>Property taxes (annual / 12)</li>
     *   <li>Insurance (annual / 12)</li>
     *   <li>HOA fees</li>
     *   <li>Property management fees (% of rent or fixed amount)</li>
     *   <li>Maintenance and repairs (% of property value or fixed amount)</li>
     *   <li>Capital expenditures reserve (% of property value or fixed amount)</li>
     *   <li>Utilities (if landlord-paid)</li>
     *   <li>Other expenses</li>
     * </ul>
     * 
     * <p><b>Note:</b> Does NOT include mortgage payments - those are debt service, not operating expenses.</p>
     * 
     * @param expenses the expense projection entity
     * @param monthlyRent the monthly rental income (used for percentage-based calculations)
     * @param propertyValue the property value (used for percentage-based calculations)
     * @return total monthly operating expenses, or null if required data is missing
     * @throws IllegalArgumentException if any parameter is null
     */
    BigDecimal calculateTotalMonthlyExpenses(ExpenseProjection expenses, BigDecimal monthlyRent, BigDecimal propertyValue);

    /**
     * Calculates the effective monthly income after vacancy adjustment.
     * 
     * <p><b>Formula:</b> Effective Income = Gross Income × (1 - Vacancy Rate)</p>
     * <p>Where: Gross Income = Monthly Rent + Other Monthly Income</p>
     * 
     * <p>Effective income accounts for the reality that rental properties are not occupied 100% of the time.
     * Typical vacancy rates range from 5-10% depending on market conditions.</p>
     * 
     * <p><b>Example:</b> $2,000 rent + $100 other income with 8% vacancy = $1,932 effective income.</p>
     * 
     * @param income the income projection entity
     * @return effective monthly income after vacancy, or null if required data is missing
     * @throws IllegalArgumentException if income is null
     */
    BigDecimal calculateEffectiveMonthlyIncome(IncomeProjection income);

    /**
     * Calculates the total cash invested in a property purchase.
     * 
     * <p><b>Formula:</b> Total Cash = Down Payment + Closing Costs + Renovation Costs</p>
     * 
     * <p>This represents the total out-of-pocket cash required to acquire and prepare the property
     * for rental. It's used as the denominator in cash-on-cash return calculations.</p>
     * 
     * <p><b>Components:</b></p>
     * <ul>
     *   <li>Down Payment: Initial equity in the property</li>
     *   <li>Closing Costs: Fees, title insurance, inspections, etc. (typically 2-5% of purchase price)</li>
     *   <li>Renovation Costs: Repairs and improvements needed before renting</li>
     * </ul>
     * 
     * @param purchase the purchase details entity
     * @return total cash invested, or null if required data is missing
     * @throws IllegalArgumentException if purchase is null
     */
    BigDecimal calculateTotalCashInvested(PurchaseDetails purchase);

    /**
     * Calculates the loan amount for a property purchase.
     * 
     * <p><b>Formula:</b> Loan Amount = Purchase Price - Down Payment</p>
     * 
     * <p>This is the amount that needs to be financed through a mortgage. It's used to calculate
     * monthly mortgage payments and track equity buildup over time.</p>
     * 
     * <p><b>Example:</b> $300,000 purchase price - $60,000 down payment = $240,000 loan.</p>
     * 
     * @param purchase the purchase details entity
     * @return loan amount, or null if required data is missing
     * @throws IllegalArgumentException if purchase is null
     */
    BigDecimal calculateLoanAmount(PurchaseDetails purchase);

    // ========================================
    // Projection Methods
    // ========================================

    /**
     * Projects cash flow over multiple years with growth assumptions.
     * 
     * <p>This method creates year-by-year projections of rental income, expenses, mortgage payments,
     * and resulting cash flow. It accounts for:</p>
     * <ul>
     *   <li>Rental income growth (market rent increases)</li>
     *   <li>Expense inflation (rising costs over time)</li>
     *   <li>Fixed mortgage payments (if financed)</li>
     * </ul>
     * 
     * <p><b>Typical Growth Rates:</b></p>
     * <ul>
     *   <li>Rent Growth: 2-4% annually (varies by market)</li>
     *   <li>Expense Inflation: 2-3% annually (tracks general inflation)</li>
     * </ul>
     * 
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Long-term investment planning</li>
     *   <li>Comparing different holding periods</li>
     *   <li>Stress testing with different growth scenarios</li>
     *   <li>Retirement income planning</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing all financial details
     * @param years the number of years to project
     * @param rentGrowthRate annual rent growth rate as a percentage (e.g., 3.0 for 3%)
     * @param expenseInflationRate annual expense inflation rate as a percentage (e.g., 2.5 for 2.5%)
     * @return list of yearly projections, one for each year; empty list if required data is missing
     * @throws IllegalArgumentException if analysis is null, years is less than 1, or rates are negative
     */
    List<YearlyProjection> projectCashFlow(RentalAnalysis analysis, Integer years, 
                                           Double rentGrowthRate, Double expenseInflationRate);

    /**
     * Projects equity buildup over multiple years with appreciation.
     * 
     * <p>This method tracks how equity in the property grows over time through:</p>
     * <ul>
     *   <li>Principal Paydown: Mortgage balance reduction from monthly payments</li>
     *   <li>Property Appreciation: Increase in property value over time</li>
     * </ul>
     * 
     * <p><b>Formula:</b> Total Equity = Initial Equity + Principal Paid + Appreciation</p>
     * 
     * <p><b>Typical Appreciation Rates:</b></p>
     * <ul>
     *   <li>Conservative: 2-3% annually</li>
     *   <li>Moderate: 3-4% annually</li>
     *   <li>Aggressive: 5%+ annually (hot markets)</li>
     * </ul>
     * 
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Wealth building analysis</li>
     *   <li>Refinancing planning</li>
     *   <li>Exit strategy evaluation</li>
     *   <li>Net worth projections</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing purchase and financing details
     * @param years the number of years to project
     * @param appreciationRate annual property appreciation rate as a percentage (e.g., 3.0 for 3%)
     * @return list of equity projections, one for each year; empty list if required data is missing
     * @throws IllegalArgumentException if analysis is null, years is less than 1, or appreciationRate is negative
     */
    List<EquityProjection> projectEquityBuildup(RentalAnalysis analysis, Integer years, Double appreciationRate);

    // ========================================
    // Validation Methods
    // ========================================

    /**
     * Validates all inputs in a rental analysis for completeness and correctness.
     * 
     * <p>This method performs comprehensive validation including:</p>
     * 
     * <p><b>Required Field Checks:</b></p>
     * <ul>
     *   <li>Purchase price is present and positive</li>
     *   <li>Monthly rent is present and positive</li>
     *   <li>Financing details are complete (if financed)</li>
     *   <li>All percentage values are within valid ranges (0-100)</li>
     * </ul>
     * 
     * <p><b>Business Logic Validation:</b></p>
     * <ul>
     *   <li>Down payment doesn't exceed purchase price</li>
     *   <li>Loan amount matches purchase price minus down payment</li>
     *   <li>Expense percentages are reasonable</li>
     *   <li>Vacancy rate is realistic (typically 5-15%)</li>
     * </ul>
     * 
     * <p><b>Warning Conditions:</b></p>
     * <ul>
     *   <li>Negative cash flow (property loses money monthly)</li>
     *   <li>Very low cap rate (< 4%)</li>
     *   <li>Very high vacancy rate (> 15%)</li>
     *   <li>Unusually high expense ratios</li>
     * </ul>
     * 
     * @param analysis the rental analysis to validate
     * @return validation result containing validity status, error messages, and warnings
     * @throws IllegalArgumentException if analysis is null
     */
    ValidationResult validateAnalysisInputs(RentalAnalysis analysis);

    /**
     * Calculates all available metrics for a rental analysis in one operation.
     * 
     * <p>This is a convenience method that computes all metrics and returns them in a single
     * CalculatedMetrics entity. It's more efficient than calling individual calculation methods
     * when you need multiple metrics.</p>
     * 
     * <p><b>Calculated Metrics Include:</b></p>
     * <ul>
     *   <li>Monthly and annual cash flow</li>
     *   <li>Gross yield</li>
     *   <li>Net yield</li>
     *   <li>Cap rate</li>
     *   <li>Cash-on-cash return</li>
     *   <li>Total ROI (5-year default)</li>
     *   <li>Debt service coverage ratio</li>
     *   <li>Break-even ratio</li>
     * </ul>
     * 
     * <p><b>Note:</b> The returned entity is not persisted. Call dataManager.save() to persist it.</p>
     * 
     * @param analysis the rental analysis containing all financial details
     * @return calculated metrics entity with all computed values, or entity with null values if data is insufficient
     * @throws IllegalArgumentException if analysis is null
     */
    CalculatedMetrics calculateAllMetrics(RentalAnalysis analysis);
}