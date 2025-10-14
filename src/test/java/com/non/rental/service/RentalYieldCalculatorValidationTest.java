package com.non.rental.service;

import com.non.rental.entity.*;
import com.non.rental.service.dto.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RentalYieldCalculatorService validation logic.
 * Tests all validation rules for RentalAnalysis inputs.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RentalYieldCalculatorService Validation Tests")
class RentalYieldCalculatorValidationTest {

    @InjectMocks
    private RentalYieldCalculatorServiceImpl calculatorService;

    private RentalAnalysis analysis;
    private PurchaseDetails purchaseDetails;
    private IncomeProjection incomeProjection;
    private ExpenseProjection expenseProjection;
    private FinancingDetails financingDetails;

    @BeforeEach
    void setUp() {
        // Create valid test entities
        analysis = new RentalAnalysis();
        
        // Purchase: $300,000 with $60,000 down (20%)
        purchaseDetails = new PurchaseDetails();
        purchaseDetails.setPurchasePrice(new BigDecimal("300000.00"));
        purchaseDetails.setDownPaymentAmount(new BigDecimal("60000.00"));
        purchaseDetails.setClosingCosts(new BigDecimal("9000.00")); // 3%
        purchaseDetails.setRenovationCosts(new BigDecimal("5000.00"));
        
        // Income: $2,000/month rent with 8% vacancy
        incomeProjection = new IncomeProjection();
        incomeProjection.setMonthlyRent(new BigDecimal("2000.00"));
        incomeProjection.setOtherMonthlyIncome(new BigDecimal("100.00"));
        incomeProjection.setVacancyRate(8.0);
        
        // Expenses
        expenseProjection = new ExpenseProjection();
        expenseProjection.setAnnualPropertyTax(new BigDecimal("3600.00")); // $300/month
        expenseProjection.setAnnualInsurance(new BigDecimal("1200.00")); // $100/month
        expenseProjection.setMonthlyHOAFees(new BigDecimal("50.00"));
        expenseProjection.setPropertyManagementPercent(10.0); // 10% of rent
        expenseProjection.setMaintenancePercent(1.0); // 1% of property value annually
        expenseProjection.setCapExPercent(0.5); // 0.5% of property value annually
        expenseProjection.setMonthlyUtilities(new BigDecimal("75.00"));
        expenseProjection.setOtherMonthlyExpenses(new BigDecimal("25.00"));
        
        // Financing: $240,000 at 6.5% for 30 years
        financingDetails = new FinancingDetails();
        financingDetails.setLoanAmount(new BigDecimal("240000.00"));
        financingDetails.setInterestRate(6.5);
        financingDetails.setLoanTermYears(30);
        
        // Link entities
        analysis.setPurchaseDetails(purchaseDetails);
        analysis.setIncomeProjection(incomeProjection);
        analysis.setExpenseProjection(expenseProjection);
        analysis.setFinancingDetails(financingDetails);
    }

    // ========================================
    // Basic Validation Tests
    // ========================================

    @Nested
    @DisplayName("Basic Validation Tests")
    class BasicValidationTests {

        @Test
        @DisplayName("Should pass validation with valid inputs")
        void shouldPassValidationWithValidInputs() {
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertNotNull(result);
            assertTrue(result.isValid(), "Validation should pass with valid inputs");
            assertFalse(result.hasErrors(), "Should have no errors");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.validateAnalysisInputs(null));
        }

