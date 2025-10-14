package com.non.rental.service;

import com.non.rental.entity.*;
import com.non.rental.service.dto.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

/**
 * Demonstration test showing validation in action with realistic scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Validation Demo - Real World Scenarios")
class ValidationDemoTest {

    @InjectMocks
    private RentalYieldCalculatorServiceImpl calculatorService;

    @Test
    @DisplayName("Demo: Perfect Investment Property - No Issues")
    void demoPerfectProperty() {
        RentalAnalysis analysis = createAnalysis(
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

        ValidationResult result = calculatorService.validateAnalysisInputs(analysis);

        System.out.println("\n=== PERFECT PROPERTY ===");
        System.out.println("Status: " + (result.isValid() ? "✅ VALID" : "❌ INVALID"));
        System.out.println("Summary: " + result.getSummary());
        System.out.println("Errors: " + result.getErrorCount());
        System.out.println("Warnings: " + result.getWarningCount());
    }

    @Test
    @DisplayName("Demo: Risky Investment - Multiple Warnings")
    void demoRiskyProperty() {
        RentalAnalysis analysis = createAnalysis(
                new BigDecimal("300000"),  // Purchase price
                new BigDecimal("3000"),    // Down payment (1% - very low!)
                new BigDecimal("20000"),   // Closing costs (6.67% - high!)
                new BigDecimal("1000"),    // Monthly rent (low for price)
                25.0,                       // Vacancy rate (very high!)
                new BigDecimal("12000"),   // Annual property tax (4% - high!)
                new BigDecimal("6000"),    // Annual insurance (2% - high!)
                new BigDecimal("297000"),  // Loan amount
                15.0,                       // Interest rate (very high!)
                25                          // Loan term (unusual)
        );

        ValidationResult result = calculatorService.validateAnalysisInputs(analysis);

        System.out.println("\n=== RISKY PROPERTY ===");
        System.out.println("Status: " + (result.isValid() ? "✅ VALID" : "❌ INVALID"));
        System.out.println("Summary: " + result.getSummary());
        System.out.println("Errors: " + result.getErrorCount());
        System.out.println("Warnings: " + result.getWarningCount());
        
        if (result.hasWarnings()) {
            System.out.println("\n⚠️  WARNINGS:");
            result.getWarnings().forEach(w -> System.out.println("  - " + w));
        }
    }

    @Test
    @DisplayName("Demo: Invalid Property - Critical Errors")
    void demoInvalidProperty() {
        RentalAnalysis analysis = createAnalysis(
                BigDecimal.ZERO,           // Purchase price (ERROR: must be > 0)
                new BigDecimal("-10000"),  // Down payment (ERROR: negative)
                new BigDecimal("-5000"),   // Closing costs (ERROR: negative)
                BigDecimal.ZERO,           // Monthly rent (ERROR: must be > 0)
                60.0,                       // Vacancy rate (ERROR: > 50%)
                new BigDecimal("-1000"),   // Annual property tax (ERROR: negative)
                new BigDecimal("-500"),    // Annual insurance (ERROR: negative)
                new BigDecimal("240000"),  // Loan amount
                0.0,                        // Interest rate (ERROR: must be > 0 with loan)
                0                           // Loan term (ERROR: must be >= 1)
        );

        ValidationResult result = calculatorService.validateAnalysisInputs(analysis);

        System.out.println("\n=== INVALID PROPERTY ===");
        System.out.println("Status: " + (result.isValid() ? "✅ VALID" : "❌ INVALID"));
        System.out.println("Summary: " + result.getSummary());
        System.out.println("Errors: " + result.getErrorCount());
        System.out.println("Warnings: " + result.getWarningCount());
        
        if (result.hasErrors()) {
            System.out.println("\n❌ ERRORS:");
            result.getErrors().forEach(e -> System.out.println("  - " + e));
        }
    }

    @Test
    @DisplayName("Demo: Cash Purchase - No Financing")
    void demoCashPurchase() {
        RentalAnalysis analysis = new RentalAnalysis();
        
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
        
        analysis.setPurchaseDetails(purchase);
        analysis.setIncomeProjection(income);
        analysis.setExpenseProjection(expenses);

        ValidationResult result = calculatorService.validateAnalysisInputs(analysis);

        System.out.println("\n=== CASH PURCHASE ===");
        System.out.println("Status: " + (result.isValid() ? "✅ VALID" : "❌ INVALID"));
        System.out.println("Summary: " + result.getSummary());
        System.out.println("Errors: " + result.getErrorCount());
        System.out.println("Warnings: " + result.getWarningCount());
        
        if (result.hasWarnings()) {
            System.out.println("\n⚠️  WARNINGS:");
            result.getWarnings().forEach(w -> System.out.println("  - " + w));
        }
    }

    @Test
    @DisplayName("Demo: Negative Cash Flow Property")
    void demoNegativeCashFlow() {
        RentalAnalysis analysis = createAnalysis(
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

        ValidationResult result = calculatorService.validateAnalysisInputs(analysis);

        System.out.println("\n=== NEGATIVE CASH FLOW PROPERTY ===");
        System.out.println("Status: " + (result.isValid() ? "✅ VALID" : "❌ INVALID"));
        System.out.println("Summary: " + result.getSummary());
        System.out.println("Errors: " + result.getErrorCount());
        System.out.println("Warnings: " + result.getWarningCount());
        
        if (result.hasWarnings()) {
            System.out.println("\n⚠️  WARNINGS:");
            result.getWarnings().forEach(w -> System.out.println("  - " + w));
        }
    }

    // Helper method to create analysis
    private RentalAnalysis createAnalysis(
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