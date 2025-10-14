# Rental Analysis Entities Documentation

## Overview
This document describes the JPA entities created for the rental property yield calculator application. All entities follow Jmix 2.6.2 best practices and are designed to work with Spring Boot 2.x.

## Entity Structure

### 1. AnalysisStatus (Enum)
**File:** `AnalysisStatus.java`

An enumeration representing the status of a rental analysis:
- `DRAFT` - Analysis is being worked on
- `COMPLETED` - Analysis is finalized
- `ARCHIVED` - Analysis is archived for historical reference

Implements `EnumClass<String>` for Jmix compatibility.

---

### 2. RentalAnalysis (Main Entity)
**File:** `RentalAnalysis.java`

The main entity that ties together all aspects of a rental property analysis.

**Key Fields:**
- `id` (UUID) - Primary key
- `user` (ManyToOne to User) - Owner of the analysis
- `propertyAddress` (String) - Address of the property
- `propertyId` (String) - RentCast property ID
- `analysisName` (String) - User-friendly name for the analysis (InstanceName)
- `createdDate` (LocalDateTime) - When the analysis was created
- `lastModifiedDate` (LocalDateTime) - Last modification timestamp
- `status` (AnalysisStatus enum) - Current status

**Relationships:**
- One-to-One with `PurchaseDetails` (cascade ALL, orphan removal)
- One-to-One with `FinancingDetails` (cascade ALL, orphan removal)
- One-to-One with `IncomeProjection` (cascade ALL, orphan removal)
- One-to-One with `ExpenseProjection` (cascade ALL, orphan removal)
- One-to-One with `CalculatedMetrics` (cascade ALL, orphan removal)

**Lifecycle Methods:**
- `@PrePersist` - Sets createdDate, lastModifiedDate, and default status (DRAFT)
- `@PreUpdate` - Updates lastModifiedDate

**Indexes:**
- `IDX_RENTAL_ANALYSIS_USER` on user_id
- `IDX_RENTAL_ANALYSIS_PROPERTY_ID` on property_id
- `IDX_RENTAL_ANALYSIS_STATUS` on status

---

### 3. PurchaseDetails
**File:** `PurchaseDetails.java`

Contains all purchase-related information for the property.

**Key Fields:**
- `id` (UUID) - Primary key
- `rentalAnalysis` (OneToOne) - Parent analysis
- `purchasePrice` (BigDecimal) - Property purchase price
- `downPaymentAmount` (BigDecimal) - Down payment in dollars
- `downPaymentPercent` (Double) - Down payment as percentage
- `closingCosts` (BigDecimal) - Closing costs in dollars
- `closingCostsPercent` (Double) - Closing costs as percentage
- `renovationCosts` (BigDecimal) - Renovation/repair costs

**Calculated Fields (Transient):**
- `getTotalCashInvested()` - Sum of down payment, closing costs, and renovation costs

**Helper Methods:**
- `calculateDownPaymentAmount()` - Calculates amount from percentage
- `calculateDownPaymentPercent()` - Calculates percentage from amount
- `calculateClosingCosts()` - Calculates costs from percentage
- `calculateClosingCostsPercent()` - Calculates percentage from costs

**Validations:**
- Purchase price must be greater than 0
- All percentages must be >= 0

---

### 4. FinancingDetails
**File:** `FinancingDetails.java`

Contains loan and financing information.

**Key Fields:**
- `id` (UUID) - Primary key
- `rentalAnalysis` (OneToOne) - Parent analysis
- `loanAmount` (BigDecimal) - Total loan amount
- `interestRate` (Double) - Annual interest rate percentage
- `loanTermYears` (Integer) - Loan term in years
- `pointsPaid` (Double) - Points paid as percentage
- `monthlyPayment` (BigDecimal) - Calculated monthly mortgage payment

**Calculated Fields (Transient):**
- `getPointsCost()` - Dollar cost of points paid

**Helper Methods:**
- `calculateLoanAmount(purchasePrice, downPayment)` - Calculates loan amount
- `calculateMonthlyPayment()` - Calculates monthly payment using amortization formula:
  ```
  M = P * [r(1+r)^n] / [(1+r)^n - 1]
  ```
  where M = monthly payment, P = principal, r = monthly rate, n = number of payments

**Validations:**
- Interest rate must be >= 0
- Loan term must be >= 1 year

---

### 5. IncomeProjection
**File:** `IncomeProjection.java`

Contains income projections and vacancy calculations.

