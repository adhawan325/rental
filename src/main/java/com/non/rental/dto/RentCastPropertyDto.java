package com.non.rental.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for RentCast Property API response.
 * Maps JSON response from /properties endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RentCastPropertyDto {

    private String id;
    private String formattedAddress;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String stateFips;
    private String zipCode;
    private String county;
    private String countyFips;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String propertyType;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer squareFootage;
    private Integer lotSize;
    private Integer yearBuilt;
    private String assessorID;
    private String legalDescription;
    private String subdivision;
    private String zoning;
    private LocalDate lastSaleDate;
    private BigDecimal lastSalePrice;
    private HoaDto hoa;
    private FeaturesDto features;
    private Map<String, TaxAssessmentDto> taxAssessments;
    private Map<String, PropertyTaxDto> propertyTaxes;
    private Map<String, HistoryEventDto> history;
    private OwnerDto owner;
    private Boolean ownerOccupied;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateFips() {
        return stateFips;
    }

    public void setStateFips(String stateFips) {
        this.stateFips = stateFips;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCountyFips() {
        return countyFips;
    }

    public void setCountyFips(String countyFips) {
        this.countyFips = countyFips;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getSquareFootage() {
        return squareFootage;
    }

    public void setSquareFootage(Integer squareFootage) {
        this.squareFootage = squareFootage;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public Integer getYearBuilt() {
        return yearBuilt;
    }

    public void setYearBuilt(Integer yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    public String getAssessorID() {
        return assessorID;
    }

    public void setAssessorID(String assessorID) {
        this.assessorID = assessorID;
    }

    public String getLegalDescription() {
        return legalDescription;
    }

    public void setLegalDescription(String legalDescription) {
        this.legalDescription = legalDescription;
    }

    public String getSubdivision() {
        return subdivision;
    }

    public void setSubdivision(String subdivision) {
        this.subdivision = subdivision;
    }

    public String getZoning() {
        return zoning;
    }

    public void setZoning(String zoning) {
        this.zoning = zoning;
    }

    public LocalDate getLastSaleDate() {
        return lastSaleDate;
    }

    public void setLastSaleDate(LocalDate lastSaleDate) {
        this.lastSaleDate = lastSaleDate;
    }

    public BigDecimal getLastSalePrice() {
        return lastSalePrice;
    }

    public void setLastSalePrice(BigDecimal lastSalePrice) {
        this.lastSalePrice = lastSalePrice;
    }

    public HoaDto getHoa() {
        return hoa;
    }

    public void setHoa(HoaDto hoa) {
        this.hoa = hoa;
    }

    public FeaturesDto getFeatures() {
        return features;
    }

    public void setFeatures(FeaturesDto features) {
        this.features = features;
    }

    public Map<String, TaxAssessmentDto> getTaxAssessments() {
        return taxAssessments;
    }

    public void setTaxAssessments(Map<String, TaxAssessmentDto> taxAssessments) {
        this.taxAssessments = taxAssessments;
    }

    public Map<String, PropertyTaxDto> getPropertyTaxes() {
        return propertyTaxes;
    }

    public void setPropertyTaxes(Map<String, PropertyTaxDto> propertyTaxes) {
        this.propertyTaxes = propertyTaxes;
    }

    public Map<String, HistoryEventDto> getHistory() {
        return history;
    }

    public void setHistory(Map<String, HistoryEventDto> history) {
        this.history = history;
    }

    public OwnerDto getOwner() {
        return owner;
    }

    public void setOwner(OwnerDto owner) {
        this.owner = owner;
    }

    public Boolean getOwnerOccupied() {
        return ownerOccupied;
    }

    public void setOwnerOccupied(Boolean ownerOccupied) {
        this.ownerOccupied = ownerOccupied;
    }

    // Nested DTOs

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HoaDto {
        private BigDecimal fee;

        public BigDecimal getFee() {
            return fee;
        }

        public void setFee(BigDecimal fee) {
            this.fee = fee;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeaturesDto {
        private String architectureType;
        private Boolean cooling;
        private String coolingType;
        private String exteriorType;
        private Boolean fireplace;
        private String fireplaceType;
        private Integer floorCount;
        private String foundationType;
        private Boolean garage;
        private Integer garageSpaces;
        private String garageType;
        private Boolean heating;
        private String heatingType;
        private Boolean pool;
        private String poolType;
        private String roofType;
        private Integer roomCount;
        private Integer unitCount;
        private String viewType;

        // Getters and Setters
        public String getArchitectureType() {
            return architectureType;
        }

        public void setArchitectureType(String architectureType) {
            this.architectureType = architectureType;
        }

        public Boolean getCooling() {
            return cooling;
        }

        public void setCooling(Boolean cooling) {
            this.cooling = cooling;
        }

        public String getCoolingType() {
            return coolingType;
        }

        public void setCoolingType(String coolingType) {
            this.coolingType = coolingType;
        }

        public String getExteriorType() {
            return exteriorType;
        }

        public void setExteriorType(String exteriorType) {
            this.exteriorType = exteriorType;
        }

        public Boolean getFireplace() {
            return fireplace;
        }

        public void setFireplace(Boolean fireplace) {
            this.fireplace = fireplace;
        }

        public String getFireplaceType() {
            return fireplaceType;
        }

        public void setFireplaceType(String fireplaceType) {
            this.fireplaceType = fireplaceType;
        }

        public Integer getFloorCount() {
            return floorCount;
        }

        public void setFloorCount(Integer floorCount) {
            this.floorCount = floorCount;
        }

        public String getFoundationType() {
            return foundationType;
        }

        public void setFoundationType(String foundationType) {
            this.foundationType = foundationType;
        }

        public Boolean getGarage() {
            return garage;
        }

        public void setGarage(Boolean garage) {
            this.garage = garage;
        }

        public Integer getGarageSpaces() {
            return garageSpaces;
        }

        public void setGarageSpaces(Integer garageSpaces) {
            this.garageSpaces = garageSpaces;
        }

        public String getGarageType() {
            return garageType;
        }

        public void setGarageType(String garageType) {
            this.garageType = garageType;
        }

        public Boolean getHeating() {
            return heating;
        }

        public void setHeating(Boolean heating) {
            this.heating = heating;
        }

        public String getHeatingType() {
            return heatingType;
        }

        public void setHeatingType(String heatingType) {
            this.heatingType = heatingType;
        }

        public Boolean getPool() {
            return pool;
        }

        public void setPool(Boolean pool) {
            this.pool = pool;
        }

        public String getPoolType() {
            return poolType;
        }

        public void setPoolType(String poolType) {
            this.poolType = poolType;
        }

        public String getRoofType() {
            return roofType;
        }

        public void setRoofType(String roofType) {
            this.roofType = roofType;
        }

        public Integer getRoomCount() {
            return roomCount;
        }

        public void setRoomCount(Integer roomCount) {
            this.roomCount = roomCount;
        }

        public Integer getUnitCount() {
            return unitCount;
        }

        public void setUnitCount(Integer unitCount) {
            this.unitCount = unitCount;
        }

        public String getViewType() {
            return viewType;
        }

        public void setViewType(String viewType) {
            this.viewType = viewType;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaxAssessmentDto {
        private Integer year;
        private BigDecimal value;
        private BigDecimal land;
        private BigDecimal improvements;

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal getLand() {
            return land;
        }

        public void setLand(BigDecimal land) {
            this.land = land;
        }

        public BigDecimal getImprovements() {
            return improvements;
        }

        public void setImprovements(BigDecimal improvements) {
            this.improvements = improvements;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropertyTaxDto {
        private Integer year;
        private BigDecimal total;

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryEventDto {
        private String event;
        private LocalDate date;
        private BigDecimal price;

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OwnerDto {
        private List<String> names;
        private String type;
        private MailingAddressDto mailingAddress;

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public MailingAddressDto getMailingAddress() {
            return mailingAddress;
        }

        public void setMailingAddress(MailingAddressDto mailingAddress) {
            this.mailingAddress = mailingAddress;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MailingAddressDto {
        private String id;
        private String formattedAddress;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String stateFips;
        private String zipCode;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFormattedAddress() {
            return formattedAddress;
        }

        public void setFormattedAddress(String formattedAddress) {
            this.formattedAddress = formattedAddress;
        }

        public String getAddressLine1() {
            return addressLine1;
        }

        public void setAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
        }

        public String getAddressLine2() {
            return addressLine2;
        }

        public void setAddressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getStateFips() {
            return stateFips;
        }

        public void setStateFips(String stateFips) {
            this.stateFips = stateFips;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
    }
}