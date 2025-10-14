package com.non.rental.view.dashboard;

import com.non.rental.entity.*;
import com.non.rental.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
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
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Dashboard View - Home screen showing portfolio overview and quick actions.
 * Displays summary statistics, recent activity, and personalized recommendations.
 */
@Route(value = "", layout = MainView.class)
@ViewController("UserDashboardView")
@ViewDescriptor("user-dashboard-view.xml")
public class UserDashboardView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(UserDashboardView.class);
    
    private static final int FREE_TIER_MAX_ANALYSES = 3;
    private static final int RECENT_ACTIVITY_LIMIT = 5;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    
    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private Notifications notifications;
    
    @Autowired
    private CurrentAuthentication currentAuthentication;
    
    // Welcome Section
    @ViewComponent
    private Span welcomeNameSpan;
    
    @ViewComponent
    private Span currentDateSpan;
    
    // Summary Statistics Cards
    @ViewComponent
    private Span totalPropertiesSpan;
    
    @ViewComponent
    private Span totalCashFlowSpan;
    
    @ViewComponent
    private Div totalCashFlowCard;
    
    @ViewComponent
    private Span averageYieldSpan;
    
    @ViewComponent
    private Span totalInvestedSpan;
    
    @ViewComponent
    private Span bestPropertySpan;
    
    @ViewComponent
    private Button bestPropertyButton;
    
    @ViewComponent
    private Span attentionPropertySpan;
    
    @ViewComponent
    private Button attentionPropertyButton;
    
    @ViewComponent
    private Div bestPropertyCard;
    
    @ViewComponent
    private Div attentionPropertyCard;
    
    // Recent Activity
    @ViewComponent
    private VerticalLayout recentActivityList;
    
    @ViewComponent
    private Div emptyActivityDiv;
    
    // Portfolio Performance
    @ViewComponent
    private Div cashFlowChartDiv;
    
    @ViewComponent
    private Div investmentChartDiv;
    
    // Quick Actions
    @ViewComponent
    private Button analyzeNewPropertyButton;
    
    @ViewComponent
    private Button comparePropertiesButton;
    
    @ViewComponent
    private Button viewAllAnalysesButton;
    
    @ViewComponent
    private Button upgradeButton;
    
    @ViewComponent
    private Div upgradeBanner;
    
    // Recommendations
    @ViewComponent
    private VerticalLayout recommendationsList;
    
    @ViewComponent
    private MessageBundle messageBundle;
    
    private List<RentalAnalysis> userAnalyses = new ArrayList<>();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
    private boolean isFreeTier = true; // TODO: Implement actual tier checking
    
    @Subscribe
    public void onInit(InitEvent event) {
        log.debug("Initializing UserDashboardView");
        
        // Configure number formats
        currencyFormat.setMaximumFractionDigits(0);
        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMaximumFractionDigits(2);
    }
    
    @Subscribe
    public void onReady(ReadyEvent event) {
        log.debug("UserDashboardView ready");
        
        // Load user data
        loadUserAnalyses();
        
        // Render all sections
        renderWelcomeSection();
        renderSummaryStatistics();
        renderRecentActivity();
        renderPortfolioCharts();
        renderRecommendations();
        setupQuickActions();
        checkTierAndShowUpgrade();
    }
    
    private void loadUserAnalyses() {
        User currentUser = (User) currentAuthentication.getUser();
        
        userAnalyses = dataManager.load(RentalAnalysis.class)
                .query("select e from RentalAnalysis e " +
                       "where e.user = :user " +
                       "order by e.lastModifiedDate desc")
                .parameter("user", currentUser)
                .fetchPlan(builder -> builder.addFetchPlan("_base")
                        .add("user", "_base")
                        .add("purchaseDetails", "_base")
                        .add("incomeProjection", "_base")
                        .add("calculatedMetrics", "_base"))
                .list();
        
        log.debug("Loaded {} analyses for dashboard", userAnalyses.size());
    }
    
    private void renderWelcomeSection() {
        User currentUser = (User) currentAuthentication.getUser();
        String firstName = currentUser.getFirstName();
        String displayName = firstName != null && !firstName.isEmpty() ? firstName : currentUser.getUsername();
        
        welcomeNameSpan.setText(messageBundle.formatMessage("welcome.greeting", displayName));
        currentDateSpan.setText(LocalDateTime.now().format(DATE_FORMATTER));
    }
    
    private void renderSummaryStatistics() {
        // Total Properties
        int totalProperties = userAnalyses.size();
        totalPropertiesSpan.setText(String.valueOf(totalProperties));
        
        // Total Monthly Cash Flow
        BigDecimal totalCashFlow = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && a.getCalculatedMetrics().getMonthlyCashFlow() != null)
                .map(a -> a.getCalculatedMetrics().getMonthlyCashFlow())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalCashFlowSpan.setText(currencyFormat.format(totalCashFlow));
        
        // Color code cash flow card
        if (totalCashFlow.compareTo(BigDecimal.ZERO) > 0) {
            totalCashFlowCard.addClassName("positive-cash-flow");
            totalCashFlowCard.removeClassName("negative-cash-flow");
        } else if (totalCashFlow.compareTo(BigDecimal.ZERO) < 0) {
            totalCashFlowCard.addClassName("negative-cash-flow");
            totalCashFlowCard.removeClassName("positive-cash-flow");
        }
        
        // Average Gross Yield
        double averageYield = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && a.getCalculatedMetrics().getGrossYield() != null)
                .mapToDouble(a -> a.getCalculatedMetrics().getGrossYield())
                .average()
                .orElse(0.0);
        
        averageYieldSpan.setText(String.format("%.2f%%", averageYield));
        
        // Total Cash Invested
        BigDecimal totalInvested = userAnalyses.stream()
                .filter(a -> a.getPurchaseDetails() != null && a.getPurchaseDetails().getTotalCashInvested() != null)
                .map(a -> a.getPurchaseDetails().getTotalCashInvested())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalInvestedSpan.setText(currencyFormat.format(totalInvested));
        
        // Best Property (Highest Cash Flow)
        Optional<RentalAnalysis> bestProperty = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && a.getCalculatedMetrics().getMonthlyCashFlow() != null)
                .max(Comparator.comparing(a -> a.getCalculatedMetrics().getMonthlyCashFlow()));
        
        if (bestProperty.isPresent()) {
            RentalAnalysis best = bestProperty.get();
            bestPropertySpan.setText(best.getAnalysisName());
            bestPropertyButton.setVisible(true);
            bestPropertyButton.addClickListener(e -> navigateToAnalysis(best.getId()));
            bestPropertyCard.setVisible(true);
        } else {
            bestPropertyCard.setVisible(false);
        }
        
        // Property Needing Attention (Negative Cash Flow or Low DSCR)
        Optional<RentalAnalysis> attentionProperty = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null)
                .filter(a -> {
                    CalculatedMetrics metrics = a.getCalculatedMetrics();
                    boolean negativeCashFlow = metrics.getMonthlyCashFlow() != null && 
                                              metrics.getMonthlyCashFlow().compareTo(BigDecimal.ZERO) < 0;
                    boolean lowDSCR = metrics.getDebtServiceCoverageRatio() != null && 
                                     metrics.getDebtServiceCoverageRatio() < 1.25;
                    return negativeCashFlow || lowDSCR;
                })
                .findFirst();
        
        if (attentionProperty.isPresent()) {
            RentalAnalysis attention = attentionProperty.get();
            attentionPropertySpan.setText(attention.getAnalysisName());
            attentionPropertyButton.setVisible(true);
            attentionPropertyButton.addClickListener(e -> navigateToAnalysis(attention.getId()));
            attentionPropertyCard.setVisible(true);
        } else {
            attentionPropertyCard.setVisible(false);
        }
    }
    
    private void renderRecentActivity() {
        recentActivityList.removeAll();
        
        if (userAnalyses.isEmpty()) {
            emptyActivityDiv.setVisible(true);
            recentActivityList.setVisible(false);
            return;
        }
        
        emptyActivityDiv.setVisible(false);
        recentActivityList.setVisible(true);
        
        // Get last 5 activities
        List<RentalAnalysis> recentAnalyses = userAnalyses.stream()
                .limit(RECENT_ACTIVITY_LIMIT)
                .collect(Collectors.toList());
        
        for (RentalAnalysis analysis : recentAnalyses) {
            recentActivityList.add(createActivityItem(analysis));
        }
    }
    
    private Component createActivityItem(RentalAnalysis analysis) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        item.addClassName("activity-item");
        
        // Left side: Icon and details
        HorizontalLayout leftSide = new HorizontalLayout();
        leftSide.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSide.setSpacing(true);
        
        Icon icon = VaadinIcon.FILE_TEXT.create();
        icon.addClassName("activity-icon");
        
        VerticalLayout details = new VerticalLayout();
        details.setSpacing(false);
        details.setPadding(false);
        
        Span name = new Span(analysis.getAnalysisName());
        name.addClassName("activity-name");
        
        Span timestamp = new Span(messageBundle.formatMessage("activity.lastModified", 
                analysis.getLastModifiedDate().format(DATE_TIME_FORMATTER)));
        timestamp.addClassName("activity-timestamp");
        
        details.add(name, timestamp);
        leftSide.add(icon, details);
        
        // Right side: Quick action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        
        Button viewButton = new Button(messageBundle.getMessage("button.view"));
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewButton.addClickListener(e -> navigateToAnalysis(analysis.getId()));
        
        actions.add(viewButton);
        
        item.add(leftSide, actions);
        return item;
    }
    
    private void renderPortfolioCharts() {
        // Cash Flow Chart
        renderCashFlowChart();
        
        // Investment Distribution Chart
        renderInvestmentChart();
    }
    
    private void renderCashFlowChart() {
        cashFlowChartDiv.removeAll();
        
        if (userAnalyses.isEmpty()) {
            Span emptyMessage = new Span(messageBundle.getMessage("chart.noData"));
            emptyMessage.addClassName("empty-chart-message");
            cashFlowChartDiv.add(emptyMessage);
            return;
        }
        
        // Create simple bar chart visualization
        VerticalLayout chartContainer = new VerticalLayout();
        chartContainer.setWidthFull();
        chartContainer.setSpacing(true);
        chartContainer.setPadding(false);
        
        // Get top 5 properties by cash flow
        List<RentalAnalysis> topProperties = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && a.getCalculatedMetrics().getMonthlyCashFlow() != null)
                .sorted((a1, a2) -> a2.getCalculatedMetrics().getMonthlyCashFlow()
                        .compareTo(a1.getCalculatedMetrics().getMonthlyCashFlow()))
                .limit(5)
                .collect(Collectors.toList());
        
        for (RentalAnalysis analysis : topProperties) {
            chartContainer.add(createCashFlowBar(analysis));
        }
        
        cashFlowChartDiv.add(chartContainer);
    }
    
    private Component createCashFlowBar(RentalAnalysis analysis) {
        VerticalLayout barContainer = new VerticalLayout();
        barContainer.setWidthFull();
        barContainer.setSpacing(false);
        barContainer.setPadding(false);
        barContainer.addClassName("cash-flow-bar-container");
        
        // Property name and value
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        Span propertyName = new Span(analysis.getAnalysisName());
        propertyName.addClassName("chart-property-name");
        
        BigDecimal cashFlow = analysis.getCalculatedMetrics().getMonthlyCashFlow();
        Span cashFlowValue = new Span(currencyFormat.format(cashFlow));
        cashFlowValue.addClassName(cashFlow.compareTo(BigDecimal.ZERO) >= 0 ? 
                "positive-value" : "negative-value");
        
        header.add(propertyName, cashFlowValue);
        
        // Bar visualization
        Div barBackground = new Div();
        barBackground.addClassName("bar-background");
        barBackground.setWidthFull();
        
        Div bar = new Div();
        bar.addClassName(cashFlow.compareTo(BigDecimal.ZERO) >= 0 ? 
                "bar-positive" : "bar-negative");
        
        // Calculate bar width (relative to max cash flow)
        double maxCashFlow = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && a.getCalculatedMetrics().getMonthlyCashFlow() != null)
                .mapToDouble(a -> Math.abs(a.getCalculatedMetrics().getMonthlyCashFlow().doubleValue()))
                .max()
                .orElse(1.0);
        
        double barWidth = Math.abs(cashFlow.doubleValue()) / maxCashFlow * 100;
        bar.setWidth(barWidth + "%");
        
        barBackground.add(bar);
        
        barContainer.add(header, barBackground);
        return barContainer;
    }
    
    private void renderInvestmentChart() {
        investmentChartDiv.removeAll();
        
        if (userAnalyses.isEmpty()) {
            Span emptyMessage = new Span(messageBundle.getMessage("chart.noData"));
            emptyMessage.addClassName("empty-chart-message");
            investmentChartDiv.add(emptyMessage);
            return;
        }
        
        // Create simple pie chart representation
        VerticalLayout chartContainer = new VerticalLayout();
        chartContainer.setWidthFull();
        chartContainer.setSpacing(true);
        chartContainer.setPadding(false);
        
        BigDecimal totalInvestment = userAnalyses.stream()
                .filter(a -> a.getPurchaseDetails() != null && a.getPurchaseDetails().getTotalCashInvested() != null)
                .map(a -> a.getPurchaseDetails().getTotalCashInvested())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalInvestment.compareTo(BigDecimal.ZERO) == 0) {
            Span emptyMessage = new Span(messageBundle.getMessage("chart.noInvestmentData"));
            emptyMessage.addClassName("empty-chart-message");
            investmentChartDiv.add(emptyMessage);
            return;
        }
        
        for (RentalAnalysis analysis : userAnalyses) {
            if (analysis.getPurchaseDetails() != null && analysis.getPurchaseDetails().getTotalCashInvested() != null) {
                chartContainer.add(createInvestmentSegment(analysis, totalInvestment));
            }
        }
        
        investmentChartDiv.add(chartContainer);
    }
    
    private Component createInvestmentSegment(RentalAnalysis analysis, BigDecimal totalInvestment) {
        HorizontalLayout segment = new HorizontalLayout();
        segment.setWidthFull();
        segment.setAlignItems(FlexComponent.Alignment.CENTER);
        segment.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        segment.addClassName("investment-segment");
        
        BigDecimal invested = analysis.getPurchaseDetails().getTotalCashInvested();
        double percentage = invested.doubleValue() / totalInvestment.doubleValue() * 100;
        
        // Left: Color indicator and name
        HorizontalLayout leftSide = new HorizontalLayout();
        leftSide.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSide.setSpacing(true);
        
        Div colorIndicator = new Div();
        colorIndicator.addClassName("color-indicator");
        colorIndicator.setWidth("12px");
        colorIndicator.setHeight("12px");
        
        Span propertyName = new Span(analysis.getAnalysisName());
        propertyName.addClassName("segment-name");
        
        leftSide.add(colorIndicator, propertyName);
        
        // Right: Amount and percentage
        VerticalLayout rightSide = new VerticalLayout();
        rightSide.setSpacing(false);
        rightSide.setPadding(false);
        rightSide.setAlignItems(FlexComponent.Alignment.END);
        
        Span amount = new Span(currencyFormat.format(invested));
        amount.addClassName("segment-amount");
        
        Span percentSpan = new Span(String.format("%.1f%%", percentage));
        percentSpan.addClassName("segment-percentage");
        
        rightSide.add(amount, percentSpan);
        
        segment.add(leftSide, rightSide);
        return segment;
    }
    
    private void renderRecommendations() {
        recommendationsList.removeAll();
        
        List<String> recommendations = generateRecommendations();
        
        if (recommendations.isEmpty()) {
            Span noRecommendations = new Span(messageBundle.getMessage("recommendations.none"));
            noRecommendations.addClassName("no-recommendations");
            recommendationsList.add(noRecommendations);
            return;
        }
        
        for (String recommendation : recommendations) {
            recommendationsList.add(createRecommendationItem(recommendation));
        }
    }
    
    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        if (userAnalyses.isEmpty()) {
            recommendations.add(messageBundle.getMessage("recommendation.getStarted"));
            return recommendations;
        }
        
        // Check for negative cash flow properties
        long negativeCashFlowCount = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && 
                           a.getCalculatedMetrics().getMonthlyCashFlow() != null &&
                           a.getCalculatedMetrics().getMonthlyCashFlow().compareTo(BigDecimal.ZERO) < 0)
                .count();
        
        if (negativeCashFlowCount > 0) {
            recommendations.add(messageBundle.formatMessage("recommendation.negativeCashFlow", negativeCashFlowCount));
        }
        
        // Check for low DSCR
        long lowDSCRCount = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && 
                           a.getCalculatedMetrics().getDebtServiceCoverageRatio() != null &&
                           a.getCalculatedMetrics().getDebtServiceCoverageRatio() < 1.25)
                .count();
        
        if (lowDSCRCount > 0) {
            recommendations.add(messageBundle.formatMessage("recommendation.lowDSCR", lowDSCRCount));
        }
        
        // Check for high yield opportunities
        long highYieldCount = userAnalyses.stream()
                .filter(a -> a.getCalculatedMetrics() != null && 
                           a.getCalculatedMetrics().getGrossYield() != null &&
                           a.getCalculatedMetrics().getGrossYield() >= 10.0)
                .count();
        
        if (highYieldCount > 0) {
            recommendations.add(messageBundle.formatMessage("recommendation.highYield", highYieldCount));
        }
        
        // Diversification recommendation
        if (userAnalyses.size() < 3) {
            recommendations.add(messageBundle.getMessage("recommendation.diversify"));
        }
        
        // Compare properties recommendation
        if (userAnalyses.size() >= 2) {
            recommendations.add(messageBundle.getMessage("recommendation.compare"));
        }
        
        return recommendations;
    }
    
    private Component createRecommendationItem(String text) {
        HorizontalLayout item = new HorizontalLayout();
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.START);
        item.setSpacing(true);
        item.addClassName("recommendation-item");
        
        Icon icon = VaadinIcon.LIGHTBULB.create();
        icon.addClassName("recommendation-icon");
        
        Span textSpan = new Span(text);
        textSpan.addClassName("recommendation-text");
        
        item.add(icon, textSpan);
        return item;
    }
    
    private void setupQuickActions() {
        analyzeNewPropertyButton.addClickListener(e -> navigateToPropertySearch());
        comparePropertiesButton.addClickListener(e -> navigateToComparison());
        viewAllAnalysesButton.addClickListener(e -> navigateToAnalysesList());
        upgradeButton.addClickListener(e -> showUpgradeDialog());
        
        // Disable compare if less than 2 properties
        if (userAnalyses.size() < 2) {
            comparePropertiesButton.setEnabled(false);
            comparePropertiesButton.setText(messageBundle.getMessage("button.compareProperties.disabled"));
        }
    }
    
    private void checkTierAndShowUpgrade() {
        // TODO: Implement actual tier checking from user subscription
        isFreeTier = true;
        
        if (isFreeTier) {
            upgradeBanner.setVisible(true);
        } else {
            upgradeBanner.setVisible(false);
            upgradeButton.setVisible(false);
        }
    }
    
    private void navigateToAnalysis(UUID analysisId) {
        getUI().ifPresent(ui -> ui.navigate("rental-analysis/" + analysisId));
    }
    
    private void navigateToPropertySearch() {
        getUI().ifPresent(ui -> ui.navigate("property-search"));
    }
    
    private void navigateToComparison() {
        getUI().ifPresent(ui -> ui.navigate("property-comparison"));
    }
    
    private void navigateToAnalysesList() {
        getUI().ifPresent(ui -> ui.navigate("rental-analyses"));
    }
    
    private void showUpgradeDialog() {
        notifications.create(messageBundle.getMessage("upgrade.comingSoon"))
                .withType(Notifications.Type.DEFAULT)
                .show();
    }
}