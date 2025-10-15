package com.non.rental.service;

import com.non.rental.entity.BillingCycle;
import com.non.rental.entity.SubscriptionPlan;
import com.non.rental.entity.User;
import com.non.rental.entity.UserSubscription;
import com.stripe.model.checkout.Session;

/**
 * Service interface for Stripe payment integration.
 * Handles checkout sessions, subscriptions, customers, and webhooks.
 */
public interface StripeService {

    /**
     * Create a Stripe Checkout Session for a subscription.
     *
     * @param user          The user subscribing
     * @param plan          The subscription plan
     * @param billingCycle  The billing cycle (monthly/annual)
     * @return The Checkout Session
     */
    Session createCheckoutSession(User user, SubscriptionPlan plan, BillingCycle billingCycle);

    /**
     * Create or retrieve a Stripe Customer for a user.
     *
     * @param user The user
     * @return The Stripe Customer ID
     */
    String getOrCreateCustomer(User user);

    /**
     * Cancel a subscription at the end of the billing period.
     *
     * @param subscription The user subscription to cancel
     */
    void cancelSubscription(UserSubscription subscription);

    /**
     * Cancel a subscription immediately.
     *
     * @param subscription The user subscription to cancel
     */
    void cancelSubscriptionImmediately(UserSubscription subscription);

    /**
     * Reactivate a cancelled subscription.
     *
     * @param subscription The user subscription to reactivate
     */
    void reactivateSubscription(UserSubscription subscription);

    /**
     * Update the payment method for a subscription.
     *
     * @param subscription      The user subscription
     * @param paymentMethodId   The new Stripe Payment Method ID
     */
    void updatePaymentMethod(UserSubscription subscription, String paymentMethodId);

    /**
     * Handle a Stripe webhook event.
     *
     * @param payload   The webhook payload
     * @param signature The Stripe signature header
     */
    void handleWebhook(String payload, String signature);

    /**
     * Sync subscription status from Stripe.
     *
     * @param subscription The user subscription to sync
     */
    void syncSubscriptionStatus(UserSubscription subscription);

    /**
     * Create a billing portal session for managing subscription.
     *
     * @param user The user
     * @return The billing portal URL
     */
    String createBillingPortalSession(User user);
}