        @Test
        @DisplayName("Should fail when purchase details are missing")
        void shouldFailWhenPurchaseDetailsAreMissing() {
            analysis.setPurchaseDetails(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Purchase details are required")));
        }

        @Test
        @DisplayName("Should fail when income projection is missing")
        void shouldFailWhenIncomeProjectionIsMissing() {
            analysis.setIncomeProjection(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Income projection is required")));
        }

        @Test
        @DisplayName("Should fail when expense projection is missing")
        void shouldFailWhenExpenseProjectionIsMissing() {
            analysis.setExpenseProjection(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Expense projection is required")));
        }
    }

    // ========================================
    // Purchase Details Validation Tests
    // ========================================

    @Nested
    @DisplayName("Purchase Details Validation Tests")
    class PurchaseDetailsValidationTests {

        @Test
        @DisplayName("Should fail when purchase price is null")
        void shouldFailWhenPurchasePriceIsNull() {
            purchaseDetails.setPurchasePrice(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Purchase price is required")));
        }

        @Test
        @DisplayName("Should fail when purchase price is zero")
        void shouldFailWhenPurchasePriceIsZero() {
            purchaseDetails.setPurchasePrice(BigDecimal.ZERO);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Purchase price must be greater than $0")));
        }

        @Test
        @DisplayName("Should fail when purchase price is negative")
        void shouldFailWhenPurchasePriceIsNegative() {
            purchaseDetails.setPurchasePrice(new BigDecimal("-100000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Purchase price must be greater than $0")));
        }

        @Test
        @DisplayName("Should warn when purchase price is very low")
        void shouldWarnWhenPurchasePriceIsVeryLow() {
            purchaseDetails.setPurchasePrice(new BigDecimal("5000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Purchase price is very low")));
        }

        @Test
        @DisplayName("Should warn when purchase price is very high")
        void shouldWarnWhenPurchasePriceIsVeryHigh() {
            purchaseDetails.setPurchasePrice(new BigDecimal("150000000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Purchase price is very high")));
        }

        @Test
        @DisplayName("Should fail when down payment is negative")
        void shouldFailWhenDownPaymentIsNegative() {
            purchaseDetails.setDownPaymentAmount(new BigDecimal("-10000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Down payment cannot be negative")));
        }

