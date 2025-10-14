package com.non.rental.view.rentalanalysis;

import com.non.rental.dto.PropertySearchResult;
import com.non.rental.entity.*;
import com.non.rental.service.RentCastService;
import com.non.rental.view.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Detail view for creating and editing rental property analyses.
 * Provides tabbed interface for entering purchase, financing, income, expense, and viewing results.
 */
@CssImport("./styles/rental-analysis-detail-view.css")
@Route(value = "rental-analysis/:id", layout = MainView.class)
@ViewController("RentalAnalysisDetailView")
@ViewDescriptor("rental-analysis-detail-view.xml")
@EditedEntityContainer("rentalAnalysisDc")
public class RentalAnalysisDetailView extends StandardDetailView<RentalAnalysis> {

    private static final Logger log = LoggerFactory.getLogger(RentalAnalysisDetailView.class);
    
    private static final double DEFAULT_DOWN_PAYMENT_PERCENT = 25.0;
    private static final double DEFAULT_CLOSING_COSTS_PERCENT = 2.0;
    private static final double MIN_DOWN_PAYMENT_WARNING = 20.0;
    private static final double MAX_DOWN_PAYMENT_WARNING = 50.0;
    private static final double VALUE_DIFFERENCE_WARNING_PERCENT = 15.0;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Notifications notifications;

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private RentCastService rentCastService;

    @ViewComponent
    private InstanceContainer<RentalAnalysis> rentalAnalysisDc;

    // Tabs
    @ViewComponent
    private Tabs mainTabs;

    @ViewComponent
    private Tab purchaseTab;

    @ViewComponent
    private Tab financingTab;

    @ViewComponent
    private Tab incomeTab;

    @ViewComponent
    private Tab expensesTab;

    @ViewComponent
    private Tab resultsTab;

    // Tab content containers
    @ViewComponent
    private VerticalLayout purchaseContent;

    @ViewComponent
    private VerticalLayout financingContent;

    @ViewComponent
    private VerticalLayout incomeContent;

    @ViewComponent
    private VerticalLayout expensesContent;

    @ViewComponent
    private VerticalLayout resultsContent;

    // Purchase Details Fields
    @ViewComponent
    private Div propertyHeaderDiv;

    @ViewComponent
    private TextField purchasePriceField;

    @ViewComponent
    private TextField downPaymentAmountField;

    @ViewComponent
    private TextField downPaymentPercentField;

    @ViewComponent
    private TextField closingCostsField;

    @ViewComponent
    private TextField closingCostsPercentField;

    @ViewComponent
    private TextField renovationCostsField;

    @ViewComponent
    private Div purchaseSummaryBox;

    @ViewComponent
    private Span totalCashInvestedSpan;

    @ViewComponent
    private Span loanAmountSpan;

    @ViewComponent
    private Div purchaseWarningsDiv;

    // Financing Details Fields
    @ViewComponent
    private TextField loanAmountField;

    @ViewComponent
    private TextField interestRateField;

    @ViewComponent
    private Select<Integer> loanTermSelect;

    @ViewComponent
    private TextField pointsPaidField;

    @ViewComponent
    private Div monthlyPaymentDisplay;

    @ViewComponent
    private Span principalInterestSpan;

    @ViewComponent
    private Span totalMonthlyPaymentSpan;

    @ViewComponent
    private VerticalLayout amortizationPreview;

    // Income Projection Fields
    @ViewComponent
    private TextField monthlyRentField;

    @ViewComponent
    private Div rentComparisonDiv;

    @ViewComponent
    private TextField otherMonthlyIncomeField;

    @ViewComponent
    private Button expandOtherIncomeButton;

    @ViewComponent
    private VerticalLayout otherIncomeBreakdownDiv;

    @ViewComponent
    private TextField vacancyRateField;

    @ViewComponent
    private Div vacancySliderDiv;

    @ViewComponent
    private Div vacancyWarningsDiv;

    @ViewComponent
    private Span grossMonthlyIncomeSpan;

    @ViewComponent
    private Span monthlyVacancyLossSpan;

    @ViewComponent
    private Span effectiveMonthlyIncomeSpan;

    @ViewComponent
    private Span annualEffectiveIncomeSpan;

    // Expense Projection Fields
    @ViewComponent
    private TextField annualPropertyTaxField;

    @ViewComponent
    private Span monthlyPropertyTaxSpan;

    @ViewComponent
    private TextField annualInsuranceField;

    @ViewComponent
    private Button estimateInsuranceButton;

    @ViewComponent
    private Span monthlyInsuranceSpan;

    @ViewComponent
    private TextField monthlyHOAFeesField;

    @ViewComponent
    private Checkbox usePropertyManagerCheckbox;

    @ViewComponent
    private VerticalLayout propertyManagementFieldsDiv;

    @ViewComponent
    private TextField propertyManagementPercentField;

    @ViewComponent
    private Span propertyManagementAmountSpan;

    @ViewComponent
    private TextField maintenancePercentField;

    @ViewComponent
    private Span maintenanceAmountSpan;

    @ViewComponent
    private TextField capExPercentField;

    @ViewComponent
    private Span capExAmountSpan;

    @ViewComponent
    private Checkbox landlordPaysUtilitiesCheckbox;

    @ViewComponent
    private VerticalLayout utilitiesBreakdownDiv;

    @ViewComponent
    private TextField otherMonthlyExpensesField;

    @ViewComponent
    private Span totalMonthlyExpensesSpan;

    @ViewComponent
    private Span totalAnnualExpensesSpan;

    @ViewComponent
    private Span expenseRatioSpan;

    @ViewComponent
    private Div expenseWarningsDiv;

    @ViewComponent
    private Div expenseChartDiv;

    // Results Tab Fields
    @ViewComponent
    private Span noiSpan;

    @ViewComponent
    private Span monthlyCashFlowSpan;

    @ViewComponent
    private Span annualCashFlowSpan;

    @ViewComponent
    private Span cashOnCashReturnSpan;

    @ViewComponent
    private Span capRateSpan;

    @ViewComponent
    private Span grossYieldSpan;

    @ViewComponent
    private Span netYieldSpan;

    @ViewComponent
    private Span dscrSpan;

    @ViewComponent
    private Span breakEvenRatioSpan;

    @ViewComponent
    private Div investmentSummaryDiv;

    @ViewComponent
    private Div resultsWarningsDiv;

    @ViewComponent
    private Button exportResultsButton;

    @ViewComponent
    private Button shareResultsButton;

    @ViewComponent
    private Div premiumFeaturesDiv;

    @ViewComponent
    private Button upgradeButton;

    @ViewComponent
    private MessageBundle messageBundle;

    private PropertySearchResult propertySearchResult;
    private ScheduledExecutorService autoSaveExecutor;
    private boolean isUpdatingFields = false;
    private boolean otherIncomeExpanded = false;
    private boolean utilitiesExpanded = false;

