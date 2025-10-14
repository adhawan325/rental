# Rental Yield Calculator Service - Complete Documentation

## Overview
This document describes the comprehensive `RentalYieldCalculatorService` interface created for the rental property analysis system. The service provides 18 calculation methods organized by complexity and subscription tier, plus 3 supporting DTO classes.

---

## Files Created

### 1. Service Interface
**Location:** `/src/main/java/com/non/rental/service/RentalYieldCalculatorService.java`

Comprehensive service interface with 18 methods for rental property financial analysis.

### 2. Supporting DTO Classes
**Location:** `/src/main/java/com/non/rental/service/dto/`

- **YearlyProjection.java** - Multi-year cash flow projection data
- **EquityProjection.java** - Multi-year equity buildup data  
- **ValidationResult.java** - Validation results with errors and warnings

---

## Service Methods Overview

### Basic Calculation Methods (Free Tier) - 5 Methods

#### 1. `calculateGrossYield(RentalAnalysis analysis) : Double`
**Formula:** `Gross Yield = (Annual Rental Income / Property Value) × 100`

**Purpose:** Quick measure of rental return without considering expenses

**Example:** $300,000 property with $24,000/year rent = 8% gross yield

**Returns:** Percentage (e.g., 8.5 for 8.5%)

---

#### 2. `calculateNetYield(RentalAnalysis analysis) : Double`
**Formula:** `Net Yield = (Annual Net Income / Property Value) × 100`

**Purpose:** Rental return after operating expenses (excludes mortgage)

**Example:** $300,000 property, $24,000 rent, $8,000 expenses = 5.33% net yield

**Returns:** Percentage (e.g., 5.33 for 5.33%)

---

#### 3. `calculateMonthlyCashFlow(RentalAnalysis analysis) : BigDecimal`
**Formula:** `Monthly Cash Flow = Effective Income - Expenses - Mortgage Payment`

**Purpose:** Actual monthly profit/loss after all expenses

**Components:**
- Effective Income: Rent adjusted for vacancy
- Expenses: Operating costs (taxes, insurance, maintenance, etc.)
- Mortgage: Principal + interest payment

**Returns:** Monthly amount (positive = profit, negative = loss)

---

#### 4. `calculateAnnualCashFlow(RentalAnalysis analysis) : BigDecimal`
**Formula:** `Annual Cash Flow = Monthly Cash Flow × 12`

**Purpose:** Yearly view of property profitability

**Returns:** Annual amount (positive = profit, negative = loss)

---

#### 5. `calculateMonthlyMortgagePayment(BigDecimal principal, Double annualRate, Integer years) : BigDecimal`
**Formula:** `M = P × [r(1+r)^n] / [(1+r)^n - 1]`

**Where:**
- M = Monthly payment
- P = Principal (loan amount)
- r = Monthly interest rate (annual / 12 / 100)
- n = Number of payments (years × 12)

**Purpose:** Calculate mortgage payment using standard amortization

**Example:** $240,000 at 6.5% for 30 years = $1,517.05/month

**Special Cases:**
- 0% interest: Returns principal / total months
- $0 principal: Returns $0

**Returns:** Monthly payment amount

---

### Advanced Calculation Methods (Plus/Pro Tier) - 4 Methods

#### 6. `calculateCapRate(RentalAnalysis analysis) : Double`
**Formula:** `Cap Rate = (Net Operating Income / Property Value) × 100`

**Purpose:** Rate of return independent of financing - key metric for comparing properties

**Key Points:**
- Higher cap rate = Higher potential return (possibly higher risk)
- Lower cap rate = Lower return (possibly more stable area)
- Typical range: 4-10%
- Does NOT include mortgage payments

**Example:** $300,000 property with $18,000 NOI = 6% cap rate

**Returns:** Percentage (e.g., 6.0 for 6%)

---

#### 7. `calculateCashOnCashReturn(RentalAnalysis analysis) : Double`
**Formula:** `CoC Return = (Annual Cash Flow / Total Cash Invested) × 100`

**Purpose:** Annual return on actual cash invested (accounts for financing)

**Total Cash Invested includes:**
- Down payment
- Closing costs
- Renovation costs

**Example:** $60,000 invested, $4,800/year cash flow = 8% CoC return

**Returns:** Percentage (e.g., 8.0 for 8%)

---

#### 8. `calculateROI(RentalAnalysis analysis, Integer years) : Double`
**Formula:** `ROI = [(Total Profit / Total Investment) × 100] / Years`

**Where:** `Total Profit = Cumulative Cash Flow + Equity Gained + Appreciation`

**Purpose:** Comprehensive investment performance over holding period

**Includes:**
- Cash flow over holding period
- Principal paydown (equity buildup)
- Property appreciation (assumed 3% annually)

