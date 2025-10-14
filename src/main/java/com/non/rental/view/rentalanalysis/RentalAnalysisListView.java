package com.non.rental.view.rentalanalysis;

import com.non.rental.entity.*;
import com.non.rental.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rental Analysis List View - displays all user's saved property analyses
 * with search, filtering, sorting, and bulk operations.
 */
@Route(value = "rental-analyses", layout = MainView.class)
@ViewController("RentalAnalysisListView")
@ViewDescriptor("rental-analysis-list-view.xml")
public class RentalAnalysisListView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(RentalAnalysisListView.class);
    
    private static final int FREE_TIER_MAX_ANALYSES = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private Notifications notifications;
    
    @Autowired
    private CurrentAuthentication currentAuthentication;
    
    @Autowired
    private Dialogs dialogs;
    
    @ViewComponent
    private TextField searchField;
    
    @ViewComponent
    private ComboBox<String> filterComboBox;
    
    @ViewComponent
    private ComboBox<String> sortComboBox;
    
    @ViewComponent
    private Button newAnalysisButton;
    
    @ViewComponent
    private Button viewToggleButton;
    
    @ViewComponent
    private VerticalLayout analysesContainer;
    
    @ViewComponent
    private Div emptyStateDiv;
    
    @ViewComponent
    private Div tierLimitDiv;
    
    @ViewComponent
    private Span analysisCountSpan;
    
    @ViewComponent
    private HorizontalLayout bulkActionsLayout;
    
    @ViewComponent
    private Button bulkDeleteButton;
    
    @ViewComponent
    private Button bulkExportButton;
    
    @ViewComponent
    private Button addToComparisonButton;
    
    @ViewComponent
    private Checkbox selectAllCheckbox;
    
    @ViewComponent
    private MessageBundle messageBundle;
    
    private List<RentalAnalysis> allAnalyses = new ArrayList<>();
    private List<RentalAnalysis> filteredAnalyses = new ArrayList<>();
    private Set<UUID> selectedAnalysisIds = new HashSet<>();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.US);
    private boolean isGridView = true;
    private boolean isFreeTier = true; // TODO: Implement actual tier checking
    
    @Subscribe
    public void onInit(InitEvent event) {
        log.debug("Initializing RentalAnalysisListView");
        
        // Configure number formats
        currencyFormat.setMaximumFractionDigits(0);
        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMaximumFractionDigits(2);
        
        // Setup components
        setupSearchField();
        setupFilterComboBox();
        setupSortComboBox();
        setupButtonListeners();
        
        // Load data
        loadUserAnalyses();
        
        // Check tier and configure features
        checkTierAndConfigureFeatures();
        
        // Initial render
        applyFiltersAndSort();
    }
    
    private void setupSearchField() {
        searchField.setPlaceholder(messageBundle.getMessage("search.placeholder"));
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFiltersAndSort());
    }
    
    private void setupFilterComboBox() {
        filterComboBox.setItems(
            "All",
            "Positive Cash Flow",
            "Negative Cash Flow",
            "High Yield",
            "Drafts"
        );
        filterComboBox.setValue("All");
        filterComboBox.addValueChangeListener(e -> applyFiltersAndSort());
    }
    
    private void setupSortComboBox() {
        sortComboBox.setItems(
            "Date (Newest First)",
            "Date (Oldest First)",
            "Name (A-Z)",
            "Name (Z-A)",
            "Cash Flow (High to Low)",
            "Cash Flow (Low to High)",
            "Yield (High to Low)",
            "Yield (Low to High)"
        );
        sortComboBox.setValue("Date (Newest First)");
        sortComboBox.addValueChangeListener(e -> applyFiltersAndSort());
    }
    
    private void setupButtonListeners() {
        newAnalysisButton.addClickListener(e -> createNewAnalysis());
        viewToggleButton.addClickListener(e -> toggleView());
        bulkDeleteButton.addClickListener(e -> bulkDelete());
        bulkExportButton.addClickListener(e -> bulkExport());
        addToComparisonButton.addClickListener(e -> addToComparison());
        selectAllCheckbox.addValueChangeListener(e -> toggleSelectAll(e.getValue()));
    }
    
    private void loadUserAnalyses() {
        User currentUser = (User) currentAuthentication.getUser();
        
        allAnalyses = dataManager.load(RentalAnalysis.class)
                .query("select e from RentalAnalysis e " +
                       "where e.user = :user " +
                       "order by e.lastModifiedDate desc")
                .parameter("user", currentUser)
                .fetchPlan(builder -> builder.addFetchPlan("_base")
                        .add("user", "_base")
                        .add("calculatedMetrics", "_base"))
                .list();
        
        log.debug("Loaded {} analyses for user", allAnalyses.size());
    }
    
    private void checkTierAndConfigureFeatures() {
        // TODO: Implement actual tier checking from user subscription
        isFreeTier = true;
        
        int analysisCount = allAnalyses.size();
        
        if (isFreeTier) {
            // Show tier limit info
            tierLimitDiv.setVisible(true);
            analysisCountSpan.setText(analysisCount + " of " + FREE_TIER_MAX_ANALYSES + " saved");
            
            // Disable new analysis button if limit reached
            if (analysisCount >= FREE_TIER_MAX_ANALYSES) {
                newAnalysisButton.setEnabled(false);
                newAnalysisButton.setText(messageBundle.getMessage("button.newAnalysis.limitReached"));
            }
            
            // Disable premium features
            bulkExportButton.setEnabled(false);
            bulkExportButton.setText(messageBundle.getMessage("button.bulkExport.premium"));
        } else {
            tierLimitDiv.setVisible(true);
            analysisCountSpan.setText(analysisCount + " saved");
            bulkExportButton.setEnabled(true);
        }
    }
    
    private void applyFiltersAndSort() {
        String searchText = searchField.getValue();
        String filter = filterComboBox.getValue();
        String sort = sortComboBox.getValue();
        
        // Apply search filter
        filteredAnalyses = allAnalyses.stream()
                .filter(analysis -> matchesSearch(analysis, searchText))
                .filter(analysis -> matchesFilter(analysis, filter))
                .collect(Collectors.toList());
        
        // Apply sorting
        applySorting(filteredAnalyses, sort);
        
        // Render
        renderAnalyses();
    }
    
    private boolean matchesSearch(RentalAnalysis analysis, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return true;
        }
        
        String lowerSearch = searchText.toLowerCase();
        return analysis.getAnalysisName().toLowerCase().contains(lowerSearch) ||
               analysis.getPropertyAddress().toLowerCase().contains(lowerSearch);
    }
    
    private boolean matchesFilter(RentalAnalysis analysis, String filter) {
        if (filter == null || filter.equals("All")) {
            return true;
        }
        
        switch (filter) {
            case "Positive Cash Flow":
                return analysis.getCalculatedMetrics() != null &&
                       analysis.getCalculatedMetrics().getMonthlyCashFlow() != null &&
                       analysis.getCalculatedMetrics().getMonthlyCashFlow().compareTo(BigDecimal.ZERO) > 0;
            
            case "Negative Cash Flow":
                return analysis.getCalculatedMetrics() != null &&
                       analysis.getCalculatedMetrics().getMonthlyCashFlow() != null &&
                       analysis.getCalculatedMetrics().getMonthlyCashFlow().compareTo(BigDecimal.ZERO) < 0;
            
            case "High Yield":
                return analysis.getCalculatedMetrics() != null &&
                       analysis.getCalculatedMetrics().getGrossYield() != null &&
                       analysis.getCalculatedMetrics().getGrossYield() >= 8.0;
            
            case "Drafts":
                return analysis.getStatus() == AnalysisStatus.DRAFT;
            
            default:
                return true;
        }
    }
    
    private void applySorting(List<RentalAnalysis> analyses, String sort) {
        if (sort == null) {
            return;
        }
        
        switch (sort) {
            case "Date (Newest First)":
                analyses.sort(Comparator.comparing(RentalAnalysis::getLastModifiedDate).reversed());
                break;
            
            case "Date (Oldest First)":
                analyses.sort(Comparator.comparing(RentalAnalysis::getLastModifiedDate));
                break;
            
            case "Name (A-Z)":
                analyses.sort(Comparator.comparing(RentalAnalysis::getAnalysisName));
                break;
            
            case "Name (Z-A)":
                analyses.sort(Comparator.comparing(RentalAnalysis::getAnalysisName).reversed());
                break;
            
            case "Cash Flow (High to Low)":
                analyses.sort((a1, a2) -> {
                    BigDecimal cf1 = a1.getCalculatedMetrics() != null ? a1.getCalculatedMetrics().getMonthlyCashFlow() : BigDecimal.ZERO;
                    BigDecimal cf2 = a2.getCalculatedMetrics() != null ? a2.getCalculatedMetrics().getMonthlyCashFlow() : BigDecimal.ZERO;
                    return cf2.compareTo(cf1);
                });
                break;
            
            case "Cash Flow (Low to High)":
                analyses.sort((a1, a2) -> {
                    BigDecimal cf1 = a1.getCalculatedMetrics() != null ? a1.getCalculatedMetrics().getMonthlyCashFlow() : BigDecimal.ZERO;
                    BigDecimal cf2 = a2.getCalculatedMetrics() != null ? a2.getCalculatedMetrics().getMonthlyCashFlow() : BigDecimal.ZERO;
                    return cf1.compareTo(cf2);
                });
                break;
            
            case "Yield (High to Low)":
                analyses.sort((a1, a2) -> {
                    Double y1 = a1.getCalculatedMetrics() != null ? a1.getCalculatedMetrics().getGrossYield() : 0.0;
                    Double y2 = a2.getCalculatedMetrics() != null ? a2.getCalculatedMetrics().getGrossYield() : 0.0;
                    return Double.compare(y2, y1);
                });
                break;
            
            case "Yield (Low to High)":
                analyses.sort((a1, a2) -> {
                    Double y1 = a1.getCalculatedMetrics() != null ? a1.getCalculatedMetrics().getGrossYield() : 0.0;
                    Double y2 = a2.getCalculatedMetrics() != null ? a2.getCalculatedMetrics().getGrossYield() : 0.0;
                    return Double.compare(y1, y2);
                });
                break;
        }
    }
    
    private void renderAnalyses() {
        analysesContainer.removeAll();
        
        if (filteredAnalyses.isEmpty()) {
            // Show empty state
            if (allAnalyses.isEmpty()) {
                emptyStateDiv.setVisible(true);
                analysesContainer.setVisible(false);
            } else {
                // No results for current filter
                analysesContainer.add(createNoResultsMessage());
                emptyStateDiv.setVisible(false);
                analysesContainer.setVisible(true);
            }
        } else {
            emptyStateDiv.setVisible(false);
            analysesContainer.setVisible(true);
            
            if (isGridView) {
                renderGridView();
            } else {
                renderListView();
            }
        }
        
        updateBulkActionsVisibility();
    }
    
    private void renderGridView() {
        // Create grid layout
        Div gridContainer = new Div();
        gridContainer.addClassName("analysis-grid");
        
        for (RentalAnalysis analysis : filteredAnalyses) {
            gridContainer.add(createAnalysisCard(analysis));
        }
        
        analysesContainer.add(gridContainer);
    }
    
    private void renderListView() {
        for (RentalAnalysis analysis : filteredAnalyses) {
            analysesContainer.add(createAnalysisListItem(analysis));
        }
    }
    
    private Component createAnalysisCard(RentalAnalysis analysis) {
        Div card = new Div();
        card.addClassName("analysis-card");
        
        // Selection checkbox
        Checkbox selectCheckbox = new Checkbox();
        selectCheckbox.setValue(selectedAnalysisIds.contains(analysis.getId()));
        selectCheckbox.addClassName("card-checkbox");
        selectCheckbox.addValueChangeListener(e -> toggleSelection(analysis.getId(), e.getValue()));
        
        // Property address (title)
        H3 addressHeading = new H3(analysis.getPropertyAddress());
        addressHeading.addClassName("card-title");
        
        // Analysis name (if different from address)
        Span analysisNameSpan = new Span(analysis.getAnalysisName());
        analysisNameSpan.addClassName("card-subtitle");
        
        // Status badge
        Span statusBadge = createStatusBadge(analysis.getStatus());
        
        // Metrics section
        Div metricsDiv = new Div();
        metricsDiv.addClassName("card-metrics");
        
        if (analysis.getCalculatedMetrics() != null) {
            CalculatedMetrics metrics = analysis.getCalculatedMetrics();
            
            // Monthly Cash Flow
            Div cashFlowDiv = createMetricItem(
                "Monthly Cash Flow",
                formatCurrency(metrics.getMonthlyCashFlow()),
                getCashFlowColor(metrics.getMonthlyCashFlow())
            );
            
            // Gross Yield
            Div grossYieldDiv = createMetricItem(
                "Gross Yield",
                formatPercent(metrics.getGrossYield()),
                "neutral"
            );
            
            // Net Yield
            Div netYieldDiv = createMetricItem(
                "Net Yield",
                formatPercent(metrics.getNetYield()),
                "neutral"
            );
            
            metricsDiv.add(cashFlowDiv, grossYieldDiv, netYieldDiv);
        } else {
            metricsDiv.add(new Span("No metrics calculated yet"));
        }
        
        // Last modified date
        Span dateSpan = new Span("Last modified: " + analysis.getLastModifiedDate().format(DATE_FORMATTER));
        dateSpan.addClassName("card-date");
        
        // Quick actions
        HorizontalLayout actionsLayout = createQuickActions(analysis);
        
        // Assemble card
        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setSpacing(true);
        cardContent.setPadding(true);
        cardContent.add(selectCheckbox, addressHeading, analysisNameSpan, statusBadge, metricsDiv, dateSpan, actionsLayout);
        
        card.add(cardContent);
        return card;
    }
    
    private Component createAnalysisListItem(RentalAnalysis analysis) {
        HorizontalLayout listItem = new HorizontalLayout();
        listItem.addClassName("analysis-list-item");
        listItem.setWidthFull();
        listItem.setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Selection checkbox
        Checkbox selectCheckbox = new Checkbox();
        selectCheckbox.setValue(selectedAnalysisIds.contains(analysis.getId()));
        selectCheckbox.addValueChangeListener(e -> toggleSelection(analysis.getId(), e.getValue()));
        
        // Info section
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setSpacing(false);
        infoLayout.setPadding(false);
        
        H4 addressHeading = new H4(analysis.getPropertyAddress());
        addressHeading.addClassName("list-title");
        
        Span analysisNameSpan = new Span(analysis.getAnalysisName());
        analysisNameSpan.addClassName("list-subtitle");
        
        infoLayout.add(addressHeading, analysisNameSpan);
        
        // Metrics section
        HorizontalLayout metricsLayout = new HorizontalLayout();
        metricsLayout.setSpacing(true);
        
        if (analysis.getCalculatedMetrics() != null) {
            CalculatedMetrics metrics = analysis.getCalculatedMetrics();
            
            Span cashFlowSpan = new Span(formatCurrency(metrics.getMonthlyCashFlow()));
            cashFlowSpan.addClassName("metric-value");
            cashFlowSpan.addClassName(getCashFlowColor(metrics.getMonthlyCashFlow()));
            
            Span grossYieldSpan = new Span(formatPercent(metrics.getGrossYield()));
            grossYieldSpan.addClassName("metric-value");
            
            Span netYieldSpan = new Span(formatPercent(metrics.getNetYield()));
            netYieldSpan.addClassName("metric-value");
            
            metricsLayout.add(cashFlowSpan, grossYieldSpan, netYieldSpan);
        }
        
        // Status and date
        VerticalLayout statusLayout = new VerticalLayout();
        statusLayout.setSpacing(false);
        statusLayout.setPadding(false);
        statusLayout.setAlignItems(FlexComponent.Alignment.END);
        
        Span statusBadge = createStatusBadge(analysis.getStatus());
        Span dateSpan = new Span(analysis.getLastModifiedDate().format(DATE_FORMATTER));
        dateSpan.addClassName("list-date");
        
        statusLayout.add(statusBadge, dateSpan);
        
        // Actions
        HorizontalLayout actionsLayout = createQuickActions(analysis);
        
        listItem.add(selectCheckbox, infoLayout, metricsLayout, statusLayout, actionsLayout);
        return listItem;
    }
    
    private Span createStatusBadge(AnalysisStatus status) {
        Span badge = new Span(status.getId());
        badge.addClassName("status-badge");
        badge.addClassName("status-" + status.getId().toLowerCase());
        return badge;
    }
    
    private Div createMetricItem(String label, String value, String colorClass) {
        Div metricDiv = new Div();
        metricDiv.addClassName("metric-item");
        
        Span labelSpan = new Span(label);
        labelSpan.addClassName("metric-label");
        
        Span valueSpan = new Span(value);
        valueSpan.addClassName("metric-value");
        valueSpan.addClassName(colorClass);
        
        metricDiv.add(labelSpan, valueSpan);
        return metricDiv;
    }
    
    private HorizontalLayout createQuickActions(RentalAnalysis analysis) {
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);
        actionsLayout.addClassName("quick-actions");
        
        Button viewButton = new Button("View", new Icon(VaadinIcon.EYE));
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        viewButton.addClickListener(e -> viewAnalysis(analysis));
        
        Button editButton = new Button("Edit", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> editAnalysis(analysis));
        
        Button duplicateButton = new Button("Duplicate", new Icon(VaadinIcon.COPY));
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        duplicateButton.addClickListener(e -> duplicateAnalysis(analysis));
        
        Button deleteButton = new Button("Delete", new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteAnalysis(analysis));
        
        Button compareButton = new Button("Compare", new Icon(VaadinIcon.SPLIT));
        compareButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        compareButton.addClickListener(e -> addToComparisonSingle(analysis));
        
        actionsLayout.add(viewButton, editButton, duplicateButton, deleteButton, compareButton);
        return actionsLayout;
    }
    
    private Component createNoResultsMessage() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("no-results-message");
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setPadding(true);
        layout.setSpacing(true);
        
        Icon icon = new Icon(VaadinIcon.SEARCH);
        icon.setSize("48px");
        
        H3 text = new H3(messageBundle.getMessage("message.noResults"));
        
        layout.add(icon, text);
        return layout;
    }
    
    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "$0";
        }
        return currencyFormat.format(value);
    }
    
    private String formatPercent(Double value) {
        if (value == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", value);
    }
    
    private String getCashFlowColor(BigDecimal cashFlow) {
        if (cashFlow == null) {
            return "neutral";
        }
        return cashFlow.compareTo(BigDecimal.ZERO) >= 0 ? "positive" : "negative";
    }
    
    private void toggleSelection(UUID analysisId, boolean selected) {
        if (selected) {
            selectedAnalysisIds.add(analysisId);
        } else {
            selectedAnalysisIds.remove(analysisId);
        }
        updateBulkActionsVisibility();
    }
    
    private void toggleSelectAll(boolean selected) {
        selectedAnalysisIds.clear();
        if (selected) {
            filteredAnalyses.forEach(analysis -> selectedAnalysisIds.add(analysis.getId()));
        }
        renderAnalyses();
    }
    
    private void updateBulkActionsVisibility() {
        boolean hasSelection = !selectedAnalysisIds.isEmpty();
        bulkActionsLayout.setVisible(hasSelection);
        
        if (hasSelection) {
            bulkDeleteButton.setText("Delete (" + selectedAnalysisIds.size() + ")");
            addToComparisonButton.setText("Compare (" + selectedAnalysisIds.size() + ")");
        }
    }
    
    private void createNewAnalysis() {
        // TODO: Navigate to property search or new analysis view
        notifications.create(messageBundle.getMessage("message.createNewAnalysis"))
                .withType(Notifications.Type.DEFAULT)
                .show();
    }
    
    private void toggleView() {
        isGridView = !isGridView;
        viewToggleButton.setIcon(new Icon(isGridView ? VaadinIcon.LIST : VaadinIcon.GRID));
        renderAnalyses();
    }
    
    private void viewAnalysis(RentalAnalysis analysis) {
        // TODO: Navigate to detail view
        notifications.create("Viewing: " + analysis.getAnalysisName())
                .withType(Notifications.Type.DEFAULT)
                .show();
    }
    
    private void editAnalysis(RentalAnalysis analysis) {
        // TODO: Navigate to edit view
        notifications.create("Editing: " + analysis.getAnalysisName())
                .withType(Notifications.Type.DEFAULT)
                .show();
    }
    
    private void duplicateAnalysis(RentalAnalysis analysis) {
        if (isFreeTier && allAnalyses.size() >= FREE_TIER_MAX_ANALYSES) {
            notifications.create(messageBundle.getMessage("validation.freeTierLimit"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // TODO: Implement duplication logic
        notifications.create("Duplicating: " + analysis.getAnalysisName())
                .withType(Notifications.Type.DEFAULT)
                .show();
    }
    
    private void deleteAnalysis(RentalAnalysis analysis) {
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("dialog.delete.title"))
                .withText(messageBundle.getMessage("dialog.delete.message"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> performDelete(analysis)),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }
    
    private void performDelete(RentalAnalysis analysis) {
        try {
            dataManager.remove(analysis);
            allAnalyses.remove(analysis);
            selectedAnalysisIds.remove(analysis.getId());
            
            notifications.create(messageBundle.getMessage("message.deleted"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            
            checkTierAndConfigureFeatures();
            applyFiltersAndSort();
        } catch (Exception e) {
            log.error("Error deleting analysis", e);
            notifications.create(messageBundle.getMessage("error.delete"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }
    
    private void bulkDelete() {
        if (selectedAnalysisIds.isEmpty()) {
            return;
        }
        
        dialogs.createOptionDialog()
                .withHeader(messageBundle.getMessage("dialog.bulkDelete.title"))
                .withText(messageBundle.getMessage("dialog.bulkDelete.message") + " (" + selectedAnalysisIds.size() + ")")
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e -> performBulkDelete()),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }
    
    private void performBulkDelete() {
        try {
            List<RentalAnalysis> toDelete = allAnalyses.stream()
                    .filter(a -> selectedAnalysisIds.contains(a.getId()))
                    .collect(Collectors.toList());
            
            for (RentalAnalysis analysis : toDelete) {
                dataManager.remove(analysis);
                allAnalyses.remove(analysis);
            }
            
            selectedAnalysisIds.clear();
            
            notifications.create(messageBundle.getMessage("message.bulkDeleted"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            
            checkTierAndConfigureFeatures();
            applyFiltersAndSort();
        } catch (Exception e) {
            log.error("Error bulk deleting analyses", e);
            notifications.create(messageBundle.getMessage("error.bulkDelete"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }
    
    private void bulkExport() {
        if (!isFreeTier) {
            // TODO: Implement bulk export
            notifications.create(messageBundle.getMessage("message.bulkExport"))
                    .withType(Notifications.Type.DEFAULT)
                    .show();
        } else {
            notifications.create(messageBundle.getMessage("validation.premiumFeature"))
                    .withType(Notifications.Type.WARNING)
                    .show();
        }
    }
    
    private void addToComparison() {
        if (selectedAnalysisIds.size() < 2) {
            notifications.create(messageBundle.getMessage("validation.selectAtLeastTwo"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // TODO: Navigate to comparison view with selected analyses
        notifications.create("Adding " + selectedAnalysisIds.size() + " analyses to comparison")
                .withType(Notifications.Type.DEFAULT)
                .show();
    }
    
    private void addToComparisonSingle(RentalAnalysis analysis) {
        // TODO: Navigate to comparison view with this analysis
        notifications.create("Adding to comparison: " + analysis.getAnalysisName())
                .withType(Notifications.Type.DEFAULT)
                .show();
    }
}