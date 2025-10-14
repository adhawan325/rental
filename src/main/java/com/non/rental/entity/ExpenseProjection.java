package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing expense projections for a rental property analysis.
 * Contains all operating expenses including taxes, insurance, maintenance, and management fees.
 */
@JmixEntity
@Table(name = "EXPENSE_PROJECTION")
@Entity
public class ExpenseProjection {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RENTAL_ANALYSIS_ID", nullable = false)
    private RentalAnalysis rentalAnalysis;

    @DecimalMin(value = "0.0")
    @Column(name = "ANNUAL_PROPERTY_TAX", precision = 19, scale = 2)
    private BigDecimal annualPropertyTax;

    @DecimalMin(value = "0.0")
    @Column(name = "ANNUAL_INSURANCE", precision = 19, scale = 2)
    private BigDecimal annualInsurance;

    @DecimalMin(value = "0.0")
    @Column(name = "MONTHLY_HOA_FEES", precision = 19, scale = 2)
    private BigDecimal monthlyHOAFees;

    @DecimalMin(value = "0.0")
    @Column(name = "PROPERTY_MANAGEMENT_PERCENT")
    private Double propertyManagementPercent;

    @Column(name = "PROPERTY_MANAGEMENT_AMOUNT", precision = 19, scale = 2)
    private BigDecimal propertyManagementAmount;

    @DecimalMin(value = "0.0")
    @Column(name = "MAINTENANCE_PERCENT")
    private Double maintenancePercent;

    @Column(name = "MAINTENANCE_AMOUNT", precision = 19, scale = 2)
    private BigDecimal maintenanceAmount;

    @DecimalMin(value = "0.0")
    @Column(name = "CAP_EX_PERCENT")
    private Double capExPercent;

    @Column(name = "CAP_EX_AMOUNT", precision = 19, scale = 2)
    private BigDecimal capExAmount;

    @DecimalMin(value = "0.0")
    @Column(name = "MONTHLY_UTILITIES", precision = 19, scale = 2)
    private BigDecimal monthlyUtilities;

    @DecimalMin(value = "0.0")
    @Column(name = "OTHER_MONTHLY_EXPENSES", precision = 19, scale = 2)
    private BigDecimal otherMonthlyExpenses;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    // Helper methods for calculated fields