**Returns:** Annualized ROI percentage (e.g., 12.5 for 12.5%)

---

#### 9. `calculateIRR(RentalAnalysis analysis, Integer years, BigDecimal exitValue) : Double`
**Formula:** `NPV = 0 = Initial Investment + Σ(Cash Flow_t / (1+IRR)^t) + Exit Value / (1+IRR)^n`

**Purpose:** Most sophisticated metric - discount rate making NPV = 0

**Accounts for:**
- Time value of money
- All cash flows over investment period
- Exit value (sale proceeds)

**Calculation Method:** Newton-Raphson iterative method

**Example:** $60,000 invested, $4,800/year for 10 years, $350,000 exit = ~14.2% IRR

**Returns:** IRR percentage (e.g., 14.2 for 14.2%)

---

### Helper Calculation Methods - 5 Methods

#### 10. `calculateNetOperatingIncome(RentalAnalysis analysis) : BigDecimal`
**Formula:** `NOI = Effective Annual Income - Annual Operating Expenses`

**Purpose:** Income before debt service - fundamental real estate metric

**Includes:**
- Rental income (adjusted for vacancy)
- Other income (parking, laundry, etc.)

**Excludes:**
- Mortgage payments
- Capital expenditures
- Depreciation
- Income taxes

**Returns:** Annual NOI amount

---

#### 11. `calculateTotalMonthlyExpenses(ExpenseProjection expenses, BigDecimal monthlyRent, BigDecimal propertyValue) : BigDecimal`
**Purpose:** Sum all monthly operating expenses

**Components:**
- Property taxes (annual / 12)
- Insurance (annual / 12)
- HOA fees
- Property management (% of rent or fixed)
- Maintenance (% of value or fixed)
- CapEx reserve (% of value or fixed)
- Utilities (if landlord-paid)
- Other expenses

**Note:** Does NOT include mortgage payments

**Returns:** Total monthly operating expenses

---

#### 12. `calculateEffectiveMonthlyIncome(IncomeProjection income) : BigDecimal`
**Formula:** `Effective Income = Gross Income × (1 - Vacancy Rate)`

**Where:** `Gross Income = Monthly Rent + Other Monthly Income`

**Purpose:** Realistic income accounting for vacancy

**Typical Vacancy Rates:** 5-10% depending on market

**Example:** $2,000 rent + $100 other with 8% vacancy = $1,932 effective

**Returns:** Effective monthly income

---

#### 13. `calculateTotalCashInvested(PurchaseDetails purchase) : BigDecimal`
**Formula:** `Total Cash = Down Payment + Closing Costs + Renovation Costs`

**Purpose:** Total out-of-pocket cash for acquisition

**Used in:** Cash-on-cash return calculations

**Returns:** Total cash invested

---

#### 14. `calculateLoanAmount(PurchaseDetails purchase) : BigDecimal`
**Formula:** `Loan Amount = Purchase Price - Down Payment`

**Purpose:** Amount to be financed through mortgage

**Example:** $300,000 purchase - $60,000 down = $240,000 loan

**Returns:** Loan amount

---

### Projection Methods - 2 Methods

#### 15. `projectCashFlow(RentalAnalysis analysis, Integer years, Double rentGrowthRate, Double expenseInflationRate) : List<YearlyProjection>`

**Purpose:** Year-by-year cash flow projections with growth assumptions

**Accounts for:**
- Rental income growth (market rent increases)
- Expense inflation (rising costs)
- Fixed mortgage payments

**Typical Growth Rates:**
- Rent Growth: 2-4% annually
- Expense Inflation: 2-3% annually

**Use Cases:**
- Long-term investment planning
- Comparing holding periods
- Stress testing scenarios
- Retirement income planning

**Returns:** List of YearlyProjection objects (one per year)

---

#### 16. `projectEquityBuildup(RentalAnalysis analysis, Integer years, Double appreciationRate) : List<EquityProjection>`

**Purpose:** Track equity growth through principal paydown and appreciation

**Formula:** `Total Equity = Initial Equity + Principal Paid + Appreciation`

**Typical Appreciation Rates:**
- Conservative: 2-3% annually
- Moderate: 3-4% annually
- Aggressive: 5%+ annually

**Use Cases:**
- Wealth building analysis
- Refinancing planning
- Exit strategy evaluation
- Net worth projections

**Returns:** List of EquityProjection objects (one per year)

---

### Validation Methods - 2 Methods

#### 17. `validateAnalysisInputs(RentalAnalysis analysis) : ValidationResult`

**Purpose:** Comprehensive validation of all inputs

**Checks:**

**Required Fields:**
- Purchase price present and positive
- Monthly rent present and positive
- Financing details complete (if financed)
- Percentages within valid ranges (0-100)