**Key Fields:**
- `id` (UUID) - Primary key
- `rentalAnalysis` (OneToOne) - Parent analysis
- `monthlyRent` (BigDecimal) - Expected monthly rent
- `otherMonthlyIncome` (BigDecimal) - Other income (parking, laundry, storage)
- `vacancyRate` (Double) - Expected vacancy rate percentage

**Calculated Fields (Transient):**
- `getGrossMonthlyIncome()` - Rent + other income
- `getEffectiveMonthlyIncome()` - Gross income adjusted for vacancy
- `getAnnualEffectiveIncome()` - Effective monthly income × 12
- `getMonthlyVacancyLoss()` - Income lost to vacancy

**Validations:**
- Monthly rent must be >= 0
- Vacancy rate must be between 0 and 100

---

### 6. ExpenseProjection
**File:** `ExpenseProjection.java`

Contains all operating expense projections.

**Key Fields:**
- `id` (UUID) - Primary key
- `rentalAnalysis` (OneToOne) - Parent analysis
- `annualPropertyTax` (BigDecimal) - Annual property taxes
- `annualInsurance` (BigDecimal) - Annual insurance premium
- `monthlyHOAFees` (BigDecimal) - Monthly HOA/condo fees
- `propertyManagementPercent` (Double) - Management fee as % of rent
- `propertyManagementAmount` (BigDecimal) - Management fee in dollars
- `maintenancePercent` (Double) - Maintenance as % of property value
- `maintenanceAmount` (BigDecimal) - Monthly maintenance cost
- `capExPercent` (Double) - Capital expenditures as % of property value
- `capExAmount` (BigDecimal) - Monthly CapEx reserve
- `monthlyUtilities` (BigDecimal) - Monthly utilities (if owner-paid)
- `otherMonthlyExpenses` (BigDecimal) - Other monthly expenses

**Calculated Fields (Transient):**
- `getTotalMonthlyExpenses()` - Sum of all monthly expenses
- `getTotalAnnualExpenses()` - Total monthly × 12
- `getMonthlyPropertyTax()` - Annual tax / 12
- `getMonthlyInsurance()` - Annual insurance / 12

**Helper Methods:**
- `calculatePropertyManagementAmount(monthlyRent)` - Calculates from percentage
- `calculateMaintenanceAmount(propertyValue)` - Calculates from percentage
- `calculateCapExAmount(propertyValue)` - Calculates from percentage

**Validations:**
- All amounts must be >= 0

---

### 7. CalculatedMetrics
**File:** `CalculatedMetrics.java`

Contains all calculated investment metrics and KPIs.

**Key Fields:**
- `id` (UUID) - Primary key
- `rentalAnalysis` (OneToOne) - Parent analysis
- `grossYield` (Double) - Gross rental yield percentage
- `netYield` (Double) - Net rental yield percentage
- `monthlyCashFlow` (BigDecimal) - Monthly cash flow
- `annualCashFlow` (BigDecimal) - Annual cash flow
- `capRate` (Double) - Capitalization rate percentage
- `cashOnCashReturn` (Double) - Cash-on-cash return percentage
- `roi` (Double) - Return on investment percentage
- `irr` (Double) - Internal rate of return percentage
- `calculatedDate` (LocalDateTime) - When metrics were calculated

**Lifecycle Methods:**
- `@PrePersist` and `@PreUpdate` - Updates calculatedDate

**Helper Methods:**
- `calculateAllMetrics(...)` - Calculates all metrics at once
- `calculateGrossYield(purchasePrice, annualGrossIncome)` - Gross Yield = (Annual Gross Income / Purchase Price) × 100
- `calculateNetYield(purchasePrice, annualNetIncome)` - Net Yield = (Annual Net Income / Purchase Price) × 100
- `calculateCapRate(purchasePrice, annualNOI)` - Cap Rate = (NOI / Purchase Price) × 100
- `calculateCashOnCashReturn(totalCashInvested, annualCashFlow)` - CoC = (Annual Cash Flow / Total Cash Invested) × 100

**Metric Definitions:**
- **Gross Yield**: Annual gross rental income as % of purchase price
- **Net Yield**: Annual net income as % of purchase price
- **Cap Rate**: Net Operating Income as % of purchase price (excludes mortgage)
- **Cash on Cash Return**: Annual cash flow as % of total cash invested
- **ROI**: Return on investment percentage
- **IRR**: Internal rate of return (approximated as CoC for now)

---

## Database Schema

All entities will create the following tables:
- `RENTAL_ANALYSIS` - Main analysis table
- `PURCHASE_DETAILS` - Purchase information
- `FINANCING_DETAILS` - Loan information
- `INCOME_PROJECTION` - Income projections
- `EXPENSE_PROJECTION` - Expense projections
- `CALCULATED_METRICS` - Investment metrics

