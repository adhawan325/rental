# RentCast Integration - UI Example

This document provides an example of how to create a Jmix Flow UI screen to use the RentCast integration.

## Example: Property Search View

### 1. Create the View Class

```java
package com.non.rental.view.rentcast;

import com.non.rental.entity.RentCastProperty;
import com.non.rental.entity.RentCastRentEstimate;
import com.non.rental.entity.RentCastValueEstimate;
import com.non.rental.exception.RentCastApiException;
import com.non.rental.service.RentCastService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "rentcast-search")
@ViewController("RentCastSearchView")
@ViewDescriptor("rentcast-search-view.xml")
public class RentCastSearchView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(RentCastSearchView.class);
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @Autowired
    private RentCastService rentCastService;

    private TextField addressField;
    private Button searchButton;
    private Button refreshButton;
    private Div resultsContainer;

    @Subscribe
    public void onInit(InitEvent event) {
        initComponents();
    }

    private void initComponents() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        // Title
        H3 title = new H3("RentCast Property Search");
        mainLayout.add(title);

        // Search form
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setWidthFull();
        searchLayout.setSpacing(true);

        addressField = new TextField("Property Address");
        addressField.setPlaceholder("e.g., 5500 Grand Lake Dr, San Antonio, TX 78244");
        addressField.setWidthFull();

        searchButton = new Button("Search");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> searchProperty(false));

        refreshButton = new Button("Force Refresh");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        refreshButton.addClickListener(e -> searchProperty(true));

        searchLayout.add(addressField, searchButton, refreshButton);
        searchLayout.setFlexGrow(1, addressField);

        mainLayout.add(searchLayout);

        // Results container
        resultsContainer = new Div();
        resultsContainer.setWidthFull();
        mainLayout.add(resultsContainer);

        getContent().add(mainLayout);
    }

    private void searchProperty(boolean forceRefresh) {
        String address = addressField.getValue();
        
        if (address == null || address.trim().isEmpty()) {
            showNotification("Please enter an address", NotificationVariant.LUMO_ERROR);
            return;
        }

        resultsContainer.removeAll();
        searchButton.setEnabled(false);
        refreshButton.setEnabled(false);

        try {
            // Fetch data
            RentCastProperty property;
            RentCastValueEstimate valueEstimate;
            RentCastRentEstimate rentEstimate;

            if (forceRefresh) {
                property = rentCastService.refreshPropertyData(address);
                valueEstimate = rentCastService.refreshValueEstimate(address);
                rentEstimate = rentCastService.refreshRentEstimate(address);
                showNotification("Data refreshed from API", NotificationVariant.LUMO_SUCCESS);
            } else {
                property = rentCastService.getPropertyByAddress(address);
                valueEstimate = rentCastService.getValueEstimate(address);
                rentEstimate = rentCastService.getRentEstimate(address);
                showNotification("Data loaded successfully", NotificationVariant.LUMO_SUCCESS);
            }

            // Display results
            displayResults(property, valueEstimate, rentEstimate);

        } catch (RentCastApiException e) {
            log.error("Error fetching property data", e);
            showNotification("Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            showNotification("Unexpected error occurred", NotificationVariant.LUMO_ERROR);
        } finally {
            searchButton.setEnabled(true);
            refreshButton.setEnabled(true);
        }
    }

    private void displayResults(RentCastProperty property, 
                               RentCastValueEstimate valueEstimate, 
                               RentCastRentEstimate rentEstimate) {
        VerticalLayout resultsLayout = new VerticalLayout();
        resultsLayout.setWidthFull();
        resultsLayout.setSpacing(true);

        // Property Information
        resultsLayout.add(createSection("Property Information", 
            createInfoRow("Address", property.getFormattedAddress()),
            createInfoRow("City", property.getCity()),
            createInfoRow("State", property.getState()),
            createInfoRow("ZIP Code", property.getZipCode()),
            createInfoRow("County", property.getCounty()),
            createInfoRow("Bedrooms", String.valueOf(property.getBedrooms())),
            createInfoRow("Bathrooms", String.valueOf(property.getBathrooms())),
            createInfoRow("Square Feet", formatNumber(property.getSquareFeet())),
            createInfoRow("Lot Size", formatNumber(property.getLotSize()) + " sq ft"),
            createInfoRow("Year Built", String.valueOf(property.getYearBuilt())),
            createInfoRow("Property Type", property.getPropertyType())
        ));

        // Value Estimate
        if (valueEstimate != null) {
            resultsLayout.add(createSection("Value Estimate",
                createInfoRow("Estimated Value", formatCurrency(valueEstimate.getPrice())),
                createInfoRow("Value Range", 
                    formatCurrency(valueEstimate.getPriceLow()) + " - " + 
                    formatCurrency(valueEstimate.getPriceHigh())),
                createInfoRow("Confidence Score", 
                    String.format("%.1f%%", valueEstimate.getConfidenceScore() * 100))
            ));
        }

        // Rent Estimate
        if (rentEstimate != null) {
            resultsLayout.add(createSection("Rent Estimate",
                createInfoRow("Estimated Rent", formatCurrency(rentEstimate.getRent()) + "/month"),
                createInfoRow("Rent Range", 
                    formatCurrency(rentEstimate.getRentLow()) + " - " + 
                    formatCurrency(rentEstimate.getRentHigh())),
                createInfoRow("Confidence Score", 
                    String.format("%.1f%%", rentEstimate.getConfidenceScore() * 100))
            ));
        }

        // Owner Information
        if (property.getOwnerName() != null) {
            resultsLayout.add(createSection("Owner Information",
                createInfoRow("Owner", property.getOwnerName()),
                createInfoRow("Owner Occupied", property.getOwnerOccupied() ? "Yes" : "No")
            ));
        }

        // Cache Information
        resultsLayout.add(createSection("Cache Information",
            createInfoRow("Data Created", property.getCreatedDate().toString()),
            createInfoRow("Last Updated", property.getLastUpdatedDate().toString()),
            createInfoRow("Cache Expires", property.getCacheExpiryDate().toString())
        ));

        resultsContainer.add(resultsLayout);
    }

    private VerticalLayout createSection(String title, HorizontalLayout... rows) {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle().set("border", "1px solid #e0e0e0");
        section.getStyle().set("border-radius", "4px");
        section.getStyle().set("background-color", "#f9f9f9");

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle().set("margin-top", "0");
        section.add(sectionTitle);

        for (HorizontalLayout row : rows) {
            section.add(row);
        }

        return section;
    }

    private HorizontalLayout createInfoRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle().set("font-weight", "bold");
        labelSpan.setWidth("200px");

        Span valueSpan = new Span(value != null ? value : "N/A");
        
        row.add(labelSpan, valueSpan);
        return row;
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }

    private String formatCurrency(Double value) {
        if (value == null) return "N/A";
        return currencyFormat.format(value);
    }

    private String formatNumber(Integer value) {
        if (value == null) return "N/A";
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }
}
```

