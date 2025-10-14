package com.non.rental.view.rentalanalysis;

import com.non.rental.dto.PropertySearchResult;
import com.non.rental.entity.*;
import com.non.rental.service.RentCastService;
import com.non.rental.view.main.MainView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
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
@Route(value = "rental-analysis/:id", layout = MainView.class)
@ViewController("RentalAnalysisDetailView")
@ViewDescriptor("rental-analysis-detail-view.xml")
@EditedEntityContainer("rentalAnalysisDc")
public class RentalAnalysisDetailView extends StandardDetailView<RentalAnalysis> {

    private static final Logger log = LoggerFactory.getLogger(RentalAnalysisDetailView.class);
    
    private static final double DEFAULT_CLOSING_COSTS_PERCENT = 3.0;
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

    @ViewComponent
    private MessageBundle messageBundle;

    private PropertySearchResult propertySearchResult;
    private ScheduledExecutorService autoSaveExecutor;
    private boolean isUpdatingFields = false;

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
        
        RentalAnalysis analysis = getEditedEntity();
        
        // Initialize property header
        updatePropertyHeader(analysis);
        
        // Initialize purchase details if not exists
        if (analysis.getPurchaseDetails() == null) {
            PurchaseDetails purchaseDetails = dataManager.create(PurchaseDetails.class);
            purchaseDetails.setRentalAnalysis(analysis);
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
        
        // Setup field listeners
        setupPurchaseFieldListeners();
        setupFinancingFieldListeners();
        
        // Initial calculations
        updatePurchaseSummary();
        updateFinancingCalculations();
        
        // Show purchase tab by default
        showTabContent(purchaseTab);
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

        // Update financing loan amount
        FinancingDetails financing = getEditedEntity().getFinancingDetails();
        if (financing != null) {
            financing.setLoanAmount(loanAmount);
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

    private void addAmortizationRow(Div container, String label, String value) {
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

    // ==================== Formatting Methods ====================

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount);
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