## Usage Example

```java
// Create a new rental analysis
RentalAnalysis analysis = new RentalAnalysis();
analysis.setUser(currentUser);
analysis.setPropertyAddress("123 Main St, City, State 12345");
analysis.setPropertyId("rentcast-property-id");
analysis.setAnalysisName("Downtown Condo Investment");
analysis.setStatus(AnalysisStatus.DRAFT);

// Add purchase details
PurchaseDetails purchase = new PurchaseDetails();
purchase.setRentalAnalysis(analysis);
purchase.setPurchasePrice(new BigDecimal("300000"));
purchase.setDownPaymentPercent(20.0);
purchase.calculateDownPaymentAmount(); // Calculates $60,000
purchase.setClosingCostsPercent(3.0);
purchase.calculateClosingCosts(); // Calculates $9,000
purchase.setRenovationCosts(new BigDecimal("10000"));
analysis.setPurchaseDetails(purchase);

// Add financing details
FinancingDetails financing = new FinancingDetails();
financing.setRentalAnalysis(analysis);
financing.calculateLoanAmount(purchase.getPurchasePrice(), purchase.getDownPaymentAmount());
financing.setInterestRate(6.5);
financing.setLoanTermYears(30);
financing.calculateMonthlyPayment(); // Calculates mortgage payment
analysis.setFinancingDetails(financing);

// Add income projection
IncomeProjection income = new IncomeProjection();
income.setRentalAnalysis(analysis);
income.setMonthlyRent(new BigDecimal("2500"));
income.setOtherMonthlyIncome(new BigDecimal("100"));
income.setVacancyRate(5.0);
analysis.setIncomeProjection(income);

// Add expense projection
ExpenseProjection expenses = new ExpenseProjection();
expenses.setRentalAnalysis(analysis);
expenses.setAnnualPropertyTax(new BigDecimal("3600"));
expenses.setAnnualInsurance(new BigDecimal("1200"));
expenses.setMonthlyHOAFees(new BigDecimal("200"));
expenses.setPropertyManagementPercent(10.0);
expenses.calculatePropertyManagementAmount(income.getMonthlyRent());
expenses.setMaintenancePercent(1.0);
expenses.calculateMaintenanceAmount(purchase.getPurchasePrice());
expenses.setCapExPercent(0.5);
expenses.calculateCapExAmount(purchase.getPurchasePrice());
analysis.setExpenseProjection(expenses);

// Calculate metrics
CalculatedMetrics metrics = new CalculatedMetrics();
metrics.setRentalAnalysis(analysis);
metrics.calculateAllMetrics(
    purchase.getPurchasePrice(),
    purchase.getTotalCashInvested(),
    income.getAnnualEffectiveIncome(),
    income.getEffectiveMonthlyIncome().subtract(expenses.getTotalMonthlyExpenses()).multiply(BigDecimal.valueOf(12)),
    income.getEffectiveMonthlyIncome().subtract(expenses.getTotalMonthlyExpenses()).subtract(financing.getMonthlyPayment()),
    expenses.getTotalAnnualExpenses()
);
analysis.setCalculatedMetrics(metrics);

// Save the analysis (cascade will save all related entities)
dataManager.save(analysis);
```

## Next Steps

1. **Run Liquibase/Database Migration**: The entities will need to be synchronized with the database. Jmix Studio can generate the necessary Liquibase changelogs.

2. **Create Views**: Create Jmix views for CRUD operations on these entities.

3. **Create Services**: Consider creating a service class (e.g., `RentalAnalysisService`) to handle:
   - Automatic calculation of all derived fields
   - Validation of data consistency
   - Integration with RentCast API to pre-fill data

4. **Add Business Logic**: Implement business rules such as:
   - Validation that down payment doesn't exceed purchase price
   - Automatic recalculation of metrics when any input changes
   - Comparison of multiple analyses

5. **Create Reports**: Generate PDF or Excel reports showing the analysis results.

## Best Practices Implemented

✅ Proper Jmix annotations (@JmixEntity, @InstanceName, etc.)
✅ Validation annotations (@NotNull, @DecimalMin, @DecimalMax)
✅ Proper cascade types (CascadeType.ALL with orphanRemoval)
✅ @PrePersist and @PreUpdate for timestamps
✅ BigDecimal for all monetary values
✅ Double for percentages
✅ Helper methods for calculated fields
✅ Proper indexes for performance
✅ Version fields for optimistic locking
✅ Comprehensive JavaDoc comments
✅ Transient methods for derived calculations