**Business Logic:**
- Down payment ≤ purchase price
- Loan amount = purchase price - down payment
- Expense percentages reasonable
- Vacancy rate realistic (typically 5-15%)

**Warnings:**
- Negative cash flow
- Very low cap rate (< 4%)
- Very high vacancy rate (> 15%)
- Unusually high expense ratios

**Returns:** ValidationResult with errors and warnings

---

#### 18. `calculateAllMetrics(RentalAnalysis analysis) : CalculatedMetrics`

**Purpose:** Calculate all metrics in one efficient operation

**Calculated Metrics:**
- Monthly and annual cash flow
- Gross yield
- Net yield
- Cap rate
- Cash-on-cash return
- Total ROI (5-year default)
- Debt service coverage ratio
- Break-even ratio

**Note:** Returned entity is not persisted - call dataManager.save() to persist

**Returns:** CalculatedMetrics entity with all computed values

---

## Supporting DTO Classes

### YearlyProjection

**Purpose:** Represents projected financial metrics for a single year

**Fields:**
- `year` - Projection year (1-based)
- `annualRent` - Projected annual rental income
- `annualExpenses` - Projected annual operating expenses
- `annualMortgage` - Annual mortgage payment
- `cashFlow` - Net cash flow for the year
- `cumulativeCashFlow` - Cumulative cash flow through this year

**Additional Methods:**
- `getNetOperatingIncome()` - NOI for the year
- `getAnnualDebtService()` - Annual mortgage payment
- `getDebtServiceCoverageRatio()` - DSCR for the year

**Immutable:** Yes (thread-safe)

---

### EquityProjection

**Purpose:** Represents projected equity buildup for a single year

**Fields:**
- `year` - Projection year (1-based)
- `totalEquity` - Total equity at year end
- `principalPaid` - Cumulative principal paid
- `appreciation` - Cumulative appreciation
- `propertyValue` - Property value at year end
- `remainingLoanBalance` - Remaining loan balance
- `loanToValueRatio` - LTV ratio at year end

**Additional Methods:**
- `getEquityPercentage()` - Equity as % of property value
- `getEquityGain(initialEquity)` - Equity gain vs initial
- `getReturnOnEquity(initialEquity)` - ROE percentage

**Immutable:** Yes (thread-safe)

---

### ValidationResult

**Purpose:** Represents validation results with errors and warnings

**Fields:**
- `valid` - Whether validation passed (no errors)
- `errors` - List of error messages (critical issues)
- `warnings` - List of warning messages (potential concerns)

**Static Factory Methods:**
- `valid()` - Create valid result with no issues
- `validWithWarnings(warnings)` - Valid with warnings
- `invalid(errors)` - Invalid with errors
- `invalid(errors, warnings)` - Invalid with both

**Utility Methods:**
- `hasErrors()` - Check if errors exist
- `hasWarnings()` - Check if warnings exist
- `getErrorCount()` - Number of errors
- `getWarningCount()` - Number of warnings
- `getSummary()` - Summary message
- `getAllMessages()` - Combined errors and warnings

**Builder Pattern:**
```java
ValidationResult result = new ValidationResult.Builder()
    .addError("Purchase price is required")
    .addWarning("Vacancy rate is unusually high")
    .build();
```

**Immutable:** Yes (thread-safe)

---

## Design Patterns & Best Practices

### 1. Jmix Service Interface Conventions
✅ Interface-based design (implementation will be separate)
✅ Comprehensive JavaDoc with formulas and examples
✅ Clear method naming following Java conventions
✅ Proper use of entity types from the domain model

### 2. Financial Calculation Standards
✅ Standard real estate formulas (industry-accepted)
✅ BigDecimal for monetary values (precision)
✅ Double for percentages and ratios
✅ Proper rounding (ROUND_HALF_UP)

### 3. Error Handling
✅ Null safety (returns null when data insufficient)
✅ IllegalArgumentException for invalid inputs
✅ Validation method for comprehensive checks

### 4. Documentation Quality
✅ Every method has comprehensive JavaDoc
✅ Formulas clearly stated
✅ Real-world examples provided
✅ Assumptions explicitly documented
✅ Use cases explained

### 5. Immutability & Thread Safety
✅ DTO classes are immutable
✅ Defensive copying in constructors
✅ Unmodifiable collections returned
✅ Thread-safe for concurrent use

---

## Usage Examples

### Example 1: Calculate Basic Metrics
```java
@Autowired
private RentalYieldCalculatorService calculatorService;

public void analyzeProperty(RentalAnalysis analysis) {
    // Basic metrics
    Double grossYield = calculatorService.calculateGrossYield(analysis);
    Double netYield = calculatorService.calculateNetYield(analysis);
    BigDecimal monthlyCashFlow = calculatorService.calculateMonthlyCashFlow(analysis);
    
    System.out.println("Gross Yield: " + grossYield + "%");
    System.out.println("Net Yield: " + netYield + "%");
    System.out.println("Monthly Cash Flow: $" + monthlyCashFlow);
}
```