### 2. Create the View Descriptor XML

Create `rentcast-search-view.xml` in `src/main/resources/com/non/rental/view/rentcast/`:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<view xmlns="http://jmix.io/schema/flowui/view"
      title="RentCast Property Search">
    <layout>
        <!-- Layout is created programmatically in the view class -->
    </layout>
</view>
```

### 3. Add Menu Item

Add to `menu.xml`:

```xml
<menu-item id="rentcast" title="RentCast" icon="SEARCH">
    <menu-item id="rentcastSearch" 
               title="Property Search" 
               view="RentCastSearchView"/>
</menu-item>
```

---

## Alternative: Simple REST Client Example

If you prefer to use the REST API from JavaScript/TypeScript:

```javascript
// Fetch property data
async function searchProperty(address) {
    try {
        const response = await fetch(
            `/api/rentcast/property?address=${encodeURIComponent(address)}`
        );
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const property = await response.json();
        displayProperty(property);
    } catch (error) {
        console.error('Error fetching property:', error);
        alert('Error fetching property data');
    }
}

// Fetch value estimate
async function getValueEstimate(address) {
    try {
        const response = await fetch(
            `/api/rentcast/value-estimate?address=${encodeURIComponent(address)}`
        );
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const estimate = await response.json();
        displayValueEstimate(estimate);
    } catch (error) {
        console.error('Error fetching value estimate:', error);
        alert('Error fetching value estimate');
    }
}

