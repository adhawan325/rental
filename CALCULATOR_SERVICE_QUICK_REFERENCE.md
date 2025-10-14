# Rental Yield Calculator Service - Quick Reference

## Method Categories

### 🟢 Basic Methods (Free Tier)
| Method | Returns | Purpose |
|--------|---------|---------|
| `calculateGrossYield()` | Double | Annual rent / property value × 100 |
| `calculateNetYield()` | Double | Net income / property value × 100 |
| `calculateMonthlyCashFlow()` | BigDecimal | Income - expenses - mortgage |
| `calculateAnnualCashFlow()` | BigDecimal | Monthly cash flow × 12 |
| `calculateMonthlyMortgagePayment()` | BigDecimal | Standard amortization formula |

### 🔵 Advanced Methods (Plus/Pro Tier)
| Method | Returns | Purpose |
|--------|---------|---------|
| `calculateCapRate()` | Double | NOI / property value × 100 |
| `calculateCashOnCashReturn()` | Double | Annual cash flow / cash invested × 100 |
| `calculateROI()` | Double | Total return over holding period |
| `calculateIRR()` | Double | Internal rate of return with exit value |

### 🟡 Helper Methods
| Method | Returns | Purpose |
|--------|---------|---------|
| `calculateNetOperatingIncome()` | BigDecimal | Income - operating expenses |
| `calculateTotalMonthlyExpenses()` | BigDecimal | Sum all operating expenses |
| `calculateEffectiveMonthlyIncome()` | BigDecimal | Income adjusted for vacancy |
| `calculateTotalCashInvested()` | BigDecimal | Down payment + closing + renovation |
| `calculateLoanAmount()` | BigDecimal | Purchase price - down payment |

### 🟣 Projection Methods
| Method | Returns | Purpose |
|--------|---------|---------|
| `projectCashFlow()` | List\<YearlyProjection\> | Multi-year cash flow with growth |
| `projectEquityBuildup()` | List\<EquityProjection\> | Multi-year equity with appreciation |

### 🟠 Validation Methods
| Method | Returns | Purpose |
|--------|---------|---------|
| `validateAnalysisInputs()` | ValidationResult | Check inputs for errors/warnings |
| `calculateAllMetrics()` | CalculatedMetrics | All metrics in one call |

---

## Key Formulas

### Gross Yield
```
Gross Yield = (Annual Rental Income / Property Value) × 100
```

### Net Yield
```
Net Yield = (Annual Net Income / Property Value) × 100
Annual Net Income = Annual Rent - Annual Operating Expenses
```

### Cash Flow
```
Monthly Cash Flow = Effective Income - Operating Expenses - Mortgage Payment
Effective Income = Gross Income × (1 - Vacancy Rate)
```

### Cap Rate
```
Cap Rate = (Net Operating Income / Property Value) × 100
NOI = Annual Income - Annual Operating Expenses (excludes mortgage)
```

### Cash-on-Cash Return
```
CoC Return = (Annual Cash Flow / Total Cash Invested) × 100
Total Cash = Down Payment + Closing Costs + Renovation Costs
```

### Monthly Mortgage Payment
```
M = P × [r(1+r)^n] / [(1+r)^n - 1]
Where:
  M = Monthly payment
  P = Principal (loan amount)
  r = Monthly interest rate (annual / 12 / 100)
  n = Number of payments (years × 12)
```

### ROI
```
ROI = [(Total Profit / Total Investment) × 100] / Years
Total Profit = Cumulative Cash Flow + Equity Gained + Appreciation
```

### IRR
```
NPV = 0 = Initial Investment + Σ(Cash Flow_t / (1+IRR)^t) + Exit Value / (1+IRR)^n
Solved using Newton-Raphson iterative method
```

---

## DTO Classes

### YearlyProjection
**Fields:**
- `year` - Year number (1-based)
- `annualRent` - Projected rent
- `annualExpenses` - Projected expenses
- `annualMortgage` - Mortgage payment
- `cashFlow` - Net cash flow
- `cumulativeCashFlow` - Running total

**Methods:**
- `getNetOperatingIncome()` - Rent - expenses
- `getDebtServiceCoverageRatio()` - NOI / mortgage

### EquityProjection
**Fields:**
- `year` - Year number (1-based)
- `totalEquity` - Total equity
- `principalPaid` - Cumulative principal
- `appreciation` - Cumulative appreciation
- `propertyValue` - Current value
- `remainingLoanBalance` - Loan balance
- `loanToValueRatio` - LTV percentage

