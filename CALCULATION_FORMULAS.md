# Rental Property Calculation Formulas - Quick Reference

## BASIC Calculations (Implemented ✅)

### 1. Gross Yield
```
Gross Yield = (Annual Rental Income / Property Purchase Price) × 100

Where:
  Annual Rental Income = Monthly Rent × 12
  
Example:
  Monthly Rent: $2,000
  Purchase Price: $300,000
  Annual Rent: $2,000 × 12 = $24,000
  Gross Yield: ($24,000 / $300,000) × 100 = 8.0%
```

### 2. Net Yield
```
Net Yield = ((Annual Rental Income - Annual Operating Expenses) / Property Purchase Price) × 100

Where:
  Annual Rental Income = Monthly Rent × 12
  Annual Operating Expenses = Monthly Operating Expenses × 12
  
Example:
  Monthly Rent: $2,000
  Monthly Expenses: $700
  Purchase Price: $300,000
  Annual Rent: $24,000
  Annual Expenses: $8,400
  Net Income: $24,000 - $8,400 = $15,600
  Net Yield: ($15,600 / $300,000) × 100 = 5.2%
```

### 3. Monthly Mortgage Payment
```
M = P × [r(1 + r)^n] / [(1 + r)^n - 1]

Where:
  M = Monthly payment
  P = Principal (loan amount)
  r = Monthly interest rate (annual rate / 12 / 100)
  n = Number of payments (years × 12)
  
Special Cases:
  - If interest rate = 0: M = P / n
  - If principal = 0: M = 0
  
Example:
  Principal: $240,000
  Annual Rate: 6.5%
  Term: 30 years
  
  r = 6.5 / 12 / 100 = 0.00541667
  n = 30 × 12 = 360
  (1 + r)^n = 1.00541667^360 = 7.2446
  
  M = $240,000 × [0.00541667 × 7.2446] / [7.2446 - 1]
  M = $240,000 × [0.03924] / [6.2446]
  M = $240,000 × 0.006283
  M = $1,516.96
```

### 4. Monthly Cash Flow
```
Monthly Cash Flow = Effective Monthly Income - Total Monthly Expenses - Monthly Mortgage Payment

Where:
  Effective Monthly Income = (Monthly Rent × (1 - Vacancy Rate)) + Other Monthly Income
  Total Monthly Expenses = Sum of all operating expenses
  Monthly Mortgage Payment = Calculated using formula above
  
Example:
  Monthly Rent: $2,000
  Other Income: $100
  Vacancy Rate: 8%
  Monthly Expenses: $700
  Mortgage Payment: $1,517
  
  Gross Income: $2,000 + $100 = $2,100
  Effective Income: $2,100 × (1 - 0.08) = $1,932
  Cash Flow: $1,932 - $700 - $1,517 = -$285 (negative cash flow)
```

### 5. Annual Cash Flow
```
Annual Cash Flow = Monthly Cash Flow × 12

Example:
  Monthly Cash Flow: -$285
  Annual Cash Flow: -$285 × 12 = -$3,420
```

---

## Helper Calculations (Implemented ✅)

### Total Monthly Expenses
```
Total Monthly Expenses = Property Tax/12 + Insurance/12 + HOA + Management + Maintenance + CapEx + Utilities + Other

Where:
  Property Tax = Annual property tax / 12
  Insurance = Annual insurance / 12
  HOA = Monthly HOA fees
  Management = Monthly rent × Management % (or fixed amount)
  Maintenance = (Property value × Maintenance %) / 12 (or fixed amount)
  CapEx = (Property value × CapEx %) / 12 (or fixed amount)
  Utilities = Monthly utilities (if landlord-paid)
  Other = Other monthly expenses
  
Example:
  Property Tax: $3,600/year → $300/month
  Insurance: $1,200/year → $100/month
  HOA: $50/month
  Management: 10% of $2,000 → $200/month
  Maintenance: 1% of $300,000 → $3,000/year → $250/month
  CapEx: 0.5% of $300,000 → $1,500/year → $125/month
  Utilities: $75/month
  Other: $25/month
  
  Total: $300 + $100 + $50 + $200 + $250 + $125 + $75 + $25 = $1,125/month
```

### Effective Monthly Income
```
Effective Monthly Income = (Monthly Rent × (1 - Vacancy Rate)) + Other Monthly Income

Where:
  Vacancy Rate is expressed as a decimal (e.g., 8% = 0.08)
  
Example:
  Monthly Rent: $2,000
  Other Income: $100
  Vacancy Rate: 8%
  
  Rent after vacancy: $2,000 × (1 - 0.08) = $2,000 × 0.92 = $1,840
  Effective Income: $1,840 + $100 = $1,940
```

### Total Cash Invested
```
Total Cash Invested = Down Payment + Closing Costs + Renovation Costs

Example:
  Down Payment: $60,000
  Closing Costs: $9,000
  Renovation: $5,000
  
  Total: $60,000 + $9,000 + $5,000 = $74,000
```

### Loan Amount
```
Loan Amount = Purchase Price - Down Payment

Example:
  Purchase Price: $300,000
  Down Payment: $60,000
  
  Loan Amount: $300,000 - $60,000 = $240,000
```

---

## ADVANCED Calculations (Not Yet Implemented)