// Fetch rent estimate
async function getRentEstimate(address) {
    try {
        const response = await fetch(
            `/api/rentcast/rent-estimate?address=${encodeURIComponent(address)}`
        );
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const estimate = await response.json();
        displayRentEstimate(estimate);
    } catch (error) {
        console.error('Error fetching rent estimate:', error);
        alert('Error fetching rent estimate');
    }
}

// Force refresh
async function refreshProperty(address) {
    try {
        const response = await fetch(
            `/api/rentcast/property/refresh?address=${encodeURIComponent(address)}`,
            { method: 'POST' }
        );
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const property = await response.json();
        displayProperty(property);
        alert('Property data refreshed successfully');
    } catch (error) {
        console.error('Error refreshing property:', error);
        alert('Error refreshing property data');
    }
}

// Display functions
function displayProperty(property) {
    console.log('Property:', property);
    // Update UI with property data
}

function displayValueEstimate(estimate) {
    console.log('Value Estimate:', estimate);
    // Update UI with value estimate
}

function displayRentEstimate(estimate) {
    console.log('Rent Estimate:', estimate);
    // Update UI with rent estimate
}
```

---

## HTML Example

```html
<!DOCTYPE html>
<html>
<head>
    <title>RentCast Property Search</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        .search-form {
            margin-bottom: 20px;
        }
        input[type="text"] {
            width: 60%;
            padding: 10px;
            font-size: 16px;
        }
        button {
            padding: 10px 20px;
            font-size: 16px;
            cursor: pointer;
        }
        .results {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 20px;
        }
        .card {
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 20px;
            background-color: #f9f9f9;
        }
        .card h3 {
            margin-top: 0;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            margin: 10px 0;
        }
        .label {
            font-weight: bold;
        }
    </style>