    /**
     * Calculates property management amount from percentage of monthly rent.
     */
    public void calculatePropertyManagementAmount(BigDecimal monthlyRent) {
        if (propertyManagementPercent != null && monthlyRent != null) {
            propertyManagementAmount = monthlyRent.multiply(
                    BigDecimal.valueOf(propertyManagementPercent / 100.0))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Calculates maintenance amount from percentage of property value.
     */
    public void calculateMaintenanceAmount(BigDecimal propertyValue) {
        if (maintenancePercent != null && propertyValue != null) {
            // Annual maintenance cost
            BigDecimal annualMaintenance = propertyValue.multiply(
                    BigDecimal.valueOf(maintenancePercent / 100.0));
            // Convert to monthly
            maintenanceAmount = annualMaintenance.divide(
                    BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Calculates capital expenditure amount from percentage of property value.
     */
    public void calculateCapExAmount(BigDecimal propertyValue) {
        if (capExPercent != null && propertyValue != null) {
            // Annual CapEx cost
            BigDecimal annualCapEx = propertyValue.multiply(
                    BigDecimal.valueOf(capExPercent / 100.0));
            // Convert to monthly
            capExAmount = annualCapEx.divide(
                    BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * Calculates the total monthly expenses.
     */
    @Transient
    public BigDecimal getTotalMonthlyExpenses() {
        BigDecimal total = BigDecimal.ZERO;

        // Property tax (annual / 12)
        if (annualPropertyTax != null) {
            total = total.add(annualPropertyTax.divide(
                    BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP));
        }

        // Insurance (annual / 12)
        if (annualInsurance != null) {
            total = total.add(annualInsurance.divide(
                    BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP));
        }

        // HOA fees
        if (monthlyHOAFees != null) {
            total = total.add(monthlyHOAFees);
        }

        // Property management
        if (propertyManagementAmount != null) {
            total = total.add(propertyManagementAmount);
        }

        // Maintenance
        if (maintenanceAmount != null) {
            total = total.add(maintenanceAmount);
        }

        // Capital expenditures
        if (capExAmount != null) {
            total = total.add(capExAmount);
        }

        // Utilities
        if (monthlyUtilities != null) {
            total = total.add(monthlyUtilities);
        }

        // Other expenses
        if (otherMonthlyExpenses != null) {
            total = total.add(otherMonthlyExpenses);
        }

        return total;
    }

    /**
     * Calculates the total annual expenses.
     */
    @Transient
    public BigDecimal getTotalAnnualExpenses() {
        return getTotalMonthlyExpenses().multiply(BigDecimal.valueOf(12));
    }

    /**
     * Gets monthly property tax.
     */
    @Transient
    public BigDecimal getMonthlyPropertyTax() {
        if (annualPropertyTax != null) {
            return annualPropertyTax.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Gets monthly insurance.
     */
    @Transient
    public BigDecimal getMonthlyInsurance() {
        if (annualInsurance != null) {
            return annualInsurance.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RentalAnalysis getRentalAnalysis() {
        return rentalAnalysis;
    }

    public void setRentalAnalysis(RentalAnalysis rentalAnalysis) {
        this.rentalAnalysis = rentalAnalysis;
    }

    public BigDecimal getAnnualPropertyTax() {
        return annualPropertyTax;
    }

    public void setAnnualPropertyTax(BigDecimal annualPropertyTax) {
        this.annualPropertyTax = annualPropertyTax;
    }

    public BigDecimal getAnnualInsurance() {
        return annualInsurance;
    }

    public void setAnnualInsurance(BigDecimal annualInsurance) {
        this.annualInsurance = annualInsurance;
    }

    public BigDecimal getMonthlyHOAFees() {
        return monthlyHOAFees;
    }

    public void setMonthlyHOAFees(BigDecimal monthlyHOAFees) {
        this.monthlyHOAFees = monthlyHOAFees;
    }

    public Double getPropertyManagementPercent() {
        return propertyManagementPercent;
    }

    public void setPropertyManagementPercent(Double propertyManagementPercent) {
        this.propertyManagementPercent = propertyManagementPercent;
    }

    public BigDecimal getPropertyManagementAmount() {
        return propertyManagementAmount;
    }

    public void setPropertyManagementAmount(BigDecimal propertyManagementAmount) {
        this.propertyManagementAmount = propertyManagementAmount;
    }

    public Double getMaintenancePercent() {
        return maintenancePercent;
    }

    public void setMaintenancePercent(Double maintenancePercent) {
        this.maintenancePercent = maintenancePercent;
    }

    public BigDecimal getMaintenanceAmount() {
        return maintenanceAmount;
    }

    public void setMaintenanceAmount(BigDecimal maintenanceAmount) {
        this.maintenanceAmount = maintenanceAmount;
    }

    public Double getCapExPercent() {
        return capExPercent;
    }

    public void setCapExPercent(Double capExPercent) {
        this.capExPercent = capExPercent;
    }

    public BigDecimal getCapExAmount() {
        return capExAmount;
    }

    public void setCapExAmount(BigDecimal capExAmount) {
        this.capExAmount = capExAmount;
    }

    public BigDecimal getMonthlyUtilities() {
        return monthlyUtilities;
    }

    public void setMonthlyUtilities(BigDecimal monthlyUtilities) {
        this.monthlyUtilities = monthlyUtilities;
    }

    public BigDecimal getOtherMonthlyExpenses() {
        return otherMonthlyExpenses;
    }

    public void setOtherMonthlyExpenses(BigDecimal otherMonthlyExpenses) {
        this.otherMonthlyExpenses = otherMonthlyExpenses;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @InstanceName
    @DependsOnProperties({"annualPropertyTax", "annualInsurance"})
    public String getInstanceName() {
        BigDecimal total = getTotalMonthlyExpenses();
        return "Expenses: $" + total + "/month";
    }
}