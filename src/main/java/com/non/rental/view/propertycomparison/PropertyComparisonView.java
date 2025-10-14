package com.non.rental.view.propertycomparison;

import com.non.rental.entity.*;
import com.non.rental.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Property Comparison View - allows users to compare multiple property analyses side-by-side.
 * Supports comparing 2-5 properties with color-coded metrics and visual charts.
 */
@Route(value = "property-comparison", layout = MainView.class)
@ViewController("PropertyComparisonView")
@ViewDescriptor("property-comparison-view.xml")
public class PropertyComparisonView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(PropertyComparisonView.class);
    
    private static final int FREE_TIER_MAX_PROPERTIES = 2;
    private static final int PLUS_PRO_TIER_MAX_PROPERTIES = 5;
    
    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private Notifications notifications;
    
    @Autowired
    private CurrentAuthentication currentAuthentication;
    
    @ViewComponent
    private ComboBox<RentalAnalysis> propertySelector;
    
    @ViewComponent
    private Button addPropertyButton;
    
    @ViewComponent
    private HorizontalLayout selectedPropertiesLayout;
    
    @ViewComponent
    private VerticalLayout comparisonTableContainer;
    
    @ViewComponent
    private VerticalLayout chartsContainer;
    
    @ViewComponent
    private Div winnerIndicatorDiv;
    
    @ViewComponent
    private Button exportPdfButton;
    
    @ViewComponent
    private Button exportCsvButton;
    
    @ViewComponent
    private Button shareButton;
    
    @ViewComponent
    private Div premiumUpgradeDiv;
    
    @ViewComponent
    private MessageBundle messageBundle;
    
    private List<RentalAnalysis> selectedProperties = new ArrayList<>();
    private List<RentalAnalysis> availableAnalyses = new ArrayList<>();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
    private boolean isFreeTier = true; // TODO: Implement actual tier checking
    
    @Subscribe
    public void onInit(InitEvent event) {
        log.debug("Initializing PropertyComparisonView");
        
        // Configure number formats
        currencyFormat.setMaximumFractionDigits(0);
        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMaximumFractionDigits(2);
        
        // Load user's analyses
        loadUserAnalyses();
        
        // Setup property selector
        setupPropertySelector();
        
        // Setup button listeners
        setupButtonListeners();
        
        // Check tier and show/hide premium features
        checkTierAndConfigureFeatures();
    }
    
    @Subscribe
    public void onReady(ReadyEvent event) {
        log.debug("PropertyComparisonView ready");
        updateComparisonView();
    }
    
    private void loadUserAnalyses() {
        User currentUser = (User) currentAuthentication.getUser();
        
        availableAnalyses = dataManager.load(RentalAnalysis.class)
                .query("select e from RentalAnalysis e " +
                       "where e.user = :user " +
                       "and e.status = :status " +
                       "order by e.lastModifiedDate desc")
                .parameter("user", currentUser)
                .parameter("status", AnalysisStatus.COMPLETED.getId())
                .list();
        
        log.debug("Loaded {} completed analyses for user", availableAnalyses.size());
    }
    
    private void setupPropertySelector() {
        propertySelector.setItems(availableAnalyses);
        propertySelector.setItemLabelGenerator(analysis -> 
            analysis.getAnalysisName() + " - " + analysis.getPropertyAddress());
        propertySelector.setPlaceholder(messageBundle.getMessage("selector.placeholder"));
        propertySelector.setClearButtonVisible(true);
    }
    
    private void setupButtonListeners() {
        addPropertyButton.addClickListener(e -> addSelectedProperty());
        exportPdfButton.addClickListener(e -> exportToPdf());
        exportCsvButton.addClickListener(e -> exportToCsv());
        shareButton.addClickListener(e -> shareComparison());
    }
    
    private void checkTierAndConfigureFeatures() {
        // TODO: Implement actual tier checking from user subscription
        isFreeTier = true;
        
        if (isFreeTier) {
            // Show upgrade prompt
            premiumUpgradeDiv.setVisible(true);
            exportPdfButton.setEnabled(false);
            exportPdfButton.setText(messageBundle.getMessage("button.exportPdf.premium"));
        } else {
            premiumUpgradeDiv.setVisible(false);
            exportPdfButton.setEnabled(true);
        }
    }
    
    private void addSelectedProperty() {
        RentalAnalysis selected = propertySelector.getValue();
        
        if (selected == null) {
            notifications.create(messageBundle.getMessage("validation.selectProperty"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // Check if already added
        if (selectedProperties.contains(selected)) {
            notifications.create(messageBundle.getMessage("validation.alreadyAdded"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // Check tier limits
        int maxProperties = isFreeTier ? FREE_TIER_MAX_PROPERTIES : PLUS_PRO_TIER_MAX_PROPERTIES;
        if (selectedProperties.size() >= maxProperties) {
            if (isFreeTier) {
                notifications.create(messageBundle.getMessage("validation.freeTierLimit"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            } else {
                notifications.create(messageBundle.getMessage("validation.maxPropertiesReached"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            }
            return;
        }
        
        // Load full entity with all relationships
        RentalAnalysis fullAnalysis = dataManager.load(RentalAnalysis.class)
                .id(selected.getId())
                .fetchPlan(builder -> builder.addFetchPlan("_base")
                        .add("user", "_base")
                        .add("purchaseDetails", "_base")
                        .add("financingDetails", "_base")
                        .add("incomeProjection", "_base")
                        .add("expenseProjection", "_base")
                        .add("calculatedMetrics", "_base"))
                .one();
        
        selectedProperties.add(fullAnalysis);
        propertySelector.clear();
        
        updateComparisonView();
        
        notifications.create(messageBundle.getMessage("message.propertyAdded"))
                .withType(Notifications.Type.SUCCESS)
                .show();
    }
    
    private void removeProperty(RentalAnalysis analysis) {
        selectedProperties.remove(analysis);
        updateComparisonView();
        
        notifications.create(messageBundle.getMessage("message.propertyRemoved"))
                .withType(Notifications.Type.SUCCESS)
                .show();
    }
    
    private void updateComparisonView() {
        // Update selected properties chips
        updateSelectedPropertiesChips();
        
        if (selectedProperties.size() < 2) {
            // Show instruction message
            comparisonTableContainer.removeAll();
            chartsContainer.removeAll();
            winnerIndicatorDiv.removeAll();
            
            if (selectedProperties.isEmpty()) {
                comparisonTableContainer.add(createInstructionMessage(
                    messageBundle.getMessage("instruction.selectProperties")));
            } else {
                comparisonTableContainer.add(createInstructionMessage(
                    messageBundle.getMessage("instruction.selectMoreProperties")));
            }
        } else {
            // Show comparison
            buildComparisonTable();
            buildCharts();
            buildWinnerIndicator();
        }
    }
    
    private void updateSelectedPropertiesChips() {
        selectedPropertiesLayout.removeAll();
        
        for (RentalAnalysis analysis : selectedProperties) {
            HorizontalLayout chip = createPropertyChip(analysis);
            selectedPropertiesLayout.add(chip);
        }
    }
    
    private HorizontalLayout createPropertyChip(RentalAnalysis analysis) {
        HorizontalLayout chip = new HorizontalLayout();
        chip.addClassName("property-chip");
        chip.setAlignItems(FlexComponent.Alignment.CENTER);
        chip.setSpacing(true);
        chip.setPadding(true);
        
        Span label = new Span(analysis.getAnalysisName());
        label.addClassName("chip-label");
        
        Button removeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(e -> removeProperty(analysis));
        removeButton.getElement().setAttribute("aria-label", "Remove");
        
        chip.add(label, removeButton);
        return chip;
    }
    
    private Component createInstructionMessage(String message) {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("instruction-message");
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(true);
        layout.setSpacing(true);
        
        Icon icon = new Icon(VaadinIcon.INFO_CIRCLE);
        icon.setSize("48px");
        icon.addClassName("instruction-icon");
        
        H3 text = new H3(message);
        text.addClassName("instruction-text");
        
        layout.add(icon, text);
        return layout;
    }
    
    private void buildComparisonTable() {
        comparisonTableContainer.removeAll();
        
        // Create table header
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.addClassName("comparison-header-row");
        headerRow.setWidthFull();
        headerRow.setSpacing(false);
        
        // Metric name column
        Div metricHeader = new Div();
        metricHeader.addClassName("comparison-cell");
        metricHeader.addClassName("metric-name-header");
        metricHeader.setText(messageBundle.getMessage("table.metric"));
        headerRow.add(metricHeader);
        
        // Property columns
        for (RentalAnalysis analysis : selectedProperties) {
            Div propertyHeader = new Div();
            propertyHeader.addClassName("comparison-cell");
            propertyHeader.addClassName("property-header");
            
            VerticalLayout propertyInfo = new VerticalLayout();
            propertyInfo.setSpacing(false);
            propertyInfo.setPadding(false);
            
            Span propertyName = new Span(analysis.getAnalysisName());
            propertyName.addClassName("property-name");
            Span propertyAddress = new Span(analysis.getPropertyAddress());
            propertyAddress.addClassName("property-address");
            
            propertyInfo.add(propertyName, propertyAddress);
            propertyHeader.add(propertyInfo);
            headerRow.add(propertyHeader);
        }
        
        comparisonTableContainer.add(headerRow);
        
        // Add metric rows
        addMetricRow("Purchase Price", analysis -> 
            formatCurrency(analysis.getPurchaseDetails().getPurchasePrice()), true);
        
        addMetricRow("Down Payment", analysis -> 
            formatCurrency(analysis.getPurchaseDetails().getDownPaymentAmount()), false);
        
        addMetricRow("Monthly Rent", analysis -> 
            formatCurrency(analysis.getIncomeProjection().getMonthlyRent()), true);
        
        addMetricRow("Monthly Cash Flow", analysis -> 
            formatCurrency(analysis.getCalculatedMetrics().getMonthlyCashFlow()), true);
        
        addMetricRow("Gross Yield %", analysis -> 
            formatPercent(analysis.getCalculatedMetrics().getGrossYield()), true);
        
        addMetricRow("Net Yield %", analysis -> 
            formatPercent(analysis.getCalculatedMetrics().getNetYield()), true);
        
        // Premium metrics
        if (!isFreeTier) {
            addMetricRow("Cap Rate %", analysis -> 
                formatPercent(analysis.getCalculatedMetrics().getCapRate()), true);
            
            addMetricRow("Cash-on-Cash Return %", analysis -> 
                formatPercent(analysis.getCalculatedMetrics().getCashOnCashReturn()), true);
            
            addMetricRow("ROI %", analysis -> 
                formatPercent(analysis.getCalculatedMetrics().getTotalROI()), true);
        }
        
        addMetricRow("Total Cash Invested", analysis -> 
            formatCurrency(analysis.getPurchaseDetails().getTotalCashInvested()), false);
        
        addMetricRow("Expense Ratio %", analysis -> {
            BigDecimal expenses = analysis.getExpenseProjection().getTotalMonthlyExpenses();
            BigDecimal income = analysis.getIncomeProjection().getGrossMonthlyIncome();
            if (income.compareTo(BigDecimal.ZERO) > 0) {
                double ratio = expenses.divide(income, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
                return formatPercent(ratio);
            }
            return "N/A";
        }, false);
    }
    
    private void addMetricRow(String metricName, MetricExtractor extractor, boolean highlightBestWorst) {
        HorizontalLayout row = new HorizontalLayout();
        row.addClassName("comparison-row");
        row.setWidthFull();
        row.setSpacing(false);
        
        // Metric name
        Div nameCell = new Div();
        nameCell.addClassName("comparison-cell");
        nameCell.addClassName("metric-name");
        nameCell.setText(metricName);
        row.add(nameCell);
        
        // Extract values for all properties
        List<String> values = new ArrayList<>();
        List<Double> numericValues = new ArrayList<>();
        
        for (RentalAnalysis analysis : selectedProperties) {
            String value = extractor.extract(analysis);
            values.add(value);
            
            // Try to parse numeric value for comparison
            try {
                String numericStr = value.replace("$", "").replace(",", "")
                        .replace("%", "").trim();
                if (!numericStr.equals("N/A") && !numericStr.isEmpty()) {
                    numericValues.add(Double.parseDouble(numericStr));
                } else {
                    numericValues.add(null);
                }
            } catch (NumberFormatException e) {
                numericValues.add(null);
            }
        }
        
        // Determine best and worst if highlighting is enabled
        Integer bestIndex = null;
        Integer worstIndex = null;
        
        if (highlightBestWorst && numericValues.stream().anyMatch(Objects::nonNull)) {
            // For cash flow and returns, higher is better
            // For purchase price and expenses, lower might be better (context dependent)
            boolean higherIsBetter = metricName.contains("Cash Flow") || 
                                    metricName.contains("Yield") || 
                                    metricName.contains("Return") ||
                                    metricName.contains("ROI") ||
                                    metricName.contains("Cap Rate") ||
                                    metricName.contains("Rent");
            
            Double bestValue = null;
            Double worstValue = null;
            
            for (int i = 0; i < numericValues.size(); i++) {
                Double val = numericValues.get(i);
                if (val != null) {
                    if (bestValue == null || (higherIsBetter ? val > bestValue : val < bestValue)) {
                        bestValue = val;
                        bestIndex = i;
                    }
                    if (worstValue == null || (higherIsBetter ? val < worstValue : val > worstValue)) {
                        worstValue = val;
                        worstIndex = i;
                    }
                }
            }
        }
        
        // Add value cells
        for (int i = 0; i < values.size(); i++) {
            Div valueCell = new Div();
            valueCell.addClassName("comparison-cell");
            valueCell.addClassName("metric-value");
            valueCell.setText(values.get(i));
            
            if (highlightBestWorst) {
                if (bestIndex != null && i == bestIndex) {
                    valueCell.addClassName("best-value");
                } else if (worstIndex != null && i == worstIndex) {
                    valueCell.addClassName("worst-value");
                }
            }
            
            row.add(valueCell);
        }
        
        comparisonTableContainer.add(row);
    }
    
    private void buildCharts() {
        chartsContainer.removeAll();
        
        H3 chartsTitle = new H3(messageBundle.getMessage("section.visualComparison"));
        chartsContainer.add(chartsTitle);
        
        // For now, show a placeholder message
        // In a full implementation, you would integrate with a charting library like ApexCharts or Chart.js
        Div chartPlaceholder = new Div();
        chartPlaceholder.addClassName("chart-placeholder");
        chartPlaceholder.setText(messageBundle.getMessage("message.chartsComingSoon"));
        chartPlaceholder.getStyle()
                .set("padding", "2em")
                .set("text-align", "center")
                .set("background-color", "#f5f5f5")
                .set("border", "2px dashed #ccc")
                .set("border-radius", "8px")
                .set("color", "#666");
        
        chartsContainer.add(chartPlaceholder);
        
        // TODO: Implement actual charts:
        // 1. Bar chart comparing key metrics
        // 2. Radar chart showing overall performance
        // 3. Scatter plot: Cash Flow vs Yield
    }
    
    private void buildWinnerIndicator() {
        winnerIndicatorDiv.removeAll();
        
        // Determine the best property based on multiple factors
        RentalAnalysis winner = determineWinner();
        
        if (winner != null) {
            VerticalLayout winnerLayout = new VerticalLayout();
            winnerLayout.addClassName("winner-indicator");
            winnerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            winnerLayout.setPadding(true);
            winnerLayout.setSpacing(true);
            
            Icon trophyIcon = new Icon(VaadinIcon.TROPHY);
            trophyIcon.setSize("48px");
            trophyIcon.addClassName("trophy-icon");
            
            H3 winnerTitle = new H3(messageBundle.getMessage("label.bestInvestment"));
            winnerTitle.addClassName("winner-title");
            
            H4 winnerName = new H4(winner.getAnalysisName());
            winnerName.addClassName("winner-name");
            
            Paragraph reasoning = new Paragraph(generateWinnerReasoning(winner));
            reasoning.addClassName("winner-reasoning");
            
            winnerLayout.add(trophyIcon, winnerTitle, winnerName, reasoning);
            winnerIndicatorDiv.add(winnerLayout);
        }
    }
    
    private RentalAnalysis determineWinner() {
        if (selectedProperties.isEmpty()) {
            return null;
        }
        
        // Score each property based on multiple factors
        Map<RentalAnalysis, Double> scores = new HashMap<>();
        
        for (RentalAnalysis analysis : selectedProperties) {
            double score = 0.0;
            CalculatedMetrics metrics = analysis.getCalculatedMetrics();
            
            // Cash flow (30% weight)
            if (metrics.getMonthlyCashFlow() != null) {
                score += metrics.getMonthlyCashFlow().doubleValue() * 0.3;
            }
            
            // Cash-on-Cash Return (25% weight)
            if (metrics.getCashOnCashReturn() != null) {
                score += metrics.getCashOnCashReturn() * 100 * 0.25;
            }
            
            // Cap Rate (20% weight)
            if (metrics.getCapRate() != null) {
                score += metrics.getCapRate() * 100 * 0.20;
            }
            
            // Net Yield (15% weight)
            if (metrics.getNetYield() != null) {
                score += metrics.getNetYield() * 100 * 0.15;
            }
            
            // DSCR (10% weight) - higher is better
            if (metrics.getDebtServiceCoverageRatio() != null) {
                score += metrics.getDebtServiceCoverageRatio() * 50 * 0.10;
            }
            
            scores.put(analysis, score);
        }
        
        // Return the property with the highest score
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    private String generateWinnerReasoning(RentalAnalysis winner) {
        StringBuilder reasoning = new StringBuilder();
        CalculatedMetrics metrics = winner.getCalculatedMetrics();
        
        reasoning.append("This property offers ");
        
        List<String> strengths = new ArrayList<>();
        
        if (metrics.getMonthlyCashFlow() != null && 
            metrics.getMonthlyCashFlow().compareTo(BigDecimal.ZERO) > 0) {
            strengths.add("positive monthly cash flow of " + 
                formatCurrency(metrics.getMonthlyCashFlow()));
        }
        
        if (metrics.getCashOnCashReturn() != null && metrics.getCashOnCashReturn() > 8.0) {
            strengths.add("strong cash-on-cash return of " + 
                formatPercent(metrics.getCashOnCashReturn()));
        }
        
        if (metrics.getCapRate() != null && metrics.getCapRate() > 6.0) {
            strengths.add("attractive cap rate of " + 
                formatPercent(metrics.getCapRate()));
        }
        
        if (metrics.getNetYield() != null && metrics.getNetYield() > 5.0) {
            strengths.add("solid net yield of " + 
                formatPercent(metrics.getNetYield()));
        }
        
        if (strengths.isEmpty()) {
            reasoning.append("the best overall metrics among the compared properties.");
        } else {
            reasoning.append(String.join(", ", strengths));
            reasoning.append(", making it the strongest investment option.");
        }
        
        return reasoning.toString();
    }
    
    private void exportToPdf() {
        if (isFreeTier) {
            notifications.create(messageBundle.getMessage("message.premiumFeature"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // TODO: Implement PDF export
        notifications.create(messageBundle.getMessage("message.exportPdfSuccess"))
                .withType(Notifications.Type.SUCCESS)
                .show();
    }
    
    private void exportToCsv() {
        if (selectedProperties.size() < 2) {
            notifications.create(messageBundle.getMessage("validation.needTwoProperties"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // TODO: Implement CSV export
        notifications.create(messageBundle.getMessage("message.exportCsvSuccess"))
                .withType(Notifications.Type.SUCCESS)
                .show();
    }
    
    private void shareComparison() {
        if (selectedProperties.size() < 2) {
            notifications.create(messageBundle.getMessage("validation.needTwoProperties"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // TODO: Implement share functionality
        notifications.create(messageBundle.getMessage("message.shareSuccess"))
                .withType(Notifications.Type.SUCCESS)
                .show();
    }
    
    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }
        return currencyFormat.format(value);
    }
    
    private String formatPercent(Double value) {
        if (value == null) {
            return "N/A";
        }
        return String.format("%.2f%%", value);
    }
    
    @FunctionalInterface
    private interface MetricExtractor {
        String extract(RentalAnalysis analysis);
    }
}