</head>
<body>
    <h1>RentCast Property Search</h1>
    
    <div class="search-form">
        <input type="text" id="addressInput" 
               placeholder="Enter property address (e.g., 5500 Grand Lake Dr, San Antonio, TX 78244)">
        <button onclick="searchProperty()">Search</button>
        <button onclick="refreshProperty()">Force Refresh</button>
    </div>
    
    <div id="results" class="results"></div>
    
    <script>
        async function searchProperty() {
            const address = document.getElementById('addressInput').value;
            if (!address) {
                alert('Please enter an address');
                return;
            }
            
            try {
                // Fetch all data in parallel
                const [property, valueEstimate, rentEstimate] = await Promise.all([
                    fetch(`/api/rentcast/property?address=${encodeURIComponent(address)}`).then(r => r.json()),
                    fetch(`/api/rentcast/value-estimate?address=${encodeURIComponent(address)}`).then(r => r.json()),
                    fetch(`/api/rentcast/rent-estimate?address=${encodeURIComponent(address)}`).then(r => r.json())
                ]);
                
                displayResults(property, valueEstimate, rentEstimate);
            } catch (error) {
                console.error('Error:', error);
                alert('Error fetching property data');
            }
        }
        
        async function refreshProperty() {
            const address = document.getElementById('addressInput').value;
            if (!address) {
                alert('Please enter an address');
                return;
            }
            
            try {
                // Force refresh all data
                const [property, valueEstimate, rentEstimate] = await Promise.all([
                    fetch(`/api/rentcast/property/refresh?address=${encodeURIComponent(address)}`, 
                          { method: 'POST' }).then(r => r.json()),
                    fetch(`/api/rentcast/value-estimate/refresh?address=${encodeURIComponent(address)}`, 
                          { method: 'POST' }).then(r => r.json()),
                    fetch(`/api/rentcast/rent-estimate/refresh?address=${encodeURIComponent(address)}`, 
                          { method: 'POST' }).then(r => r.json())
                ]);
                
                displayResults(property, valueEstimate, rentEstimate);
                alert('Data refreshed successfully');
            } catch (error) {
                console.error('Error:', error);
                alert('Error refreshing property data');
            }
        }
        
        function displayResults(property, valueEstimate, rentEstimate) {
            const resultsDiv = document.getElementById('results');
            resultsDiv.innerHTML = '';
            
            // Property card
            resultsDiv.innerHTML += `
                <div class="card">
                    <h3>Property Information</h3>
                    <div class="info-row">
                        <span class="label">Address:</span>
                        <span>${property.formattedAddress}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">City:</span>
                        <span>${property.city}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">State:</span>
                        <span>${property.state}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Bedrooms:</span>
                        <span>${property.bedrooms}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Bathrooms:</span>
                        <span>${property.bathrooms}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Square Feet:</span>
                        <span>${property.squareFeet?.toLocaleString()}</span>
                    </div>
                    <div class="info-row">
                        <span class="label">Year Built:</span>
                        <span>${property.yearBuilt}</span>
                    </div>
                </div>
            `;
            
            // Value estimate card
            if (valueEstimate) {
                resultsDiv.innerHTML += `
                    <div class="card">
                        <h3>Value Estimate</h3>
                        <div class="info-row">
                            <span class="label">Estimated Value:</span>
                            <span>$${valueEstimate.price?.toLocaleString()}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">Value Range:</span>
                            <span>$${valueEstimate.priceLow?.toLocaleString()} - $${valueEstimate.priceHigh?.toLocaleString()}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">Confidence:</span>
                            <span>${(valueEstimate.confidenceScore * 100).toFixed(1)}%</span>
                        </div>
                    </div>
                `;
            }
            
            // Rent estimate card
            if (rentEstimate) {
                resultsDiv.innerHTML += `
                    <div class="card">
                        <h3>Rent Estimate</h3>
                        <div class="info-row">
                            <span class="label">Estimated Rent:</span>
                            <span>$${rentEstimate.rent?.toLocaleString()}/month</span>
                        </div>
                        <div class="info-row">
                            <span class="label">Rent Range:</span>
                            <span>$${rentEstimate.rentLow?.toLocaleString()} - $${rentEstimate.rentHigh?.toLocaleString()}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">Confidence:</span>
                            <span>${(rentEstimate.confidenceScore * 100).toFixed(1)}%</span>
                        </div>
                    </div>
                `;
            }
        }
    </script>
</body>
</html>
```

---

## Testing the UI

1. **Start the application:**
   ```bash
   ./gradlew bootRun
   ```

2. **Access the HTML example:**
   - Save the HTML file to `src/main/resources/static/rentcast-search.html`
   - Open browser: `http://localhost:8080/rentcast-search.html`

3. **Test with sample addresses:**
   - 5500 Grand Lake Dr, San Antonio, TX 78244
   - 123 Main St, Austin, TX 78701
   - 456 Oak Ave, Dallas, TX 75201

---

## Next Steps

1. **Customize the UI** to match your application's design
2. **Add validation** for address input
3. **Add loading indicators** during API calls
4. **Add error handling** with user-friendly messages
5. **Add export functionality** (PDF, Excel, etc.)
6. **Add comparison features** to compare multiple properties
7. **Add favorites/bookmarks** to save properties
8. **Add history** to track searched properties

---

This example provides a starting point for building a user interface on top of the RentCast integration. Customize it to fit your specific needs!