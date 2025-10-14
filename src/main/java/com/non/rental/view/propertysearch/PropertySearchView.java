package com.non.rental.view.propertysearch;

import com.non.rental.dto.PropertySearchResult;
import com.non.rental.entity.*;
import com.non.rental.exception.RentCastApiException;
import com.non.rental.service.RentCastService;
import com.non.rental.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Property Search View - allows users to search for properties using RentCast API
 * and view property details, value estimates, and rent estimates.
 */
@Route(value = "property-search", layout = MainView.class)
@ViewController("PropertySearchView")
@ViewDescriptor("property-search-view.xml")
public class PropertySearchView extends StandardView {
    
    private static final Logger log = LoggerFactory.getLogger(PropertySearchView.class);
    
    @Autowired
    private RentCastService rentCastService;
    
    @Autowired
    private Notifications notifications;
    
    @Autowired
    private ViewNavigators viewNavigators;
    
    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private CurrentAuthentication currentAuthentication;
    
    @Autowired
    private SystemAuthenticator systemAuthenticator;
    
    @ViewComponent
    private TextField addressField;
    
    @ViewComponent
    private JmixButton searchButton;
    
    @ViewComponent
    private JmixButton clearButton;
    
    @ViewComponent
    private ProgressBar loadingIndicator;
    
    @ViewComponent
    private Div errorMessageDiv;
    
    @ViewComponent
    private VerticalLayout resultsSection;
    
    @ViewComponent
    private VerticalLayout propertyDetailsCard;
    
    @ViewComponent
    private VerticalLayout valuationCard;
    
    @ViewComponent
    private VerticalLayout rentalInfoCard;
    
    @ViewComponent
    private VerticalLayout taxOwnershipCard;
    
    @ViewComponent
    private HorizontalLayout actionButtonsLayout;
    
    @ViewComponent
    private JmixButton analyzeButton;
    
    @ViewComponent
    private JmixButton searchAgainButton;
    
    @ViewComponent
    private MessageBundle messageBundle;
    
    private PropertySearchResult currentSearchResult;
    
    @Subscribe
    public void onInit(InitEvent event) {
        // Initialize view state
        hideResults();
        hideError();
        loadingIndicator.setVisible(false);
    }
    
    @Subscribe
    public void onReady(ReadyEvent event) {
        // Focus on address field when view is ready
        addressField.focus();
    }
    
