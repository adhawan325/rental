package com.non.rental.security;

import com.non.rental.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Rental User Role - Grants access to all rental application features
 * without administrative privileges like user management.
 * 
 * This role allows users to:
 * - Search for properties
 * - Create and manage rental analyses
 * - Compare properties
 * - View and manage their subscription
 * - Access the dashboard
 * 
 * This role does NOT allow:
 * - Managing other users
 * - System administration
 */
@ResourceRole(name = "Rental User", code = RentalUserRole.CODE)
public interface RentalUserRole {

    String CODE = "rental-user";

    // ========== Entity Policies ==========
    
    // Rental Analysis - Full CRUD access
    @EntityPolicy(entityClass = RentalAnalysis.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = RentalAnalysis.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void rentalAnalysis();

    // Purchase Details - Full CRUD access
    @EntityPolicy(entityClass = PurchaseDetails.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = PurchaseDetails.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void purchaseDetails();

    // Financing Details - Full CRUD access
    @EntityPolicy(entityClass = FinancingDetails.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = FinancingDetails.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void financingDetails();

    // Income Projection - Full CRUD access
    @EntityPolicy(entityClass = IncomeProjection.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = IncomeProjection.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void incomeProjection();

    // Expense Projection - Full CRUD access
    @EntityPolicy(entityClass = ExpenseProjection.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = ExpenseProjection.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void expenseProjection();

    // Calculated Metrics - Full CRUD access
    @EntityPolicy(entityClass = CalculatedMetrics.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = CalculatedMetrics.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void calculatedMetrics();

    // RentCast Property - Read access (data comes from API)
    @EntityPolicy(entityClass = RentCastProperty.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = RentCastProperty.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void rentCastProperty();

    // RentCast Value Estimate - Read access (data comes from API)
    @EntityPolicy(entityClass = RentCastValueEstimate.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = RentCastValueEstimate.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void rentCastValueEstimate();

    // RentCast Rent Estimate - Read access (data comes from API)
    @EntityPolicy(entityClass = RentCastRentEstimate.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = RentCastRentEstimate.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void rentCastRentEstimate();

    // User Subscription - Read and Update access (users can view and manage their own subscription)
    @EntityPolicy(entityClass = UserSubscription.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    @EntityAttributePolicy(entityClass = UserSubscription.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    void userSubscription();

    // Subscription Plan - Read access (users can view available plans)
    @EntityPolicy(entityClass = SubscriptionPlan.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = SubscriptionPlan.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void subscriptionPlan();

    // Billing History - Read access (users can view their billing history)
    @EntityPolicy(entityClass = BillingHistory.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = BillingHistory.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void billingHistory();

    // User - Limited access (users can read and update their own profile)
    @EntityPolicy(entityClass = User.class, actions = {EntityPolicyAction.READ, EntityPolicyAction.UPDATE})
    @EntityAttributePolicy(entityClass = User.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityAttributePolicy(entityClass = User.class, attributes = {"firstName", "lastName", "email", "timeZoneId"}, action = EntityAttributePolicyAction.MODIFY)
    void user();

    // Email Verification Token - Read access
    @EntityPolicy(entityClass = EmailVerificationToken.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = EmailVerificationToken.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void emailVerificationToken();

    // ========== View Policies ==========
    
    // Main application views
    @ViewPolicy(viewIds = {"rental_MainView", "rental_LoginView"})
    void mainViews();

    // Dashboard
    @ViewPolicy(viewIds = "UserDashboardView")
    void dashboard();

    // Property Search
    @ViewPolicy(viewIds = "PropertySearchView")
    void propertySearch();

    // Rental Analysis
    @ViewPolicy(viewIds = {"RentalAnalysisListView", "RentalAnalysisDetailView"})
    void rentalAnalysisViews();

    // Property Comparison
    @ViewPolicy(viewIds = "PropertyComparisonView")
    void propertyComparison();

    // Subscription Management
    @ViewPolicy(viewIds = "SubscriptionView")
    void subscription();

    // Registration and Verification (for public access)
    @ViewPolicy(viewIds = {"rental_RegistrationView", "rental_EmailVerificationView"})
    void publicViews();

    // ========== Menu Policies ==========
    
    // Application menu (all items except user management)
    @MenuPolicy(menuIds = {
        "UserDashboardView",
        "PropertySearchView", 
        "RentalAnalysisListView",
        "PropertyComparisonView",
        "SubscriptionView"
    })
    void applicationMenu();

    // ========== Specific Policies ==========
    
    // UI access
    @SpecificPolicy(resources = "ui.loginToUi")
    void loginToUi();

    // REST API access for RentCast operations
    @SpecificPolicy(resources = {
        "rest.enabled",
        "rentcast.api.access"
    })
    void restApiAccess();
}