### Cap Rate (Capitalization Rate)
```
Cap Rate = (Net Operating Income / Property Value) × 100

Where:
  Net Operating Income = Annual Effective Income - Annual Operating Expenses
  (Does NOT include mortgage payments)
  
Example:
  Annual Effective Income: $23,280
  Annual Operating Expenses: $8,400
  Property Value: $300,000
  
  NOI: $23,280 - $8,400 = $14,880
  Cap Rate: ($14,880 / $300,000) × 100 = 4.96%
```

### Cash-on-Cash Return
```
Cash-on-Cash Return = (Annual Cash Flow / Total Cash Invested) × 100

Example:
  Annual Cash Flow: $4,800
  Total Cash Invested: $74,000
  
  CoC Return: ($4,800 / $74,000) × 100 = 6.49%
```

### ROI (Return on Investment)
```
ROI = [(Total Profit / Total Investment) × 100] / Years

Where:
  Total Profit = Cumulative Cash Flow + Equity Gained + Appreciation
  
Example (5 years):
  Cumulative Cash Flow: $24,000
  Principal Paid Down: $15,000
  Appreciation (3%/year): $47,739
  Total Cash Invested: $74,000
  
  Total Profit: $24,000 + $15,000 + $47,739 = $86,739
  Total ROI: ($86,739 / $74,000) × 100 = 117.2%
  Annualized ROI: 117.2% / 5 = 23.4%
```

### IRR (Internal Rate of Return)
```
NPV = 0 = Initial Investment + Σ(Cash Flow_t / (1+IRR)^t) + Exit Value / (1+IRR)^n

Where:
  NPV = Net Present Value
  t = Time period (year)
  n = Total number of periods
  
Solved iteratively using Newton-Raphson method
```

---

## Typical Ranges & Benchmarks

### Gross Yield
- **Excellent:** 10%+
- **Good:** 7-10%
- **Average:** 5-7%
- **Poor:** <5%

### Net Yield
- **Excellent:** 7%+
- **Good:** 5-7%
- **Average:** 3-5%
- **Poor:** <3%

### Cap Rate
- **High Risk/High Return:** 8-10%+
- **Moderate:** 6-8%
- **Low Risk/Low Return:** 4-6%
- **Very Low:** <4%

### Cash-on-Cash Return
- **Excellent:** 10%+
- **Good:** 7-10%
- **Average:** 5-7%
- **Poor:** <5%

### Vacancy Rate
- **Conservative:** 10-15%
- **Moderate:** 5-10%
- **Aggressive:** <5%

### Property Management
- **Typical:** 8-12% of rent
- **Self-managed:** 0%

### Maintenance
- **Conservative:** 1-2% of property value/year
- **Moderate:** 0.5-1%
- **Aggressive:** <0.5%

### CapEx (Capital Expenditures)
- **Conservative:** 1% of property value/year
- **Moderate:** 0.5-1%
- **Aggressive:** <0.5%

---

## Calculation Order

When analyzing a property, calculate in this order:

1. **Loan Amount** (if financed)
2. **Monthly Mortgage Payment** (if financed)
3. **Effective Monthly Income** (accounts for vacancy)
4. **Total Monthly Expenses**
5. **Monthly Cash Flow**
6. **Annual Cash Flow**
7. **Gross Yield**
8. **Net Yield**
9. **Total Cash Invested**
10. **Cap Rate** (advanced)
11. **Cash-on-Cash Return** (advanced)
12. **ROI** (advanced)
13. **IRR** (advanced)

---

## Common Pitfalls

### 1. Forgetting Vacancy
❌ Using gross rent instead of effective rent
✅ Always apply vacancy rate to rental income

### 2. Mixing Operating Expenses and Debt Service
❌ Including mortgage in operating expenses
✅ Keep mortgage separate; it's debt service, not an operating expense

### 3. Ignoring CapEx
❌ Only budgeting for maintenance
✅ Budget separately for capital expenditures (roof, HVAC, etc.)

### 4. Using Purchase Price vs Current Value
❌ Using current market value for yield calculations
✅ Use actual purchase price for accurate ROI

### 5. Forgetting Closing Costs
❌ Only counting down payment as cash invested
✅ Include closing costs and renovation in total cash invested

---

## Precision & Rounding

### Currency Values
- **Scale:** 2 decimal places
- **Example:** $1,234.56
- **Rounding:** HALF_UP

### Percentages
- **Scale:** 4 decimal places
- **Example:** 8.5000%
- **Rounding:** HALF_UP

### Interest Rates
- **Input:** As percentage (e.g., 6.5 for 6.5%)
- **Calculation:** Convert to decimal (6.5 / 100 = 0.065)
- **Monthly:** Divide by 12 (0.065 / 12 = 0.00541667)

---

## Formula Verification

To verify calculations, use these online calculators:

1. **Mortgage Payment:** https://www.calculator.net/mortgage-calculator.html
2. **Cap Rate:** https://www.calculator.net/cap-rate-calculator.html
3. **Cash Flow:** https://www.calculator.net/rental-property-calculator.html
4. **IRR:** https://www.calculator.net/irr-calculator.html

---

## References

- **Real Estate Investment Analysis** by William B. Brueggeman
- **The Book on Rental Property Investing** by Brandon Turner
- **BiggerPockets Rental Property Calculator**
- **Standard Mortgage Amortization Formula** (Fannie Mae)