**Methods:**
- `getEquityPercentage()` - Equity / value × 100
- `getEquityGain(initial)` - Gain vs initial
- `getReturnOnEquity(initial)` - ROE percentage

### ValidationResult
**Fields:**
- `valid` - Passed validation?
- `errors` - Critical issues
- `warnings` - Potential concerns

**Methods:**
- `hasErrors()` / `hasWarnings()`
- `getErrorCount()` / `getWarningCount()`
- `getSummary()` - Summary message
- `getAllMessages()` - Combined list

**Builder:**
```java
new ValidationResult.Builder()
    .addError("Error message")
    .addWarning("Warning message")
    .build()
```

---

## Typical Values & Ranges

### Yields
- **Gross Yield:** 6-12% (varies by market)
- **Net Yield:** 3-8% (after expenses)

### Returns
- **Cap Rate:** 4-10% (lower in hot markets)
- **Cash-on-Cash:** 5-15% (depends on financing)
- **ROI:** 10-20% (long-term with appreciation)
- **IRR:** 12-18% (with good exit)

### Rates
- **Vacancy Rate:** 5-10% (typical)
- **Property Management:** 8-12% of rent
- **Maintenance:** 1-2% of property value/year
- **CapEx Reserve:** 0.5-1% of property value/year

### Growth Assumptions
- **Rent Growth:** 2-4% annually
- **Expense Inflation:** 2-3% annually
- **Property Appreciation:** 3-4% annually (moderate)

