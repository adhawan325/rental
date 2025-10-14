# Property Search Feature

## Overview

The Property Search feature allows users to search for properties using the RentCast API and view comprehensive property information including details, valuations, and rental estimates. Users can then create a rental analysis directly from the search results.

## Components Created

### 1. DTO Class
**File**: `src/main/java/com/non/rental/dto/PropertySearchResult.java`

A Data Transfer Object that combines:
- `RentCastProperty` - Property details
- `RentCastValueEstimate` - Market value estimates
- `RentCastRentEstimate` - Rental income estimates

**Key Methods**:
- `calculateGrossYield()` - Calculates estimated gross yield percentage
- `getFormattedAddress()` - Returns the best available formatted address
- `hasData()`, `hasPropertyDetails()`, `hasValueEstimate()`, `hasRentEstimate()` - Validation methods

### 2. View Controller
**File**: `src/main/java/com/non/rental/view/propertysearch/PropertySearchView.java`

Main view controller that handles:
- Property search via RentCast API
- Asynchronous data fetching
- Result display in organized cards
- Navigation to analysis creation
- Error handling

**Key Features**:
- Async search with loading indicator
- Comprehensive error handling
- Currency and percentage formatting
- User-friendly notifications

### 3. XML Descriptor
**File**: `src/main/resources/com/non/rental/view/propertysearch/property-search-view.xml`

Defines the UI layout with:
- Search input section
- Loading indicator
- Error message display
- Four information cards:
  - Property Details
  - Valuation
  - Rental Information
  - Tax & Ownership
- Action buttons

### 4. Message Bundle
**File**: `src/main/resources/com/non/rental/view/propertysearch/messages.properties`

Contains all localized strings for:
- Field labels
- Button text
- Card titles
- Validation messages
- Error messages

### 5. Styling
**File**: `src/main/frontend/themes/rental/view/property-search-view.css`

Custom CSS for:
- Card hover effects
- Error message animations
- Responsive design

## User Flow

1. **Search**
   - User enters property address
   - Clicks "Search" button
   - Loading indicator appears

2. **View Results**
   - Property details displayed in organized cards
   - Value estimate with range
   - Rent estimate with range
   - Calculated gross yield
   - Tax and ownership information

3. **Create Analysis**
   - User clicks "Analyze This Property"
   - New `RentalAnalysis` entity created with:
     - Property address
     - Purchase price (from value estimate)
     - Monthly rent (from rent estimate)
     - Default vacancy rate (8%)
   - Analysis saved to database

4. **Search Again**
   - User can clear results and search for another property

## API Integration

The view integrates with three RentCast API endpoints via `RentCastService`:

1. **Property Data**: `getPropertyByAddress(address)`
   - Returns property details (bedrooms, bathrooms, square footage, etc.)

2. **Value Estimate**: `getValueEstimate(address)`
   - Returns estimated market value with range

3. **Rent Estimate**: `getRentEstimate(address)`
   - Returns estimated monthly rent with range

All API calls are made asynchronously using `CompletableFuture` to prevent UI blocking.

## Data Pre-population

When creating an analysis from search results, the following fields are automatically populated:

### RentalAnalysis
- `propertyAddress` - From search result
- `analysisName` - Generated from address
- `user` - Current authenticated user
- `status` - Set to DRAFT

### PurchaseDetails
- `purchasePrice` - From value estimate

### IncomeProjection
- `monthlyRent` - From rent estimate
- `vacancyRate` - Default 8%

## Error Handling

The view handles several error scenarios:

1. **Empty Address**: Warning notification
2. **Property Not Found**: User-friendly error message
3. **API Error**: RentCast API communication error
4. **General Error**: Catch-all for unexpected errors

All errors are logged and displayed to the user with appropriate messages.

## Menu Integration

The Property Search view is added to the application menu in `menu.xml`:

```xml
<item view="PropertySearchView" title="msg://com.non.rental.view.propertysearch/propertySearchView.title"/>
```

## Styling and UX

### Cards
- White background with subtle borders
- Box shadow for depth
- Hover effect for interactivity
- Rounded corners (8px)

### Loading State
- Indeterminate progress bar
- Disabled input fields and buttons
- Clear visual feedback

### Error Messages
- Red background with border
- Fade-in animation
- Clear, actionable text

### Responsive Design
- Maximum width of 1200px
- Centered layout
- Stacks on mobile devices

## Future Enhancements

Potential improvements for future versions:

1. **Search History**
   - Cache last 10 searches
   - Dropdown with recent addresses
   - Quick re-search functionality

2. **Address Validation**
   - Format validation before search
   - Autocomplete suggestions
   - Address disambiguation

3. **Enhanced Property Details**
   - Property photos
   - Neighborhood information
   - School ratings
   - Crime statistics

4. **Comparison Feature**
   - Compare multiple properties
   - Side-by-side view
   - Export comparison report

5. **Saved Searches**
   - Save favorite properties
   - Watch list functionality
   - Price change alerts

6. **Advanced Filtering**
   - Filter by property type
   - Price range
   - Bedrooms/bathrooms
   - Yield threshold

7. **Map Integration**
   - Show property location
   - Nearby amenities
   - Comparable properties

8. **Export Functionality**
   - PDF report generation
   - Excel export
   - Email sharing

## Testing

To test the Property Search feature:

1. **Start the application**
   ```bash
   ./gradlew bootRun
   ```

2. **Navigate to Property Search**
   - Login to the application
   - Click "Find a Property" in the menu

3. **Test Search**
   - Enter a valid US address (e.g., "123 Main St, San Francisco, CA 94102")
   - Click "Search"
   - Verify results display correctly

4. **Test Analysis Creation**
   - After successful search, click "Analyze This Property"
   - Verify analysis is created with pre-populated data

5. **Test Error Handling**
   - Try searching with empty address
   - Try searching with invalid address
   - Verify appropriate error messages

## Dependencies

The Property Search feature depends on:

- **RentCast API**: External API for property data
- **RentCastService**: Service layer for API integration
- **RentalAnalysis entities**: Domain model for analysis
- **Jmix Flow UI**: UI framework
- **Vaadin Components**: UI components

## Configuration

Ensure RentCast API is properly configured in `application.properties`:

```properties
rentcast.api.key=your-api-key-here
rentcast.api.base-url=https://api.rentcast.io/v1
```

## Security

The view is protected by Jmix security:
- Requires authenticated user
- Uses `@Route` with `MainView` layout
- Respects user permissions

## Performance Considerations

- **Async API Calls**: Prevents UI blocking
- **Caching**: RentCast service uses caching to reduce API calls
- **Lazy Loading**: Property details loaded on demand
- **Efficient Rendering**: Only visible cards are populated

## Troubleshooting

### Property Not Found
- Verify address format is correct
- Check RentCast API key is valid
- Ensure property exists in RentCast database

### API Errors
- Check network connectivity
- Verify API key has not expired
- Check RentCast API status

### UI Not Loading
- Clear browser cache
- Check browser console for errors
- Verify all files are properly deployed

## Support

For issues or questions:
1. Check application logs
2. Review RentCast API documentation
3. Contact development team