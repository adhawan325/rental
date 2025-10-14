package com.non.rental.service;

import com.non.rental.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RentalYieldCalculatorServiceImpl.
 * Tests all BASIC calculation methods with various scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RentalYieldCalculatorService Tests")
class RentalYieldCalculatorServiceImplTest {

    @InjectMocks
    private RentalYieldCalculatorServiceImpl calculatorService;

    private RentalAnalysis analysis;
    private PurchaseDetails purchaseDetails;
    private IncomeProjection incomeProjection;
    private ExpenseProjection expenseProjection;
    private FinancingDetails financingDetails;

    @BeforeEach
    void setUp() {
        // Create test entities with typical values
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
    // calculateGrossYield Tests
    // ========================================

    @Nested
    @DisplayName("calculateGrossYield Tests")
    class CalculateGrossYieldTests {

        @Test
        @DisplayName("Should calculate gross yield correctly with standard values")
        void shouldCalculateGrossYieldCorrectly() {
            // $2,000/month × 12 = $24,000/year
            // $24,000 / $300,000 × 100 = 8%
            Double grossYield = calculatorService.calculateGrossYield(analysis);
            
            assertNotNull(grossYield);
            assertEquals(8.0, grossYield, 0.01, "Gross yield should be 8%");
        }

        @Test
        @DisplayName("Should calculate gross yield with different rent")
        void shouldCalculateGrossYieldWithDifferentRent() {
            incomeProjection.setMonthlyRent(new BigDecimal("2500.00"));
            // $2,500/month × 12 = $30,000/year
            // $30,000 / $300,000 × 100 = 10%
            
            Double grossYield = calculatorService.calculateGrossYield(analysis);
            
            assertNotNull(grossYield);
            assertEquals(10.0, grossYield, 0.01, "Gross yield should be 10%");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateGrossYield(null));
        }

        @Test
        @DisplayName("Should return null when purchase details are missing")
        void shouldReturnNullWhenPurchaseDetailsAreMissing() {
            analysis.setPurchaseDetails(null);
            
            Double grossYield = calculatorService.calculateGrossYield(analysis);
            
            assertNull(grossYield);
        }

        @Test
        @DisplayName("Should return null when income projection is missing")
        void shouldReturnNullWhenIncomeProjectionIsMissing() {
            analysis.setIncomeProjection(null);
            
            Double grossYield = calculatorService.calculateGrossYield(analysis);
            
            assertNull(grossYield);
        }

        @Test
        @DisplayName("Should throw exception when purchase price is zero")
        void shouldThrowExceptionWhenPurchasePriceIsZero() {
            purchaseDetails.setPurchasePrice(BigDecimal.ZERO);
            
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateGrossYield(analysis));
        }
    }

    // ========================================
    // calculateNetYield Tests
    // ========================================

    @Nested
    @DisplayName("calculateNetYield Tests")
    class CalculateNetYieldTests {

        @Test
        @DisplayName("Should calculate net yield correctly with standard values")
        void shouldCalculateNetYieldCorrectly() {
            // Annual rent: $2,000 × 12 = $24,000
            // Monthly expenses: ~$700 (calculated by helper method)
            // Annual expenses: ~$8,400
            // Net income: $24,000 - $8,400 = $15,600
            // Net yield: $15,600 / $300,000 × 100 = 5.2%
            
            Double netYield = calculatorService.calculateNetYield(analysis);
            
            assertNotNull(netYield);
            assertTrue(netYield > 0, "Net yield should be positive");
            assertTrue(netYield < 8.0, "Net yield should be less than gross yield");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateNetYield(null));
        }

        @Test
        @DisplayName("Should return null when expense projection is missing")
        void shouldReturnNullWhenExpenseProjectionIsMissing() {
            analysis.setExpenseProjection(null);
            
            Double netYield = calculatorService.calculateNetYield(analysis);
            
            assertNull(netYield);
        }

        @Test
        @DisplayName("Should handle high expenses resulting in low net yield")
        void shouldHandleHighExpenses() {
            // Set very high expenses
            expenseProjection.setAnnualPropertyTax(new BigDecimal("12000.00"));
            expenseProjection.setAnnualInsurance(new BigDecimal("6000.00"));
            
            Double netYield = calculatorService.calculateNetYield(analysis);
            
            assertNotNull(netYield);
            // Net yield could be negative with high expenses
        }
    }

    // ========================================
    // calculateMonthlyMortgagePayment Tests
    // ========================================

    @Nested
    @DisplayName("calculateMonthlyMortgagePayment Tests")
    class CalculateMonthlyMortgagePaymentTests {

        @Test
        @DisplayName("Should calculate mortgage payment correctly with standard values")
        void shouldCalculateMortgagePaymentCorrectly() {
            // $240,000 at 6.5% for 30 years should be approximately $1,517
            BigDecimal payment = calculatorService.calculateMonthlyMortgagePayment(
                    new BigDecimal("240000.00"), 6.5, 30);
            
            assertNotNull(payment);
            assertEquals(new BigDecimal("1516.96"), payment, 
                    "Payment should be approximately $1,516.96");
        }

        @Test
        @DisplayName("Should handle zero interest rate")
        void shouldHandleZeroInterestRate() {
            // $240,000 at 0% for 30 years = $240,000 / 360 = $666.67
            BigDecimal payment = calculatorService.calculateMonthlyMortgagePayment(
                    new BigDecimal("240000.00"), 0.0, 30);
            
            assertNotNull(payment);
            assertEquals(new BigDecimal("666.67"), payment, 
                    "Payment should be $666.67 with 0% interest");
        }

        @Test
        @DisplayName("Should handle zero principal")
        void shouldHandleZeroPrincipal() {
            BigDecimal payment = calculatorService.calculateMonthlyMortgagePayment(
                    BigDecimal.ZERO, 6.5, 30);
            
            assertNotNull(payment);
            assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), payment, 
                    "Payment should be zero when principal is zero");
        }

        @Test
        @DisplayName("Should calculate payment for different loan terms")
        void shouldCalculatePaymentForDifferentLoanTerms() {
            // 15-year loan should have higher payment than 30-year
            BigDecimal payment15 = calculatorService.calculateMonthlyMortgagePayment(
                    new BigDecimal("240000.00"), 6.5, 15);
            BigDecimal payment30 = calculatorService.calculateMonthlyMortgagePayment(
                    new BigDecimal("240000.00"), 6.5, 30);
            
            assertNotNull(payment15);
            assertNotNull(payment30);
            assertTrue(payment15.compareTo(payment30) > 0, 
                    "15-year payment should be higher than 30-year payment");
        }

        @Test
        @DisplayName("Should return null when parameters are null")
        void shouldReturnNullWhenParametersAreNull() {
            assertNull(calculatorService.calculateMonthlyMortgagePayment(null, 6.5, 30));
            assertNull(calculatorService.calculateMonthlyMortgagePayment(
                    new BigDecimal("240000.00"), null, 30));
            assertNull(calculatorService.calculateMonthlyMortgagePayment(
                    new BigDecimal("240000.00"), 6.5, null));
        }

        @Test
        @DisplayName("Should throw exception for negative principal")
        void shouldThrowExceptionForNegativePrincipal() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateMonthlyMortgagePayment(
                            new BigDecimal("-100000.00"), 6.5, 30));
        }

        @Test
        @DisplayName("Should throw exception for negative interest rate")
        void shouldThrowExceptionForNegativeInterestRate() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateMonthlyMortgagePayment(
                            new BigDecimal("240000.00"), -1.0, 30));
        }

        @Test
        @DisplayName("Should throw exception for invalid loan term")
        void shouldThrowExceptionForInvalidLoanTerm() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateMonthlyMortgagePayment(
                            new BigDecimal("240000.00"), 6.5, 0));
        }
    }

    // ========================================
    // calculateMonthlyCashFlow Tests
    // ========================================

    @Nested
    @DisplayName("calculateMonthlyCashFlow Tests")
    class CalculateMonthlyCashFlowTests {

        @Test
        @DisplayName("Should calculate monthly cash flow correctly")
        void shouldCalculateMonthlyCashFlowCorrectly() {
            BigDecimal cashFlow = calculatorService.calculateMonthlyCashFlow(analysis);
            
            assertNotNull(cashFlow);
            // With typical values, cash flow could be positive or negative
            // Just verify it's calculated
        }

        @Test
        @DisplayName("Should calculate positive cash flow with good numbers")
        void shouldCalculatePositiveCashFlow() {
            // Set favorable conditions
            incomeProjection.setMonthlyRent(new BigDecimal("3000.00"));
            incomeProjection.setVacancyRate(5.0);
            expenseProjection.setAnnualPropertyTax(new BigDecimal("2400.00"));
            expenseProjection.setAnnualInsurance(new BigDecimal("1200.00"));
            
            BigDecimal cashFlow = calculatorService.calculateMonthlyCashFlow(analysis);
            
            assertNotNull(cashFlow);
            assertTrue(cashFlow.compareTo(BigDecimal.ZERO) > 0, 
                    "Cash flow should be positive with favorable numbers");
        }

        @Test
        @DisplayName("Should handle property with no financing")
        void shouldHandlePropertyWithNoFinancing() {
            analysis.setFinancingDetails(null);
            
            BigDecimal cashFlow = calculatorService.calculateMonthlyCashFlow(analysis);
            
            assertNotNull(cashFlow);
            // Without mortgage, cash flow should be higher
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateMonthlyCashFlow(null));
        }

        @Test
        @DisplayName("Should return null when required data is missing")
        void shouldReturnNullWhenRequiredDataIsMissing() {
            analysis.setIncomeProjection(null);
            
            BigDecimal cashFlow = calculatorService.calculateMonthlyCashFlow(analysis);
            
            assertNull(cashFlow);
        }
    }

    // ========================================
    // calculateAnnualCashFlow Tests
    // ========================================

    @Nested
    @DisplayName("calculateAnnualCashFlow Tests")
    class CalculateAnnualCashFlowTests {

        @Test
        @DisplayName("Should calculate annual cash flow correctly")
        void shouldCalculateAnnualCashFlowCorrectly() {
            BigDecimal monthlyCashFlow = calculatorService.calculateMonthlyCashFlow(analysis);
            BigDecimal annualCashFlow = calculatorService.calculateAnnualCashFlow(analysis);
            
            assertNotNull(annualCashFlow);
            if (monthlyCashFlow != null) {
                BigDecimal expected = monthlyCashFlow.multiply(new BigDecimal("12"))
                        .setScale(2, RoundingMode.HALF_UP);
                assertEquals(expected, annualCashFlow, 
                        "Annual cash flow should be monthly × 12");
            }
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateAnnualCashFlow(null));
        }

        @Test
        @DisplayName("Should return null when monthly cash flow cannot be calculated")
        void shouldReturnNullWhenMonthlyCashFlowCannotBeCalculated() {
            analysis.setIncomeProjection(null);
            
            BigDecimal annualCashFlow = calculatorService.calculateAnnualCashFlow(analysis);
            
            assertNull(annualCashFlow);
        }
    }

    // ========================================
    // Helper Methods Tests
    // ========================================

    @Nested
    @DisplayName("calculateTotalMonthlyExpenses Tests")
    class CalculateTotalMonthlyExpensesTests {

        @Test
        @DisplayName("Should calculate total monthly expenses correctly")
        void shouldCalculateTotalMonthlyExpensesCorrectly() {
            BigDecimal total = calculatorService.calculateTotalMonthlyExpenses(
                    expenseProjection, 
                    new BigDecimal("2000.00"), 
                    new BigDecimal("300000.00"));
            
            assertNotNull(total);
            assertTrue(total.compareTo(BigDecimal.ZERO) > 0, 
                    "Total expenses should be positive");
            
            // Verify it includes all components
            // Property tax: $3,600/12 = $300
            // Insurance: $1,200/12 = $100
            // HOA: $50
            // Management: 10% of $2,000 = $200
            // Maintenance: 1% of $300,000 / 12 = $250
            // CapEx: 0.5% of $300,000 / 12 = $125
            // Utilities: $75
            // Other: $25
            // Total: ~$1,125
            
            assertEquals(new BigDecimal("1125.00"), total, 
                    "Total should be approximately $1,125");
        }

        @Test
        @DisplayName("Should handle expenses with fixed amounts instead of percentages")
        void shouldHandleFixedAmounts() {
            expenseProjection.setPropertyManagementPercent(null);
            expenseProjection.setPropertyManagementAmount(new BigDecimal("150.00"));
            expenseProjection.setMaintenancePercent(null);
            expenseProjection.setMaintenanceAmount(new BigDecimal("200.00"));
            
            BigDecimal total = calculatorService.calculateTotalMonthlyExpenses(
                    expenseProjection, 
                    new BigDecimal("2000.00"), 
                    new BigDecimal("300000.00"));
            
            assertNotNull(total);
            assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Should throw exception when parameters are null")
        void shouldThrowExceptionWhenParametersAreNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateTotalMonthlyExpenses(
                            null, new BigDecimal("2000.00"), new BigDecimal("300000.00")));
            
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateTotalMonthlyExpenses(
                            expenseProjection, null, new BigDecimal("300000.00")));
            
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateTotalMonthlyExpenses(
                            expenseProjection, new BigDecimal("2000.00"), null));
        }
    }

    @Nested
    @DisplayName("calculateEffectiveMonthlyIncome Tests")
    class CalculateEffectiveMonthlyIncomeTests {

        @Test
        @DisplayName("Should calculate effective income with vacancy rate")
        void shouldCalculateEffectiveIncomeWithVacancy() {
            // Gross: $2,000 + $100 = $2,100
            // Vacancy: 8%
            // Effective: $2,100 × 0.92 = $1,932
            
            BigDecimal effective = calculatorService.calculateEffectiveMonthlyIncome(incomeProjection);
            
            assertNotNull(effective);
            assertEquals(new BigDecimal("1932.00"), effective, 
                    "Effective income should be $1,932");
        }

        @Test
        @DisplayName("Should handle zero vacancy rate")
        void shouldHandleZeroVacancyRate() {
            incomeProjection.setVacancyRate(0.0);
            
            BigDecimal effective = calculatorService.calculateEffectiveMonthlyIncome(incomeProjection);
            
            assertNotNull(effective);
            assertEquals(new BigDecimal("2100.00"), effective, 
                    "Effective income should equal gross income with 0% vacancy");
        }

        @Test
        @DisplayName("Should handle income without other sources")
        void shouldHandleIncomeWithoutOtherSources() {
            incomeProjection.setOtherMonthlyIncome(null);
            incomeProjection.setVacancyRate(10.0);
            
            // $2,000 × 0.90 = $1,800
            BigDecimal effective = calculatorService.calculateEffectiveMonthlyIncome(incomeProjection);
            
            assertNotNull(effective);
            assertEquals(new BigDecimal("1800.00"), effective);
        }

        @Test
        @DisplayName("Should throw exception when income projection is null")
        void shouldThrowExceptionWhenIncomeProjectionIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateEffectiveMonthlyIncome(null));
        }
    }

    @Nested
    @DisplayName("calculateTotalCashInvested Tests")
    class CalculateTotalCashInvestedTests {

        @Test
        @DisplayName("Should calculate total cash invested correctly")
        void shouldCalculateTotalCashInvestedCorrectly() {
            // Down: $60,000 + Closing: $9,000 + Renovation: $5,000 = $74,000
            BigDecimal total = calculatorService.calculateTotalCashInvested(purchaseDetails);
            
            assertNotNull(total);
            assertEquals(new BigDecimal("74000.00"), total, 
                    "Total cash invested should be $74,000");
        }

        @Test
        @DisplayName("Should handle missing optional costs")
        void shouldHandleMissingOptionalCosts() {
            purchaseDetails.setClosingCosts(null);
            purchaseDetails.setRenovationCosts(null);
            
            BigDecimal total = calculatorService.calculateTotalCashInvested(purchaseDetails);
            
            assertNotNull(total);
            assertEquals(new BigDecimal("60000.00"), total, 
                    "Total should only include down payment");
        }

        @Test
        @DisplayName("Should throw exception when purchase details are null")
        void shouldThrowExceptionWhenPurchaseDetailsAreNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateTotalCashInvested(null));
        }
    }

    @Nested
    @DisplayName("calculateLoanAmount Tests")
    class CalculateLoanAmountTests {

        @Test
        @DisplayName("Should calculate loan amount correctly")
        void shouldCalculateLoanAmountCorrectly() {
            // $300,000 - $60,000 = $240,000
            BigDecimal loanAmount = calculatorService.calculateLoanAmount(purchaseDetails);
            
            assertNotNull(loanAmount);
            assertEquals(new BigDecimal("240000.00"), loanAmount, 
                    "Loan amount should be $240,000");
        }

        @Test
        @DisplayName("Should handle no down payment")
        void shouldHandleNoDownPayment() {
            purchaseDetails.setDownPaymentAmount(null);
            
            BigDecimal loanAmount = calculatorService.calculateLoanAmount(purchaseDetails);
            
            assertNotNull(loanAmount);
            assertEquals(new BigDecimal("300000.00"), loanAmount, 
                    "Loan amount should equal purchase price with no down payment");
        }

        @Test
        @DisplayName("Should throw exception when purchase details are null")
        void shouldThrowExceptionWhenPurchaseDetailsAreNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateLoanAmount(null));
        }

        @Test
        @DisplayName("Should return null when purchase price is null")
        void shouldReturnNullWhenPurchasePriceIsNull() {
            purchaseDetails.setPurchasePrice(null);
            
            BigDecimal loanAmount = calculatorService.calculateLoanAmount(purchaseDetails);
            
            assertNull(loanAmount);
        }
    }

    // ========================================
    // ADVANCED Calculation Methods Tests
    // ========================================

    @Nested
    @DisplayName("calculateNetOperatingIncome Tests")
    class CalculateNetOperatingIncomeTests {

        @Test
        @DisplayName("Should calculate NOI correctly with standard values")
        void shouldCalculateNOICorrectly() {
            // Annual rent: $2,000 × 12 = $24,000
            // Monthly expenses: ~$1,125 (from expense calculation)
            // Annual expenses: $1,125 × 12 = $13,500
            // NOI: $24,000 - $13,500 = $10,500
            
            BigDecimal noi = calculatorService.calculateNetOperatingIncome(analysis);
            
            assertNotNull(noi);
            assertEquals(new BigDecimal("10500.00"), noi, 
                    "NOI should be $10,500");
        }

        @Test
        @DisplayName("Should handle high expenses resulting in negative NOI")
        void shouldHandleNegativeNOI() {
            // Set very high expenses
            expenseProjection.setAnnualPropertyTax(new BigDecimal("15000.00"));
            expenseProjection.setAnnualInsurance(new BigDecimal("6000.00"));
            
            BigDecimal noi = calculatorService.calculateNetOperatingIncome(analysis);
            
            assertNotNull(noi);
            assertTrue(noi.compareTo(BigDecimal.ZERO) < 0, 
                    "NOI should be negative with very high expenses");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateNetOperatingIncome(null));
        }

        @Test
        @DisplayName("Should return null when required data is missing")
        void shouldReturnNullWhenDataIsMissing() {
            analysis.setIncomeProjection(null);
            
            BigDecimal noi = calculatorService.calculateNetOperatingIncome(analysis);
            
            assertNull(noi);
        }
    }

    @Nested
    @DisplayName("calculateCapRate Tests")
    class CalculateCapRateTests {

        @Test
        @DisplayName("Should calculate Cap Rate correctly with standard values")
        void shouldCalculateCapRateCorrectly() {
            // NOI: $10,500 (from NOI test)
            // Purchase Price: $300,000
            // Cap Rate: ($10,500 / $300,000) × 100 = 3.5%
            
            Double capRate = calculatorService.calculateCapRate(analysis);
            
            assertNotNull(capRate);
            assertEquals(3.5, capRate, 0.01, "Cap Rate should be 3.5%");
        }

        @Test
        @DisplayName("Should calculate higher Cap Rate with better income")
        void shouldCalculateHigherCapRateWithBetterIncome() {
            // Increase rent to improve NOI
            incomeProjection.setMonthlyRent(new BigDecimal("3000.00"));
            
            Double capRate = calculatorService.calculateCapRate(analysis);
            
            assertNotNull(capRate);
            assertTrue(capRate > 3.5, "Cap Rate should be higher with increased rent");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateCapRate(null));
        }

        @Test
        @DisplayName("Should throw exception when purchase price is zero")
        void shouldThrowExceptionWhenPurchasePriceIsZero() {
            purchaseDetails.setPurchasePrice(BigDecimal.ZERO);
            
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateCapRate(analysis));
        }

        @Test
        @DisplayName("Should return null when NOI cannot be calculated")
        void shouldReturnNullWhenNOICannotBeCalculated() {
            analysis.setExpenseProjection(null);
            
            Double capRate = calculatorService.calculateCapRate(analysis);
            
            assertNull(capRate);
        }
    }

    @Nested
    @DisplayName("calculateCashOnCashReturn Tests")
    class CalculateCashOnCashReturnTests {

        @Test
        @DisplayName("Should calculate Cash-on-Cash Return correctly")
        void shouldCalculateCoCReturnCorrectly() {
            // This will depend on the actual cash flow calculation
            // With our test data, cash flow might be negative or small
            
            Double cocReturn = calculatorService.calculateCashOnCashReturn(analysis);
            
            assertNotNull(cocReturn);
            // Just verify it's calculated - actual value depends on cash flow
        }

        @Test
        @DisplayName("Should calculate positive CoC with favorable numbers")
        void shouldCalculatePositiveCoCWithFavorableNumbers() {
            // Set favorable conditions
            incomeProjection.setMonthlyRent(new BigDecimal("3500.00"));
            incomeProjection.setVacancyRate(5.0);
            expenseProjection.setAnnualPropertyTax(new BigDecimal("2400.00"));
            expenseProjection.setAnnualInsurance(new BigDecimal("1200.00"));
            
            Double cocReturn = calculatorService.calculateCashOnCashReturn(analysis);
            
            assertNotNull(cocReturn);
            assertTrue(cocReturn > 0, "CoC Return should be positive with favorable numbers");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateCashOnCashReturn(null));
        }

        @Test
        @DisplayName("Should throw exception when total cash invested is zero")
        void shouldThrowExceptionWhenTotalCashInvestedIsZero() {
            purchaseDetails.setDownPaymentAmount(BigDecimal.ZERO);
            purchaseDetails.setClosingCosts(BigDecimal.ZERO);
            purchaseDetails.setRenovationCosts(BigDecimal.ZERO);
            
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateCashOnCashReturn(analysis));
        }

        @Test
        @DisplayName("Should return null when cash flow cannot be calculated")
        void shouldReturnNullWhenCashFlowCannotBeCalculated() {
            analysis.setIncomeProjection(null);
            
            Double cocReturn = calculatorService.calculateCashOnCashReturn(analysis);
            
            assertNull(cocReturn);
        }
    }

    @Nested
    @DisplayName("calculateROI Tests")
    class CalculateROITests {

        @Test
        @DisplayName("Should calculate ROI correctly with default appreciation")
        void shouldCalculateROICorrectly() {
            // ROI includes: cash flow + principal paydown + appreciation
            // With 3% default appreciation on $300,000 = $9,000
            
            Double roi = calculatorService.calculateROI(analysis, 1, null);
            
            assertNotNull(roi);
            assertTrue(roi > 0, "ROI should be positive with appreciation");
        }

        @Test
        @DisplayName("Should calculate ROI with custom appreciation rate")
        void shouldCalculateROIWithCustomAppreciation() {
            // Test with 5% appreciation
            Double roi5 = calculatorService.calculateROI(analysis, 1, 5.0);
            
            // Test with 2% appreciation
            Double roi2 = calculatorService.calculateROI(analysis, 1, 2.0);
            
            assertNotNull(roi5);
            assertNotNull(roi2);
            assertTrue(roi5 > roi2, "ROI should be higher with higher appreciation rate");
        }

        @Test
        @DisplayName("Should calculate ROI for different holding periods")
        void shouldCalculateROIForDifferentPeriods() {
            Double roi1 = calculatorService.calculateROI(analysis, 1, 3.0);
            Double roi5 = calculatorService.calculateROI(analysis, 5, 3.0);
            
            assertNotNull(roi1);
            assertNotNull(roi5);
            // Both should be calculated (though formula uses year 1 for now)
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateROI(null, 1, 3.0));
        }

        @Test
        @DisplayName("Should throw exception when years is invalid")
        void shouldThrowExceptionWhenYearsIsInvalid() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateROI(analysis, 0, 3.0));
            
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateROI(analysis, null, 3.0));
        }

        @Test
        @DisplayName("Should throw exception when appreciation rate is negative")
        void shouldThrowExceptionWhenAppreciationRateIsNegative() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateROI(analysis, 1, -5.0));
        }

        @Test
        @DisplayName("Should handle zero appreciation rate")
        void shouldHandleZeroAppreciationRate() {
            Double roi = calculatorService.calculateROI(analysis, 1, 0.0);
            
            assertNotNull(roi);
            // ROI should still be calculated, just without appreciation component
        }
    }

    @Nested
    @DisplayName("calculateIRR Tests")
    class CalculateIRRTests {

        @Test
        @DisplayName("Should calculate IRR correctly with provided exit value")
        void shouldCalculateIRRCorrectly() {
            // 10-year hold with $400,000 exit value
            BigDecimal exitValue = new BigDecimal("400000.00");
            
            Double irr = calculatorService.calculateIRR(analysis, 10, exitValue, null);
            
            assertNotNull(irr);
            assertTrue(irr > 0, "IRR should be positive with profitable investment");
        }

        @Test
        @DisplayName("Should calculate IRR with auto-calculated exit value")
        void shouldCalculateIRRWithAutoCalculatedExitValue() {
            // Let it calculate exit value based on 3% appreciation
            Double irr = calculatorService.calculateIRR(analysis, 10, null, 3.0);
            
            assertNotNull(irr);
            assertTrue(irr > 0, "IRR should be positive");
        }

        @Test
        @DisplayName("Should calculate different IRR for different holding periods")
        void shouldCalculateDifferentIRRForDifferentPeriods() {
            BigDecimal exitValue = new BigDecimal("400000.00");
            
            Double irr5 = calculatorService.calculateIRR(analysis, 5, exitValue, null);
            Double irr10 = calculatorService.calculateIRR(analysis, 10, exitValue, null);
            
            assertNotNull(irr5);
            assertNotNull(irr10);
            // IRR values should be different for different holding periods
        }

        @Test
        @DisplayName("Should calculate different IRR for different exit values")
        void shouldCalculateDifferentIRRForDifferentExitValues() {
            BigDecimal exitValue1 = new BigDecimal("350000.00");
            BigDecimal exitValue2 = new BigDecimal("450000.00");
            
            Double irr1 = calculatorService.calculateIRR(analysis, 10, exitValue1, null);
            Double irr2 = calculatorService.calculateIRR(analysis, 10, exitValue2, null);
            
            assertNotNull(irr1);
            assertNotNull(irr2);
            assertTrue(irr2 > irr1, "IRR should be higher with higher exit value");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateIRR(null, 10, new BigDecimal("400000.00"), null));
        }

        @Test
        @DisplayName("Should throw exception when years is invalid")
        void shouldThrowExceptionWhenYearsIsInvalid() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateIRR(analysis, 0, new BigDecimal("400000.00"), null));
            
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateIRR(analysis, null, new BigDecimal("400000.00"), null));
        }

        @Test
        @DisplayName("Should throw exception when exit value is negative")
        void shouldThrowExceptionWhenExitValueIsNegative() {
            assertThrows(IllegalArgumentException.class, 
                    () -> calculatorService.calculateIRR(analysis, 10, new BigDecimal("-100000.00"), null));
        }

        @Test
        @DisplayName("Should handle very high exit value")
        void shouldHandleVeryHighExitValue() {
            // Test with very profitable scenario
            BigDecimal exitValue = new BigDecimal("600000.00");
            
            Double irr = calculatorService.calculateIRR(analysis, 10, exitValue, null);
            
            assertNotNull(irr);
            assertTrue(irr > 10, "IRR should be high with very profitable exit");
        }

        @Test
        @DisplayName("Should return null when cash flow cannot be calculated")
        void shouldReturnNullWhenCashFlowCannotBeCalculated() {
            analysis.setIncomeProjection(null);
            
            Double irr = calculatorService.calculateIRR(analysis, 10, new BigDecimal("400000.00"), null);
            
            assertNull(irr);
        }
    }

    // ========================================
    // projectCashFlow Tests
    // ========================================

    @Nested
    @DisplayName("projectCashFlow Tests")
    class ProjectCashFlowTests {

        @Test
        @DisplayName("Should project cash flow for multiple years with growth")
        void shouldProjectCashFlowWithGrowth() {
            // Project 5 years with 3% rent growth and 2% expense inflation
            var projections = calculatorService.projectCashFlow(analysis, 5, 3.0, 2.0);
            
            assertNotNull(projections);
            assertEquals(5, projections.size(), "Should have 5 yearly projections");
            
            // Verify year numbers
            for (int i = 0; i < 5; i++) {
                assertEquals(i + 1, projections.get(i).getYear(), 
                        "Year should be " + (i + 1));
            }
            
            // Verify rent increases each year
            for (int i = 1; i < 5; i++) {
                assertTrue(projections.get(i).getAnnualRent()
                        .compareTo(projections.get(i - 1).getAnnualRent()) > 0,
                        "Rent should increase each year");
            }
            
            // Verify expenses increase each year
            for (int i = 1; i < 5; i++) {
                assertTrue(projections.get(i).getAnnualExpenses()
                        .compareTo(projections.get(i - 1).getAnnualExpenses()) > 0,
                        "Expenses should increase each year");
            }
            
            // Verify mortgage stays constant
            BigDecimal firstYearMortgage = projections.get(0).getAnnualMortgage();
            for (int i = 1; i < 5; i++) {
                assertEquals(firstYearMortgage, projections.get(i).getAnnualMortgage(),
                        "Mortgage payment should remain constant");
            }
            
            // Verify cumulative cash flow increases
            for (int i = 1; i < 5; i++) {
                assertTrue(Math.abs(projections.get(i).getCumulativeCashFlow().doubleValue()) >
                        Math.abs(projections.get(i - 1).getCumulativeCashFlow().doubleValue()),
                        "Cumulative cash flow should grow in absolute value");
            }
        }

        @Test
        @DisplayName("Should handle zero growth rates")
        void shouldHandleZeroGrowthRates() {
            var projections = calculatorService.projectCashFlow(analysis, 3, 0.0, 0.0);
            
            assertNotNull(projections);
            assertEquals(3, projections.size());
            
            // With zero growth, rent and expenses should stay the same
            BigDecimal firstYearRent = projections.get(0).getAnnualRent();
            BigDecimal firstYearExpenses = projections.get(0).getAnnualExpenses();
            
            for (int i = 1; i < 3; i++) {
                assertEquals(firstYearRent, projections.get(i).getAnnualRent(),
                        "Rent should remain constant with 0% growth");
                assertEquals(firstYearExpenses, projections.get(i).getAnnualExpenses(),
                        "Expenses should remain constant with 0% inflation");
            }
        }

        @Test
        @DisplayName("Should handle negative growth rates")
        void shouldHandleNegativeGrowthRates() {
            // Test with declining rent and expenses
            var projections = calculatorService.projectCashFlow(analysis, 3, -2.0, -1.0);
            
            assertNotNull(projections);
            assertEquals(3, projections.size());
            
            // Verify rent decreases each year
            for (int i = 1; i < 3; i++) {
                assertTrue(projections.get(i).getAnnualRent()
                        .compareTo(projections.get(i - 1).getAnnualRent()) < 0,
                        "Rent should decrease with negative growth");
            }
        }

        @Test
        @DisplayName("Should project cash flow for property without financing")
        void shouldProjectCashFlowWithoutFinancing() {
            analysis.setFinancingDetails(null);
            
            var projections = calculatorService.projectCashFlow(analysis, 3, 3.0, 2.0);
            
            assertNotNull(projections);
            assertEquals(3, projections.size());
            
            // Verify no mortgage payments
            for (var projection : projections) {
                assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), 
                        projection.getAnnualMortgage(),
                        "Mortgage should be zero without financing");
            }
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(null, 5, 3.0, 2.0));
        }

        @Test
        @DisplayName("Should throw exception when years is invalid")
        void shouldThrowExceptionWhenYearsIsInvalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(analysis, 0, 3.0, 2.0));
            
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(analysis, null, 3.0, 2.0));
        }

        @Test
        @DisplayName("Should use default values when growth rates are null")
        void shouldUseDefaultValuesWhenGrowthRatesAreNull() {
            // Should not throw exception, should use defaults (3% for both)
            var projections1 = calculatorService.projectCashFlow(analysis, 5, null, 2.0);
            assertNotNull(projections1);
            assertEquals(5, projections1.size());
            
            var projections2 = calculatorService.projectCashFlow(analysis, 5, 3.0, null);
            assertNotNull(projections2);
            assertEquals(5, projections2.size());
            
            var projections3 = calculatorService.projectCashFlow(analysis, 5, null, null);
            assertNotNull(projections3);
            assertEquals(5, projections3.size());
        }

        @Test
        @DisplayName("Should return null when required data is missing")
        void shouldReturnNullWhenDataIsMissing() {
            analysis.setIncomeProjection(null);
            
            var projections = calculatorService.projectCashFlow(analysis, 5, 3.0, 2.0);
            
            assertNull(projections);
        }

        @Test
        @DisplayName("Should calculate correct cash flow formula")
        void shouldCalculateCorrectCashFlowFormula() {
            var projections = calculatorService.projectCashFlow(analysis, 1, 0.0, 0.0);
            
            assertNotNull(projections);
            assertEquals(1, projections.size());
            
            var projection = projections.get(0);
            
            // Verify: cashFlow = rent - expenses - mortgage
            BigDecimal expectedCashFlow = projection.getAnnualRent()
                    .subtract(projection.getAnnualExpenses())
                    .subtract(projection.getAnnualMortgage())
                    .setScale(2, RoundingMode.HALF_UP);
            
            assertEquals(expectedCashFlow, projection.getCashFlow(),
                    "Cash flow should equal rent - expenses - mortgage");
        }
        
        @Test
        @DisplayName("Should throw exception when years exceeds maximum")
        void shouldThrowExceptionWhenYearsExceedsMaximum() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(analysis, 31, 3.0, 2.0),
                    "Should not allow projections beyond 30 years");
        }
        
        @Test
        @DisplayName("Should throw exception when growth rates are out of range")
        void shouldThrowExceptionWhenGrowthRatesOutOfRange() {
            // Test rent growth rate too high
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(analysis, 5, 51.0, 2.0),
                    "Rent growth rate should not exceed 50%");
            
            // Test rent growth rate too low
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(analysis, 5, -51.0, 2.0),
                    "Rent growth rate should not be below -50%");
            
            // Test expense inflation rate too high
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(analysis, 5, 3.0, 51.0),
                    "Expense inflation rate should not exceed 50%");
            
            // Test expense inflation rate too low
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectCashFlow(analysis, 5, 3.0, -51.0),
                    "Expense inflation rate should not be below -50%");
        }
        
        @Test
        @DisplayName("Should support maximum 30 year projections")
        void shouldSupportMaximum30YearProjections() {
            var projections = calculatorService.projectCashFlow(analysis, 30, 3.0, 2.0);
            
            assertNotNull(projections);
            assertEquals(30, projections.size());
            
            // Verify first and last year
            assertEquals(1, projections.get(0).getYear());
            assertEquals(30, projections.get(29).getYear());
        }
    }

    // ========================================
    // projectEquityBuildup Tests
    // ========================================

    @Nested
    @DisplayName("projectEquityBuildup Tests")
    class ProjectEquityBuildupTests {

        @Test
        @DisplayName("Should project equity buildup for multiple years")
        void shouldProjectEquityBuildup() {
            // Project 5 years with 3% appreciation
            var projections = calculatorService.projectEquityBuildup(analysis, 5, 3.0);
            
            assertNotNull(projections);
            assertEquals(5, projections.size(), "Should have 5 yearly projections");
            
            // Verify year numbers
            for (int i = 0; i < 5; i++) {
                assertEquals(i + 1, projections.get(i).getYear(),
                        "Year should be " + (i + 1));
            }
            
            // Verify equity increases each year
            for (int i = 1; i < 5; i++) {
                assertTrue(projections.get(i).getTotalEquity()
                        .compareTo(projections.get(i - 1).getTotalEquity()) > 0,
                        "Total equity should increase each year");
            }
            
            // Verify property value increases each year
            for (int i = 1; i < 5; i++) {
                assertTrue(projections.get(i).getPropertyValue()
                        .compareTo(projections.get(i - 1).getPropertyValue()) > 0,
                        "Property value should increase each year");
            }
            
            // Verify principal paid increases each year
            for (int i = 1; i < 5; i++) {
                assertTrue(projections.get(i).getPrincipalPaid()
                        .compareTo(projections.get(i - 1).getPrincipalPaid()) > 0,
                        "Cumulative principal paid should increase each year");
            }
            
            // Verify loan balance decreases each year
            for (int i = 1; i < 5; i++) {
                assertTrue(projections.get(i).getRemainingLoanBalance()
                        .compareTo(projections.get(i - 1).getRemainingLoanBalance()) < 0,
                        "Remaining loan balance should decrease each year");
            }
            
            // Verify LTV ratio decreases each year
            for (int i = 1; i < 5; i++) {
                if (projections.get(i).getLoanToValueRatio() != null && 
                    projections.get(i - 1).getLoanToValueRatio() != null) {
                    assertTrue(projections.get(i).getLoanToValueRatio() <
                            projections.get(i - 1).getLoanToValueRatio(),
                            "LTV ratio should decrease each year");
                }
            }
        }

        @Test
        @DisplayName("Should handle zero appreciation rate")
        void shouldHandleZeroAppreciation() {
            var projections = calculatorService.projectEquityBuildup(analysis, 3, 0.0);
            
            assertNotNull(projections);
            assertEquals(3, projections.size());
            
            // With zero appreciation, property value should stay the same
            BigDecimal purchasePrice = analysis.getPurchaseDetails().getPurchasePrice();
            
            for (var projection : projections) {
                assertEquals(purchasePrice, projection.getPropertyValue(),
                        "Property value should remain constant with 0% appreciation");
            }
            
            // But equity should still grow due to principal paydown
            for (int i = 1; i < 3; i++) {
                assertTrue(projections.get(i).getTotalEquity()
                        .compareTo(projections.get(i - 1).getTotalEquity()) > 0,
                        "Equity should still grow from principal paydown");
            }
        }

        @Test
        @DisplayName("Should handle negative appreciation rate")
        void shouldHandleNegativeAppreciation() {
            var projections = calculatorService.projectEquityBuildup(analysis, 3, -2.0);
            
            assertNotNull(projections);
            assertEquals(3, projections.size());
            
            // Property value should decrease
            for (int i = 1; i < 3; i++) {
                assertTrue(projections.get(i).getPropertyValue()
                        .compareTo(projections.get(i - 1).getPropertyValue()) < 0,
                        "Property value should decrease with negative appreciation");
            }
        }

        @Test
        @DisplayName("Should project equity for cash purchase")
        void shouldProjectEquityForCashPurchase() {
            analysis.setFinancingDetails(null);
            
            var projections = calculatorService.projectEquityBuildup(analysis, 3, 3.0);
            
            assertNotNull(projections);
            assertEquals(3, projections.size());
            
            // Verify no loan balance
            for (var projection : projections) {
                assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                        projection.getRemainingLoanBalance(),
                        "Loan balance should be zero for cash purchase");
                
                assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                        projection.getPrincipalPaid(),
                        "Principal paid should be zero for cash purchase");
                
                assertNull(projection.getLoanToValueRatio(),
                        "LTV should be null for cash purchase");
            }
            
            // Equity should equal property value for cash purchase
            for (var projection : projections) {
                assertEquals(projection.getPropertyValue(), projection.getTotalEquity(),
                        "Equity should equal property value for cash purchase");
            }
        }

        @Test
        @DisplayName("Should calculate correct equity components")
        void shouldCalculateCorrectEquityComponents() {
            var projections = calculatorService.projectEquityBuildup(analysis, 1, 3.0);
            
            assertNotNull(projections);
            assertEquals(1, projections.size());
            
            var projection = projections.get(0);
            BigDecimal downPayment = analysis.getPurchaseDetails().getDownPaymentAmount();
            
            // Verify: totalEquity = downPayment + principalPaid + appreciation
            BigDecimal expectedEquity = downPayment
                    .add(projection.getPrincipalPaid())
                    .add(projection.getAppreciation())
                    .setScale(2, RoundingMode.HALF_UP);
            
            assertEquals(expectedEquity, projection.getTotalEquity(),
                    "Total equity should equal down payment + principal paid + appreciation");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectEquityBuildup(null, 5, 3.0));
        }

        @Test
        @DisplayName("Should throw exception when years is invalid")
        void shouldThrowExceptionWhenYearsIsInvalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectEquityBuildup(analysis, 0, 3.0));
            
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectEquityBuildup(analysis, null, 3.0));
        }

        @Test
        @DisplayName("Should use default value when appreciation rate is null")
        void shouldUseDefaultValueWhenAppreciationRateIsNull() {
            // Should not throw exception, should use default (3%)
            var projections = calculatorService.projectEquityBuildup(analysis, 5, null);
            assertNotNull(projections);
            assertEquals(5, projections.size());
        }

        @Test
        @DisplayName("Should return null when required data is missing")
        void shouldReturnNullWhenDataIsMissing() {
            analysis.setPurchaseDetails(null);
            
            var projections = calculatorService.projectEquityBuildup(analysis, 5, 3.0);
            
            assertNull(projections);
        }

        @Test
        @DisplayName("Should handle property with no down payment")
        void shouldHandleNoDownPayment() {
            purchaseDetails.setDownPaymentAmount(null);
            
            var projections = calculatorService.projectEquityBuildup(analysis, 3, 3.0);
            
            assertNotNull(projections);
            assertEquals(3, projections.size());
            
            // Initial equity should be zero (no down payment)
            // But equity should grow from principal paydown and appreciation
            assertTrue(projections.get(2).getTotalEquity().compareTo(BigDecimal.ZERO) > 0,
                    "Equity should be positive after 3 years even with no down payment");
        }

        @Test
        @DisplayName("Should verify principal paydown accelerates over time")
        void shouldVerifyPrincipalPaydownAccelerates() {
            var projections = calculatorService.projectEquityBuildup(analysis, 10, 0.0);
            
            assertNotNull(projections);
            assertEquals(10, projections.size());
            
            // Calculate principal paid each year (not cumulative)
            BigDecimal year1Principal = projections.get(0).getPrincipalPaid();
            BigDecimal year10Principal = projections.get(9).getPrincipalPaid()
                    .subtract(projections.get(8).getPrincipalPaid());
            
            // Later years should have more principal paydown (amortization)
            assertTrue(year10Principal.compareTo(year1Principal) > 0,
                    "Principal paydown should be higher in later years due to amortization");
        }
        
        @Test
        @DisplayName("Should throw exception when years exceeds maximum")
        void shouldThrowExceptionWhenYearsExceedsMaximum() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectEquityBuildup(analysis, 31, 3.0),
                    "Should not allow projections beyond 30 years");
        }
        
        @Test
        @DisplayName("Should throw exception when appreciation rate is out of range")
        void shouldThrowExceptionWhenAppreciationRateOutOfRange() {
            // Test appreciation rate too high
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectEquityBuildup(analysis, 5, 51.0),
                    "Appreciation rate should not exceed 50%");
            
            // Test appreciation rate too low
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.projectEquityBuildup(analysis, 5, -51.0),
                    "Appreciation rate should not be below -50%");
        }
        
        @Test
        @DisplayName("Should support maximum 30 year projections")
        void shouldSupportMaximum30YearProjections() {
            var projections = calculatorService.projectEquityBuildup(analysis, 30, 3.0);
            
            assertNotNull(projections);
            assertEquals(30, projections.size());
            
            // Verify first and last year
            assertEquals(1, projections.get(0).getYear());
            assertEquals(30, projections.get(29).getYear());
            
            // Verify equity grows over time
            assertTrue(projections.get(29).getTotalEquity()
                    .compareTo(projections.get(0).getTotalEquity()) > 0,
                    "Equity should grow over 30 years");
        }
    }

    // ========================================
    // calculateAllMetrics Tests
    // ========================================

    @Nested
    @DisplayName("calculateAllMetrics Tests")
    class CalculateAllMetricsTests {

        @Test
        @DisplayName("Should calculate all metrics for a complete analysis")
        void shouldCalculateAllMetricsForCompleteAnalysis() {
            // Given: A complete rental analysis with all required data
            RentalAnalysis completeAnalysis = createCompleteAnalysis(
                    new BigDecimal("300000"),  // Purchase price
                    new BigDecimal("60000"),   // Down payment (20%)
                    new BigDecimal("9000"),    // Closing costs (3%)
                    new BigDecimal("2000"),    // Monthly rent
                    8.0,                        // Vacancy rate
                    new BigDecimal("3600"),    // Annual property tax
                    new BigDecimal("1200"),    // Annual insurance
                    new BigDecimal("240000"),  // Loan amount
                    6.5,                        // Interest rate
                    30                          // Loan term
            );

            // When: Calculate all metrics
            CalculatedMetrics metrics = calculatorService.calculateAllMetrics(completeAnalysis);

            // Then: All metrics should be calculated
            assertNotNull(metrics, "Metrics should not be null");
            assertNotNull(metrics.getGrossYield(), "Gross yield should be calculated");
            assertNotNull(metrics.getNetYield(), "Net yield should be calculated");
            assertNotNull(metrics.getMonthlyCashFlow(), "Monthly cash flow should be calculated");
            assertNotNull(metrics.getAnnualCashFlow(), "Annual cash flow should be calculated");
            assertNotNull(metrics.getCapRate(), "Cap rate should be calculated");
            assertNotNull(metrics.getCashOnCashReturn(), "Cash-on-cash return should be calculated");
            assertNotNull(metrics.getTotalROI(), "Total ROI should be calculated");
            assertNotNull(metrics.getDebtServiceCoverageRatio(), "DSCR should be calculated");
            assertNotNull(metrics.getBreakEvenRatio(), "Break-even ratio should be calculated");

            // Verify reasonable values
            assertTrue(metrics.getGrossYield() > 0, "Gross yield should be positive");
            assertTrue(metrics.getNetYield() > 0, "Net yield should be positive");
            assertTrue(metrics.getCapRate() > 0, "Cap rate should be positive");
            assertTrue(metrics.getDebtServiceCoverageRatio() > 0, "DSCR should be positive");
            assertTrue(metrics.getBreakEvenRatio() > 0, "Break-even ratio should be positive");
        }

        @Test
        @DisplayName("Should calculate metrics for cash purchase (no financing)")
        void shouldCalculateMetricsForCashPurchase() {
            // Given: A cash purchase (no financing)
            RentalAnalysis cashAnalysis = new RentalAnalysis();
            
            PurchaseDetails purchase = new PurchaseDetails();
            purchase.setPurchasePrice(new BigDecimal("200000"));
            purchase.setDownPaymentAmount(new BigDecimal("200000")); // 100% cash
            purchase.setClosingCosts(new BigDecimal("6000"));
            
            IncomeProjection income = new IncomeProjection();
            income.setMonthlyRent(new BigDecimal("1800"));
            income.setVacancyRate(5.0);
            
            ExpenseProjection expenses = new ExpenseProjection();
            expenses.setAnnualPropertyTax(new BigDecimal("2400"));
            expenses.setAnnualInsurance(new BigDecimal("1000"));
            expenses.setPropertyManagementPercent(10.0);
            expenses.setMaintenancePercent(1.0);
            
            // No financing details - cash purchase
            
            cashAnalysis.setPurchaseDetails(purchase);
            cashAnalysis.setIncomeProjection(income);
            cashAnalysis.setExpenseProjection(expenses);

            // When: Calculate all metrics
            CalculatedMetrics metrics = calculatorService.calculateAllMetrics(cashAnalysis);

            // Then: Basic metrics should be calculated
            assertNotNull(metrics, "Metrics should not be null");
            assertNotNull(metrics.getGrossYield(), "Gross yield should be calculated");
            assertNotNull(metrics.getNetYield(), "Net yield should be calculated");
            assertNotNull(metrics.getMonthlyCashFlow(), "Monthly cash flow should be calculated");
            assertNotNull(metrics.getAnnualCashFlow(), "Annual cash flow should be calculated");
            assertNotNull(metrics.getCapRate(), "Cap rate should be calculated");
            assertNotNull(metrics.getCashOnCashReturn(), "Cash-on-cash return should be calculated");
            
            // DSCR should be null for cash purchase (no debt)
            assertNull(metrics.getDebtServiceCoverageRatio(), "DSCR should be null for cash purchase");
        }

        @Test
        @DisplayName("Should populate derived fields during calculation")
        void shouldPopulateDerivedFieldsDuringCalculation() {
            // Given: Analysis with percentage-based values that need calculation
            // Note: We need to manually calculate down payment first for validation to pass
            RentalAnalysis analysisWithPercents = new RentalAnalysis();
            
            PurchaseDetails purchase = new PurchaseDetails();
            purchase.setPurchasePrice(new BigDecimal("300000"));
            purchase.setDownPaymentPercent(20.0); // Percent instead of amount
            purchase.setClosingCostsPercent(3.0);  // Percent instead of amount
            // Calculate down payment and closing costs for validation
            purchase.calculateDownPaymentAmount();
            purchase.calculateClosingCosts();
            
            IncomeProjection income = new IncomeProjection();
            income.setMonthlyRent(new BigDecimal("2000"));
            income.setVacancyRate(8.0);
            
            ExpenseProjection expenses = new ExpenseProjection();
            expenses.setAnnualPropertyTax(new BigDecimal("3600"));
            expenses.setAnnualInsurance(new BigDecimal("1200"));
            expenses.setPropertyManagementPercent(10.0);
            expenses.setMaintenancePercent(1.0);
            expenses.setCapExPercent(0.5);
            
            FinancingDetails financing = new FinancingDetails();
            // Loan amount will be calculated from purchase price - down payment
            financing.setInterestRate(6.5);
            financing.setLoanTermYears(30);
            
            analysisWithPercents.setPurchaseDetails(purchase);
            analysisWithPercents.setIncomeProjection(income);
            analysisWithPercents.setExpenseProjection(expenses);
            analysisWithPercents.setFinancingDetails(financing);

            // When: Calculate all metrics
            CalculatedMetrics metrics = calculatorService.calculateAllMetrics(analysisWithPercents);

            // Then: Derived fields should be populated
            assertNotNull(purchase.getDownPaymentAmount(), "Down payment amount should be calculated");
            assertEquals(new BigDecimal("60000.00"), purchase.getDownPaymentAmount().setScale(2, RoundingMode.HALF_UP),
                    "Down payment should be 20% of $300,000");
            
            assertNotNull(purchase.getClosingCosts(), "Closing costs should be calculated");
            assertEquals(new BigDecimal("9000.00"), purchase.getClosingCosts().setScale(2, RoundingMode.HALF_UP),
                    "Closing costs should be 3% of $300,000");
            
            assertNotNull(financing.getLoanAmount(), "Loan amount should be calculated");
            assertEquals(new BigDecimal("240000.00"), financing.getLoanAmount().setScale(2, RoundingMode.HALF_UP),
                    "Loan amount should be $300,000 - $60,000");
            
            assertNotNull(financing.getMonthlyPayment(), "Monthly payment should be calculated");
            
            assertNotNull(expenses.getPropertyManagementAmount(), "Property management amount should be calculated");
            assertNotNull(expenses.getMaintenanceAmount(), "Maintenance amount should be calculated");
            assertNotNull(expenses.getCapExAmount(), "CapEx amount should be calculated");
            
            // Metrics should also be calculated
            assertNotNull(metrics, "Metrics should not be null");
            assertNotNull(metrics.getGrossYield(), "Gross yield should be calculated");
        }

        @Test
        @DisplayName("Should throw exception when analysis is null")
        void shouldThrowExceptionWhenAnalysisIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.calculateAllMetrics(null),
                    "Should throw exception for null analysis");
        }

        @Test
        @DisplayName("Should throw exception when validation fails")
        void shouldThrowExceptionWhenValidationFails() {
            // Given: Invalid analysis (missing required fields)
            RentalAnalysis invalidAnalysis = new RentalAnalysis();
            
            PurchaseDetails purchase = new PurchaseDetails();
            purchase.setPurchasePrice(BigDecimal.ZERO); // Invalid: must be > 0
            
            invalidAnalysis.setPurchaseDetails(purchase);

            // When/Then: Should throw exception
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> calculatorService.calculateAllMetrics(invalidAnalysis),
                    "Should throw exception for invalid analysis");
            
            assertTrue(exception.getMessage().contains("Validation failed"),
                    "Exception message should indicate validation failure");
        }

        @Test
        @DisplayName("Should handle negative cash flow property")
        void shouldHandleNegativeCashFlowProperty() {
            // Given: Property with negative cash flow
            RentalAnalysis negativeFlowAnalysis = createCompleteAnalysis(
                    new BigDecimal("500000"),  // Expensive property
                    new BigDecimal("100000"),  // Down payment (20%)
                    new BigDecimal("15000"),   // Closing costs
                    new BigDecimal("2000"),    // Monthly rent (low for price)
                    10.0,                       // Vacancy rate
                    new BigDecimal("8000"),    // Annual property tax
                    new BigDecimal("3000"),    // Annual insurance
                    new BigDecimal("400000"),  // Loan amount
                    7.0,                        // Interest rate
                    30                          // Loan term
            );

            // When: Calculate all metrics
            CalculatedMetrics metrics = calculatorService.calculateAllMetrics(negativeFlowAnalysis);

            // Then: Metrics should still be calculated
            assertNotNull(metrics, "Metrics should not be null");
            assertNotNull(metrics.getMonthlyCashFlow(), "Monthly cash flow should be calculated");
            assertNotNull(metrics.getAnnualCashFlow(), "Annual cash flow should be calculated");
            
            // Cash flow should be negative
            assertTrue(metrics.getMonthlyCashFlow().compareTo(BigDecimal.ZERO) < 0,
                    "Monthly cash flow should be negative");
            assertTrue(metrics.getAnnualCashFlow().compareTo(BigDecimal.ZERO) < 0,
                    "Annual cash flow should be negative");
            
            // Other metrics should still be calculated
            assertNotNull(metrics.getGrossYield(), "Gross yield should be calculated");
            assertNotNull(metrics.getCapRate(), "Cap rate should be calculated");
        }

        @Test
        @DisplayName("Should calculate metrics with minimal expenses")
        void shouldCalculateMetricsWithMinimalExpenses() {
            // Given: Analysis with minimal expenses
            RentalAnalysis minimalExpensesAnalysis = new RentalAnalysis();
            
            PurchaseDetails purchase = new PurchaseDetails();
            purchase.setPurchasePrice(new BigDecimal("200000"));
            purchase.setDownPaymentAmount(new BigDecimal("40000"));
            purchase.setClosingCosts(new BigDecimal("6000"));
            
            IncomeProjection income = new IncomeProjection();
            income.setMonthlyRent(new BigDecimal("1500"));
            income.setVacancyRate(5.0);
            
            ExpenseProjection expenses = new ExpenseProjection();
            expenses.setAnnualPropertyTax(new BigDecimal("2000"));
            expenses.setAnnualInsurance(new BigDecimal("800"));
            // No other expenses
            
            FinancingDetails financing = new FinancingDetails();
            financing.setLoanAmount(new BigDecimal("160000"));
            financing.setInterestRate(6.0);
            financing.setLoanTermYears(30);
            
            minimalExpensesAnalysis.setPurchaseDetails(purchase);
            minimalExpensesAnalysis.setIncomeProjection(income);
            minimalExpensesAnalysis.setExpenseProjection(expenses);
            minimalExpensesAnalysis.setFinancingDetails(financing);

            // When: Calculate all metrics
            CalculatedMetrics metrics = calculatorService.calculateAllMetrics(minimalExpensesAnalysis);

            // Then: All metrics should be calculated
            assertNotNull(metrics, "Metrics should not be null");
            assertNotNull(metrics.getGrossYield(), "Gross yield should be calculated");
            assertNotNull(metrics.getNetYield(), "Net yield should be calculated");
            assertNotNull(metrics.getMonthlyCashFlow(), "Monthly cash flow should be calculated");
            
            // Net yield should be higher with minimal expenses
            assertTrue(metrics.getNetYield() > 0, "Net yield should be positive");
        }

        @Test
        @DisplayName("Should calculate metrics with high vacancy rate")
        void shouldCalculateMetricsWithHighVacancyRate() {
            // Given: Analysis with high vacancy rate
            RentalAnalysis highVacancyAnalysis = createCompleteAnalysis(
                    new BigDecimal("300000"),
                    new BigDecimal("60000"),
                    new BigDecimal("9000"),
                    new BigDecimal("2000"),
                    20.0,                       // High vacancy rate (20%)
                    new BigDecimal("3600"),
                    new BigDecimal("1200"),
                    new BigDecimal("240000"),
                    6.5,
                    30
            );

            // When: Calculate all metrics
            CalculatedMetrics metrics = calculatorService.calculateAllMetrics(highVacancyAnalysis);

            // Then: Metrics should reflect reduced income
            assertNotNull(metrics, "Metrics should not be null");
            assertNotNull(metrics.getMonthlyCashFlow(), "Monthly cash flow should be calculated");
            
            // Cash flow should be lower due to high vacancy
            // Compare with a similar property with normal vacancy
            RentalAnalysis normalVacancyAnalysis = createCompleteAnalysis(
                    new BigDecimal("300000"),
                    new BigDecimal("60000"),
                    new BigDecimal("9000"),
                    new BigDecimal("2000"),
                    8.0,                        // Normal vacancy rate (8%)
                    new BigDecimal("3600"),
                    new BigDecimal("1200"),
                    new BigDecimal("240000"),
                    6.5,
                    30
            );
            
            CalculatedMetrics normalMetrics = calculatorService.calculateAllMetrics(normalVacancyAnalysis);
            
            assertTrue(metrics.getMonthlyCashFlow().compareTo(normalMetrics.getMonthlyCashFlow()) < 0,
                    "High vacancy should result in lower cash flow");
        }

        @Test
        @DisplayName("Should verify all metrics are within reasonable ranges")
        void shouldVerifyAllMetricsAreWithinReasonableRanges() {
            // Given: A typical rental property
            RentalAnalysis typicalAnalysis = createCompleteAnalysis(
                    new BigDecimal("300000"),
                    new BigDecimal("60000"),
                    new BigDecimal("9000"),
                    new BigDecimal("2000"),
                    8.0,
                    new BigDecimal("3600"),
                    new BigDecimal("1200"),
                    new BigDecimal("240000"),
                    6.5,
                    30
            );

            // When: Calculate all metrics
            CalculatedMetrics metrics = calculatorService.calculateAllMetrics(typicalAnalysis);

            // Then: Verify reasonable ranges
            assertNotNull(metrics, "Metrics should not be null");
            
            // Gross yield typically 4-12%
            assertTrue(metrics.getGrossYield() >= 4.0 && metrics.getGrossYield() <= 12.0,
                    "Gross yield should be between 4% and 12%");
            
            // Net yield typically 2-10%
            assertTrue(metrics.getNetYield() >= 0.0 && metrics.getNetYield() <= 12.0,
                    "Net yield should be between 0% and 12%");
            
            // Cap rate typically 3-10%
            assertTrue(metrics.getCapRate() >= 0.0 && metrics.getCapRate() <= 15.0,
                    "Cap rate should be between 0% and 15%");
            
            // DSCR typically 0.8-2.0
            assertTrue(metrics.getDebtServiceCoverageRatio() >= 0.0 && 
                      metrics.getDebtServiceCoverageRatio() <= 3.0,
                    "DSCR should be between 0.0 and 3.0");
            
            // Break-even ratio typically 60-95%
            assertTrue(metrics.getBreakEvenRatio() >= 0.0 && metrics.getBreakEvenRatio() <= 150.0,
                    "Break-even ratio should be between 0% and 150%");
        }

        /**
         * Helper method to create a complete RentalAnalysis for testing.
         */
        private RentalAnalysis createCompleteAnalysis(
                BigDecimal purchasePrice, BigDecimal downPayment, BigDecimal closingCosts,
                BigDecimal monthlyRent, Double vacancyRate,
                BigDecimal propertyTax, BigDecimal insurance,
                BigDecimal loanAmount, Double interestRate, Integer loanTerm) {
            
            RentalAnalysis analysis = new RentalAnalysis();
            
            PurchaseDetails purchase = new PurchaseDetails();
            purchase.setPurchasePrice(purchasePrice);
            purchase.setDownPaymentAmount(downPayment);
            purchase.setClosingCosts(closingCosts);
            
            IncomeProjection income = new IncomeProjection();
            income.setMonthlyRent(monthlyRent);
            income.setVacancyRate(vacancyRate);
            
            ExpenseProjection expenses = new ExpenseProjection();
            expenses.setAnnualPropertyTax(propertyTax);
            expenses.setAnnualInsurance(insurance);
            expenses.setPropertyManagementPercent(10.0);
            expenses.setMaintenancePercent(1.0);
            expenses.setCapExPercent(0.5);
            
            FinancingDetails financing = new FinancingDetails();
            financing.setLoanAmount(loanAmount);
            financing.setInterestRate(interestRate);
            financing.setLoanTermYears(loanTerm);
            
            analysis.setPurchaseDetails(purchase);
            analysis.setIncomeProjection(income);
            analysis.setExpenseProjection(expenses);
            analysis.setFinancingDetails(financing);
            
            return analysis;
        }
    }
}