### Example 2: Calculate Advanced Metrics
```java
public void advancedAnalysis(RentalAnalysis analysis) {
    // Advanced metrics
    Double capRate = calculatorService.calculateCapRate(analysis);
    Double cocReturn = calculatorService.calculateCashOnCashReturn(analysis);
    Double roi = calculatorService.calculateROI(analysis, 10);
    
    System.out.println("Cap Rate: " + capRate + "%");
    System.out.println("Cash-on-Cash Return: " + cocReturn + "%");
    System.out.println("10-Year ROI: " + roi + "%");
}
```

### Example 3: Project Cash Flow
```java
public void projectFuture(RentalAnalysis analysis) {
    List<YearlyProjection> projections = calculatorService.projectCashFlow(
        analysis, 
        10,    // 10 years
        3.0,   // 3% rent growth
        2.5    // 2.5% expense inflation
    );
    
    for (YearlyProjection projection : projections) {
        System.out.printf("Year %d: Cash Flow = $%s, Cumulative = $%s%n",
            projection.getYear(),
            projection.getCashFlow(),
            projection.getCumulativeCashFlow()
        );
    }
}
```

### Example 4: Validate Inputs
```java
public void validateAndCalculate(RentalAnalysis analysis) {
    ValidationResult validation = calculatorService.validateAnalysisInputs(analysis);
    
    if (!validation.isValid()) {
        System.out.println("Validation failed:");
        validation.getErrors().forEach(error -> 
            System.out.println("  ERROR: " + error)
        );
        return;
    }
    
    if (validation.hasWarnings()) {
        System.out.println("Warnings:");
        validation.getWarnings().forEach(warning -> 
            System.out.println("  WARNING: " + warning)
        );
    }
    
    // Proceed with calculations
    CalculatedMetrics metrics = calculatorService.calculateAllMetrics(analysis);
}
```

### Example 5: Calculate All Metrics at Once
```java
@Autowired
private DataManager dataManager;

public void calculateAndSave(RentalAnalysis analysis) {
    // Calculate all metrics efficiently
    CalculatedMetrics metrics = calculatorService.calculateAllMetrics(analysis);
    
    // Link to analysis
    metrics.setRentalAnalysis(analysis);
    
    // Persist
    dataManager.save(metrics);
    
    System.out.println("Metrics calculated and saved:");
    System.out.println("  Cap Rate: " + metrics.getCapRate() + "%");
    System.out.println("  CoC Return: " + metrics.getCashOnCashReturn() + "%");
    System.out.println("  Monthly Cash Flow: $" + metrics.getMonthlyCashFlow());
}
```

---

## Next Steps

### 1. Implementation
Create `RentalYieldCalculatorServiceImpl` class implementing the interface:
- Implement all 18 calculation methods
- Add proper error handling and validation
- Use Spring's `@Service` annotation
- Inject DataManager if needed for entity operations

### 2. Unit Tests
Create comprehensive test suite:
- Test each calculation method with various scenarios
- Test edge cases (zero values, null inputs, etc.)
- Test projection methods with different growth rates
- Test validation logic thoroughly

### 3. Integration
Integrate with existing components:
- Wire service into controllers
- Use in Jmix views for property analysis
- Add to REST API endpoints
- Integrate with RentCast data

### 4. Subscription Tier Enforcement
Add tier-based access control:
- Free tier: Methods 1-5 only
- Plus tier: Methods 1-14
- Pro tier: All methods

### 5. Performance Optimization
- Cache frequently calculated metrics
- Batch calculations when possible
- Optimize projection algorithms for large year ranges

---

## Compilation Status

✅ **BUILD SUCCESSFUL**

All files compile without errors:
- RentalYieldCalculatorService.java
- YearlyProjection.java
- EquityProjection.java
- ValidationResult.java

Project entities recognized:
- RentalAnalysis
- PurchaseDetails
- FinancingDetails
- IncomeProjection
- ExpenseProjection
- CalculatedMetrics

---

## Summary

This comprehensive service interface provides:
- **18 calculation methods** covering basic to advanced metrics
- **3 supporting DTO classes** for projections and validation
- **Extensive documentation** with formulas, examples, and use cases
- **Industry-standard formulas** for real estate investment analysis
- **Jmix best practices** for service design and entity integration
- **Thread-safe, immutable DTOs** for concurrent use
- **Flexible validation** with errors and warnings
- **Multi-year projections** for long-term planning

The service is ready for implementation and provides a solid foundation for rental property financial analysis in the Jmix application.