    @Subscribe("searchButton")
    public void onSearchButtonClick(ClickEvent<Button> event) {
        String address = addressField.getValue();
        
        if (address == null || address.trim().isEmpty()) {
            notifications.create(messageBundle.getMessage("validation.addressRequired"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        searchProperty(address.trim());
    }
    
    @Subscribe("clearButton")
    public void onClearButtonClick(ClickEvent<Button> event) {
        addressField.clear();
        addressField.focus();
    }
    
    @Subscribe("searchAgainButton")
    public void onSearchAgainButtonClick(ClickEvent<Button> event) {
        hideResults();
        hideError();
        addressField.clear();
        addressField.focus();
    }
    
    @Subscribe("analyzeButton")
    public void onAnalyzeButtonClick(ClickEvent<Button> event) {
        if (currentSearchResult == null || !currentSearchResult.hasData()) {
            notifications.create(messageBundle.getMessage("error.noPropertyData"))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }
        
        // Create new RentalAnalysis and navigate to detail view
        createAnalysisFromSearchResult();
    }
    
    /**
     * Searches for property using RentCast API.
     */
    private void searchProperty(String address) {
        showLoading();
        hideError();
        hideResults();
        
        // Get current user for authentication context
        String currentUser = currentAuthentication.getUser().getUsername();
        
        CompletableFuture.supplyAsync(() -> {
            // Use SystemAuthenticator to provide authentication context in async thread
            return systemAuthenticator.withUser(currentUser, () -> {
                try {
                    // Fetch all data from RentCast
                    RentCastProperty property = rentCastService.getPropertyByAddress(address);
                    RentCastValueEstimate valueEstimate = rentCastService.getValueEstimate(address);
                    RentCastRentEstimate rentEstimate = rentCastService.getRentEstimate(address);
                    
                    return new PropertySearchResult(property, valueEstimate, rentEstimate);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        })
        .thenAccept(result -> {
            getUI().ifPresent(ui -> ui.access(() -> {
                hideLoading();
                if (result.hasData()) {
                    displayResults(result);
                } else {
                    showError(messageBundle.getMessage("error.propertyNotFound"));
                }
            }));
        })
        .exceptionally(throwable -> {
            getUI().ifPresent(ui -> ui.access(() -> {
                hideLoading();
                handleSearchError(throwable);
            }));
            return null;
        });
    }
    
    /**
     * Displays search results in the UI.
     */
    private void displayResults(PropertySearchResult result) {
        currentSearchResult = result;
        
        // Clear previous content
        propertyDetailsCard.removeAll();
        valuationCard.removeAll();
        rentalInfoCard.removeAll();
        taxOwnershipCard.removeAll();
        
        // Populate cards
        populatePropertyDetailsCard(result);
        populateValuationCard(result);
        populateRentalInfoCard(result);
        populateTaxOwnershipCard(result);
        
        // Show results
        resultsSection.setVisible(true);
    }
    
    /**
     * Populates the property details card.
     */
    private void populatePropertyDetailsCard(PropertySearchResult result) {
        H3 title = new H3(messageBundle.getMessage("card.propertyDetails.title"));
        propertyDetailsCard.add(title);
        
        if (result.hasPropertyDetails()) {
            RentCastProperty property = result.getProperty();
            
            addDetailRow(propertyDetailsCard, messageBundle.getMessage("field.address"), 
                    result.getFormattedAddress());
            
            if (property.getPropertyType() != null) {
                addDetailRow(propertyDetailsCard, messageBundle.getMessage("field.propertyType"), 
                        property.getPropertyType());
            }
            
            // Bedrooms / Bathrooms / Square Feet
            StringBuilder specs = new StringBuilder();
            if (property.getBedrooms() != null) {
                specs.append(property.getBedrooms()).append(" bed");
            }
            if (property.getBathrooms() != null) {
                if (specs.length() > 0) specs.append(" / ");
                specs.append(property.getBathrooms()).append(" bath");
            }
            if (property.getSquareFootage() != null) {
                if (specs.length() > 0) specs.append(" / ");
                specs.append(formatNumber(property.getSquareFootage())).append(" sq ft");
            }
            if (specs.length() > 0) {
                addDetailRow(propertyDetailsCard, messageBundle.getMessage("field.specifications"), 
                        specs.toString());
            }
            
            if (property.getYearBuilt() != null) {
                addDetailRow(propertyDetailsCard, messageBundle.getMessage("field.yearBuilt"), 
                        property.getYearBuilt().toString());
            }
            
            if (property.getLotSize() != null) {
                addDetailRow(propertyDetailsCard, messageBundle.getMessage("field.lotSize"), 
                        formatNumber(property.getLotSize()) + " sq ft");
            }
        } else {
            addDetailRow(propertyDetailsCard, messageBundle.getMessage("message.noPropertyDetails"), "");
        }
    }
    
    /**
     * Populates the valuation card.
     */
    private void populateValuationCard(PropertySearchResult result) {
        H3 title = new H3(messageBundle.getMessage("card.valuation.title"));
        valuationCard.add(title);
        
        if (result.hasValueEstimate()) {
            RentCastValueEstimate valueEstimate = result.getValueEstimate();
            
            if (valueEstimate.getPrice() != null) {
                addDetailRow(valuationCard, messageBundle.getMessage("field.estimatedValue"), 
                        formatCurrency(valueEstimate.getPrice()));
            }
            
            // Value range
            if (valueEstimate.getPriceLow() != null && valueEstimate.getPriceHigh() != null) {
                String range = formatCurrency(valueEstimate.getPriceLow()) + " - " + 
                              formatCurrency(valueEstimate.getPriceHigh());
                addDetailRow(valuationCard, messageBundle.getMessage("field.valueRange"), range);
            }
            
            // Last sale info from property
            if (result.hasPropertyDetails()) {
                RentCastProperty property = result.getProperty();
                if (property.getLastSaleDate() != null) {
                    String saleInfo = property.getLastSaleDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                    if (property.getLastSalePrice() != null) {
                        saleInfo += " - " + formatCurrency(property.getLastSalePrice());
                    }
                    addDetailRow(valuationCard, messageBundle.getMessage("field.lastSale"), saleInfo);
                }
            }
        } else {
            addDetailRow(valuationCard, messageBundle.getMessage("message.noValueEstimate"), "");
        }
    }
    
    /**
     * Populates the rental information card.
     */
    private void populateRentalInfoCard(PropertySearchResult result) {
        H3 title = new H3(messageBundle.getMessage("card.rentalInfo.title"));
        rentalInfoCard.add(title);
        
        if (result.hasRentEstimate()) {
            RentCastRentEstimate rentEstimate = result.getRentEstimate();
            
            if (rentEstimate.getRent() != null) {
                addDetailRow(rentalInfoCard, messageBundle.getMessage("field.estimatedRent"), 
                        formatCurrency(rentEstimate.getRent()) + "/month");
            }
            
            // Rent range
            if (rentEstimate.getRentLow() != null && rentEstimate.getRentHigh() != null) {
                String range = formatCurrency(rentEstimate.getRentLow()) + " - " + 
                              formatCurrency(rentEstimate.getRentHigh());
                addDetailRow(rentalInfoCard, messageBundle.getMessage("field.rentRange"), range);
            }
            
            // Calculate and display gross yield
            Double grossYield = result.calculateGrossYield();
            if (grossYield != null) {
                addDetailRow(rentalInfoCard, messageBundle.getMessage("field.grossYield"), 
                        formatPercentage(grossYield));
            }
        } else {
            addDetailRow(rentalInfoCard, messageBundle.getMessage("message.noRentEstimate"), "");
        }
    }
    
    /**
     * Populates the tax and ownership card.
     */
    private void populateTaxOwnershipCard(PropertySearchResult result) {
        H3 title = new H3(messageBundle.getMessage("card.taxOwnership.title"));
        taxOwnershipCard.add(title);
        
        if (result.hasPropertyDetails()) {
            RentCastProperty property = result.getProperty();
            boolean hasData = false;
            
            // Property tax info would come from tax assessments JSON
            // For now, we'll show what's available
            
            if (property.getOwnerNames() != null) {
                addDetailRow(taxOwnershipCard, messageBundle.getMessage("field.owner"), 
                        property.getOwnerNames());
                hasData = true;
            }
            
            if (property.getAssessorId() != null) {
                addDetailRow(taxOwnershipCard, messageBundle.getMessage("field.parcelNumber"), 
                        property.getAssessorId());
                hasData = true;
            }
            
            if (!hasData) {
                addDetailRow(taxOwnershipCard, messageBundle.getMessage("message.noTaxOwnershipData"), "");
            }
        } else {
            addDetailRow(taxOwnershipCard, messageBundle.getMessage("message.noTaxOwnershipData"), "");
        }
    }
    
    /**
     * Adds a detail row to a card.
     */
    private void addDetailRow(VerticalLayout card, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        
        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle().set("font-weight", "bold");
        labelSpan.getStyle().set("min-width", "150px");
        
        Span valueSpan = new Span(value != null ? value : "-");
        
        row.add(labelSpan, valueSpan);
        card.add(row);
    }
    
    /**
     * Creates a RentalAnalysis from the search result and navigates to it.
     */
    private void createAnalysisFromSearchResult() {
        try {
            // Create new RentalAnalysis
            RentalAnalysis analysis = dataManager.create(RentalAnalysis.class);
            
            // Get current user
            User currentUser = (User) currentAuthentication.getUser();
            analysis.setUser(currentUser);
            
            // Set property address
            analysis.setPropertyAddress(currentSearchResult.getFormattedAddress());
            
            // Set analysis name
            analysis.setAnalysisName("Analysis - " + currentSearchResult.getFormattedAddress());
            
            // Set status
            analysis.setStatus(AnalysisStatus.DRAFT);
            
            // Create and populate PurchaseDetails
            PurchaseDetails purchaseDetails = dataManager.create(PurchaseDetails.class);
            purchaseDetails.setRentalAnalysis(analysis);
            purchaseDetails.setDownPaymentPercent(25.0); // Default 25%
            purchaseDetails.setClosingCostsPercent(2.0); // Default 2%
            
            if (currentSearchResult.hasValueEstimate()) {
                BigDecimal estimatedValue = currentSearchResult.getValueEstimate().getPrice();
                if (estimatedValue != null) {
                    purchaseDetails.setPurchasePrice(estimatedValue);
                }
            }
            analysis.setPurchaseDetails(purchaseDetails);
            
            // Create and populate FinancingDetails
            FinancingDetails financingDetails = dataManager.create(FinancingDetails.class);
            financingDetails.setRentalAnalysis(analysis);
            financingDetails.setInterestRate(6.5); // Default rate
            financingDetails.setLoanTermYears(30); // Default term
            analysis.setFinancingDetails(financingDetails);
            
            // Create and populate IncomeProjection
            IncomeProjection incomeProjection = dataManager.create(IncomeProjection.class);
            incomeProjection.setRentalAnalysis(analysis);
            incomeProjection.setVacancyRate(8.0); // Default 8%
            
            if (currentSearchResult.hasRentEstimate()) {
                BigDecimal estimatedRent = currentSearchResult.getRentEstimate().getRent();
                if (estimatedRent != null) {
                    incomeProjection.setMonthlyRent(estimatedRent);
                } else {
                    incomeProjection.setMonthlyRent(BigDecimal.ZERO);
                }
            } else {
                incomeProjection.setMonthlyRent(BigDecimal.ZERO);
            }
            analysis.setIncomeProjection(incomeProjection);
            
            // Create and populate ExpenseProjection
            ExpenseProjection expenseProjection = dataManager.create(ExpenseProjection.class);
            expenseProjection.setRentalAnalysis(analysis);
            expenseProjection.setMaintenancePercent(1.0); // 1% of property value
            expenseProjection.setCapExPercent(0.5); // 0.5% of property value
            analysis.setExpenseProjection(expenseProjection);
            
            // Create CalculatedMetrics
            CalculatedMetrics calculatedMetrics = dataManager.create(CalculatedMetrics.class);
            calculatedMetrics.setRentalAnalysis(analysis);
            analysis.setCalculatedMetrics(calculatedMetrics);
            
            // Save the analysis (cascade will save all child entities)
            RentalAnalysis savedAnalysis = dataManager.save(analysis);
            
            // Reload the entity to ensure we have the latest version with all IDs and versions set
            RentalAnalysis reloadedAnalysis = dataManager.load(RentalAnalysis.class)
                    .id(savedAnalysis.getId())
                    .fetchPlan(builder -> builder.addFetchPlan(io.jmix.core.FetchPlan.BASE)
                            .add("user", io.jmix.core.FetchPlan.BASE)
                            .add("purchaseDetails", io.jmix.core.FetchPlan.BASE)
                            .add("financingDetails", io.jmix.core.FetchPlan.BASE)
                            .add("incomeProjection", io.jmix.core.FetchPlan.BASE)
                            .add("expenseProjection", io.jmix.core.FetchPlan.BASE)
                            .add("calculatedMetrics", io.jmix.core.FetchPlan.BASE))
                    .one();
            
            // Navigate to the detail view
            notifications.create(messageBundle.getMessage("message.analysisCreated"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            
            // Navigate to detail view using route
            getUI().ifPresent(ui -> ui.navigate("rental-analysis/" + reloadedAnalysis.getId()));
            
        } catch (Exception e) {
            log.error("Error creating analysis from search result", e);
            notifications.create(messageBundle.getMessage("error.createAnalysisFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }
    
    /**
     * Handles search errors.
     */
    private void handleSearchError(Throwable throwable) {
        log.error("Error searching for property", throwable);
        
        String errorMessage;
        if (throwable.getCause() instanceof RentCastApiException) {
            errorMessage = messageBundle.getMessage("error.apiError");
        } else {
            errorMessage = messageBundle.getMessage("error.searchFailed");
        }
        
        showError(errorMessage);
    }
    
    // UI Helper Methods
    
    private void showLoading() {
        loadingIndicator.setVisible(true);
        searchButton.setEnabled(false);
        clearButton.setEnabled(false);
        addressField.setEnabled(false);
    }
    
    private void hideLoading() {
        loadingIndicator.setVisible(false);
        searchButton.setEnabled(true);
        clearButton.setEnabled(true);
        addressField.setEnabled(true);
    }
    
    private void showError(String message) {
        errorMessageDiv.setText(message);
        errorMessageDiv.setVisible(true);
    }
    
    private void hideError() {
        errorMessageDiv.setVisible(false);
    }
    
    private void hideResults() {
        resultsSection.setVisible(false);
    }
    
    // Formatting Helper Methods
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "-";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount);
    }
    
    private String formatNumber(Integer number) {
        if (number == null) return "-";
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }
    
    private String formatPercentage(Double percentage) {
        if (percentage == null) return "-";
        return String.format("%.2f%%", percentage);
    }
}