### Debt Metrics
- **DSCR (Debt Service Coverage Ratio):**
  - \> 1.25: Excellent
  - 1.0-1.25: Acceptable
  - < 1.0: Poor (income doesn't cover debt)

---

## Code Examples

### Calculate Basic Metrics
```java
@Autowired
private RentalYieldCalculatorService calculator;

Double grossYield = calculator.calculateGrossYield(analysis);
Double netYield = calculator.calculateNetYield(analysis);
BigDecimal cashFlow = calculator.calculateMonthlyCashFlow(analysis);
```

### Calculate Advanced Metrics
```java
Double capRate = calculator.calculateCapRate(analysis);
Double cocReturn = calculator.calculateCashOnCashReturn(analysis);
Double roi = calculator.calculateROI(analysis, 10); // 10 years
```

### Project Future Performance
```java
// Cash flow projection
List<YearlyProjection> cashFlowProjections = calculator.projectCashFlow(
    analysis, 10, 3.0, 2.5  // 10 years, 3% rent growth, 2.5% expense inflation
);

// Equity projection
List<EquityProjection> equityProjections = calculator.projectEquityBuildup(
    analysis, 10, 3.5  // 10 years, 3.5% appreciation
);
```

### Validate Before Calculating
```java
ValidationResult validation = calculator.validateAnalysisInputs(analysis);

if (!validation.isValid()) {
    validation.getErrors().forEach(System.out::println);
    return;
}

if (validation.hasWarnings()) {
    validation.getWarnings().forEach(System.out::println);
}

// Proceed with calculations
CalculatedMetrics metrics = calculator.calculateAllMetrics(analysis);
```

### Calculate All Metrics at Once
```java
@Autowired
private DataManager dataManager;

CalculatedMetrics metrics = calculator.calculateAllMetrics(analysis);
metrics.setRentalAnalysis(analysis);
dataManager.save(metrics);
```

---

## Common Patterns

### Pattern 1: Validate → Calculate → Save
```java
public CalculatedMetrics analyzeProperty(RentalAnalysis analysis) {
    // 1. Validate
    ValidationResult validation = calculator.validateAnalysisInputs(analysis);
    if (!validation.isValid()) {
        throw new ValidationException(validation.getErrors());
    }
    
    // 2. Calculate
    CalculatedMetrics metrics = calculator.calculateAllMetrics(analysis);
    
    // 3. Save
    metrics.setRentalAnalysis(analysis);
    return dataManager.save(metrics);
}
```

### Pattern 2: Progressive Disclosure
```java
public PropertyAnalysisDTO getAnalysis(UUID analysisId, String tier) {
    RentalAnalysis analysis = dataManager.load(RentalAnalysis.class)
        .id(analysisId).one();
    
    PropertyAnalysisDTO dto = new PropertyAnalysisDTO();
    
    // Free tier - basic metrics
    dto.setGrossYield(calculator.calculateGrossYield(analysis));
    dto.setNetYield(calculator.calculateNetYield(analysis));
    dto.setCashFlow(calculator.calculateMonthlyCashFlow(analysis));
    
    // Plus/Pro tier - advanced metrics
    if ("PLUS".equals(tier) || "PRO".equals(tier)) {
        dto.setCapRate(calculator.calculateCapRate(analysis));
        dto.setCocReturn(calculator.calculateCashOnCashReturn(analysis));
    }
    
    // Pro tier only - projections
    if ("PRO".equals(tier)) {
        dto.setProjections(calculator.projectCashFlow(analysis, 10, 3.0, 2.5));
        dto.setEquityProjections(calculator.projectEquityBuildup(analysis, 10, 3.5));
    }
    
    return dto;
}
```

### Pattern 3: Batch Analysis
```java
public List<PropertyComparison> compareProperties(List<UUID> analysisIds) {
    return analysisIds.stream()
        .map(id -> dataManager.load(RentalAnalysis.class).id(id).one())
        .map(analysis -> {
            PropertyComparison comparison = new PropertyComparison();
            comparison.setAnalysis(analysis);
            comparison.setCapRate(calculator.calculateCapRate(analysis));
            comparison.setCocReturn(calculator.calculateCashOnCashReturn(analysis));
            comparison.setCashFlow(calculator.calculateMonthlyCashFlow(analysis));
            return comparison;
        })
        .sorted(Comparator.comparing(PropertyComparison::getCapRate).reversed())
        .collect(Collectors.toList());
}
```

---

## Error Handling

### Null Returns
Methods return `null` when required data is missing:
```java
Double capRate = calculator.calculateCapRate(analysis);
if (capRate == null) {
    // Missing purchase price, income, or expense data
    log.warn("Cannot calculate cap rate - insufficient data");
}
```

### Validation Errors
Use validation method to check before calculating:
```java
ValidationResult result = calculator.validateAnalysisInputs(analysis);
if (result.hasErrors()) {
    throw new IllegalStateException("Invalid analysis: " + 
        String.join(", ", result.getErrors()));
}
```

### Exception Handling
```java
try {
    CalculatedMetrics metrics = calculator.calculateAllMetrics(analysis);
    return metrics;
} catch (IllegalArgumentException e) {
    log.error("Invalid input: " + e.getMessage());
    throw new BadRequestException("Invalid analysis data");
}
```

---

## Testing Checklist

### Unit Tests
- ✅ Each calculation method with valid inputs
- ✅ Edge cases (zero values, null inputs)
- ✅ Boundary conditions (100% vacancy, 0% interest)
- ✅ Precision (BigDecimal rounding)
- ✅ Validation logic (errors and warnings)

### Integration Tests
- ✅ Full analysis workflow
- ✅ Database persistence
- ✅ Entity relationships
- ✅ Transaction handling

### Performance Tests
- ✅ Projection methods with large year ranges
- ✅ Batch calculations
- ✅ Concurrent access

---

## Files Location

```
src/main/java/com/non/rental/
├── service/
│   ├── RentalYieldCalculatorService.java      (Interface)
│   └── dto/
│       ├── YearlyProjection.java              (DTO)
│       ├── EquityProjection.java              (DTO)
│       └── ValidationResult.java              (DTO)
└── entity/
    ├── RentalAnalysis.java                    (Entity)
    ├── PurchaseDetails.java                   (Entity)
    ├── FinancingDetails.java                  (Entity)
    ├── IncomeProjection.java                  (Entity)
    ├── ExpenseProjection.java                 (Entity)
    └── CalculatedMetrics.java                 (Entity)
```

---

## Next Implementation Steps

1. **Create Service Implementation**
   - `RentalYieldCalculatorServiceImpl.java`
   - Implement all 18 methods
   - Add `@Service` annotation

2. **Create Unit Tests**
   - `RentalYieldCalculatorServiceTest.java`
   - Test all calculation methods
   - Test validation logic

3. **Integrate with Controllers**
   - Add endpoints for calculations
   - Wire service into existing controllers

4. **Add to Jmix Views**
   - Display calculated metrics in UI
   - Add projection charts/tables

5. **Implement Tier Restrictions**
   - Add subscription tier checks
   - Restrict advanced methods to paid tiers

---

## Support & Documentation

- **Full Documentation:** `RENTAL_YIELD_CALCULATOR_SERVICE.md`
- **Entity Documentation:** See individual entity JavaDoc
- **Service Interface:** `RentalYieldCalculatorService.java` (comprehensive JavaDoc)

---

**Status:** ✅ Interface and DTOs complete and compiled successfully
**Next:** Implement service and create tests