package com.non.rental.service;

import com.non.rental.entity.*;
import com.non.rental.service.dto.EquityProjection;
import com.non.rental.service.dto.ValidationResult;
import com.non.rental.service.dto.YearlyProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of RentalYieldCalculatorService.
 * Provides comprehensive financial analysis methods for rental properties.
 * 
 * <p>This implementation uses BigDecimal for all monetary calculations to ensure precision.
 * All percentage values are rounded to 4 decimal places, and currency values to 2 decimal places.</p>
 * 
 * @author Rental Analysis System
 * @version 1.0
 */
@Service
public class RentalYieldCalculatorServiceImpl implements RentalYieldCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(RentalYieldCalculatorServiceImpl.class);
    
    // Constants for rounding
    private static final int CURRENCY_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    // Constants for calculations
    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    
    // Default projection parameters
    private static final double DEFAULT_RENT_GROWTH_RATE = 3.0;
    private static final double DEFAULT_EXPENSE_INFLATION_RATE = 3.0;
    private static final double DEFAULT_APPRECIATION_RATE = 3.0;
    
    // Validation limits
    private static final int MAX_PROJECTION_YEARS = 30;
    private static final double MIN_GROWTH_RATE = -50.0;  // Allow for market crashes
    private static final double MAX_GROWTH_RATE = 50.0;   // Reasonable upper bound

    // ========================================
    // Basic Calculation Methods (Free Tier)
    // ========================================

    @Override
    public Double calculateGrossYield(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Calculating gross yield for analysis: {}", analysis.getId());
        
        // Validate required data
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        IncomeProjection income = analysis.getIncomeProjection();
        
        if (purchase == null || income == null) {
            log.warn("Missing purchase details or income projection for analysis: {}", analysis.getId());
            return null;
        }
        
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        BigDecimal monthlyRent = income.getMonthlyRent();
        
        if (purchasePrice == null || monthlyRent == null) {
            log.warn("Missing purchase price or monthly rent for analysis: {}", analysis.getId());
            return null;
        }
        
        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be greater than zero");
        }
        
        // Calculate annual rental income
        BigDecimal annualRent = monthlyRent.multiply(MONTHS_PER_YEAR);
        
        // Calculate gross yield: (Annual Rental Income / Property Purchase Price) × 100
        BigDecimal grossYield = annualRent
                .divide(purchasePrice, PERCENTAGE_SCALE + 2, ROUNDING_MODE)
                .multiply(ONE_HUNDRED)
                .setScale(PERCENTAGE_SCALE, ROUNDING_MODE);
        
        double result = grossYield.doubleValue();
        log.debug("Gross yield calculated: {}%", result);
        
        return result;
    }

    @Override
    public Double calculateNetYield(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Calculating net yield for analysis: {}", analysis.getId());
        
        // Validate required data
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        IncomeProjection income = analysis.getIncomeProjection();
        ExpenseProjection expenses = analysis.getExpenseProjection();
        
        if (purchase == null || income == null || expenses == null) {
            log.warn("Missing required data for net yield calculation: {}", analysis.getId());
            return null;
        }
        
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        BigDecimal monthlyRent = income.getMonthlyRent();
        
        if (purchasePrice == null || monthlyRent == null) {
            log.warn("Missing purchase price or monthly rent for analysis: {}", analysis.getId());
            return null;
        }
        
        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be greater than zero");
        }
        
        // Calculate annual rental income
        BigDecimal annualRent = monthlyRent.multiply(MONTHS_PER_YEAR);
        
        // Calculate annual operating expenses
        BigDecimal monthlyExpenses = calculateTotalMonthlyExpenses(expenses, monthlyRent, purchasePrice);
        if (monthlyExpenses == null) {
            log.warn("Unable to calculate monthly expenses for analysis: {}", analysis.getId());
            return null;
        }
        BigDecimal annualExpenses = monthlyExpenses.multiply(MONTHS_PER_YEAR);
        
        // Calculate net yield: ((Annual Rental Income - Annual Operating Expenses) / Property Purchase Price) × 100
        BigDecimal annualNetIncome = annualRent.subtract(annualExpenses);
        BigDecimal netYield = annualNetIncome
                .divide(purchasePrice, PERCENTAGE_SCALE + 2, ROUNDING_MODE)
                .multiply(ONE_HUNDRED)
                .setScale(PERCENTAGE_SCALE, ROUNDING_MODE);
        
        double result = netYield.doubleValue();
        log.debug("Net yield calculated: {}%", result);
        
        return result;
    }

    @Override
    public BigDecimal calculateMonthlyMortgagePayment(BigDecimal principal, Double annualRate, Integer years) {
        log.debug("Calculating monthly mortgage payment: principal={}, rate={}%, years={}", 
                principal, annualRate, years);
        
        // Validate inputs
        if (principal == null || annualRate == null || years == null) {
            log.warn("Missing required parameters for mortgage calculation");
            return null;
        }
        
        if (principal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Principal cannot be negative");
        }
        
        if (annualRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        
        if (years < 1) {
            throw new IllegalArgumentException("Loan term must be at least 1 year");
        }
        
        // Handle edge case: zero principal
        if (principal.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("Principal is zero, returning zero payment");
            return BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        }
        
        // Handle edge case: zero interest rate
        if (annualRate == 0.0) {
            int totalMonths = years * 12;
            BigDecimal payment = principal.divide(BigDecimal.valueOf(totalMonths), CURRENCY_SCALE, ROUNDING_MODE);
            log.debug("Zero interest rate, simple division: ${}", payment);
            return payment;
        }
        
        // Standard mortgage calculation: M = P × [r(1 + r)^n] / [(1 + r)^n - 1]
        // where r = monthly interest rate, n = number of payments
        
        double monthlyRate = annualRate / 12.0 / 100.0;
        int numberOfPayments = years * 12;
        
        // Calculate (1 + r)^n
        double onePlusRatePowerN = Math.pow(1 + monthlyRate, numberOfPayments);
        
        // Calculate monthly payment
        double paymentAmount = principal.doubleValue() * 
                (monthlyRate * onePlusRatePowerN) / 
                (onePlusRatePowerN - 1);
        
        BigDecimal payment = BigDecimal.valueOf(paymentAmount).setScale(CURRENCY_SCALE, ROUNDING_MODE);
        log.debug("Monthly mortgage payment calculated: ${}", payment);
        
        return payment;
    }

    @Override
    public BigDecimal calculateMonthlyCashFlow(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Calculating monthly cash flow for analysis: {}", analysis.getId());
        
        // Get required components
        IncomeProjection income = analysis.getIncomeProjection();
        ExpenseProjection expenses = analysis.getExpenseProjection();
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        FinancingDetails financing = analysis.getFinancingDetails();
        
        if (income == null || expenses == null || purchase == null) {
            log.warn("Missing required data for cash flow calculation: {}", analysis.getId());
            return null;
        }
        
        // Calculate effective monthly income (rent × (1 - vacancy rate) + other income)
        BigDecimal effectiveIncome = calculateEffectiveMonthlyIncome(income);
        if (effectiveIncome == null) {
            log.warn("Unable to calculate effective income for analysis: {}", analysis.getId());
            return null;
        }
        
        // Calculate total monthly expenses
        BigDecimal monthlyRent = income.getMonthlyRent();
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        BigDecimal totalExpenses = calculateTotalMonthlyExpenses(expenses, monthlyRent, purchasePrice);
        if (totalExpenses == null) {
            log.warn("Unable to calculate monthly expenses for analysis: {}", analysis.getId());
            return null;
        }
        
        // Calculate monthly mortgage payment (if financed)
        BigDecimal mortgagePayment = BigDecimal.ZERO;
        if (financing != null) {
            BigDecimal loanAmount = calculateLoanAmount(purchase);
            if (loanAmount != null && loanAmount.compareTo(BigDecimal.ZERO) > 0) {
                Double interestRate = financing.getInterestRate();
                Integer loanTermYears = financing.getLoanTermYears();
                
                if (interestRate != null && loanTermYears != null) {
                    mortgagePayment = calculateMonthlyMortgagePayment(loanAmount, interestRate, loanTermYears);
                    if (mortgagePayment == null) {
                        mortgagePayment = BigDecimal.ZERO;
                    }
                }
            }
        }
        
        // Calculate cash flow: Monthly Rent - Monthly Operating Expenses - Monthly Mortgage Payment
        BigDecimal cashFlow = effectiveIncome
                .subtract(totalExpenses)
                .subtract(mortgagePayment)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);
        
        log.debug("Monthly cash flow calculated: ${} (Income: ${}, Expenses: ${}, Mortgage: ${})", 
                cashFlow, effectiveIncome, totalExpenses, mortgagePayment);
        
        return cashFlow;
    }

    @Override
    public BigDecimal calculateAnnualCashFlow(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Calculating annual cash flow for analysis: {}", analysis.getId());
        
        BigDecimal monthlyCashFlow = calculateMonthlyCashFlow(analysis);
        if (monthlyCashFlow == null) {
            log.warn("Unable to calculate monthly cash flow for analysis: {}", analysis.getId());
            return null;
        }
        
        // Annual Cash Flow = Monthly Cash Flow × 12
        BigDecimal annualCashFlow = monthlyCashFlow
                .multiply(MONTHS_PER_YEAR)
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);
        
        log.debug("Annual cash flow calculated: ${}", annualCashFlow);
        
        return annualCashFlow;
    }

    // ========================================
    // Advanced Calculation Methods (Plus/Pro Tier)
    // ========================================

    /**
     * Calculates the Capitalization Rate (Cap Rate) for a rental property.
     * 
     * <p>Cap Rate is a key metric used to evaluate the profitability of a real estate investment.
     * It represents the ratio of Net Operating Income (NOI) to the property purchase price.</p>
     * 
     * <p><strong>Formula:</strong> (Net Operating Income / Property Purchase Price) × 100</p>
     * 
     * <p>Where Net Operating Income (NOI) = Annual Rental Income - Annual Operating Expenses</p>
     * 
     * <p><strong>Note:</strong> Cap Rate does NOT include mortgage payments. It measures the property's
     * ability to generate income regardless of financing.</p>
     * 
     * @param analysis the rental analysis containing all necessary data
     * @return the capitalization rate as a percentage, or null if required data is missing
     * @throws IllegalArgumentException if analysis is null or purchase price is zero/negative
     */
    @Override
    public Double calculateCapRate(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Calculating Cap Rate for analysis: {}", analysis.getId());
        
        // Calculate Net Operating Income
        BigDecimal noi = calculateNetOperatingIncome(analysis);
        if (noi == null) {
            log.warn("Unable to calculate NOI for Cap Rate: {}", analysis.getId());
            return null;
        }
        
        // Get purchase price
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        if (purchase == null || purchase.getPurchasePrice() == null) {
            log.warn("Missing purchase details for Cap Rate calculation: {}", analysis.getId());
            return null;
        }
        
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be greater than zero");
        }
        
        // Calculate Cap Rate: (NOI / Purchase Price) × 100
        BigDecimal capRate = noi
                .divide(purchasePrice, PERCENTAGE_SCALE + 2, ROUNDING_MODE)
                .multiply(ONE_HUNDRED)
                .setScale(PERCENTAGE_SCALE, ROUNDING_MODE);
        
        double result = capRate.doubleValue();
        log.debug("Cap Rate calculated: {}% (NOI: ${}, Purchase Price: ${})", 
                result, noi, purchasePrice);
        
        return result;
    }

    /**
     * Calculates the Cash-on-Cash Return for a rental property investment.
     * 
     * <p>Cash-on-Cash Return measures the annual pre-tax cash flow relative to the total
     * cash invested in the property. It's a key metric for evaluating leveraged investments.</p>
     * 
     * <p><strong>Formula:</strong> (Annual Cash Flow / Total Cash Invested) × 100</p>
     * 
     * <p>Where:</p>
     * <ul>
     *   <li>Annual Cash Flow = Monthly Cash Flow × 12</li>
     *   <li>Total Cash Invested = Down Payment + Closing Costs + Renovation Costs</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing all necessary data
     * @return the cash-on-cash return as a percentage, or null if required data is missing
     * @throws IllegalArgumentException if analysis is null or total cash invested is zero/negative
     */
    @Override
    public Double calculateCashOnCashReturn(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Calculating Cash-on-Cash Return for analysis: {}", analysis.getId());
        
        // Calculate annual cash flow
        BigDecimal annualCashFlow = calculateAnnualCashFlow(analysis);
        if (annualCashFlow == null) {
            log.warn("Unable to calculate annual cash flow for CoC Return: {}", analysis.getId());
            return null;
        }
        
        // Calculate total cash invested
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        if (purchase == null) {
            log.warn("Missing purchase details for CoC Return calculation: {}", analysis.getId());
            return null;
        }
        
        BigDecimal totalCashInvested = calculateTotalCashInvested(purchase);
        if (totalCashInvested == null || totalCashInvested.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total cash invested must be greater than zero");
        }
        
        // Calculate Cash-on-Cash Return: (Annual Cash Flow / Total Cash Invested) × 100
        BigDecimal cocReturn = annualCashFlow
                .divide(totalCashInvested, PERCENTAGE_SCALE + 2, ROUNDING_MODE)
                .multiply(ONE_HUNDRED)
                .setScale(PERCENTAGE_SCALE, ROUNDING_MODE);
        
        double result = cocReturn.doubleValue();
        log.debug("Cash-on-Cash Return calculated: {}% (Annual Cash Flow: ${}, Total Invested: ${})", 
                result, annualCashFlow, totalCashInvested);
        
        return result;
    }

    /**
     * Calculates the Return on Investment (ROI) for a rental property over a specified period.
     * 
     * <p>ROI measures the total return including cash flow, principal paydown, and appreciation.
     * This provides a comprehensive view of the investment's performance.</p>
     * 
     * <p><strong>Formula:</strong> ((Annual Cash Flow + Annual Principal Paid + Annual Appreciation) / Total Cash Invested) × 100</p>
     * 
     * <p>For first year calculation:</p>
     * <ul>
     *   <li>Annual Cash Flow = Monthly Cash Flow × 12</li>
     *   <li>Annual Principal Paid = Total Mortgage Payments - Interest Paid in Year 1</li>
     *   <li>Annual Appreciation = Property Value × Appreciation Rate</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing all necessary data
     * @param years the holding period in years (currently uses year 1 calculations)
     * @param appreciationRate the annual property appreciation rate (default 3% if null)
     * @return the ROI as a percentage, or null if required data is missing
     * @throws IllegalArgumentException if analysis is null, years is invalid, or total cash invested is zero
     */
    @Override
    public Double calculateROI(RentalAnalysis analysis, Integer years, Double appreciationRate) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        if (years == null || years < 1) {
            throw new IllegalArgumentException("Years must be at least 1");
        }
        
        // Default appreciation rate to 3% if not provided
        if (appreciationRate == null) {
            appreciationRate = 3.0;
        }
        
        if (appreciationRate < 0) {
            throw new IllegalArgumentException("Appreciation rate cannot be negative");
        }
        
        log.debug("Calculating ROI for analysis: {} (Years: {}, Appreciation: {}%)", 
                analysis.getId(), years, appreciationRate);
        
        // Calculate annual cash flow
        BigDecimal annualCashFlow = calculateAnnualCashFlow(analysis);
        if (annualCashFlow == null) {
            log.warn("Unable to calculate annual cash flow for ROI: {}", analysis.getId());
            return null;
        }
        
        // Get purchase details
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        if (purchase == null || purchase.getPurchasePrice() == null) {
            log.warn("Missing purchase details for ROI calculation: {}", analysis.getId());
            return null;
        }
        
        // Calculate total cash invested
        BigDecimal totalCashInvested = calculateTotalCashInvested(purchase);
        if (totalCashInvested == null || totalCashInvested.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total cash invested must be greater than zero");
        }
        
        // Calculate annual principal paid (Year 1)
        BigDecimal annualPrincipalPaid = BigDecimal.ZERO;
        FinancingDetails financing = analysis.getFinancingDetails();
        if (financing != null) {
            BigDecimal loanAmount = calculateLoanAmount(purchase);
            if (loanAmount != null && loanAmount.compareTo(BigDecimal.ZERO) > 0) {
                Double interestRate = financing.getInterestRate();
                Integer loanTermYears = financing.getLoanTermYears();
                
                if (interestRate != null && loanTermYears != null) {
                    BigDecimal monthlyPayment = calculateMonthlyMortgagePayment(loanAmount, interestRate, loanTermYears);
                    if (monthlyPayment != null) {
                        BigDecimal annualPayment = monthlyPayment.multiply(MONTHS_PER_YEAR);
                        
                        // Calculate first year interest (approximate using average balance)
                        // More accurate would be to calculate month by month, but this is a good approximation
                        BigDecimal averageBalance = loanAmount; // First year, use full loan amount as approximation
                        BigDecimal annualInterest = averageBalance
                                .multiply(BigDecimal.valueOf(interestRate / 100.0))
                                .setScale(CURRENCY_SCALE, ROUNDING_MODE);
                        
                        annualPrincipalPaid = annualPayment.subtract(annualInterest);
                        if (annualPrincipalPaid.compareTo(BigDecimal.ZERO) < 0) {
                            annualPrincipalPaid = BigDecimal.ZERO;
                        }
                    }
                }
            }
        }
        
        // Calculate annual appreciation
        BigDecimal propertyValue = purchase.getPurchasePrice();
        BigDecimal annualAppreciation = propertyValue
                .multiply(BigDecimal.valueOf(appreciationRate / 100.0))
                .setScale(CURRENCY_SCALE, ROUNDING_MODE);
        
        // Calculate total annual gain
        BigDecimal totalAnnualGain = annualCashFlow
                .add(annualPrincipalPaid)
                .add(annualAppreciation);
        
        // Calculate ROI: (Total Annual Gain / Total Cash Invested) × 100
        BigDecimal roi = totalAnnualGain
                .divide(totalCashInvested, PERCENTAGE_SCALE + 2, ROUNDING_MODE)
                .multiply(ONE_HUNDRED)
                .setScale(PERCENTAGE_SCALE, ROUNDING_MODE);
        
        double result = roi.doubleValue();
        log.debug("ROI calculated: {}% (Cash Flow: ${}, Principal: ${}, Appreciation: ${}, Total Invested: ${})", 
                result, annualCashFlow, annualPrincipalPaid, annualAppreciation, totalCashInvested);
        
        return result;
    }

    /**
     * Calculates the Internal Rate of Return (IRR) for a rental property investment.
     * 
     * <p>IRR is the discount rate that makes the Net Present Value (NPV) of all cash flows equal to zero.
     * It represents the annualized effective compounded return rate.</p>
     * 
     * <p><strong>Formula:</strong> NPV = Σ [CFt / (1 + IRR)^t] = 0</p>
     * 
     * <p>This implementation uses the Newton-Raphson iterative method to find the IRR:</p>
     * <ul>
     *   <li>Initial guess: 10%</li>
     *   <li>Convergence tolerance: 0.00001</li>
     *   <li>Maximum iterations: 100</li>
     * </ul>
     * 
     * <p>Cash flows include:</p>
     * <ul>
     *   <li>Year 0: -Total Cash Invested (initial investment)</li>
     *   <li>Years 1-N: Annual Cash Flow</li>
     *   <li>Year N: Annual Cash Flow + Exit Value (property sale)</li>
     * </ul>
     * 
     * @param analysis the rental analysis containing all necessary data
     * @param years the holding period in years
     * @param exitValue the expected property sale value at the end of the holding period
     * @param appreciationRate the annual property appreciation rate (default 3% if null)
     * @return the IRR as a percentage, or null if calculation fails or required data is missing
     * @throws IllegalArgumentException if analysis is null, years is invalid, or exit value is negative
     */
    @Override
    public Double calculateIRR(RentalAnalysis analysis, Integer years, BigDecimal exitValue, Double appreciationRate) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        if (years == null || years < 1) {
            throw new IllegalArgumentException("Years must be at least 1");
        }
        
        if (exitValue != null && exitValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Exit value cannot be negative");
        }
        
        // Default appreciation rate to 3% if not provided
        if (appreciationRate == null) {
            appreciationRate = 3.0;
        }
        
        log.debug("Calculating IRR for analysis: {} (Years: {}, Exit Value: ${})", 
                analysis.getId(), years, exitValue);
        
        // Calculate annual cash flow
        BigDecimal annualCashFlow = calculateAnnualCashFlow(analysis);
        if (annualCashFlow == null) {
            log.warn("Unable to calculate annual cash flow for IRR: {}", analysis.getId());
            return null;
        }
        
        // Get purchase details
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        if (purchase == null) {
            log.warn("Missing purchase details for IRR calculation: {}", analysis.getId());
            return null;
        }
        
        // Calculate total cash invested (initial investment - negative cash flow)
        BigDecimal totalCashInvested = calculateTotalCashInvested(purchase);
        if (totalCashInvested == null || totalCashInvested.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total cash invested must be greater than zero");
        }
        
        // If exit value not provided, calculate based on appreciation
        if (exitValue == null) {
            BigDecimal purchasePrice = purchase.getPurchasePrice();
            if (purchasePrice == null) {
                log.warn("Cannot calculate exit value without purchase price: {}", analysis.getId());
                return null;
            }
            
            // Calculate exit value with appreciation: Purchase Price × (1 + appreciation rate)^years
            double appreciationMultiplier = Math.pow(1.0 + (appreciationRate / 100.0), years);
            exitValue = purchasePrice
                    .multiply(BigDecimal.valueOf(appreciationMultiplier))
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            
            log.debug("Calculated exit value with {}% appreciation over {} years: ${}", 
                    appreciationRate, years, exitValue);
        }
        
        // Build cash flow array
        // Year 0: -Initial Investment
        // Years 1 to N-1: Annual Cash Flow
        // Year N: Annual Cash Flow + Exit Value
        double[] cashFlows = new double[years + 1];
        cashFlows[0] = -totalCashInvested.doubleValue();
        
        for (int i = 1; i < years; i++) {
            cashFlows[i] = annualCashFlow.doubleValue();
        }
        
        // Final year includes exit value
        cashFlows[years] = annualCashFlow.doubleValue() + exitValue.doubleValue();
        
        // Calculate IRR using Newton-Raphson method
        Double irr = calculateIRRNewtonRaphson(cashFlows);
        
        if (irr != null) {
            log.debug("IRR calculated: {}%", irr);
        } else {
            log.warn("IRR calculation did not converge for analysis: {}", analysis.getId());
        }
        
        return irr;
    }

    /**
     * Calculates IRR using the Newton-Raphson iterative method.
     * 
     * @param cashFlows array of cash flows (year 0 to N)
     * @return the IRR as a percentage, or null if calculation does not converge
     */
    private Double calculateIRRNewtonRaphson(double[] cashFlows) {
        final double TOLERANCE = 0.00001;
        final int MAX_ITERATIONS = 100;
        final double INITIAL_GUESS = 0.10; // 10%
        
        double rate = INITIAL_GUESS;
        
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double npv = 0.0;
            double dnpv = 0.0; // Derivative of NPV
            
            // Calculate NPV and its derivative
            for (int t = 0; t < cashFlows.length; t++) {
                double denominator = Math.pow(1.0 + rate, t);
                npv += cashFlows[t] / denominator;
                
                if (t > 0) {
                    dnpv -= t * cashFlows[t] / Math.pow(1.0 + rate, t + 1);
                }
            }
            
            // Check for convergence
            if (Math.abs(npv) < TOLERANCE) {
                return BigDecimal.valueOf(rate * 100.0)
                        .setScale(PERCENTAGE_SCALE, ROUNDING_MODE)
                        .doubleValue();
            }
            
            // Newton-Raphson iteration: rate_new = rate_old - f(rate) / f'(rate)
            if (Math.abs(dnpv) < TOLERANCE) {
                // Derivative too small, cannot continue
                log.warn("IRR calculation: derivative too small at iteration {}", iteration);
                return null;
            }
            
            rate = rate - (npv / dnpv);
            
            // Sanity check: rate should be reasonable (-100% to 1000%)
            if (rate < -1.0 || rate > 10.0) {
                log.warn("IRR calculation: rate out of reasonable bounds at iteration {}", iteration);
                return null;
            }
        }
        
        log.warn("IRR calculation did not converge after {} iterations", MAX_ITERATIONS);
        return null;
    }

    // ========================================
    // Helper Calculation Methods
    // ========================================

    /**
     * Calculates the Net Operating Income (NOI) for a rental property.
     * 
     * <p>NOI is a fundamental metric in real estate that measures the property's
     * profitability before financing costs.</p>
     * 
     * <p><strong>Formula:</strong> Annual Rental Income - Annual Operating Expenses</p>
     * 
     * <p><strong>Note:</strong> NOI excludes mortgage payments. It only considers
     * operating income and expenses.</p>
     * 
     * @param analysis the rental analysis containing all necessary data
     * @return the net operating income, or null if required data is missing
     * @throws IllegalArgumentException if analysis is null
     */
    @Override
    public BigDecimal calculateNetOperatingIncome(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Calculating Net Operating Income for analysis: {}", analysis.getId());
        
        // Get required components
        IncomeProjection income = analysis.getIncomeProjection();
        ExpenseProjection expenses = analysis.getExpenseProjection();
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        
        if (income == null || expenses == null || purchase == null) {
            log.warn("Missing required data for NOI calculation: {}", analysis.getId());
            return null;
        }
        
        BigDecimal monthlyRent = income.getMonthlyRent();
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        
        if (monthlyRent == null || purchasePrice == null) {
            log.warn("Missing monthly rent or purchase price for NOI calculation: {}", analysis.getId());
            return null;
        }
        
        // Calculate annual rental income
        BigDecimal annualRent = monthlyRent.multiply(MONTHS_PER_YEAR);
        
        // Calculate annual operating expenses (excluding mortgage)
        BigDecimal monthlyExpenses = calculateTotalMonthlyExpenses(expenses, monthlyRent, purchasePrice);
        if (monthlyExpenses == null) {
            log.warn("Unable to calculate monthly expenses for NOI: {}", analysis.getId());
            return null;
        }
        BigDecimal annualExpenses = monthlyExpenses.multiply(MONTHS_PER_YEAR);
        
        // Calculate NOI: Annual Income - Annual Expenses
        BigDecimal noi = annualRent.subtract(annualExpenses).setScale(CURRENCY_SCALE, ROUNDING_MODE);
        
        log.debug("Net Operating Income calculated: ${} (Annual Rent: ${}, Annual Expenses: ${})", 
                noi, annualRent, annualExpenses);
        
        return noi;
    }

    @Override
    public BigDecimal calculateTotalMonthlyExpenses(ExpenseProjection expenses, BigDecimal monthlyRent, BigDecimal propertyValue) {
        log.debug("Calculating total monthly expenses");
        
        if (expenses == null) {
            throw new IllegalArgumentException("ExpenseProjection cannot be null");
        }
        
        if (monthlyRent == null) {
            throw new IllegalArgumentException("Monthly rent cannot be null");
        }
        
        if (propertyValue == null) {
            throw new IllegalArgumentException("Property value cannot be null");
        }
        
        BigDecimal total = BigDecimal.ZERO;
        
        // Property tax (annual / 12)
        if (expenses.getAnnualPropertyTax() != null) {
            BigDecimal monthlyTax = expenses.getAnnualPropertyTax()
                    .divide(MONTHS_PER_YEAR, CURRENCY_SCALE, ROUNDING_MODE);
            total = total.add(monthlyTax);
            log.trace("Added property tax: ${}", monthlyTax);
        }
        
        // Insurance (annual / 12)
        if (expenses.getAnnualInsurance() != null) {
            BigDecimal monthlyInsurance = expenses.getAnnualInsurance()
                    .divide(MONTHS_PER_YEAR, CURRENCY_SCALE, ROUNDING_MODE);
            total = total.add(monthlyInsurance);
            log.trace("Added insurance: ${}", monthlyInsurance);
        }
        
        // HOA fees
        if (expenses.getMonthlyHOAFees() != null) {
            total = total.add(expenses.getMonthlyHOAFees());
            log.trace("Added HOA fees: ${}", expenses.getMonthlyHOAFees());
        }
        
        // Property management (% of rent or fixed amount)
        BigDecimal propertyManagement = BigDecimal.ZERO;
        if (expenses.getPropertyManagementAmount() != null) {
            propertyManagement = expenses.getPropertyManagementAmount();
        } else if (expenses.getPropertyManagementPercent() != null) {
            propertyManagement = monthlyRent
                    .multiply(BigDecimal.valueOf(expenses.getPropertyManagementPercent() / 100.0))
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);
        }
        total = total.add(propertyManagement);
        log.trace("Added property management: ${}", propertyManagement);
        
        // Maintenance (% of property value / 12 or fixed amount)
        BigDecimal maintenance = BigDecimal.ZERO;
        if (expenses.getMaintenanceAmount() != null) {
            maintenance = expenses.getMaintenanceAmount();
        } else if (expenses.getMaintenancePercent() != null) {
            // Annual maintenance as % of property value, then divide by 12
            maintenance = propertyValue
                    .multiply(BigDecimal.valueOf(expenses.getMaintenancePercent() / 100.0))
                    .divide(MONTHS_PER_YEAR, CURRENCY_SCALE, ROUNDING_MODE);
        }
        total = total.add(maintenance);
        log.trace("Added maintenance: ${}", maintenance);
        
        // Capital expenditures (% of property value / 12 or fixed amount)
        BigDecimal capEx = BigDecimal.ZERO;
        if (expenses.getCapExAmount() != null) {
            capEx = expenses.getCapExAmount();
        } else if (expenses.getCapExPercent() != null) {
            // Annual CapEx as % of property value, then divide by 12
            capEx = propertyValue
                    .multiply(BigDecimal.valueOf(expenses.getCapExPercent() / 100.0))
                    .divide(MONTHS_PER_YEAR, CURRENCY_SCALE, ROUNDING_MODE);
        }
        total = total.add(capEx);
        log.trace("Added capital expenditures: ${}", capEx);
        
        // Utilities
        if (expenses.getMonthlyUtilities() != null) {
            total = total.add(expenses.getMonthlyUtilities());
            log.trace("Added utilities: ${}", expenses.getMonthlyUtilities());
        }
        
        // Other expenses
        if (expenses.getOtherMonthlyExpenses() != null) {
            total = total.add(expenses.getOtherMonthlyExpenses());
            log.trace("Added other expenses: ${}", expenses.getOtherMonthlyExpenses());
        }
        
        total = total.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        log.debug("Total monthly expenses calculated: ${}", total);
        
        return total;
    }

    @Override
    public BigDecimal calculateEffectiveMonthlyIncome(IncomeProjection income) {
        log.debug("Calculating effective monthly income");
        
        if (income == null) {
            throw new IllegalArgumentException("IncomeProjection cannot be null");
        }
        
        BigDecimal monthlyRent = income.getMonthlyRent();
        if (monthlyRent == null) {
            log.warn("Monthly rent is null");
            return null;
        }
        
        // Calculate gross income (rent + other income)
        BigDecimal grossIncome = monthlyRent;
        if (income.getOtherMonthlyIncome() != null) {
            grossIncome = grossIncome.add(income.getOtherMonthlyIncome());
        }
        
        // Apply vacancy rate: Effective Income = Gross Income × (1 - Vacancy Rate)
        Double vacancyRate = income.getVacancyRate();
        if (vacancyRate != null && vacancyRate > 0) {
            double occupancyRate = 1.0 - (vacancyRate / 100.0);
            BigDecimal effectiveIncome = grossIncome
                    .multiply(BigDecimal.valueOf(occupancyRate))
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            
            log.debug("Effective monthly income calculated: ${} (Gross: ${}, Vacancy: {}%)", 
                    effectiveIncome, grossIncome, vacancyRate);
            
            return effectiveIncome;
        }
        
        log.debug("Effective monthly income (no vacancy): ${}", grossIncome);
        return grossIncome.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    @Override
    public BigDecimal calculateTotalCashInvested(PurchaseDetails purchase) {
        log.debug("Calculating total cash invested");
        
        if (purchase == null) {
            throw new IllegalArgumentException("PurchaseDetails cannot be null");
        }
        
        BigDecimal total = BigDecimal.ZERO;
        
        // Down payment
        if (purchase.getDownPaymentAmount() != null) {
            total = total.add(purchase.getDownPaymentAmount());
            log.trace("Added down payment: ${}", purchase.getDownPaymentAmount());
        }
        
        // Closing costs
        if (purchase.getClosingCosts() != null) {
            total = total.add(purchase.getClosingCosts());
            log.trace("Added closing costs: ${}", purchase.getClosingCosts());
        }
        
        // Renovation costs
        if (purchase.getRenovationCosts() != null) {
            total = total.add(purchase.getRenovationCosts());
            log.trace("Added renovation costs: ${}", purchase.getRenovationCosts());
        }
        
        total = total.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        log.debug("Total cash invested calculated: ${}", total);
        
        return total;
    }

    @Override
    public BigDecimal calculateLoanAmount(PurchaseDetails purchase) {
        log.debug("Calculating loan amount");
        
        if (purchase == null) {
            throw new IllegalArgumentException("PurchaseDetails cannot be null");
        }
        
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        if (purchasePrice == null) {
            log.warn("Purchase price is null");
            return null;
        }
        
        BigDecimal downPayment = purchase.getDownPaymentAmount();
        if (downPayment == null) {
            // If no down payment, loan amount equals purchase price
            log.debug("No down payment specified, loan amount equals purchase price: ${}", purchasePrice);
            return purchasePrice.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        }
        
        // Loan Amount = Purchase Price - Down Payment
        BigDecimal loanAmount = purchasePrice.subtract(downPayment).setScale(CURRENCY_SCALE, ROUNDING_MODE);
        log.debug("Loan amount calculated: ${} (Purchase: ${}, Down: ${})", 
                loanAmount, purchasePrice, downPayment);
        
        return loanAmount;
    }

    // ========================================
    // Projection Methods
    // ========================================

    @Override
    public List<YearlyProjection> projectCashFlow(RentalAnalysis analysis, Integer years, 
                                                   Double rentGrowthRate, Double expenseInflationRate) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        if (years == null || years < 1) {
            throw new IllegalArgumentException("Years must be at least 1");
        }
        
        if (years > MAX_PROJECTION_YEARS) {
            throw new IllegalArgumentException("Years cannot exceed " + MAX_PROJECTION_YEARS);
        }
        
        // Apply default values if not provided
        if (rentGrowthRate == null) {
            rentGrowthRate = DEFAULT_RENT_GROWTH_RATE;
            log.debug("Using default rent growth rate: {}%", rentGrowthRate);
        }
        
        if (expenseInflationRate == null) {
            expenseInflationRate = DEFAULT_EXPENSE_INFLATION_RATE;
            log.debug("Using default expense inflation rate: {}%", expenseInflationRate);
        }
        
        // Validate rate ranges
        if (rentGrowthRate < MIN_GROWTH_RATE || rentGrowthRate > MAX_GROWTH_RATE) {
            throw new IllegalArgumentException(
                    String.format("Rent growth rate must be between %.1f%% and %.1f%%", 
                            MIN_GROWTH_RATE, MAX_GROWTH_RATE));
        }
        
        if (expenseInflationRate < MIN_GROWTH_RATE || expenseInflationRate > MAX_GROWTH_RATE) {
            throw new IllegalArgumentException(
                    String.format("Expense inflation rate must be between %.1f%% and %.1f%%", 
                            MIN_GROWTH_RATE, MAX_GROWTH_RATE));
        }
        
        log.debug("Projecting cash flow for {} years with rent growth {}% and expense inflation {}%", 
                years, rentGrowthRate, expenseInflationRate);
        
        // Get required data
        IncomeProjection income = analysis.getIncomeProjection();
        ExpenseProjection expenses = analysis.getExpenseProjection();
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        FinancingDetails financing = analysis.getFinancingDetails();
        
        if (income == null || expenses == null || purchase == null) {
            log.warn("Missing required data for cash flow projection");
            return null;
        }
        
        // Get initial values
        BigDecimal monthlyRent = income.getMonthlyRent();
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        
        if (monthlyRent == null || purchasePrice == null) {
            log.warn("Missing monthly rent or purchase price");
            return null;
        }
        
        // Calculate initial annual values
        BigDecimal currentAnnualRent = calculateEffectiveMonthlyIncome(income)
                .multiply(MONTHS_PER_YEAR);
        
        BigDecimal monthlyExpenses = calculateTotalMonthlyExpenses(expenses, monthlyRent, purchasePrice);
        if (monthlyExpenses == null) {
            log.warn("Unable to calculate monthly expenses");
            return null;
        }
        BigDecimal currentAnnualExpenses = monthlyExpenses.multiply(MONTHS_PER_YEAR);
        
        // Calculate annual mortgage payment (constant for fixed-rate loan)
        BigDecimal annualMortgage = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        if (financing != null) {
            BigDecimal loanAmount = calculateLoanAmount(purchase);
            if (loanAmount != null && loanAmount.compareTo(BigDecimal.ZERO) > 0) {
                Double interestRate = financing.getInterestRate();
                Integer loanTermYears = financing.getLoanTermYears();
                
                if (interestRate != null && loanTermYears != null) {
                    BigDecimal monthlyPayment = calculateMonthlyMortgagePayment(loanAmount, interestRate, loanTermYears);
                    if (monthlyPayment != null) {
                        annualMortgage = monthlyPayment.multiply(MONTHS_PER_YEAR);
                    }
                }
            }
        }
        
        // Project cash flow for each year
        List<YearlyProjection> projections = new ArrayList<>();
        BigDecimal cumulativeCashFlow = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        
        BigDecimal rentGrowthMultiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(rentGrowthRate).divide(ONE_HUNDRED, 6, ROUNDING_MODE));
        BigDecimal expenseInflationMultiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(expenseInflationRate).divide(ONE_HUNDRED, 6, ROUNDING_MODE));
        
        for (int year = 1; year <= years; year++) {
            // Apply growth rates
            if (year > 1) {
                currentAnnualRent = currentAnnualRent.multiply(rentGrowthMultiplier)
                        .setScale(CURRENCY_SCALE, ROUNDING_MODE);
                currentAnnualExpenses = currentAnnualExpenses.multiply(expenseInflationMultiplier)
                        .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            }
            
            // Calculate cash flow for this year
            BigDecimal cashFlow = currentAnnualRent
                    .subtract(currentAnnualExpenses)
                    .subtract(annualMortgage)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            
            cumulativeCashFlow = cumulativeCashFlow.add(cashFlow);
            
            // Create projection for this year
            YearlyProjection projection = new YearlyProjection(
                    year,
                    currentAnnualRent,
                    currentAnnualExpenses,
                    annualMortgage,
                    cashFlow,
                    cumulativeCashFlow
            );
            
            projections.add(projection);
            
            log.trace("Year {}: Rent=${}, Expenses=${}, Mortgage=${}, CashFlow=${}", 
                    year, currentAnnualRent, currentAnnualExpenses, annualMortgage, cashFlow);
        }
        
        log.debug("Cash flow projection completed for {} years", years);
        return projections;
    }

    @Override
    public List<EquityProjection> projectEquityBuildup(RentalAnalysis analysis, Integer years, 
                                                        Double appreciationRate) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        if (years == null || years < 1) {
            throw new IllegalArgumentException("Years must be at least 1");
        }
        
        if (years > MAX_PROJECTION_YEARS) {
            throw new IllegalArgumentException("Years cannot exceed " + MAX_PROJECTION_YEARS);
        }
        
        // Apply default value if not provided
        if (appreciationRate == null) {
            appreciationRate = DEFAULT_APPRECIATION_RATE;
            log.debug("Using default appreciation rate: {}%", appreciationRate);
        }
        
        // Validate rate range
        if (appreciationRate < MIN_GROWTH_RATE || appreciationRate > MAX_GROWTH_RATE) {
            throw new IllegalArgumentException(
                    String.format("Appreciation rate must be between %.1f%% and %.1f%%", 
                            MIN_GROWTH_RATE, MAX_GROWTH_RATE));
        }
        
        log.debug("Projecting equity buildup for {} years with appreciation rate {}%", 
                years, appreciationRate);
        
        // Get required data
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        FinancingDetails financing = analysis.getFinancingDetails();
        
        if (purchase == null) {
            log.warn("Missing purchase details for equity projection");
            return null;
        }
        
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        BigDecimal downPayment = purchase.getDownPaymentAmount();
        
        if (purchasePrice == null) {
            log.warn("Missing purchase price");
            return null;
        }
        
        if (downPayment == null) {
            downPayment = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        }
        
        // Calculate initial values
        BigDecimal loanAmount;
        if (financing == null) {
            // Cash purchase - no loan
            loanAmount = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        } else {
            loanAmount = calculateLoanAmount(purchase);
            if (loanAmount == null) {
                loanAmount = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
            }
        }
        
        BigDecimal remainingBalance = loanAmount;
        BigDecimal totalPrincipalPaid = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        BigDecimal currentPropertyValue = purchasePrice;
        
        // Get mortgage details
        BigDecimal monthlyPayment = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        Double annualInterestRate = 0.0;
        
        if (financing != null && loanAmount.compareTo(BigDecimal.ZERO) > 0) {
            Double interestRate = financing.getInterestRate();
            Integer loanTermYears = financing.getLoanTermYears();
            
            if (interestRate != null && loanTermYears != null) {
                annualInterestRate = interestRate;
                monthlyPayment = calculateMonthlyMortgagePayment(loanAmount, interestRate, loanTermYears);
                if (monthlyPayment == null) {
                    monthlyPayment = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
                }
            }
        }
        
        // Project equity for each year
        List<EquityProjection> projections = new ArrayList<>();
        
        BigDecimal appreciationMultiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(appreciationRate).divide(ONE_HUNDRED, 6, ROUNDING_MODE));
        
        for (int year = 1; year <= years; year++) {
            // Calculate principal paid this year
            BigDecimal yearPrincipalPaid = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
            
            if (remainingBalance.compareTo(BigDecimal.ZERO) > 0 && monthlyPayment.compareTo(BigDecimal.ZERO) > 0) {
                double monthlyRate = annualInterestRate / 12.0 / 100.0;
                
                // Calculate principal paid each month for this year
                for (int month = 1; month <= 12; month++) {
                    if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }
                    
                    // Calculate interest payment for this month
                    BigDecimal interestPayment = remainingBalance
                            .multiply(BigDecimal.valueOf(monthlyRate))
                            .setScale(CURRENCY_SCALE, ROUNDING_MODE);
                    
                    // Calculate principal payment for this month
                    BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
                    
                    // Ensure we don't pay more principal than remaining balance
                    if (principalPayment.compareTo(remainingBalance) > 0) {
                        principalPayment = remainingBalance;
                    }
                    
                    // Update balances
                    remainingBalance = remainingBalance.subtract(principalPayment);
                    yearPrincipalPaid = yearPrincipalPaid.add(principalPayment);
                }
            }
            
            totalPrincipalPaid = totalPrincipalPaid.add(yearPrincipalPaid)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            
            // Apply appreciation
            currentPropertyValue = currentPropertyValue.multiply(appreciationMultiplier)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            
            // Calculate cumulative appreciation
            BigDecimal cumulativeAppreciation = currentPropertyValue.subtract(purchasePrice)
                    .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            
            // Calculate total equity
            // For cash purchase (no financing), equity = property value
            // For financed purchase, equity = down payment + principal paid + appreciation
            BigDecimal totalEquity;
            if (financing == null) {
                // Cash purchase: equity equals current property value
                totalEquity = currentPropertyValue;
            } else {
                // Financed purchase: equity = down payment + principal paid + appreciation
                totalEquity = downPayment
                        .add(totalPrincipalPaid)
                        .add(cumulativeAppreciation)
                        .setScale(CURRENCY_SCALE, ROUNDING_MODE);
            }
            
            // Calculate LTV ratio
            Double ltvRatio = null;
            if (currentPropertyValue.compareTo(BigDecimal.ZERO) > 0 && remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
                ltvRatio = remainingBalance
                        .divide(currentPropertyValue, PERCENTAGE_SCALE + 2, ROUNDING_MODE)
                        .multiply(ONE_HUNDRED)
                        .setScale(PERCENTAGE_SCALE, ROUNDING_MODE)
                        .doubleValue();
            }
            
            // Create projection for this year
            EquityProjection projection = new EquityProjection(
                    year,
                    totalEquity,
                    totalPrincipalPaid,
                    cumulativeAppreciation,
                    currentPropertyValue,
                    remainingBalance,
                    ltvRatio
            );
            
            projections.add(projection);
            
            log.trace("Year {}: Equity=${}, PrincipalPaid=${}, Appreciation=${}, PropertyValue=${}, LoanBalance=${}", 
                    year, totalEquity, totalPrincipalPaid, cumulativeAppreciation, currentPropertyValue, remainingBalance);
        }
        
        log.debug("Equity projection completed for {} years", years);
        return projections;
    }

    // ========================================
    // Validation Methods
    // ========================================

    @Override
    public ValidationResult validateAnalysisInputs(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.debug("Validating analysis inputs for analysis: {}", analysis.getId());
        
        ValidationResult.Builder builder = new ValidationResult.Builder();
        
        // Validate each component
        validatePurchaseDetails(analysis.getPurchaseDetails(), builder);
        validateFinancingDetails(analysis.getFinancingDetails(), analysis.getPurchaseDetails(), builder);
        validateIncomeProjection(analysis.getIncomeProjection(), analysis.getPurchaseDetails(), builder);
        validateExpenseProjection(analysis.getExpenseProjection(), analysis.getIncomeProjection(), 
                                 analysis.getPurchaseDetails(), builder);
        
        // Validate calculated metrics (only if no critical errors)
        ValidationResult preliminaryResult = builder.build();
        if (preliminaryResult.isValid()) {
            validateCalculatedMetrics(analysis, builder);
        }
        
        ValidationResult result = builder.build();
        log.debug("Validation completed: {}", result.getSummary());
        
        return result;
    }
    
    /**
     * Validates purchase details.
     */
    private void validatePurchaseDetails(PurchaseDetails purchase, ValidationResult.Builder builder) {
        if (purchase == null) {
            builder.addError("Purchase details are required");
            return;
        }
        
        // Purchase price validation
        BigDecimal purchasePrice = purchase.getPurchasePrice();
        if (purchasePrice == null) {
            builder.addError("Purchase price is required");
        } else if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            builder.addError("Purchase price must be greater than $0 (current: $" + purchasePrice + ")");
        } else {
            // Warning for unusual purchase prices
            if (purchasePrice.compareTo(new BigDecimal("10000")) < 0) {
                builder.addWarning("Purchase price is very low ($" + purchasePrice + "). Expected range: $10,000 - $100,000,000");
            } else if (purchasePrice.compareTo(new BigDecimal("100000000")) > 0) {
                builder.addWarning("Purchase price is very high ($" + purchasePrice + "). Expected range: $10,000 - $100,000,000");
            }
        }
        
        // Down payment validation
        BigDecimal downPayment = purchase.getDownPaymentAmount();
        if (downPayment == null) {
            downPayment = BigDecimal.ZERO;
        }
        
        if (downPayment.compareTo(BigDecimal.ZERO) < 0) {
            builder.addError("Down payment cannot be negative (current: $" + downPayment + ")");
        } else if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            if (downPayment.compareTo(purchasePrice) > 0) {
                builder.addError("Down payment ($" + downPayment + ") cannot exceed purchase price ($" + purchasePrice + ")");
            } else {
                // Calculate down payment percentage
                BigDecimal downPaymentPercent = downPayment
                        .divide(purchasePrice, 4, ROUNDING_MODE)
                        .multiply(ONE_HUNDRED);
                
                if (downPaymentPercent.compareTo(new BigDecimal("3")) < 0 && downPayment.compareTo(BigDecimal.ZERO) > 0) {
                    builder.addWarning("Very low down payment (" + downPaymentPercent.setScale(2, ROUNDING_MODE) + "%). " +
                                     "Typical range: 3% - 50%");
                } else if (downPaymentPercent.compareTo(new BigDecimal("50")) > 0) {
                    builder.addWarning("Unusually high down payment (" + downPaymentPercent.setScale(2, ROUNDING_MODE) + "%). " +
                                     "Typical range: 3% - 50%");
                }
            }
        }
        
        // Closing costs validation
        BigDecimal closingCosts = purchase.getClosingCosts();
        if (closingCosts != null && purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            if (closingCosts.compareTo(BigDecimal.ZERO) < 0) {
                builder.addError("Closing costs cannot be negative (current: $" + closingCosts + ")");
            } else {
                BigDecimal closingCostsPercent = closingCosts
                        .divide(purchasePrice, 4, ROUNDING_MODE)
                        .multiply(ONE_HUNDRED);
                
                if (closingCostsPercent.compareTo(new BigDecimal("2")) < 0 && closingCosts.compareTo(BigDecimal.ZERO) > 0) {
                    builder.addWarning("Closing costs are low (" + closingCostsPercent.setScale(2, ROUNDING_MODE) + "%). " +
                                     "Typical range: 2% - 5% of purchase price");
                } else if (closingCostsPercent.compareTo(new BigDecimal("5")) > 0) {
                    builder.addWarning("Closing costs are high (" + closingCostsPercent.setScale(2, ROUNDING_MODE) + "%). " +
                                     "Typical range: 2% - 5% of purchase price");
                }
            }
        }
        
        // Renovation costs validation
        BigDecimal renovationCosts = purchase.getRenovationCosts();
        if (renovationCosts != null && renovationCosts.compareTo(BigDecimal.ZERO) < 0) {
            builder.addError("Renovation costs cannot be negative (current: $" + renovationCosts + ")");
        }
    }
    
    /**
     * Validates financing details.
     */
    private void validateFinancingDetails(FinancingDetails financing, PurchaseDetails purchase, 
                                         ValidationResult.Builder builder) {
        if (financing == null) {
            // Financing is optional (cash purchase)
            return;
        }
        
        // Loan amount validation
        BigDecimal loanAmount = financing.getLoanAmount();
        if (loanAmount == null) {
            loanAmount = BigDecimal.ZERO;
        }
        
        if (loanAmount.compareTo(BigDecimal.ZERO) < 0) {
            builder.addError("Loan amount cannot be negative (current: $" + loanAmount + ")");
        }
        
        // Interest rate validation
        Double interestRate = financing.getInterestRate();
        if (loanAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (interestRate == null) {
                builder.addError("Interest rate is required when loan amount is greater than $0");
            } else if (interestRate < 0 || interestRate > 20) {
                builder.addError("Interest rate must be between 0% and 20% (current: " + interestRate + "%)");
            } else if (interestRate == 0) {
                builder.addError("Interest rate must be greater than 0% when loan amount is greater than $0 (current: " + interestRate + "%)");
            } else if (interestRate > 12) {
                builder.addWarning("Very high interest rate (" + interestRate + "%). Typical range: 3% - 12%");
            }
        }
        
        // Loan term validation
        Integer loanTermYears = financing.getLoanTermYears();
        if (loanAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (loanTermYears == null) {
                builder.addError("Loan term is required when loan amount is greater than $0");
            } else if (loanTermYears < 1 || loanTermYears > 40) {
                builder.addError("Loan term must be between 1 and 40 years (current: " + loanTermYears + " years)");
            } else if (loanTermYears != 15 && loanTermYears != 20 && loanTermYears != 30) {
                builder.addWarning("Unusual loan term (" + loanTermYears + " years). Common terms: 15, 20, or 30 years");
            }
        }
        
        // Validate loan amount against purchase price
        if (purchase != null && purchase.getPurchasePrice() != null) {
            BigDecimal purchasePrice = purchase.getPurchasePrice();
            BigDecimal downPayment = purchase.getDownPaymentAmount();
            if (downPayment == null) {
                downPayment = BigDecimal.ZERO;
            }
            
            BigDecimal expectedLoanAmount = purchasePrice.subtract(downPayment);
            if (loanAmount.compareTo(BigDecimal.ZERO) > 0 && 
                loanAmount.subtract(expectedLoanAmount).abs().compareTo(new BigDecimal("100")) > 0) {
                builder.addWarning("Loan amount ($" + loanAmount + ") does not match purchase price ($" + 
                                 purchasePrice + ") minus down payment ($" + downPayment + "). Expected: $" + 
                                 expectedLoanAmount.setScale(2, ROUNDING_MODE));
            }
        }
    }
    
    /**
     * Validates income projection.
     */
    private void validateIncomeProjection(IncomeProjection income, PurchaseDetails purchase, 
                                         ValidationResult.Builder builder) {
        if (income == null) {
            builder.addError("Income projection is required");
            return;
        }
        
        // Monthly rent validation
        BigDecimal monthlyRent = income.getMonthlyRent();
        if (monthlyRent == null) {
            builder.addError("Monthly rent is required");
        } else if (monthlyRent.compareTo(BigDecimal.ZERO) <= 0) {
            builder.addError("Monthly rent must be greater than $0 (current: $" + monthlyRent + ")");
        } else if (purchase != null && purchase.getPurchasePrice() != null) {
            BigDecimal purchasePrice = purchase.getPurchasePrice();
            if (purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                // Calculate monthly rent as percentage of purchase price
                BigDecimal rentToValueRatio = monthlyRent
                        .divide(purchasePrice, 6, ROUNDING_MODE)
                        .multiply(ONE_HUNDRED);
                
                if (rentToValueRatio.compareTo(new BigDecimal("0.5")) < 0) {
                    builder.addWarning("Monthly rent is low relative to purchase price (" + 
                                     rentToValueRatio.setScale(3, ROUNDING_MODE) + "% of property value). " +
                                     "Typical range: 0.5% - 2% per month");
                } else if (rentToValueRatio.compareTo(new BigDecimal("2")) > 0) {
                    builder.addWarning("Monthly rent is high relative to purchase price (" + 
                                     rentToValueRatio.setScale(3, ROUNDING_MODE) + "% of property value). " +
                                     "Typical range: 0.5% - 2% per month");
                }
            }
        }
        
        // Vacancy rate validation
        Double vacancyRate = income.getVacancyRate();
        if (vacancyRate == null) {
            vacancyRate = 0.0;
        }
        
        if (vacancyRate < 0 || vacancyRate > 50) {
            builder.addError("Vacancy rate must be between 0% and 50% (current: " + vacancyRate + "%)");
        } else if (vacancyRate > 20) {
            builder.addWarning("Very high vacancy rate (" + vacancyRate + "%). Typical range: 5% - 20%");
        }
        
        // Other monthly income validation
        BigDecimal otherIncome = income.getOtherMonthlyIncome();
        if (otherIncome != null && otherIncome.compareTo(BigDecimal.ZERO) < 0) {
            builder.addError("Other monthly income cannot be negative (current: $" + otherIncome + ")");
        }
    }
    
    /**
     * Validates expense projection.
     */
    private void validateExpenseProjection(ExpenseProjection expenses, IncomeProjection income, 
                                          PurchaseDetails purchase, ValidationResult.Builder builder) {
        if (expenses == null) {
            builder.addError("Expense projection is required");
            return;
        }
        
        BigDecimal purchasePrice = (purchase != null) ? purchase.getPurchasePrice() : null;
        BigDecimal monthlyRent = (income != null) ? income.getMonthlyRent() : null;
        
        // Property tax validation
        BigDecimal propertyTax = expenses.getAnnualPropertyTax();
        if (propertyTax != null) {
            if (propertyTax.compareTo(BigDecimal.ZERO) < 0) {
                builder.addError("Annual property tax cannot be negative (current: $" + propertyTax + ")");
            } else if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal taxRate = propertyTax
                        .divide(purchasePrice, 6, ROUNDING_MODE)
                        .multiply(ONE_HUNDRED);
                
                if (taxRate.compareTo(new BigDecimal("0.5")) < 0 && propertyTax.compareTo(BigDecimal.ZERO) > 0) {
                    builder.addWarning("Property tax is low (" + taxRate.setScale(2, ROUNDING_MODE) + "% annually). " +
                                     "Typical range: 0.5% - 3% of property value");
                } else if (taxRate.compareTo(new BigDecimal("3")) > 0) {
                    builder.addWarning("Property tax is high (" + taxRate.setScale(2, ROUNDING_MODE) + "% annually). " +
                                     "Typical range: 0.5% - 3% of property value");
                }
            }
        }
        
        // Insurance validation
        BigDecimal insurance = expenses.getAnnualInsurance();
        if (insurance != null) {
            if (insurance.compareTo(BigDecimal.ZERO) < 0) {
                builder.addError("Annual insurance cannot be negative (current: $" + insurance + ")");
            } else if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal insuranceRate = insurance
                        .divide(purchasePrice, 6, ROUNDING_MODE)
                        .multiply(ONE_HUNDRED);
                
                if (insuranceRate.compareTo(new BigDecimal("0.3")) < 0 && insurance.compareTo(BigDecimal.ZERO) > 0) {
                    builder.addWarning("Insurance is low (" + insuranceRate.setScale(2, ROUNDING_MODE) + "% annually). " +
                                     "Typical range: 0.3% - 1.5% of property value");
                } else if (insuranceRate.compareTo(new BigDecimal("1.5")) > 0) {
                    builder.addWarning("Insurance is high (" + insuranceRate.setScale(2, ROUNDING_MODE) + "% annually). " +
                                     "Typical range: 0.3% - 1.5% of property value");
                }
            }
        }
        
        // HOA fees validation
        BigDecimal hoaFees = expenses.getMonthlyHOAFees();
        if (hoaFees != null && hoaFees.compareTo(BigDecimal.ZERO) < 0) {
            builder.addError("Monthly HOA fees cannot be negative (current: $" + hoaFees + ")");
        }
        
        // Property management validation
        Double managementPercent = expenses.getPropertyManagementPercent();
        if (managementPercent != null) {
            if (managementPercent < 0) {
                builder.addError("Property management percentage cannot be negative (current: " + managementPercent + "%)");
            } else if (managementPercent > 0 && (monthlyRent == null || monthlyRent.compareTo(BigDecimal.ZERO) <= 0)) {
                builder.addError("Property management percentage requires monthly rent to be greater than $0");
            } else if (managementPercent > 0 && (managementPercent < 8 || managementPercent > 12)) {
                builder.addWarning("Property management fee (" + managementPercent + "%) is outside typical range: 8% - 12% of rent");
            }
        }
        
        // Maintenance validation
        Double maintenancePercent = expenses.getMaintenancePercent();
        if (maintenancePercent != null) {
            if (maintenancePercent < 0) {
                builder.addError("Maintenance percentage cannot be negative (current: " + maintenancePercent + "%)");
            } else if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                if (maintenancePercent < 0.5 && maintenancePercent > 0) {
                    builder.addWarning("Maintenance budget is low (" + maintenancePercent + "% annually). " +
                                     "Typical range: 0.5% - 2% of property value");
                } else if (maintenancePercent > 2) {
                    builder.addWarning("Maintenance budget is high (" + maintenancePercent + "% annually). " +
                                     "Typical range: 0.5% - 2% of property value");
                }
            }
        }
        
        // CapEx validation
        Double capExPercent = expenses.getCapExPercent();
        if (capExPercent != null && capExPercent < 0) {
            builder.addError("CapEx percentage cannot be negative (current: " + capExPercent + "%)");
        }
        
        // Utilities validation
        BigDecimal utilities = expenses.getMonthlyUtilities();
        if (utilities != null && utilities.compareTo(BigDecimal.ZERO) < 0) {
            builder.addError("Monthly utilities cannot be negative (current: $" + utilities + ")");
        }
        
        // Other expenses validation
        BigDecimal otherExpenses = expenses.getOtherMonthlyExpenses();
        if (otherExpenses != null && otherExpenses.compareTo(BigDecimal.ZERO) < 0) {
            builder.addError("Other monthly expenses cannot be negative (current: $" + otherExpenses + ")");
        }
        
        // Total expenses validation
        if (monthlyRent != null && monthlyRent.compareTo(BigDecimal.ZERO) > 0 && 
            purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal totalMonthlyExpenses = calculateTotalMonthlyExpenses(expenses, monthlyRent, purchasePrice);
            if (totalMonthlyExpenses != null) {
                BigDecimal annualExpenses = totalMonthlyExpenses.multiply(MONTHS_PER_YEAR);
                BigDecimal annualRent = monthlyRent.multiply(MONTHS_PER_YEAR);
                
                BigDecimal expenseRatio = annualExpenses
                        .divide(annualRent, 4, ROUNDING_MODE)
                        .multiply(ONE_HUNDRED);
                
                if (expenseRatio.compareTo(new BigDecimal("80")) > 0) {
                    builder.addWarning("Expenses are very high (" + expenseRatio.setScale(2, ROUNDING_MODE) + 
                                     "% of rental income). This may result in negative cash flow");
                }
            }
        }
    }
    
    /**
     * Validates calculated metrics.
     */
    private void validateCalculatedMetrics(RentalAnalysis analysis, ValidationResult.Builder builder) {
        // Cash flow validation
        BigDecimal monthlyCashFlow = calculateMonthlyCashFlow(analysis);
        if (monthlyCashFlow != null && monthlyCashFlow.compareTo(BigDecimal.ZERO) < 0) {
            builder.addWarning("Property has negative cash flow ($" + monthlyCashFlow + " per month). " +
                             "This property will lose money each month");
        }
        
        // Gross yield validation
        Double grossYield = calculateGrossYield(analysis);
        if (grossYield != null && grossYield < 3.0) {
            builder.addWarning("Very low gross yield (" + String.format("%.2f", grossYield) + "%). " +
                             "Typical range: 5% - 12%");
        }
        
        // Cap rate validation
        Double capRate = calculateCapRate(analysis);
        if (capRate != null && capRate < 2.0) {
            builder.addWarning("Very low cap rate (" + String.format("%.2f", capRate) + "%). " +
                             "Typical range: 4% - 10%");
        }
        
        // Cash-on-cash return validation
        Double cashOnCash = calculateCashOnCashReturn(analysis);
        if (cashOnCash != null && cashOnCash < 0) {
            builder.addWarning("Negative return on investment (" + String.format("%.2f", cashOnCash) + "%). " +
                             "This investment will lose money");
        }
        
        // Effective income vs expenses validation
        IncomeProjection income = analysis.getIncomeProjection();
        ExpenseProjection expenses = analysis.getExpenseProjection();
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        FinancingDetails financing = analysis.getFinancingDetails();
        
        if (income != null && expenses != null && purchase != null) {
            BigDecimal effectiveIncome = calculateEffectiveMonthlyIncome(income);
            BigDecimal totalExpenses = calculateTotalMonthlyExpenses(expenses, income.getMonthlyRent(), 
                                                                     purchase.getPurchasePrice());
            
            if (effectiveIncome != null && totalExpenses != null) {
                BigDecimal mortgagePayment = BigDecimal.ZERO;
                if (financing != null && financing.getLoanAmount() != null && 
                    financing.getLoanAmount().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal loanAmount = financing.getLoanAmount();
                    Double interestRate = financing.getInterestRate();
                    Integer loanTermYears = financing.getLoanTermYears();
                    
                    if (interestRate != null && loanTermYears != null) {
                        BigDecimal payment = calculateMonthlyMortgagePayment(loanAmount, interestRate, loanTermYears);
                        if (payment != null) {
                            mortgagePayment = payment;
                        }
                    }
                }
                
                BigDecimal totalMonthlyObligations = totalExpenses.add(mortgagePayment);
                if (effectiveIncome.compareTo(totalMonthlyObligations) < 0) {
                    builder.addWarning("Effective monthly income ($" + effectiveIncome + 
                                     ") is less than monthly expenses + mortgage ($" + totalMonthlyObligations + ")");
                }
            }
        }
    }

    @Override
    public CalculatedMetrics calculateAllMetrics(RentalAnalysis analysis) {
        if (analysis == null) {
            throw new IllegalArgumentException("RentalAnalysis cannot be null");
        }
        
        log.info("Calculating all metrics for analysis: {}", analysis.getId());
        
        // Step 1: Validate inputs first
        ValidationResult validationResult = validateAnalysisInputs(analysis);
        
        if (!validationResult.isValid()) {
            String errorMessage = "Validation failed: " + String.join(", ", validationResult.getErrors());
            log.error("Cannot calculate metrics - validation errors: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        // Log warnings but continue
        if (validationResult.hasWarnings()) {
            log.warn("Validation warnings for analysis {}: {}", 
                    analysis.getId(), String.join(", ", validationResult.getWarnings()));
        }
        
        // Step 2: Calculate and populate all derived fields
        populateDerivedFields(analysis);
        
        // Step 3: Create CalculatedMetrics entity
        CalculatedMetrics metrics = new CalculatedMetrics();
        metrics.setRentalAnalysis(analysis);
        
        // Track calculation errors
        List<String> calculationErrors = new ArrayList<>();
        
        // Step 4: Calculate basic metrics (always calculate)
        try {
            log.debug("Calculating gross yield...");
            Double grossYield = calculateGrossYield(analysis);
            metrics.setGrossYield(grossYield);
            log.info("Gross yield calculated: {}%", grossYield);
        } catch (Exception e) {
            log.error("Failed to calculate gross yield", e);
            calculationErrors.add("Gross Yield: " + e.getMessage());
            metrics.setGrossYield(null);
        }
        
        try {
            log.debug("Calculating net yield...");
            Double netYield = calculateNetYield(analysis);
            metrics.setNetYield(netYield);
            log.info("Net yield calculated: {}%", netYield);
        } catch (Exception e) {
            log.error("Failed to calculate net yield", e);
            calculationErrors.add("Net Yield: " + e.getMessage());
            metrics.setNetYield(null);
        }
        
        try {
            log.debug("Calculating monthly cash flow...");
            BigDecimal monthlyCashFlow = calculateMonthlyCashFlow(analysis);
            metrics.setMonthlyCashFlow(monthlyCashFlow);
            log.info("Monthly cash flow calculated: ${}", monthlyCashFlow);
        } catch (Exception e) {
            log.error("Failed to calculate monthly cash flow", e);
            calculationErrors.add("Monthly Cash Flow: " + e.getMessage());
            metrics.setMonthlyCashFlow(null);
        }
        
        try {
            log.debug("Calculating annual cash flow...");
            BigDecimal annualCashFlow = calculateAnnualCashFlow(analysis);
            metrics.setAnnualCashFlow(annualCashFlow);
            log.info("Annual cash flow calculated: ${}", annualCashFlow);
        } catch (Exception e) {
            log.error("Failed to calculate annual cash flow", e);
            calculationErrors.add("Annual Cash Flow: " + e.getMessage());
            metrics.setAnnualCashFlow(null);
        }
        
        // Step 5: Calculate advanced metrics (calculate if enabled/possible)
        try {
            log.debug("Calculating cap rate...");
            Double capRate = calculateCapRate(analysis);
            metrics.setCapRate(capRate);
            log.info("Cap rate calculated: {}%", capRate);
        } catch (Exception e) {
            log.error("Failed to calculate cap rate", e);
            calculationErrors.add("Cap Rate: " + e.getMessage());
            metrics.setCapRate(null);
        }
        
        try {
            log.debug("Calculating cash-on-cash return...");
            Double cashOnCashReturn = calculateCashOnCashReturn(analysis);
            metrics.setCashOnCashReturn(cashOnCashReturn);
            log.info("Cash-on-cash return calculated: {}%", cashOnCashReturn);
        } catch (Exception e) {
            log.error("Failed to calculate cash-on-cash return", e);
            calculationErrors.add("Cash-on-Cash Return: " + e.getMessage());
            metrics.setCashOnCashReturn(null);
        }
        
        try {
            log.debug("Calculating ROI (Year 1, 3% appreciation)...");
            Double roi = calculateROI(analysis, 1, 3.0);
            metrics.setTotalROI(roi);
            log.info("ROI calculated: {}%", roi);
        } catch (Exception e) {
            log.error("Failed to calculate ROI", e);
            calculationErrors.add("ROI: " + e.getMessage());
            metrics.setTotalROI(null);
        }
        
        // Calculate Debt Service Coverage Ratio (DSCR)
        try {
            log.debug("Calculating debt service coverage ratio...");
            Double dscr = calculateDebtServiceCoverageRatio(analysis);
            metrics.setDebtServiceCoverageRatio(dscr);
            log.info("Debt service coverage ratio calculated: {}", dscr);
        } catch (Exception e) {
            log.error("Failed to calculate debt service coverage ratio", e);
            calculationErrors.add("DSCR: " + e.getMessage());
            metrics.setDebtServiceCoverageRatio(null);
        }
        
        // Calculate Break-Even Ratio
        try {
            log.debug("Calculating break-even ratio...");
            Double breakEvenRatio = calculateBreakEvenRatio(analysis);
            metrics.setBreakEvenRatio(breakEvenRatio);
            log.info("Break-even ratio calculated: {}%", breakEvenRatio);
        } catch (Exception e) {
            log.error("Failed to calculate break-even ratio", e);
            calculationErrors.add("Break-Even Ratio: " + e.getMessage());
            metrics.setBreakEvenRatio(null);
        }
        
        // Step 6: Check if any critical calculations failed
        if (!calculationErrors.isEmpty()) {
            log.warn("Some metrics could not be calculated: {}", String.join("; ", calculationErrors));
        }
        
        log.info("All metrics calculation completed for analysis: {}", analysis.getId());
        
        return metrics;
    }
    
    /**
     * Populates all derived fields in the analysis entities.
     * This includes calculating loan amounts, monthly payments, and expense amounts.
     */
    private void populateDerivedFields(RentalAnalysis analysis) {
        log.debug("Populating derived fields for analysis: {}", analysis.getId());
        
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        FinancingDetails financing = analysis.getFinancingDetails();
        IncomeProjection income = analysis.getIncomeProjection();
        ExpenseProjection expenses = analysis.getExpenseProjection();
        
        // Populate PurchaseDetails derived fields
        if (purchase != null) {
            // Calculate down payment amount/percent if one is missing
            if (purchase.getDownPaymentAmount() == null && purchase.getDownPaymentPercent() != null) {
                purchase.calculateDownPaymentAmount();
                log.debug("Calculated down payment amount: ${}", purchase.getDownPaymentAmount());
            } else if (purchase.getDownPaymentPercent() == null && purchase.getDownPaymentAmount() != null) {
                purchase.calculateDownPaymentPercent();
                log.debug("Calculated down payment percent: {}%", purchase.getDownPaymentPercent());
            }
            
            // Calculate closing costs amount/percent if one is missing
            if (purchase.getClosingCosts() == null && purchase.getClosingCostsPercent() != null) {
                purchase.calculateClosingCosts();
                log.debug("Calculated closing costs: ${}", purchase.getClosingCosts());
            } else if (purchase.getClosingCostsPercent() == null && purchase.getClosingCosts() != null) {
                purchase.calculateClosingCostsPercent();
                log.debug("Calculated closing costs percent: {}%", purchase.getClosingCostsPercent());
            }
        }
        
        // Populate FinancingDetails derived fields
        if (financing != null && purchase != null) {
            // Calculate loan amount if not set
            if (financing.getLoanAmount() == null) {
                financing.calculateLoanAmount(purchase.getPurchasePrice(), purchase.getDownPaymentAmount());
                log.debug("Calculated loan amount: ${}", financing.getLoanAmount());
            }
            
            // Calculate monthly payment
            financing.calculateMonthlyPayment();
            log.debug("Calculated monthly payment: ${}", financing.getMonthlyPayment());
        }
        
        // Populate ExpenseProjection derived fields
        if (expenses != null) {
            if (income != null && income.getMonthlyRent() != null) {
                // Calculate property management amount from percent
                if (expenses.getPropertyManagementAmount() == null && expenses.getPropertyManagementPercent() != null) {
                    expenses.calculatePropertyManagementAmount(income.getMonthlyRent());
                    log.debug("Calculated property management amount: ${}", expenses.getPropertyManagementAmount());
                }
            }
            
            if (purchase != null && purchase.getPurchasePrice() != null) {
                // Calculate maintenance amount from percent
                if (expenses.getMaintenanceAmount() == null && expenses.getMaintenancePercent() != null) {
                    expenses.calculateMaintenanceAmount(purchase.getPurchasePrice());
                    log.debug("Calculated maintenance amount: ${}", expenses.getMaintenanceAmount());
                }
                
                // Calculate CapEx amount from percent
                if (expenses.getCapExAmount() == null && expenses.getCapExPercent() != null) {
                    expenses.calculateCapExAmount(purchase.getPurchasePrice());
                    log.debug("Calculated CapEx amount: ${}", expenses.getCapExAmount());
                }
            }
        }
        
        log.debug("Derived fields population completed");
    }
    
    /**
     * Calculates the Debt Service Coverage Ratio (DSCR).
     * DSCR = Net Operating Income / Annual Debt Service
     * A ratio above 1.0 means the property generates enough income to cover debt payments.
     */
    private Double calculateDebtServiceCoverageRatio(RentalAnalysis analysis) {
        log.debug("Calculating debt service coverage ratio");
        
        // Calculate NOI
        BigDecimal noi = calculateNetOperatingIncome(analysis);
        if (noi == null) {
            log.warn("Cannot calculate DSCR - NOI is null");
            return null;
        }
        
        // Get financing details
        FinancingDetails financing = analysis.getFinancingDetails();
        if (financing == null || financing.getLoanAmount() == null || 
            financing.getLoanAmount().compareTo(BigDecimal.ZERO) == 0) {
            log.debug("No financing - DSCR not applicable for cash purchase");
            return null;
        }
        
        // Calculate annual debt service
        BigDecimal monthlyPayment = financing.getMonthlyPayment();
        if (monthlyPayment == null) {
            monthlyPayment = calculateMonthlyMortgagePayment(
                financing.getLoanAmount(), 
                financing.getInterestRate(), 
                financing.getLoanTermYears()
            );
        }
        
        if (monthlyPayment == null || monthlyPayment.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Cannot calculate DSCR - monthly payment is null or zero");
            return null;
        }
        
        BigDecimal annualDebtService = monthlyPayment.multiply(MONTHS_PER_YEAR);
        
        // Calculate DSCR
        BigDecimal dscr = noi.divide(annualDebtService, PERCENTAGE_SCALE + 2, ROUNDING_MODE);
        
        double result = dscr.doubleValue();
        log.debug("DSCR calculated: {} (NOI: ${}, Annual Debt Service: ${})", 
                result, noi, annualDebtService);
        
        return result;
    }
    
    /**
     * Calculates the Break-Even Ratio.
     * Break-Even Ratio = (Operating Expenses + Debt Service) / Gross Operating Income
     * Shows what percentage of income is needed to cover expenses and debt.
     * Lower is better - typically should be below 85%.
     */
    private Double calculateBreakEvenRatio(RentalAnalysis analysis) {
        log.debug("Calculating break-even ratio");
        
        IncomeProjection income = analysis.getIncomeProjection();
        ExpenseProjection expenses = analysis.getExpenseProjection();
        PurchaseDetails purchase = analysis.getPurchaseDetails();
        FinancingDetails financing = analysis.getFinancingDetails();
        
        if (income == null || expenses == null || purchase == null) {
            log.warn("Cannot calculate break-even ratio - missing required data");
            return null;
        }
        
        // Calculate gross operating income (before vacancy)
        BigDecimal monthlyRent = income.getMonthlyRent();
        if (monthlyRent == null) {
            log.warn("Cannot calculate break-even ratio - monthly rent is null");
            return null;
        }
        
        BigDecimal grossIncome = monthlyRent;
        if (income.getOtherMonthlyIncome() != null) {
            grossIncome = grossIncome.add(income.getOtherMonthlyIncome());
        }
        BigDecimal annualGrossIncome = grossIncome.multiply(MONTHS_PER_YEAR);
        
        if (annualGrossIncome.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Cannot calculate break-even ratio - gross income is zero");
            return null;
        }
        
        // Calculate annual operating expenses
        BigDecimal monthlyExpenses = calculateTotalMonthlyExpenses(expenses, monthlyRent, purchase.getPurchasePrice());
        if (monthlyExpenses == null) {
            log.warn("Cannot calculate break-even ratio - expenses calculation failed");
            return null;
        }
        BigDecimal annualExpenses = monthlyExpenses.multiply(MONTHS_PER_YEAR);
        
        // Calculate annual debt service
        BigDecimal annualDebtService = BigDecimal.ZERO;
        if (financing != null && financing.getLoanAmount() != null && 
            financing.getLoanAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyPayment = financing.getMonthlyPayment();
            if (monthlyPayment == null) {
                monthlyPayment = calculateMonthlyMortgagePayment(
                    financing.getLoanAmount(), 
                    financing.getInterestRate(), 
                    financing.getLoanTermYears()
                );
            }
            if (monthlyPayment != null) {
                annualDebtService = monthlyPayment.multiply(MONTHS_PER_YEAR);
            }
        }
        
        // Calculate break-even ratio
        BigDecimal totalAnnualObligations = annualExpenses.add(annualDebtService);
        BigDecimal breakEvenRatio = totalAnnualObligations
                .divide(annualGrossIncome, PERCENTAGE_SCALE + 2, ROUNDING_MODE)
                .multiply(ONE_HUNDRED);
        
        double result = breakEvenRatio.doubleValue();
        log.debug("Break-even ratio calculated: {}% (Obligations: ${}, Gross Income: ${})", 
                result, totalAnnualObligations, annualGrossIncome);
        
        return result;
    }
}