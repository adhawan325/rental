package com.non.rental.service;

import com.non.rental.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.security.CurrentAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing user subscriptions.
 * Handles subscription creation, upgrades, downgrades, and cancellations.
 * 
 * Note: This is a basic implementation. In production, integrate with Stripe API.
 */
@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final DataManager dataManager;
    private final CurrentAuthentication currentAuthentication;

    public SubscriptionService(DataManager dataManager, CurrentAuthentication currentAuthentication) {
        this.dataManager = dataManager;
        this.currentAuthentication = currentAuthentication;
    }

    /**
     * Get the current user's subscription.
     * Creates a free subscription if none exists.
     */
    @Transactional
    public UserSubscription getCurrentUserSubscription() {
        User currentUser = (User) currentAuthentication.getUser();
        
        Optional<UserSubscription> subscription = dataManager.load(UserSubscription.class)
                .query("select s from rental_UserSubscription s where s.user.id = :userId")
                .parameter("userId", currentUser.getId())
                .optional();
        
        if (subscription.isPresent()) {
            return subscription.get();
        }
        
        // Create a free subscription for the user
        return createFreeSubscription(currentUser);
    }

    /**
     * Create a free subscription for a user.
     */
    @Transactional
    public UserSubscription createFreeSubscription(User user) {
        UserSubscription subscription = dataManager.create(UserSubscription.class);
        subscription.setUser(user);
        subscription.setPlan(SubscriptionPlan.FREE);
        subscription.setStartDate(LocalDate.now());
        subscription.setActive(true);
        subscription.setSavedAnalysesCount(0);
        subscription.setComparisonsCount(0);
        
        return dataManager.save(subscription);
    }

    /**
     * Upgrade or change subscription plan.
     * 
     * @param newPlan The new subscription plan
     * @param billingCycle The billing cycle (monthly or annual)
     * @return Updated subscription
     */
    @Transactional
    public UserSubscription changePlan(SubscriptionPlan newPlan, BillingCycle billingCycle) {
        UserSubscription subscription = getCurrentUserSubscription();
        SubscriptionPlan oldPlan = subscription.getPlan();
        
        log.info("Changing subscription from {} to {} for user {}", 
                oldPlan, newPlan, subscription.getUser().getUsername());
        
        subscription.setPlan(newPlan);
        subscription.setBillingCycle(billingCycle);
        
        if (newPlan != SubscriptionPlan.FREE) {
            // Set renewal date based on billing cycle
            LocalDate renewalDate = billingCycle == BillingCycle.MONTHLY 
                    ? LocalDate.now().plusMonths(1)
                    : LocalDate.now().plusYears(1);
            subscription.setRenewalDate(renewalDate);
            
            if (subscription.getStartDate() == null) {
                subscription.setStartDate(LocalDate.now());
            }
            
            // TODO: In production, integrate with Stripe to create/update subscription
            // String stripeSubscriptionId = stripeService.createSubscription(subscription);
            // subscription.setStripeSubscriptionId(stripeSubscriptionId);
            
            // Create billing history entry
            createBillingHistoryEntry(subscription, newPlan, billingCycle);
        } else {
            // Downgrading to free
            subscription.setRenewalDate(null);
            subscription.setBillingCycle(null);
            subscription.setStripeSubscriptionId(null);
        }
        
        subscription.setCancelAtPeriodEnd(false);
        subscription.setActive(true);
        
        return dataManager.save(subscription);
    }

    /**
     * Cancel subscription at the end of the billing period.
     */
    @Transactional
    public UserSubscription cancelSubscription() {
        UserSubscription subscription = getCurrentUserSubscription();
        
        log.info("Canceling subscription for user {}", subscription.getUser().getUsername());
        
        subscription.setCancelAtPeriodEnd(true);
        
        // TODO: In production, cancel Stripe subscription
        // stripeService.cancelSubscription(subscription.getStripeSubscriptionId());
        
        return dataManager.save(subscription);
    }

    /**
     * Reactivate a canceled subscription.
     */
    @Transactional
    public UserSubscription reactivateSubscription() {
        UserSubscription subscription = getCurrentUserSubscription();
        
        log.info("Reactivating subscription for user {}", subscription.getUser().getUsername());
        
        subscription.setCancelAtPeriodEnd(false);
        subscription.setActive(true);
        
        // TODO: In production, reactivate Stripe subscription
        // stripeService.reactivateSubscription(subscription.getStripeSubscriptionId());
        
        return dataManager.save(subscription);
    }

    /**
     * Update payment method.
     * 
     * @param paymentMethodId Stripe payment method ID
     * @param last4 Last 4 digits of card
     * @param brand Card brand (Visa, Mastercard, etc.)
     */
    @Transactional
    public UserSubscription updatePaymentMethod(String paymentMethodId, String last4, String brand) {
        UserSubscription subscription = getCurrentUserSubscription();
        
        log.info("Updating payment method for user {}", subscription.getUser().getUsername());
        
        subscription.setStripePaymentMethodId(paymentMethodId);
        subscription.setPaymentMethodLast4(last4);
        subscription.setPaymentMethodBrand(brand);
        
        // TODO: In production, update Stripe payment method
        // stripeService.updatePaymentMethod(subscription.getStripeCustomerId(), paymentMethodId);
        
        return dataManager.save(subscription);
    }

    /**
     * Get billing history for the current user.
     */
    public List<BillingHistory> getBillingHistory() {
        User currentUser = (User) currentAuthentication.getUser();
        
        return dataManager.load(BillingHistory.class)
                .query("select b from rental_BillingHistory b where b.user.id = :userId order by b.billingDate desc")
                .parameter("userId", currentUser.getId())
                .list();
    }

    /**
     * Create a billing history entry.
     */
    @Transactional
    public BillingHistory createBillingHistoryEntry(UserSubscription subscription, 
                                                     SubscriptionPlan plan, 
                                                     BillingCycle billingCycle) {
        BillingHistory history = dataManager.create(BillingHistory.class);
        history.setUser(subscription.getUser());
        history.setBillingDate(LocalDateTime.now());
        history.setPlan(plan);
        history.setBillingCycle(billingCycle);
        history.setStatus("paid");
        
        // Calculate amount based on plan and billing cycle
        BigDecimal amount = billingCycle == BillingCycle.MONTHLY
                ? BigDecimal.valueOf(plan.getMonthlyPrice())
                : BigDecimal.valueOf(plan.getAnnualPrice());
        history.setAmount(amount);
        
        String description = String.format("%s Plan - %s", 
                plan.getDisplayName(), 
                billingCycle.getDisplayName());
        history.setDescription(description);
        
        if (subscription.getPaymentMethodLast4() != null) {
            history.setPaymentMethodLast4(subscription.getPaymentMethodLast4());
        }
        
        return dataManager.save(history);
    }

    /**
     * Check if user has access to a feature based on their plan.
     */
    public boolean hasFeatureAccess(String featureName) {
        UserSubscription subscription = getCurrentUserSubscription();
        SubscriptionPlan plan = subscription.getPlan();
        
        // Define feature access by plan
        switch (featureName) {
            case "UNLIMITED_ANALYSES":
                return plan == SubscriptionPlan.PRO;
            case "ADVANCED_ANALYTICS":
                return plan == SubscriptionPlan.PLUS || plan == SubscriptionPlan.PRO;
            case "EXPORT_PDF":
                return plan == SubscriptionPlan.PLUS || plan == SubscriptionPlan.PRO;
            case "PROPERTY_COMPARISON":
                return plan == SubscriptionPlan.PLUS || plan == SubscriptionPlan.PRO;
            case "MARKET_INSIGHTS":
                return plan == SubscriptionPlan.PRO;
            case "PRIORITY_SUPPORT":
                return plan == SubscriptionPlan.PRO;
            default:
                return true; // Free features
        }
    }

    /**
     * Get plan features as a list.
     */
    public List<String> getPlanFeatures(SubscriptionPlan plan) {
        switch (plan) {
            case FREE:
                return List.of(
                    "Up to 3 saved analyses",
                    "Basic property search",
                    "Rent & value estimates",
                    "Basic ROI calculations",
                    "Compare up to 2 properties"
                );
            case PLUS:
                return List.of(
                    "Up to 25 saved analyses",
                    "Advanced analytics",
                    "Export to PDF",
                    "Compare up to 10 properties",
                    "Market trends",
                    "Email support"
                );
            case PRO:
                return List.of(
                    "Unlimited saved analyses",
                    "All Plus features",
                    "Unlimited comparisons",
                    "Market insights & forecasts",
                    "Custom reports",
                    "Priority support",
                    "API access"
                );
            default:
                return List.of();
        }
    }
}