    @Subscribe
    public void onInit(InitEvent event) {
        log.debug("Initializing RentalAnalysisDetailView");
        
        // Initialize loan term options
        loanTermSelect.setItems(15, 20, 25, 30);
        loanTermSelect.setValue(30);
        
        // Setup auto-save (every 30 seconds)
        setupAutoSave();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        log.debug("RentalAnalysisDetailView ready");
        
        // Set flag to prevent field listeners from triggering during initialization
        isUpdatingFields = true;
        
        try {
            RentalAnalysis analysis = getEditedEntity();
            
            // Initialize property header
            updatePropertyHeader(analysis);
            
            // Initialize purchase details if not exists
            if (analysis.getPurchaseDetails() == null) {
                PurchaseDetails purchaseDetails = dataManager.create(PurchaseDetails.class);
                purchaseDetails.setRentalAnalysis(analysis);
                // Set default percentages
                purchaseDetails.setDownPaymentPercent(DEFAULT_DOWN_PAYMENT_PERCENT);
                purchaseDetails.setClosingCostsPercent(DEFAULT_CLOSING_COSTS_PERCENT);
                analysis.setPurchaseDetails(purchaseDetails);
            }
            
            // Initialize financing details if not exists
            if (analysis.getFinancingDetails() == null) {
                FinancingDetails financingDetails = dataManager.create(FinancingDetails.class);
                financingDetails.setRentalAnalysis(analysis);
                financingDetails.setInterestRate(6.5); // Default rate
                financingDetails.setLoanTermYears(30); // Default term
                analysis.setFinancingDetails(financingDetails);
            }
            
            // Initialize income projection if not exists
            if (analysis.getIncomeProjection() == null) {
                IncomeProjection incomeProjection = dataManager.create(IncomeProjection.class);
                incomeProjection.setRentalAnalysis(analysis);
                incomeProjection.setVacancyRate(8.0); // Default 8%
                // Set monthly rent from RentCast estimate if available
                if (propertySearchResult != null && propertySearchResult.hasRentEstimate()) {
                    incomeProjection.setMonthlyRent(propertySearchResult.getRentEstimate().getRent());
                } else {
                    incomeProjection.setMonthlyRent(BigDecimal.ZERO);
                }
                analysis.setIncomeProjection(incomeProjection);
            }
            
            // Initialize expense projection if not exists
            if (analysis.getExpenseProjection() == null) {
                ExpenseProjection expenseProjection = dataManager.create(ExpenseProjection.class);
                expenseProjection.setRentalAnalysis(analysis);
                // Set defaults
                expenseProjection.setMaintenancePercent(1.0); // 1% of property value
                expenseProjection.setCapExPercent(0.5); // 0.5% of property value
                analysis.setExpenseProjection(expenseProjection);
            }
            
            // Initialize calculated metrics if not exists
            if (analysis.getCalculatedMetrics() == null) {
                CalculatedMetrics calculatedMetrics = dataManager.create(CalculatedMetrics.class);
                calculatedMetrics.setRentalAnalysis(analysis);
                analysis.setCalculatedMetrics(calculatedMetrics);
            }
            
            // Setup field listeners
            setupPurchaseFieldListeners();
            setupFinancingFieldListeners();
            setupIncomeFieldListeners();
            setupExpenseFieldListeners();
            setupResultsButtonListeners();
            
            // Initial calculations (these should not modify the entity, only update UI)
            updatePurchaseSummary();
            updateFinancingCalculations();
            updateIncomeSummary();
            updateExpenseSummary();
            calculateAndUpdateResults();
            
            // Show purchase tab by default
            showTabContent(purchaseTab);
            
            // Check if user should see premium features teaser
            checkPremiumFeatures();
        } finally {
            // Reset flag after initialization
            isUpdatingFields = false;
        }
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        // Cleanup auto-save executor
        if (autoSaveExecutor != null && !autoSaveExecutor.isShutdown()) {
            autoSaveExecutor.shutdown();
        }
    }

    @Subscribe("mainTabs")
    public void onMainTabsSelectedChange(Tabs.SelectedChangeEvent event) {
        Tab selectedTab = event.getSelectedTab();
        showTabContent(selectedTab);
    }

    // ==================== Purchase Details Methods ====================