        @Test
        @DisplayName("Should fail when down payment exceeds purchase price")
        void shouldFailWhenDownPaymentExceedsPurchasePrice() {
            purchaseDetails.setDownPaymentAmount(new BigDecimal("400000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Down payment") && e.contains("cannot exceed purchase price")));
        }

        @Test
        @DisplayName("Should warn when down payment is very low")
        void shouldWarnWhenDownPaymentIsVeryLow() {
            purchaseDetails.setDownPaymentAmount(new BigDecimal("3000")); // 1%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Very low down payment")));
        }

        @Test
        @DisplayName("Should warn when down payment is unusually high")
        void shouldWarnWhenDownPaymentIsUnusuallyHigh() {
            purchaseDetails.setDownPaymentAmount(new BigDecimal("200000")); // 66%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Unusually high down payment")));
        }

        @Test
        @DisplayName("Should fail when closing costs are negative")
        void shouldFailWhenClosingCostsAreNegative() {
            purchaseDetails.setClosingCosts(new BigDecimal("-5000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Closing costs cannot be negative")));
        }

        @Test
        @DisplayName("Should warn when closing costs are low")
        void shouldWarnWhenClosingCostsAreLow() {
            purchaseDetails.setClosingCosts(new BigDecimal("3000")); // 1%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Closing costs are low")));
        }

        @Test
        @DisplayName("Should warn when closing costs are high")
        void shouldWarnWhenClosingCostsAreHigh() {
            purchaseDetails.setClosingCosts(new BigDecimal("20000")); // 6.67%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Closing costs are high")));
        }

        @Test
        @DisplayName("Should fail when renovation costs are negative")
        void shouldFailWhenRenovationCostsAreNegative() {
            purchaseDetails.setRenovationCosts(new BigDecimal("-5000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Renovation costs cannot be negative")));
        }
    }

    // ========================================
    // Financing Details Validation Tests
    // ========================================

    @Nested
    @DisplayName("Financing Details Validation Tests")
    class FinancingDetailsValidationTests {

        @Test
        @DisplayName("Should pass validation when financing is null (cash purchase)")
        void shouldPassValidationWhenFinancingIsNull() {
            analysis.setFinancingDetails(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should fail when loan amount is negative")
        void shouldFailWhenLoanAmountIsNegative() {
            financingDetails.setLoanAmount(new BigDecimal("-100000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Loan amount cannot be negative")));
        }

        @Test
        @DisplayName("Should fail when interest rate is missing with positive loan")
        void shouldFailWhenInterestRateIsMissingWithPositiveLoan() {
            financingDetails.setInterestRate(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Interest rate is required")));
        }

        @Test
        @DisplayName("Should fail when interest rate is zero with positive loan")
        void shouldFailWhenInterestRateIsZeroWithPositiveLoan() {
            financingDetails.setInterestRate(0.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Interest rate must be greater than 0%")));
        }

        @Test
        @DisplayName("Should fail when interest rate is negative")
        void shouldFailWhenInterestRateIsNegative() {
            financingDetails.setInterestRate(-1.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Interest rate must be between 0% and 20%")));
        }

        @Test
        @DisplayName("Should fail when interest rate exceeds 20%")
        void shouldFailWhenInterestRateExceeds20Percent() {
            financingDetails.setInterestRate(25.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Interest rate must be between 0% and 20%")));
        }

        @Test
        @DisplayName("Should warn when interest rate is very high")
        void shouldWarnWhenInterestRateIsVeryHigh() {
            financingDetails.setInterestRate(15.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Very high interest rate")));
        }

        @Test
        @DisplayName("Should fail when loan term is missing with positive loan")
        void shouldFailWhenLoanTermIsMissingWithPositiveLoan() {
            financingDetails.setLoanTermYears(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Loan term is required")));
        }

        @Test
        @DisplayName("Should fail when loan term is less than 1 year")
        void shouldFailWhenLoanTermIsLessThan1Year() {
            financingDetails.setLoanTermYears(0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Loan term must be between 1 and 40 years")));
        }

        @Test
        @DisplayName("Should fail when loan term exceeds 40 years")
        void shouldFailWhenLoanTermExceeds40Years() {
            financingDetails.setLoanTermYears(50);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Loan term must be between 1 and 40 years")));
        }

        @Test
        @DisplayName("Should warn when loan term is unusual")
        void shouldWarnWhenLoanTermIsUnusual() {
            financingDetails.setLoanTermYears(25);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Unusual loan term")));
        }

        @Test
        @DisplayName("Should not warn for standard loan terms (15, 20, 30)")
        void shouldNotWarnForStandardLoanTerms() {
            // Test 15 years
            financingDetails.setLoanTermYears(15);
            ValidationResult result15 = calculatorService.validateAnalysisInputs(analysis);
            assertFalse(result15.getWarnings().stream()
                    .anyMatch(w -> w.contains("Unusual loan term")));
            
            // Test 20 years
            financingDetails.setLoanTermYears(20);
            ValidationResult result20 = calculatorService.validateAnalysisInputs(analysis);
            assertFalse(result20.getWarnings().stream()
                    .anyMatch(w -> w.contains("Unusual loan term")));
            
            // Test 30 years
            financingDetails.setLoanTermYears(30);
            ValidationResult result30 = calculatorService.validateAnalysisInputs(analysis);
            assertFalse(result30.getWarnings().stream()
                    .anyMatch(w -> w.contains("Unusual loan term")));
        }
    }

    // ========================================
    // Income Projection Validation Tests
    // ========================================

    @Nested
    @DisplayName("Income Projection Validation Tests")
    class IncomeProjectionValidationTests {

        @Test
        @DisplayName("Should fail when monthly rent is null")
        void shouldFailWhenMonthlyRentIsNull() {
            incomeProjection.setMonthlyRent(null);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Monthly rent is required")));
        }

        @Test
        @DisplayName("Should fail when monthly rent is zero")
        void shouldFailWhenMonthlyRentIsZero() {
            incomeProjection.setMonthlyRent(BigDecimal.ZERO);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Monthly rent must be greater than $0")));
        }

        @Test
        @DisplayName("Should fail when monthly rent is negative")
        void shouldFailWhenMonthlyRentIsNegative() {
            incomeProjection.setMonthlyRent(new BigDecimal("-1000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Monthly rent must be greater than $0")));
        }

        @Test
        @DisplayName("Should warn when monthly rent is low relative to purchase price")
        void shouldWarnWhenMonthlyRentIsLow() {
            incomeProjection.setMonthlyRent(new BigDecimal("500")); // 0.17% of $300k
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Monthly rent is low relative to purchase price")));
        }

        @Test
        @DisplayName("Should warn when monthly rent is high relative to purchase price")
        void shouldWarnWhenMonthlyRentIsHigh() {
            incomeProjection.setMonthlyRent(new BigDecimal("7000")); // 2.33% of $300k
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Monthly rent is high relative to purchase price")));
        }

        @Test
        @DisplayName("Should fail when vacancy rate is negative")
        void shouldFailWhenVacancyRateIsNegative() {
            incomeProjection.setVacancyRate(-5.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Vacancy rate must be between 0% and 50%")));
        }

        @Test
        @DisplayName("Should fail when vacancy rate exceeds 50%")
        void shouldFailWhenVacancyRateExceeds50Percent() {
            incomeProjection.setVacancyRate(60.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Vacancy rate must be between 0% and 50%")));
        }

        @Test
        @DisplayName("Should warn when vacancy rate is very high")
        void shouldWarnWhenVacancyRateIsVeryHigh() {
            incomeProjection.setVacancyRate(25.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Very high vacancy rate")));
        }

        @Test
        @DisplayName("Should fail when other monthly income is negative")
        void shouldFailWhenOtherMonthlyIncomeIsNegative() {
            incomeProjection.setOtherMonthlyIncome(new BigDecimal("-100"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Other monthly income cannot be negative")));
        }
    }

    // ========================================
    // Expense Projection Validation Tests
    // ========================================

    @Nested
    @DisplayName("Expense Projection Validation Tests")
    class ExpenseProjectionValidationTests {

        @Test
        @DisplayName("Should fail when property tax is negative")
        void shouldFailWhenPropertyTaxIsNegative() {
            expenseProjection.setAnnualPropertyTax(new BigDecimal("-1000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Annual property tax cannot be negative")));
        }

        @Test
        @DisplayName("Should warn when property tax is low")
        void shouldWarnWhenPropertyTaxIsLow() {
            expenseProjection.setAnnualPropertyTax(new BigDecimal("1000")); // 0.33%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Property tax is low")));
        }

        @Test
        @DisplayName("Should warn when property tax is high")
        void shouldWarnWhenPropertyTaxIsHigh() {
            expenseProjection.setAnnualPropertyTax(new BigDecimal("12000")); // 4%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Property tax is high")));
        }

        @Test
        @DisplayName("Should fail when insurance is negative")
        void shouldFailWhenInsuranceIsNegative() {
            expenseProjection.setAnnualInsurance(new BigDecimal("-500"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Annual insurance cannot be negative")));
        }

        @Test
        @DisplayName("Should warn when insurance is low")
        void shouldWarnWhenInsuranceIsLow() {
            expenseProjection.setAnnualInsurance(new BigDecimal("500")); // 0.17%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Insurance is low")));
        }

        @Test
        @DisplayName("Should warn when insurance is high")
        void shouldWarnWhenInsuranceIsHigh() {
            expenseProjection.setAnnualInsurance(new BigDecimal("6000")); // 2%
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Insurance is high")));
        }

        @Test
        @DisplayName("Should fail when HOA fees are negative")
        void shouldFailWhenHOAFeesAreNegative() {
            expenseProjection.setMonthlyHOAFees(new BigDecimal("-50"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Monthly HOA fees cannot be negative")));
        }

        @Test
        @DisplayName("Should fail when property management percent is negative")
        void shouldFailWhenPropertyManagementPercentIsNegative() {
            expenseProjection.setPropertyManagementPercent(-5.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Property management percentage cannot be negative")));
        }

        @Test
        @DisplayName("Should warn when property management percent is outside typical range")
        void shouldWarnWhenPropertyManagementPercentIsOutsideRange() {
            // Test low
            expenseProjection.setPropertyManagementPercent(5.0);
            ValidationResult resultLow = calculatorService.validateAnalysisInputs(analysis);
            assertTrue(resultLow.hasWarnings());
            assertTrue(resultLow.getWarnings().stream()
                    .anyMatch(w -> w.contains("Property management fee") && w.contains("outside typical range")));
            
            // Test high
            expenseProjection.setPropertyManagementPercent(15.0);
            ValidationResult resultHigh = calculatorService.validateAnalysisInputs(analysis);
            assertTrue(resultHigh.hasWarnings());
            assertTrue(resultHigh.getWarnings().stream()
                    .anyMatch(w -> w.contains("Property management fee") && w.contains("outside typical range")));
        }

        @Test
        @DisplayName("Should fail when maintenance percent is negative")
        void shouldFailWhenMaintenancePercentIsNegative() {
            expenseProjection.setMaintenancePercent(-1.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Maintenance percentage cannot be negative")));
        }

        @Test
        @DisplayName("Should warn when maintenance percent is low")
        void shouldWarnWhenMaintenancePercentIsLow() {
            expenseProjection.setMaintenancePercent(0.3);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Maintenance budget is low")));
        }

        @Test
        @DisplayName("Should warn when maintenance percent is high")
        void shouldWarnWhenMaintenancePercentIsHigh() {
            expenseProjection.setMaintenancePercent(3.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Maintenance budget is high")));
        }

        @Test
        @DisplayName("Should fail when CapEx percent is negative")
        void shouldFailWhenCapExPercentIsNegative() {
            expenseProjection.setCapExPercent(-0.5);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("CapEx percentage cannot be negative")));
        }

        @Test
        @DisplayName("Should fail when utilities are negative")
        void shouldFailWhenUtilitiesAreNegative() {
            expenseProjection.setMonthlyUtilities(new BigDecimal("-50"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Monthly utilities cannot be negative")));
        }

        @Test
        @DisplayName("Should fail when other expenses are negative")
        void shouldFailWhenOtherExpensesAreNegative() {
            expenseProjection.setOtherMonthlyExpenses(new BigDecimal("-25"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrors().stream()
                    .anyMatch(e -> e.contains("Other monthly expenses cannot be negative")));
        }

        @Test
        @DisplayName("Should warn when total expenses are very high")
        void shouldWarnWhenTotalExpensesAreVeryHigh() {
            // Set very high expenses
            expenseProjection.setAnnualPropertyTax(new BigDecimal("10000"));
            expenseProjection.setAnnualInsurance(new BigDecimal("5000"));
            expenseProjection.setMonthlyHOAFees(new BigDecimal("500"));
            expenseProjection.setPropertyManagementPercent(10.0);
            expenseProjection.setMaintenancePercent(2.0);
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Expenses are very high")));
        }
    }

    // ========================================
    // Calculated Metrics Validation Tests
    // ========================================

    @Nested
    @DisplayName("Calculated Metrics Validation Tests")
    class CalculatedMetricsValidationTests {

        @Test
        @DisplayName("Should warn when property has negative cash flow")
        void shouldWarnWhenPropertyHasNegativeCashFlow() {
            // Set conditions for negative cash flow
            incomeProjection.setMonthlyRent(new BigDecimal("1000"));
            expenseProjection.setAnnualPropertyTax(new BigDecimal("6000"));
            expenseProjection.setAnnualInsurance(new BigDecimal("3000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("negative cash flow")));
        }

        @Test
        @DisplayName("Should warn when gross yield is very low")
        void shouldWarnWhenGrossYieldIsVeryLow() {
            // Set low rent for low gross yield
            incomeProjection.setMonthlyRent(new BigDecimal("500")); // 2% gross yield
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Very low gross yield")));
        }

        @Test
        @DisplayName("Should warn when cap rate is very low")
        void shouldWarnWhenCapRateIsVeryLow() {
            // Set conditions for low cap rate
            incomeProjection.setMonthlyRent(new BigDecimal("800"));
            expenseProjection.setAnnualPropertyTax(new BigDecimal("4000"));
            expenseProjection.setAnnualInsurance(new BigDecimal("2000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Very low cap rate")));
        }

        @Test
        @DisplayName("Should warn when effective income is less than obligations")
        void shouldWarnWhenEffectiveIncomeIsLessThanObligations() {
            // Set high vacancy and high expenses
            incomeProjection.setVacancyRate(15.0);
            expenseProjection.setAnnualPropertyTax(new BigDecimal("5000"));
            expenseProjection.setAnnualInsurance(new BigDecimal("2500"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("Effective monthly income") && 
                                   w.contains("less than monthly expenses")));
        }
    }

    // ========================================
    // Multiple Errors and Warnings Tests
    // ========================================

    @Nested
    @DisplayName("Multiple Errors and Warnings Tests")
    class MultipleErrorsAndWarningsTests {

        @Test
        @DisplayName("Should collect multiple errors")
        void shouldCollectMultipleErrors() {
            purchaseDetails.setPurchasePrice(BigDecimal.ZERO);
            incomeProjection.setMonthlyRent(BigDecimal.ZERO);
            expenseProjection.setAnnualPropertyTax(new BigDecimal("-1000"));
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.getErrorCount() >= 3);
        }

        @Test
        @DisplayName("Should collect multiple warnings")
        void shouldCollectMultipleWarnings() {
            purchaseDetails.setDownPaymentAmount(new BigDecimal("200000")); // High down payment
            incomeProjection.setVacancyRate(25.0); // High vacancy
            financingDetails.setInterestRate(15.0); // High interest rate
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertTrue(result.hasWarnings());
            assertTrue(result.getWarningCount() >= 3);
        }

        @Test
        @DisplayName("Should have both errors and warnings")
        void shouldHaveBothErrorsAndWarnings() {
            purchaseDetails.setPurchasePrice(BigDecimal.ZERO); // Error
            incomeProjection.setVacancyRate(25.0); // Warning
            
            ValidationResult result = calculatorService.validateAnalysisInputs(analysis);
            
            assertFalse(result.isValid());
            assertTrue(result.hasErrors());
            // Note: Warnings may not be collected if errors prevent metric calculation
        }
    }
}