    private void setupPurchaseFieldListeners() {
        // Set value change mode to eager for real-time updates
        purchasePriceField.setValueChangeMode(ValueChangeMode.LAZY);
        downPaymentAmountField.setValueChangeMode(ValueChangeMode.LAZY);
        downPaymentPercentField.setValueChangeMode(ValueChangeMode.LAZY);
        closingCostsField.setValueChangeMode(ValueChangeMode.LAZY);
        closingCostsPercentField.setValueChangeMode(ValueChangeMode.LAZY);
        renovationCostsField.setValueChangeMode(ValueChangeMode.LAZY);

        // Purchase price changes
        purchasePriceField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                onPurchasePriceChange();
            }
        });

        // Down payment amount changes
        downPaymentAmountField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                onDownPaymentAmountChange();
            }
        });

        // Down payment percent changes
        downPaymentPercentField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                onDownPaymentPercentChange();
            }
        });

        // Closing costs amount changes
        closingCostsField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                onClosingCostsAmountChange();
            }
        });

        // Closing costs percent changes
        closingCostsPercentField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                onClosingCostsPercentChange();
            }
        });

        // Renovation costs changes
        renovationCostsField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updatePurchaseSummary();
            }
        });
    }

    private void onPurchasePriceChange() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        if (details == null) return;

        // Recalculate dependent fields
        if (details.getDownPaymentPercent() != null) {
            calculateDownPaymentFromPercent();
        }
        if (details.getClosingCostsPercent() != null) {
            calculateClosingCostsFromPercent();
        } else {
            // Apply default closing costs
            applyDefaultClosingCosts();
        }

        updatePurchaseSummary();
        checkPurchaseWarnings();
    }

    private void onDownPaymentAmountChange() {
        calculateDownPaymentPercent();
        updatePurchaseSummary();
        checkPurchaseWarnings();
    }

    private void onDownPaymentPercentChange() {
        calculateDownPaymentFromPercent();
        updatePurchaseSummary();
        checkPurchaseWarnings();
    }

    private void onClosingCostsAmountChange() {
        calculateClosingCostsPercent();
        updatePurchaseSummary();
    }

    private void onClosingCostsPercentChange() {
        calculateClosingCostsFromPercent();
        updatePurchaseSummary();
    }

    private void calculateDownPaymentFromPercent() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        if (details == null || details.getPurchasePrice() == null || details.getDownPaymentPercent() == null) {
            return;
        }

        isUpdatingFields = true;
        try {
            BigDecimal amount = details.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(details.getDownPaymentPercent() / 100.0))
                    .setScale(2, RoundingMode.HALF_UP);
            details.setDownPaymentAmount(amount);
        } finally {
            isUpdatingFields = false;
        }
    }

    private void calculateDownPaymentPercent() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        if (details == null || details.getPurchasePrice() == null || details.getDownPaymentAmount() == null) {
            return;
        }

        if (details.getPurchasePrice().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        isUpdatingFields = true;
        try {
            Double percent = details.getDownPaymentAmount()
                    .divide(details.getPurchasePrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0))
                    .doubleValue();
            details.setDownPaymentPercent(percent);
        } finally {
            isUpdatingFields = false;
        }
    }

    private void calculateClosingCostsFromPercent() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        if (details == null || details.getPurchasePrice() == null || details.getClosingCostsPercent() == null) {
            return;
        }

        isUpdatingFields = true;
        try {
            BigDecimal amount = details.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(details.getClosingCostsPercent() / 100.0))
                    .setScale(2, RoundingMode.HALF_UP);
            details.setClosingCosts(amount);
        } finally {
            isUpdatingFields = false;
        }
    }

    private void calculateClosingCostsPercent() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        if (details == null || details.getPurchasePrice() == null || details.getClosingCosts() == null) {
            return;
        }

        if (details.getPurchasePrice().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        isUpdatingFields = true;
        try {
            Double percent = details.getClosingCosts()
                    .divide(details.getPurchasePrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0))
                    .doubleValue();
            details.setClosingCostsPercent(percent);
        } finally {
            isUpdatingFields = false;
        }
    }

    private void applyDefaultClosingCosts() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        if (details == null || details.getPurchasePrice() == null) {
            return;
        }

        if (details.getClosingCosts() == null && details.getClosingCostsPercent() == null) {
            isUpdatingFields = true;
            try {
                details.setClosingCostsPercent(DEFAULT_CLOSING_COSTS_PERCENT);
                calculateClosingCostsFromPercent();
            } finally {
                isUpdatingFields = false;
            }
        }
    }

    private void updatePurchaseSummary() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        if (details == null) return;

        // Calculate total cash invested
        BigDecimal totalCash = details.getTotalCashInvested();
        totalCashInvestedSpan.setText(formatCurrency(totalCash));

        // Calculate loan amount
        BigDecimal loanAmount = BigDecimal.ZERO;
        if (details.getPurchasePrice() != null && details.getDownPaymentAmount() != null) {
            loanAmount = details.getPurchasePrice().subtract(details.getDownPaymentAmount());
        }
        loanAmountSpan.setText(formatCurrency(loanAmount));

        // Update financing loan amount only if it has changed
        FinancingDetails financing = getEditedEntity().getFinancingDetails();
        if (financing != null) {
            // Only update if the value has actually changed to avoid marking entity as dirty
            BigDecimal currentLoanAmount = financing.getLoanAmount();
            if (currentLoanAmount == null || currentLoanAmount.compareTo(loanAmount) != 0) {
                financing.setLoanAmount(loanAmount);
            }
            updateFinancingCalculations();
        }
    }

    private void checkPurchaseWarnings() {
        PurchaseDetails details = getEditedEntity().getPurchaseDetails();
        purchaseWarningsDiv.removeAll();

        if (details == null) return;

        // Check down payment percentage
        if (details.getDownPaymentPercent() != null) {
            if (details.getDownPaymentPercent() < MIN_DOWN_PAYMENT_WARNING) {
                addWarning(purchaseWarningsDiv, 
                    "Down payment below 20% may result in higher interest rates and PMI requirements.",
                    "warning");
            } else if (details.getDownPaymentPercent() > MAX_DOWN_PAYMENT_WARNING) {
                addWarning(purchaseWarningsDiv, 
                    "Down payment above 50% may limit your leverage and return on investment.",
                    "info");
            }
        }

        // Check purchase price vs estimated value
        if (propertySearchResult != null && propertySearchResult.hasValueEstimate()) {
            BigDecimal estimatedValue = propertySearchResult.getValueEstimate().getPrice();
            BigDecimal purchasePrice = details.getPurchasePrice();
            
            if (estimatedValue != null && purchasePrice != null && estimatedValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal difference = purchasePrice.subtract(estimatedValue)
                        .divide(estimatedValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100.0));
                
                if (Math.abs(difference.doubleValue()) > VALUE_DIFFERENCE_WARNING_PERCENT) {
                    String message = difference.doubleValue() > 0 
                        ? String.format("Purchase price is %.1f%% above estimated value (%s)", 
                            difference.doubleValue(), formatCurrency(estimatedValue))
                        : String.format("Purchase price is %.1f%% below estimated value (%s)", 
                            Math.abs(difference.doubleValue()), formatCurrency(estimatedValue));
                    addWarning(purchaseWarningsDiv, message, "info");
                }
            }
        }
    }

    // ==================== Financing Details Methods ====================

    private void setupFinancingFieldListeners() {
        interestRateField.setValueChangeMode(ValueChangeMode.LAZY);
        pointsPaidField.setValueChangeMode(ValueChangeMode.LAZY);

        interestRateField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateFinancingCalculations();
            }
        });

        loanTermSelect.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                FinancingDetails financing = getEditedEntity().getFinancingDetails();
                if (financing != null) {
                    financing.setLoanTermYears(e.getValue());
                    updateFinancingCalculations();
                }
            }
        });

        pointsPaidField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateFinancingCalculations();
            }
        });
    }

    private void updateFinancingCalculations() {
        FinancingDetails financing = getEditedEntity().getFinancingDetails();
        if (financing == null) return;

        // Calculate monthly payment
        financing.calculateMonthlyPayment();

        // Update display
        BigDecimal monthlyPayment = financing.getMonthlyPayment();
        if (monthlyPayment != null) {
            principalInterestSpan.setText(formatCurrency(monthlyPayment));
            
            // For now, just show P&I. Property tax and insurance would come from expense projection
            totalMonthlyPaymentSpan.setText(formatCurrency(monthlyPayment));
            
            // Update amortization preview
            updateAmortizationPreview(financing);
        } else {
            principalInterestSpan.setText("$0");
            totalMonthlyPaymentSpan.setText("$0");
            amortizationPreview.removeAll();
        }
    }

    private void updateAmortizationPreview(FinancingDetails financing) {
        amortizationPreview.removeAll();

        if (financing.getLoanAmount() == null || financing.getMonthlyPayment() == null) {
            return;
        }

        // Calculate first year breakdown
        BigDecimal loanAmount = financing.getLoanAmount();
        BigDecimal monthlyPayment = financing.getMonthlyPayment();
        double monthlyRate = financing.getInterestRate() / 100.0 / 12.0;

        BigDecimal totalFirstYearPayments = monthlyPayment.multiply(BigDecimal.valueOf(12));
        BigDecimal principalPaidFirstYear = BigDecimal.ZERO;
        BigDecimal interestPaidFirstYear = BigDecimal.ZERO;

        BigDecimal balance = loanAmount;
        for (int month = 1; month <= 12; month++) {
            BigDecimal interestPayment = balance.multiply(BigDecimal.valueOf(monthlyRate))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment);
            
            principalPaidFirstYear = principalPaidFirstYear.add(principalPayment);
            interestPaidFirstYear = interestPaidFirstYear.add(interestPayment);
            balance = balance.subtract(principalPayment);
        }

        // Calculate total over loan term
        BigDecimal totalPaid = monthlyPayment.multiply(BigDecimal.valueOf(financing.getLoanTermYears() * 12));
        BigDecimal totalInterest = totalPaid.subtract(loanAmount);

        // Display first year
        H4 firstYearTitle = new H4("First Year Breakdown");
        firstYearTitle.getStyle().set("margin-top", "0");
        amortizationPreview.add(firstYearTitle);
        
        addAmortizationRow(amortizationPreview, "Total Payments:", formatCurrency(totalFirstYearPayments));
        addAmortizationRow(amortizationPreview, "Principal Paid:", formatCurrency(principalPaidFirstYear));
        addAmortizationRow(amortizationPreview, "Interest Paid:", formatCurrency(interestPaidFirstYear));

        // Display loan term totals
        H4 totalTitle = new H4(financing.getLoanTermYears() + "-Year Totals");
        totalTitle.getStyle().set("margin-top", "var(--lumo-space-m)");
        amortizationPreview.add(totalTitle);
        
        addAmortizationRow(amortizationPreview, "Total Paid:", formatCurrency(totalPaid));
        addAmortizationRow(amortizationPreview, "Total Interest:", formatCurrency(totalInterest));
    }

    private void addAmortizationRow(VerticalLayout container, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "500");
        
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-weight", "bold");
        
        row.add(labelSpan, valueSpan);
        container.add(row);
    }

    // ==================== Income Projection Methods ====================

    private void setupIncomeFieldListeners() {
        monthlyRentField.setValueChangeMode(ValueChangeMode.LAZY);
        otherMonthlyIncomeField.setValueChangeMode(ValueChangeMode.LAZY);
        vacancyRateField.setValueChangeMode(ValueChangeMode.LAZY);

        monthlyRentField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateIncomeSummary();
                updateRentComparison();
                // Recalculate expense projections that depend on rent
                updateExpenseSummary();
            }
        });

        otherMonthlyIncomeField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateIncomeSummary();
            }
        });

        vacancyRateField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateIncomeSummary();
                checkVacancyWarnings();
            }
        });

        // Expand/collapse other income breakdown
        expandOtherIncomeButton.addClickListener(e -> {
            otherIncomeExpanded = !otherIncomeExpanded;
            if (otherIncomeExpanded) {
                showOtherIncomeBreakdown();
                expandOtherIncomeButton.setText(messageBundle.getMessage("button.collapseOtherIncome"));
            } else {
                otherIncomeBreakdownDiv.setVisible(false);
                expandOtherIncomeButton.setText(messageBundle.getMessage("button.expandOtherIncome"));
            }
        });
    }

    private void updateIncomeSummary() {
        IncomeProjection income = getEditedEntity().getIncomeProjection();
        if (income == null) return;

        // Calculate and display income metrics
        BigDecimal grossIncome = income.getGrossMonthlyIncome();
        BigDecimal vacancyLoss = income.getMonthlyVacancyLoss();
        BigDecimal effectiveIncome = income.getEffectiveMonthlyIncome();
        BigDecimal annualIncome = income.getAnnualEffectiveIncome();

        grossMonthlyIncomeSpan.setText(formatCurrency(grossIncome));
        monthlyVacancyLossSpan.setText(formatCurrency(vacancyLoss));
        effectiveMonthlyIncomeSpan.setText(formatCurrency(effectiveIncome));
        annualEffectiveIncomeSpan.setText(formatCurrency(annualIncome));

        // Recalculate results when income changes
        calculateAndUpdateResults();
    }

    private void updateRentComparison() {
        rentComparisonDiv.removeAll();
        
        IncomeProjection income = getEditedEntity().getIncomeProjection();
        if (income == null || income.getMonthlyRent() == null) return;

        // Compare with RentCast estimate if available
        if (propertySearchResult != null && propertySearchResult.hasRentEstimate()) {
            BigDecimal estimatedRent = propertySearchResult.getRentEstimate().getRent();
            BigDecimal actualRent = income.getMonthlyRent();

            if (estimatedRent != null && estimatedRent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal difference = actualRent.subtract(estimatedRent);
                BigDecimal percentDiff = difference.divide(estimatedRent, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100.0));

                // Create comparison layout with CSS classes
                HorizontalLayout comparisonLayout = new HorizontalLayout();
                comparisonLayout.setWidthFull();
                comparisonLayout.addClassName("rent-comparison-layout");

                Span label = new Span("RentCast Estimate:");
                label.addClassName("rent-comparison-label");
                
                Span value = new Span(formatCurrency(estimatedRent));
                value.addClassName("rent-comparison-value");
                
                comparisonLayout.add(label, value);
                rentComparisonDiv.add(comparisonLayout);

                // Show difference indicator with appropriate styling
                Div diffDiv = new Div();
                String diffText;
                if (percentDiff.doubleValue() > 0) {
                    diffText = String.format("+%.1f%% above estimate", percentDiff.doubleValue());
                    diffDiv.addClassName("rent-difference-positive");
                } else {
                    diffText = String.format("%.1f%% below estimate", percentDiff.doubleValue());
                    diffDiv.addClassName("rent-difference-negative");
                }
                diffDiv.setText(diffText);
                rentComparisonDiv.add(diffDiv);
            }
        }
    }

    private void checkVacancyWarnings() {
        vacancyWarningsDiv.removeAll();
        
        IncomeProjection income = getEditedEntity().getIncomeProjection();
        if (income == null || income.getVacancyRate() == null) return;

        if (income.getVacancyRate() > 15.0) {
            addWarning(vacancyWarningsDiv, 
                messageBundle.getMessage("warning.highVacancyRate"), 
                "warning");
        }
    }

    private void showOtherIncomeBreakdown() {
        otherIncomeBreakdownDiv.removeAll();
        otherIncomeBreakdownDiv.setVisible(true);

        // Create fields for different income sources
        TextField parkingField = new TextField(messageBundle.getMessage("field.parkingFees"));
        TextField laundryField = new TextField(messageBundle.getMessage("field.laundryIncome"));
        TextField storageField = new TextField(messageBundle.getMessage("field.storageFees"));
        TextField petField = new TextField(messageBundle.getMessage("field.petFees"));
        TextField otherField = new TextField(messageBundle.getMessage("field.otherIncome"));

        // Add listeners to calculate total
        HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> calculateTotal = e -> {
            BigDecimal total = BigDecimal.ZERO;
            total = total.add(parseAmount(parkingField.getValue()));
            total = total.add(parseAmount(laundryField.getValue()));
            total = total.add(parseAmount(storageField.getValue()));
            total = total.add(parseAmount(petField.getValue()));
            total = total.add(parseAmount(otherField.getValue()));
            
            otherMonthlyIncomeField.setValue(total.toString());
        };

        parkingField.addValueChangeListener(calculateTotal);
        laundryField.addValueChangeListener(calculateTotal);
        storageField.addValueChangeListener(calculateTotal);
        petField.addValueChangeListener(calculateTotal);
        otherField.addValueChangeListener(calculateTotal);

        otherIncomeBreakdownDiv.add(parkingField, laundryField, storageField, petField, otherField);
    }

    // ==================== Expense Projection Methods ====================

    private void setupExpenseFieldListeners() {
        annualPropertyTaxField.setValueChangeMode(ValueChangeMode.LAZY);
        annualInsuranceField.setValueChangeMode(ValueChangeMode.LAZY);
        monthlyHOAFeesField.setValueChangeMode(ValueChangeMode.LAZY);
        propertyManagementPercentField.setValueChangeMode(ValueChangeMode.LAZY);
        maintenancePercentField.setValueChangeMode(ValueChangeMode.LAZY);
        capExPercentField.setValueChangeMode(ValueChangeMode.LAZY);
        otherMonthlyExpensesField.setValueChangeMode(ValueChangeMode.LAZY);

        // Property tax changes
        annualPropertyTaxField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateMonthlyPropertyTax();
                updateExpenseSummary();
            }
        });

        // Insurance changes
        annualInsuranceField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateMonthlyInsurance();
                updateExpenseSummary();
            }
        });

        // Estimate insurance button
        estimateInsuranceButton.addClickListener(e -> {
            estimateInsurance();
        });

        // HOA fees changes
        monthlyHOAFeesField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateExpenseSummary();
            }
        });

        // Property management checkbox
        usePropertyManagerCheckbox.addValueChangeListener(e -> {
            propertyManagementFieldsDiv.setVisible(e.getValue());
            if (e.getValue()) {
                // Set default 10%
                ExpenseProjection expense = getEditedEntity().getExpenseProjection();
                if (expense != null && expense.getPropertyManagementPercent() == null) {
                    expense.setPropertyManagementPercent(10.0);
                }
            } else {
                // Clear property management
                ExpenseProjection expense = getEditedEntity().getExpenseProjection();
                if (expense != null) {
                    expense.setPropertyManagementPercent(null);
                    expense.setPropertyManagementAmount(null);
                }
            }
            updateExpenseSummary();
        });

        // Property management percent changes
        propertyManagementPercentField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                calculatePropertyManagementAmount();
                updateExpenseSummary();
            }
        });

        // Maintenance percent changes
        maintenancePercentField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                calculateMaintenanceAmount();
                updateExpenseSummary();
            }
        });

        // CapEx percent changes
        capExPercentField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                calculateCapExAmount();
                updateExpenseSummary();
            }
        });

        // Utilities checkbox
        landlordPaysUtilitiesCheckbox.addValueChangeListener(e -> {
            if (e.getValue()) {
                showUtilitiesBreakdown();
            } else {
                utilitiesBreakdownDiv.setVisible(false);
                ExpenseProjection expense = getEditedEntity().getExpenseProjection();
                if (expense != null) {
                    expense.setMonthlyUtilities(null);
                }
                updateExpenseSummary();
            }
        });

        // Other expenses changes
        otherMonthlyExpensesField.addValueChangeListener(e -> {
            if (!isUpdatingFields && e.isFromClient()) {
                updateExpenseSummary();
            }
        });
    }

    private void updateMonthlyPropertyTax() {
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        if (expense == null) return;

        BigDecimal monthlyTax = expense.getMonthlyPropertyTax();
        monthlyPropertyTaxSpan.setText("Monthly: " + formatCurrency(monthlyTax));
    }

    private void updateMonthlyInsurance() {
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        if (expense == null) return;

        BigDecimal monthlyInsurance = expense.getMonthlyInsurance();
        monthlyInsuranceSpan.setText("Monthly: " + formatCurrency(monthlyInsurance));
    }

    private void estimateInsurance() {
        PurchaseDetails purchase = getEditedEntity().getPurchaseDetails();
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        
        if (purchase != null && expense != null && purchase.getPurchasePrice() != null) {
            // Estimate at 0.75% of property value annually
            BigDecimal annualInsurance = purchase.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(0.0075))
                    .setScale(2, RoundingMode.HALF_UP);
            
            expense.setAnnualInsurance(annualInsurance);
            updateMonthlyInsurance();
            updateExpenseSummary();
            
            notifications.create("Insurance estimated at 0.75% of property value")
                    .withType(Notifications.Type.SUCCESS)
                    .show();
        }
    }

    private void calculatePropertyManagementAmount() {
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        IncomeProjection income = getEditedEntity().getIncomeProjection();
        
        if (expense != null && income != null && income.getMonthlyRent() != null) {
            expense.calculatePropertyManagementAmount(income.getMonthlyRent());
            
            if (expense.getPropertyManagementAmount() != null) {
                propertyManagementAmountSpan.setText("Monthly: " + 
                        formatCurrency(expense.getPropertyManagementAmount()));
            }
        }
    }

    private void calculateMaintenanceAmount() {
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        PurchaseDetails purchase = getEditedEntity().getPurchaseDetails();
        
        if (expense != null && purchase != null && purchase.getPurchasePrice() != null) {
            expense.calculateMaintenanceAmount(purchase.getPurchasePrice());
            
            if (expense.getMaintenanceAmount() != null) {
                maintenanceAmountSpan.setText("Monthly: " + 
                        formatCurrency(expense.getMaintenanceAmount()));
            }
        }
    }

    private void calculateCapExAmount() {
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        PurchaseDetails purchase = getEditedEntity().getPurchaseDetails();
        
        if (expense != null && purchase != null && purchase.getPurchasePrice() != null) {
            expense.calculateCapExAmount(purchase.getPurchasePrice());
            
            if (expense.getCapExAmount() != null) {
                capExAmountSpan.setText("Monthly: " + 
                        formatCurrency(expense.getCapExAmount()));
            }
        }
    }

    private void showUtilitiesBreakdown() {
        utilitiesBreakdownDiv.removeAll();
        utilitiesBreakdownDiv.setVisible(true);

        // Create fields for different utilities
        TextField waterField = new TextField(messageBundle.getMessage("field.waterSewer"));
        TextField electricityField = new TextField(messageBundle.getMessage("field.electricity"));
        TextField gasField = new TextField(messageBundle.getMessage("field.gas"));
        TextField trashField = new TextField(messageBundle.getMessage("field.trash"));
        TextField internetField = new TextField(messageBundle.getMessage("field.internetCable"));

        // Add listeners to calculate total
        HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> calculateTotal = e -> {
            BigDecimal total = BigDecimal.ZERO;
            total = total.add(parseAmount(waterField.getValue()));
            total = total.add(parseAmount(electricityField.getValue()));
            total = total.add(parseAmount(gasField.getValue()));
            total = total.add(parseAmount(trashField.getValue()));
            total = total.add(parseAmount(internetField.getValue()));
            
            ExpenseProjection expense = getEditedEntity().getExpenseProjection();
            if (expense != null) {
                expense.setMonthlyUtilities(total);
                updateExpenseSummary();
            }
        };

        waterField.addValueChangeListener(calculateTotal);
        electricityField.addValueChangeListener(calculateTotal);
        gasField.addValueChangeListener(calculateTotal);
        trashField.addValueChangeListener(calculateTotal);
        internetField.addValueChangeListener(calculateTotal);

        utilitiesBreakdownDiv.add(waterField, electricityField, gasField, trashField, internetField);
    }

    private void updateExpenseSummary() {
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        IncomeProjection income = getEditedEntity().getIncomeProjection();
        
        if (expense == null) return;

        // Recalculate amounts based on percentages
        if (expense.getPropertyManagementPercent() != null && income != null) {
            calculatePropertyManagementAmount();
        }
        if (expense.getMaintenancePercent() != null) {
            calculateMaintenanceAmount();
        }
        if (expense.getCapExPercent() != null) {
            calculateCapExAmount();
        }

        // Calculate totals
        BigDecimal monthlyExpenses = expense.getTotalMonthlyExpenses();
        BigDecimal annualExpenses = expense.getTotalAnnualExpenses();

        totalMonthlyExpensesSpan.setText(formatCurrency(monthlyExpenses));
        totalAnnualExpensesSpan.setText(formatCurrency(annualExpenses));

        // Calculate expense ratio
        if (income != null && income.getMonthlyRent() != null && 
                income.getMonthlyRent().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal expenseRatio = monthlyExpenses
                    .divide(income.getMonthlyRent(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0));
            expenseRatioSpan.setText(String.format("%.1f%%", expenseRatio.doubleValue()));

            // Check for warnings
            checkExpenseWarnings(expenseRatio.doubleValue());
        } else {
            expenseRatioSpan.setText("N/A");
        }

        // Update expense chart
        updateExpenseChart();

        // Recalculate results when expenses change
        calculateAndUpdateResults();
    }

    private void checkExpenseWarnings(double expenseRatio) {
        expenseWarningsDiv.removeAll();

        if (expenseRatio > 50.0) {
            addWarning(expenseWarningsDiv, 
                messageBundle.getMessage("warning.highExpenseRatio"), 
                "warning");
        }
    }

    private void updateExpenseChart() {
        // TODO: Implement pie chart showing expense breakdown
        // This will use the Jmix charts add-on
        expenseChartDiv.removeAll();
        
        ExpenseProjection expense = getEditedEntity().getExpenseProjection();
        if (expense == null) return;

        // For now, just show a text breakdown
        VerticalLayout breakdown = new VerticalLayout();
        breakdown.setPadding(false);
        breakdown.setSpacing(false);

        if (expense.getAnnualPropertyTax() != null) {
            addExpenseBreakdownRow(breakdown, "Property Tax", expense.getMonthlyPropertyTax());
        }
        if (expense.getAnnualInsurance() != null) {
            addExpenseBreakdownRow(breakdown, "Insurance", expense.getMonthlyInsurance());
        }
        if (expense.getMonthlyHOAFees() != null) {
            addExpenseBreakdownRow(breakdown, "HOA Fees", expense.getMonthlyHOAFees());
        }
        if (expense.getPropertyManagementAmount() != null) {
            addExpenseBreakdownRow(breakdown, "Property Management", expense.getPropertyManagementAmount());
        }
        if (expense.getMaintenanceAmount() != null) {
            addExpenseBreakdownRow(breakdown, "Maintenance", expense.getMaintenanceAmount());
        }
        if (expense.getCapExAmount() != null) {
            addExpenseBreakdownRow(breakdown, "CapEx", expense.getCapExAmount());
        }
        if (expense.getMonthlyUtilities() != null) {
            addExpenseBreakdownRow(breakdown, "Utilities", expense.getMonthlyUtilities());
        }
        if (expense.getOtherMonthlyExpenses() != null) {
            addExpenseBreakdownRow(breakdown, "Other", expense.getOtherMonthlyExpenses());
        }

        expenseChartDiv.add(breakdown);
    }

    private void addExpenseBreakdownRow(VerticalLayout container, String label, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return;

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        row.getStyle()
                .set("padding", "var(--lumo-space-xs)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        Span labelSpan = new Span(label);
        Span valueSpan = new Span(formatCurrency(amount));
        valueSpan.getStyle().set("font-weight", "500");

        row.add(labelSpan, valueSpan);
        container.add(row);
    }

    private BigDecimal parseAmount(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    // ==================== UI Helper Methods ====================

    private void updatePropertyHeader(RentalAnalysis analysis) {
        propertyHeaderDiv.removeAll();

        if (analysis.getPropertyAddress() != null) {
            // Property address (large)
            Span addressSpan = new Span(analysis.getPropertyAddress());
            addressSpan.getStyle()
                    .set("font-size", "var(--lumo-font-size-xl)")
                    .set("font-weight", "bold")
                    .set("display", "block")
                    .set("margin-bottom", "var(--lumo-space-s)");
            propertyHeaderDiv.add(addressSpan);

            // Property details (if available from search result)
            if (propertySearchResult != null && propertySearchResult.hasPropertyDetails()) {
                RentCastProperty property = propertySearchResult.getProperty();
                
                StringBuilder specs = new StringBuilder();
                if (property.getPropertyType() != null) {
                    specs.append(property.getPropertyType());
                }
                if (property.getBedrooms() != null) {
                    if (specs.length() > 0) specs.append(" • ");
                    specs.append(property.getBedrooms()).append(" bed");
                }
                if (property.getBathrooms() != null) {
                    if (specs.length() > 0) specs.append(" • ");
                    specs.append(property.getBathrooms()).append(" bath");
                }
                if (property.getSquareFootage() != null) {
                    if (specs.length() > 0) specs.append(" • ");
                    specs.append(formatNumber(property.getSquareFootage())).append(" sq ft");
                }

                if (specs.length() > 0) {
                    Span specsSpan = new Span(specs.toString());
                    specsSpan.getStyle()
                            .set("font-size", "var(--lumo-font-size-m)")
                            .set("color", "var(--lumo-secondary-text-color)");
                    propertyHeaderDiv.add(specsSpan);
                }
            }
        }
    }

    private void showTabContent(Tab selectedTab) {
        // Hide all content
        purchaseContent.setVisible(false);
        financingContent.setVisible(false);
        incomeContent.setVisible(false);
        expensesContent.setVisible(false);
        resultsContent.setVisible(false);

        // Show selected content
        if (selectedTab == purchaseTab) {
            purchaseContent.setVisible(true);
        } else if (selectedTab == financingTab) {
            financingContent.setVisible(true);
        } else if (selectedTab == incomeTab) {
            incomeContent.setVisible(true);
        } else if (selectedTab == expensesTab) {
            expensesContent.setVisible(true);
        } else if (selectedTab == resultsTab) {
            resultsContent.setVisible(true);
        }
    }

    private void addWarning(Div container, String message, String type) {
        Div warningDiv = new Div();
        warningDiv.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius)")
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-s)");

        if ("warning".equals(type)) {
            warningDiv.getStyle()
                    .set("background", "var(--lumo-warning-color-10pct)")
                    .set("border-left", "4px solid var(--lumo-warning-color)");
        } else {
            warningDiv.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("border-left", "4px solid var(--lumo-primary-color)");
        }

        Span messageSpan = new Span(message);
        messageSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
        
        warningDiv.add(messageSpan);
        container.add(warningDiv);
    }

    private void setupAutoSave() {
        autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();
        autoSaveExecutor.scheduleAtFixedRate(() -> {
            try {
                getUI().ifPresent(ui -> ui.access(() -> {
                    try {
                        // Auto-save draft
                        RentalAnalysis analysis = getEditedEntity();
                        if (analysis != null && analysis.getId() != null) {
                            dataManager.save(analysis);
                            log.debug("Auto-saved analysis: {}", analysis.getId());
                        }
                    } catch (Exception e) {
                        log.error("Error during auto-save", e);
                    }
                }));
            } catch (Exception e) {
                log.error("Error scheduling auto-save", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    // ==================== Results Tab Methods ====================

    /**
     * Calculates all financial metrics and updates the Results tab display.
     */
    private void calculateAndUpdateResults() {
        try {
            RentalAnalysis analysis = getEditedEntity();
            if (analysis == null) return;

            IncomeProjection income = analysis.getIncomeProjection();
            ExpenseProjection expense = analysis.getExpenseProjection();
            PurchaseDetails purchase = analysis.getPurchaseDetails();
            FinancingDetails financing = analysis.getFinancingDetails();
            CalculatedMetrics metrics = analysis.getCalculatedMetrics();

            if (income == null || expense == null || purchase == null || financing == null || metrics == null) {
                return;
            }

            // Calculate Net Operating Income (NOI)
            BigDecimal effectiveIncome = income.getEffectiveMonthlyIncome();
            BigDecimal totalExpenses = expense.getTotalMonthlyExpenses();
            BigDecimal monthlyPayment = financing.getMonthlyPayment();
            
            // NOI = Effective Income - Operating Expenses (excluding debt service)
            BigDecimal monthlyNOI = effectiveIncome.subtract(totalExpenses);
            BigDecimal annualNOI = monthlyNOI.multiply(BigDecimal.valueOf(12));

            // Calculate Cash Flow (NOI - Debt Service)
            BigDecimal monthlyCashFlow = monthlyNOI.subtract(monthlyPayment != null ? monthlyPayment : BigDecimal.ZERO);
            BigDecimal annualCashFlow = monthlyCashFlow.multiply(BigDecimal.valueOf(12));

            // Update metrics entity (only if values have changed to avoid marking as dirty)
            updateMetricIfChanged(metrics::getMonthlyCashFlow, metrics::setMonthlyCashFlow, monthlyCashFlow);
            updateMetricIfChanged(metrics::getAnnualCashFlow, metrics::setAnnualCashFlow, annualCashFlow);

            // Calculate Cash-on-Cash Return
            // CoC = Annual Cash Flow / Total Cash Invested
            BigDecimal totalCashInvested = purchase.getTotalCashInvested();
            double cocReturn;
            if (totalCashInvested != null && totalCashInvested.compareTo(BigDecimal.ZERO) > 0) {
                cocReturn = annualCashFlow.divide(totalCashInvested, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            } else {
                cocReturn = 0.0;
            }
            updateMetricIfChanged(metrics::getCashOnCashReturn, metrics::setCashOnCashReturn, cocReturn);

            // Calculate Cap Rate
            // Cap Rate = Annual NOI / Purchase Price
            BigDecimal purchasePrice = purchase.getPurchasePrice();
            double capRate;
            if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                capRate = annualNOI.divide(purchasePrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            } else {
                capRate = 0.0;
            }
            updateMetricIfChanged(metrics::getCapRate, metrics::setCapRate, capRate);

            // Calculate Gross Yield
            // Gross Yield = (Annual Rent / Purchase Price) × 100
            BigDecimal annualRent = income.getGrossMonthlyIncome().multiply(BigDecimal.valueOf(12));
            double grossYield;
            if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                grossYield = annualRent.divide(purchasePrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            } else {
                grossYield = 0.0;
            }
            updateMetricIfChanged(metrics::getGrossYield, metrics::setGrossYield, grossYield);

            // Calculate Net Yield
            // Net Yield = (Annual NOI / Purchase Price) × 100
            double netYield;
            if (purchasePrice != null && purchasePrice.compareTo(BigDecimal.ZERO) > 0) {
                netYield = annualNOI.divide(purchasePrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            } else {
                netYield = 0.0;
            }
            updateMetricIfChanged(metrics::getNetYield, metrics::setNetYield, netYield);

            // Calculate Debt Service Coverage Ratio (DSCR)
            // DSCR = Annual NOI / Annual Debt Service
            double dscr;
            if (monthlyPayment != null && monthlyPayment.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal annualDebtService = monthlyPayment.multiply(BigDecimal.valueOf(12));
                dscr = annualNOI.divide(annualDebtService, 2, RoundingMode.HALF_UP).doubleValue();
            } else {
                dscr = 0.0;
            }
            updateMetricIfChanged(metrics::getDebtServiceCoverageRatio, metrics::setDebtServiceCoverageRatio, dscr);

            // Calculate Break-Even Ratio
            // Break-Even Ratio = (Operating Expenses + Debt Service) / Gross Income
            BigDecimal grossIncome = income.getGrossMonthlyIncome();
            double breakEvenRatio;
            if (grossIncome != null && grossIncome.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal totalMonthlyObligations = totalExpenses.add(monthlyPayment != null ? monthlyPayment : BigDecimal.ZERO);
                breakEvenRatio = totalMonthlyObligations.divide(grossIncome, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            } else {
                breakEvenRatio = 0.0;
            }
            updateMetricIfChanged(metrics::getBreakEvenRatio, metrics::setBreakEvenRatio, breakEvenRatio);

            // Update UI
            updateResultsDisplay(metrics, monthlyNOI, annualNOI);

        } catch (Exception e) {
            log.error("Error calculating results", e);
        }
    }

    /**
     * Updates the Results tab UI with calculated metrics.
     */
    private void updateResultsDisplay(CalculatedMetrics metrics, BigDecimal monthlyNOI, BigDecimal annualNOI) {
        // Update NOI
        noiSpan.setText(formatCurrency(annualNOI) + " / year");

        // Update Cash Flow with color coding
        monthlyCashFlowSpan.setText(formatCurrency(metrics.getMonthlyCashFlow()) + " / month");
        applyCashFlowColor(monthlyCashFlowSpan, metrics.getMonthlyCashFlow());
        
        annualCashFlowSpan.setText(formatCurrency(metrics.getAnnualCashFlow()) + " / year");
        applyCashFlowColor(annualCashFlowSpan, metrics.getAnnualCashFlow());

        // Update Cash-on-Cash Return
        cashOnCashReturnSpan.setText(formatPercentage(metrics.getCashOnCashReturn()));
        applyCoCReturnColor(cashOnCashReturnSpan, metrics.getCashOnCashReturn());

        // Update Cap Rate
        capRateSpan.setText(formatPercentage(metrics.getCapRate()));
        applyCapRateColor(capRateSpan, metrics.getCapRate());

        // Update Gross Yield with color coding
        grossYieldSpan.setText(formatPercentage(metrics.getGrossYield()));
        applyYieldColor(grossYieldSpan, metrics.getGrossYield());

        // Update Net Yield with color coding
        netYieldSpan.setText(formatPercentage(metrics.getNetYield()));
        applyYieldColor(netYieldSpan, metrics.getNetYield());

        // Update DSCR
        dscrSpan.setText(formatDecimal(metrics.getDebtServiceCoverageRatio()));
        applyDSCRColor(dscrSpan, metrics.getDebtServiceCoverageRatio());

        // Update Break-Even Ratio with color coding
        breakEvenRatioSpan.setText(formatPercentage(metrics.getBreakEvenRatio()));
        applyBreakEvenRatioColor(breakEvenRatioSpan, metrics.getBreakEvenRatio());

        // Update investment summary
        updateInvestmentSummary(metrics);

        // Check for warnings
        checkResultsWarnings(metrics);
    }

    /**
     * Updates the investment summary section with key insights.
     */
    private void updateInvestmentSummary(CalculatedMetrics metrics) {
        investmentSummaryDiv.removeAll();

        // Add summary header with prominent styling
        H3 summaryHeader = new H3(messageBundle.getMessage("section.investmentSummary"));
        summaryHeader.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("color", "var(--lumo-primary-text-color)");
        investmentSummaryDiv.add(summaryHeader);

        // Determine investment quality
        String quality = determineInvestmentQuality(metrics);
        String qualityColor = getQualityColor(quality);
        String qualityTextColor = getQualityTextColor(quality);

        // Create prominent quality badge
        Div qualityBadge = new Div();
        qualityBadge.addClassName("investment-quality-badge");
        qualityBadge.getStyle()
                .set("padding", "var(--lumo-space-m) var(--lumo-space-xl)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", qualityColor)
                .set("color", qualityTextColor)
                .set("text-align", "center")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");

        Span qualityText = new Span(quality + " Investment");
        qualityBadge.add(qualityText);
        investmentSummaryDiv.add(qualityBadge);

        // Add key insights with icons
        addInsightWithIcon(investmentSummaryDiv, "Cash Flow", 
                metrics.getMonthlyCashFlow().compareTo(BigDecimal.ZERO) > 0 ? "Positive" : "Negative",
                metrics.getMonthlyCashFlow().compareTo(BigDecimal.ZERO) > 0,
                "Monthly income after all expenses and debt service");

        addInsightWithIcon(investmentSummaryDiv, "Cash-on-Cash Return", 
                formatPercentage(metrics.getCashOnCashReturn()),
                metrics.getCashOnCashReturn() >= 8.0,
                "Annual return on your cash investment (8%+ is good)");

        addInsightWithIcon(investmentSummaryDiv, "Cap Rate", 
                formatPercentage(metrics.getCapRate()),
                metrics.getCapRate() >= 6.0,
                "Property's return potential (6%+ is good)");

        addInsightWithIcon(investmentSummaryDiv, "DSCR", 
                formatDecimal(metrics.getDebtServiceCoverageRatio()),
                metrics.getDebtServiceCoverageRatio() >= 1.25,
                "Income coverage of debt payments (1.25+ is good)");
        
        // Add market comparison if available
        addMarketComparison(investmentSummaryDiv, metrics);
    }

    /**
     * Adds an insight row with icon and tooltip to the investment summary.
     */
    private void addInsightWithIcon(Div container, String label, String value, boolean isGood, String helpText) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        row.setAlignItems(HorizontalLayout.Alignment.CENTER);
        row.getStyle()
                .set("padding", "var(--lumo-space-s) 0")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        // Left side: icon + label + help icon
        HorizontalLayout leftSide = new HorizontalLayout();
        leftSide.setAlignItems(HorizontalLayout.Alignment.CENTER);
        leftSide.setSpacing(true);
        
        // Status icon
        Span icon = new Span(isGood ? "✓" : "✗");
        icon.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "bold")
                .set("color", isGood ? "#22c55e" : "#ef4444")
                .set("margin-right", "var(--lumo-space-xs)");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("color", "var(--lumo-body-text-color)")
                .set("font-weight", "500");

        // Help icon with tooltip
        Span helpIcon = new Span("ⓘ");
        helpIcon.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("cursor", "help")
                .set("margin-left", "var(--lumo-space-xs)");
        helpIcon.getElement().setAttribute("title", helpText);

        leftSide.add(icon, labelSpan, helpIcon);

        // Right side: value
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-weight", "700")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", isGood ? "#22c55e" : "#ef4444");

        row.add(leftSide, valueSpan);
        container.add(row);
    }

    /**
     * Adds market comparison section if data is available.
     */
    private void addMarketComparison(Div container, CalculatedMetrics metrics) {
        // Check if we have RentCast data for comparison
        if (propertySearchResult == null || !propertySearchResult.hasRentEstimate()) {
            return;
        }

        Div comparisonDiv = new Div();
        comparisonDiv.addClassName("market-comparison");

        H4 comparisonHeader = new H4("Market Comparison");
        comparisonDiv.add(comparisonHeader);

        // Compare rent with market estimate
        RentalAnalysis analysis = getEditedEntity();
        if (analysis.getIncomeProjection() != null && analysis.getIncomeProjection().getMonthlyRent() != null) {
            BigDecimal actualRent = analysis.getIncomeProjection().getMonthlyRent();
            BigDecimal estimatedRent = propertySearchResult.getRentEstimate().getRent();
            
            if (estimatedRent != null && estimatedRent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal difference = actualRent.subtract(estimatedRent);
                BigDecimal percentDiff = difference.divide(estimatedRent, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100.0));

                Span comparisonText = new Span();
                comparisonText.addClassName("market-comparison-text");
                
                if (percentDiff.doubleValue() > 0) {
                    comparisonText.setText(String.format("Your rent is %.1f%% above market estimate", percentDiff.doubleValue()));
                    comparisonText.addClassName("positive");
                } else {
                    comparisonText.setText(String.format("Your rent is %.1f%% below market estimate", Math.abs(percentDiff.doubleValue())));
                    comparisonText.addClassName("negative");
                }
                comparisonDiv.add(comparisonText);
            }
        }

        container.add(comparisonDiv);
    }

    /**
     * Returns the text color for investment quality badge.
     */
    private String getQualityTextColor(String quality) {
        return "white"; // All badges use white text for better contrast
    }

    /**
     * Determines the overall investment quality based on metrics.
     */
    private String determineInvestmentQuality(CalculatedMetrics metrics) {
        int score = 0;

        // Cash Flow positive
        if (metrics.getMonthlyCashFlow().compareTo(BigDecimal.ZERO) > 0) score++;

        // CoC Return >= 8%
        if (metrics.getCashOnCashReturn() >= 8.0) score += 2;
        else if (metrics.getCashOnCashReturn() >= 5.0) score++;

        // Cap Rate >= 6%
        if (metrics.getCapRate() >= 6.0) score += 2;
        else if (metrics.getCapRate() >= 4.0) score++;

        // DSCR >= 1.25
        if (metrics.getDebtServiceCoverageRatio() >= 1.25) score += 2;
        else if (metrics.getDebtServiceCoverageRatio() >= 1.0) score++;

        // Determine quality
        if (score >= 6) return "Excellent";
        if (score >= 4) return "Good";
        if (score >= 2) return "Fair";
        return "Poor";
    }

    /**
     * Returns the background color for investment quality badge.
     */
    private String getQualityColor(String quality) {
        return switch (quality) {
            case "Excellent" -> "#22c55e"; // Green
            case "Good" -> "#3b82f6"; // Blue
            case "Fair" -> "#f59e0b"; // Orange
            case "Poor" -> "#ef4444"; // Red
            default -> "#6b7280"; // Gray
        };
    }

    /**
     * Checks for warnings in the results and displays them.
     */
    private void checkResultsWarnings(CalculatedMetrics metrics) {
        resultsWarningsDiv.removeAll();

        // Negative cash flow warning
        if (metrics.getMonthlyCashFlow().compareTo(BigDecimal.ZERO) < 0) {
            addWarning(resultsWarningsDiv, 
                    messageBundle.getMessage("warning.negativeCashFlow"), 
                    "warning");
        }

        // Low CoC Return warning
        if (metrics.getCashOnCashReturn() < 5.0) {
            addWarning(resultsWarningsDiv, 
                    messageBundle.getMessage("warning.lowCoCReturn"), 
                    "warning");
        }

        // Low Cap Rate warning
        if (metrics.getCapRate() < 4.0) {
            addWarning(resultsWarningsDiv, 
                    messageBundle.getMessage("warning.lowCapRate"), 
                    "warning");
        }

        // Low DSCR warning
        if (metrics.getDebtServiceCoverageRatio() < 1.0) {
            addWarning(resultsWarningsDiv, 
                    messageBundle.getMessage("warning.lowDSCR"), 
                    "warning");
        }

        // High break-even ratio warning
        if (metrics.getBreakEvenRatio() > 85.0) {
            addWarning(resultsWarningsDiv, 
                    messageBundle.getMessage("warning.highBreakEvenRatio"), 
                    "warning");
        }
    }

    /**
     * Applies color coding to Cash Flow based on value.
     */
    private void applyCashFlowColor(Span span, BigDecimal value) {
        if (value == null) return;
        span.removeClassNames("metric-excellent", "metric-good", "metric-caution", "metric-poor");
        
        if (value.compareTo(BigDecimal.ZERO) > 0) {
            span.addClassName("metric-excellent");
            span.getStyle().set("color", "#22c55e"); // Green
        } else {
            span.addClassName("metric-poor");
            span.getStyle().set("color", "#ef4444"); // Red
        }
        span.getStyle().set("font-weight", "700");
    }

    /**
     * Applies color coding to Cash-on-Cash Return based on value.
     */
    private void applyCoCReturnColor(Span span, Double value) {
        if (value == null) return;
        span.removeClassNames("metric-excellent", "metric-good", "metric-caution", "metric-poor");
        
        if (value >= 10.0) {
            span.addClassName("metric-excellent");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value >= 8.0) {
            span.addClassName("metric-good");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value >= 5.0) {
            span.addClassName("metric-caution");
            span.getStyle().set("color", "#f59e0b"); // Yellow/Orange
        } else {
            span.addClassName("metric-poor");
            span.getStyle().set("color", "#ef4444"); // Red
        }
        span.getStyle().set("font-weight", "700");
    }

    /**
     * Applies color coding to Cap Rate based on value.
     */
    private void applyCapRateColor(Span span, Double value) {
        if (value == null) return;
        span.removeClassNames("metric-excellent", "metric-good", "metric-caution", "metric-poor");
        
        if (value >= 8.0) {
            span.addClassName("metric-excellent");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value >= 6.0) {
            span.addClassName("metric-good");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value >= 4.0) {
            span.addClassName("metric-caution");
            span.getStyle().set("color", "#f59e0b"); // Yellow/Orange
        } else {
            span.addClassName("metric-poor");
            span.getStyle().set("color", "#ef4444"); // Red
        }
        span.getStyle().set("font-weight", "700");
    }

    /**
     * Applies color coding to DSCR based on value.
     */
    private void applyDSCRColor(Span span, Double value) {
        if (value == null) return;
        span.removeClassNames("metric-excellent", "metric-good", "metric-caution", "metric-poor");
        
        if (value >= 1.25) {
            span.addClassName("metric-excellent");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value >= 1.0) {
            span.addClassName("metric-caution");
            span.getStyle().set("color", "#f59e0b"); // Yellow/Orange
        } else {
            span.addClassName("metric-poor");
            span.getStyle().set("color", "#ef4444"); // Red
        }
        span.getStyle().set("font-weight", "700");
    }

    /**
     * Applies color coding to Yield metrics based on value.
     */
    private void applyYieldColor(Span span, Double value) {
        if (value == null) return;
        span.removeClassNames("metric-excellent", "metric-good", "metric-caution", "metric-poor");
        
        if (value >= 8.0) {
            span.addClassName("metric-excellent");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value >= 6.0) {
            span.addClassName("metric-good");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value >= 4.0) {
            span.addClassName("metric-caution");
            span.getStyle().set("color", "#f59e0b"); // Yellow/Orange
        } else {
            span.addClassName("metric-poor");
            span.getStyle().set("color", "#ef4444"); // Red
        }
        span.getStyle().set("font-weight", "700");
    }

    /**
     * Applies color coding to Break-Even Ratio based on value.
     * Lower is better for break-even ratio.
     */
    private void applyBreakEvenRatioColor(Span span, Double value) {
        if (value == null) return;
        span.removeClassNames("metric-excellent", "metric-good", "metric-caution", "metric-poor");
        
        if (value <= 70.0) {
            span.addClassName("metric-excellent");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value <= 80.0) {
            span.addClassName("metric-good");
            span.getStyle().set("color", "#22c55e"); // Green
        } else if (value <= 85.0) {
            span.addClassName("metric-caution");
            span.getStyle().set("color", "#f59e0b"); // Yellow/Orange
        } else {
            span.addClassName("metric-poor");
            span.getStyle().set("color", "#ef4444"); // Red
        }
        span.getStyle().set("font-weight", "700");
    }

    // ==================== Results Tab Button Handlers ====================

    /**
     * Sets up button listeners for Results tab actions.
     */
    private void setupResultsButtonListeners() {
        // Export results button
        exportResultsButton.addClickListener(e -> {
            exportResults();
        });

        // Share results button
        shareResultsButton.addClickListener(e -> {
            shareResults();
        });

        // Upgrade button (if premium features div is visible)
        if (upgradeButton != null) {
            upgradeButton.addClickListener(e -> {
                showUpgradeDialog();
            });
        }
    }

    /**
     * Exports the analysis results to PDF or CSV.
     */
    private void exportResults() {
        try {
            RentalAnalysis analysis = getEditedEntity();
            if (analysis == null) {
                notifications.create("No analysis to export")
                        .withType(Notifications.Type.WARNING)
                        .show();
                return;
            }

            // TODO: Implement actual export functionality
            // For now, show a notification
            notifications.create("Export functionality coming soon!")
                    .withType(Notifications.Type.DEFAULT)
                    .withDuration(3000)
                    .show();

            log.info("Export results requested for analysis: {}", analysis.getId());
        } catch (Exception e) {
            log.error("Error exporting results", e);
            notifications.create("Failed to export results")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Shares the analysis results via link or email.
     */
    private void shareResults() {
        try {
            RentalAnalysis analysis = getEditedEntity();
            if (analysis == null) {
                notifications.create("No analysis to share")
                        .withType(Notifications.Type.WARNING)
                        .show();
                return;
            }

            // TODO: Implement actual share functionality
            // For now, show a notification
            notifications.create("Share functionality coming soon!")
                    .withType(Notifications.Type.DEFAULT)
                    .withDuration(3000)
                    .show();

            log.info("Share results requested for analysis: {}", analysis.getId());
        } catch (Exception e) {
            log.error("Error sharing results", e);
            notifications.create("Failed to share results")
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Shows upgrade dialog for premium features.
     */
    private void showUpgradeDialog() {
        notifications.create("Premium features coming soon!")
                .withType(Notifications.Type.DEFAULT)
                .withDuration(3000)
                .show();
        
        log.info("Upgrade to premium requested");
    }

    /**
     * Checks if user should see premium features teaser.
     * For now, we'll hide it by default. In production, this would check user's subscription tier.
     */
    private void checkPremiumFeatures() {
        // TODO: Implement actual premium tier checking
        // For now, hide the premium features div
        if (premiumFeaturesDiv != null) {
            premiumFeaturesDiv.setVisible(false);
        }
    }

    // ==================== Conditional Update Helper Methods ====================
    
    /**
     * Helper method to conditionally update a BigDecimal metric field.
     * Only updates if the new value differs from the current value to avoid marking entity as dirty.
     */
    private void updateMetricIfChanged(java.util.function.Supplier<BigDecimal> getter, 
                                       java.util.function.Consumer<BigDecimal> setter, 
                                       BigDecimal newValue) {
        BigDecimal currentValue = getter.get();
        if (currentValue == null || newValue.compareTo(currentValue) != 0) {
            setter.accept(newValue);
        }
    }
    
    /**
     * Helper method to conditionally update a Double metric field.
     * Only updates if the new value differs from the current value to avoid marking entity as dirty.
     */
    private void updateMetricIfChanged(java.util.function.Supplier<Double> getter, 
                                       java.util.function.Consumer<Double> setter, 
                                       Double newValue) {
        Double currentValue = getter.get();
        if (currentValue == null || !currentValue.equals(newValue)) {
            setter.accept(newValue);
        }
    }

    // ==================== Formatting Methods ====================

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount);
    }

    private String formatPercentage(Double value) {
        if (value == null) return "0.00%";
        return String.format("%.2f%%", value);
    }

    private String formatDecimal(Double value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }

    private String formatNumber(Integer number) {
        if (number == null) return "0";
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    // ==================== Public Setters ====================

    public void setPropertySearchResult(PropertySearchResult result) {
        this.propertySearchResult = result;
    }
}