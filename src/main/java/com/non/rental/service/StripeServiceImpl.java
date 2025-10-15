package com.non.rental.service;

import com.non.rental.config.StripeProperties;
import com.non.rental.entity.*;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionUpdateParams;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of StripeService for Stripe payment integration.
 */
@Service
public class StripeServiceImpl implements StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeServiceImpl.class);

    private final StripeProperties stripeProperties;
    private final DataManager dataManager;

    public StripeServiceImpl(StripeProperties stripeProperties, DataManager dataManager) {
        this.stripeProperties = stripeProperties;
        this.dataManager = dataManager;
    }

    @Override
    @Transactional
    public Session createCheckoutSession(User user, SubscriptionPlan plan, BillingCycle billingCycle) {
        try {
            log.info("Creating checkout session for user: {}, plan: {}, cycle: {}", 
                user.getUsername(), plan, billingCycle);

            // Get or create Stripe customer
            String customerId = getOrCreateCustomer(user);

            // Get the price ID for the plan and billing cycle
            String priceId = stripeProperties.getPrice().getPriceId(plan, billingCycle);
            if (priceId == null || priceId.isEmpty()) {
                throw new IllegalArgumentException(
                    "No Stripe Price ID configured for plan: " + plan + ", cycle: " + billingCycle);
            }

            // Create checkout session parameters
            com.stripe.param.checkout.SessionCreateParams params = 
                com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .addLineItem(
                    com.stripe.param.checkout.SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build()
                )
                .setSuccessUrl(stripeProperties.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(stripeProperties.getCancelUrl())
                .putMetadata("userId", user.getId().toString())
                .putMetadata("plan", plan.getId())
                .putMetadata("billingCycle", billingCycle.getId())
                .build();

            Session session = Session.create(params);
            log.info("Checkout session created: {}", session.getId());

            return session;

        } catch (StripeException e) {
            log.error("Failed to create checkout session", e);
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String getOrCreateCustomer(User user) {
        try {
            // Check if user already has a subscription with a customer ID
            UserSubscription subscription = dataManager.load(UserSubscription.class)
                .query("select s from rental_UserSubscription s where s.user.id = :userId")
                .parameter("userId", user.getId())
                .optional()
                .orElse(null);

            if (subscription != null && subscription.getStripeCustomerId() != null) {
                log.debug("Using existing Stripe customer: {}", subscription.getStripeCustomerId());
                return subscription.getStripeCustomerId();
            }

            // Create new Stripe customer
            log.info("Creating new Stripe customer for user: {}", user.getUsername());
            
            CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getUsername())
                .putMetadata("userId", user.getId().toString())
                .build();

            Customer customer = Customer.create(params);
            log.info("Stripe customer created: {}", customer.getId());

            // Update or create subscription record with customer ID
            if (subscription == null) {
                subscription = dataManager.create(UserSubscription.class);
                subscription.setUser(user);
                subscription.setPlan(SubscriptionPlan.FREE);
                subscription.setActive(true);
            }
            subscription.setStripeCustomerId(customer.getId());
            dataManager.save(subscription);

            return customer.getId();

        } catch (StripeException e) {
            log.error("Failed to create Stripe customer", e);
            throw new RuntimeException("Failed to create Stripe customer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cancelSubscription(UserSubscription subscription) {
        try {
            if (subscription.getStripeSubscriptionId() == null) {
                throw new IllegalArgumentException("No Stripe subscription ID found");
            }

            log.info("Cancelling subscription at period end: {}", subscription.getStripeSubscriptionId());

            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());
            
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();

            stripeSubscription.update(params);

            subscription.setCancelAtPeriodEnd(true);
            dataManager.save(subscription);

            log.info("Subscription will be cancelled at period end");

        } catch (StripeException e) {
            log.error("Failed to cancel subscription", e);
            throw new RuntimeException("Failed to cancel subscription: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cancelSubscriptionImmediately(UserSubscription subscription) {
        try {
            if (subscription.getStripeSubscriptionId() == null) {
                throw new IllegalArgumentException("No Stripe subscription ID found");
            }

            log.info("Cancelling subscription immediately: {}", subscription.getStripeSubscriptionId());

            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());
            
            SubscriptionCancelParams params = SubscriptionCancelParams.builder()
                .setProrate(true)
                .build();

            stripeSubscription.cancel(params);

            subscription.setActive(false);
            subscription.setPlan(SubscriptionPlan.FREE);
            subscription.setStripeSubscriptionId(null);
            subscription.setCancelAtPeriodEnd(false);
            dataManager.save(subscription);

            log.info("Subscription cancelled immediately");

        } catch (StripeException e) {
            log.error("Failed to cancel subscription immediately", e);
            throw new RuntimeException("Failed to cancel subscription: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void reactivateSubscription(UserSubscription subscription) {
        try {
            if (subscription.getStripeSubscriptionId() == null) {
                throw new IllegalArgumentException("No Stripe subscription ID found");
            }

            log.info("Reactivating subscription: {}", subscription.getStripeSubscriptionId());

            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());
            
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(false)
                .build();

            stripeSubscription.update(params);

            subscription.setCancelAtPeriodEnd(false);
            dataManager.save(subscription);

            log.info("Subscription reactivated");

        } catch (StripeException e) {
            log.error("Failed to reactivate subscription", e);
            throw new RuntimeException("Failed to reactivate subscription: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updatePaymentMethod(UserSubscription subscription, String paymentMethodId) {
        try {
            if (subscription.getStripeSubscriptionId() == null) {
                throw new IllegalArgumentException("No Stripe subscription ID found");
            }

            log.info("Updating payment method for subscription: {}", subscription.getStripeSubscriptionId());

            // Attach payment method to customer
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            Map<String, Object> attachParams = new HashMap<>();
            attachParams.put("customer", subscription.getStripeCustomerId());
            paymentMethod.attach(attachParams);

            // Set as default payment method
            Customer customer = Customer.retrieve(subscription.getStripeCustomerId());
            Map<String, Object> updateParams = new HashMap<>();
            Map<String, Object> invoiceSettings = new HashMap<>();
            invoiceSettings.put("default_payment_method", paymentMethodId);
            updateParams.put("invoice_settings", invoiceSettings);
            customer.update(updateParams);

            // Update subscription record
            subscription.setStripePaymentMethodId(paymentMethodId);
            if (paymentMethod.getCard() != null) {
                subscription.setPaymentMethodLast4(paymentMethod.getCard().getLast4());
                subscription.setPaymentMethodBrand(paymentMethod.getCard().getBrand());
            }
            dataManager.save(subscription);

            log.info("Payment method updated successfully");

        } catch (StripeException e) {
            log.error("Failed to update payment method", e);
            throw new RuntimeException("Failed to update payment method: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (!stripeProperties.getWebhook().isEnabled()) {
            log.warn("Webhook handling is disabled");
            return;
        }

        try {
            Event event = Webhook.constructEvent(
                payload, signature, stripeProperties.getWebhook().getSecret()
            );

            log.info("Processing webhook event: {}", event.getType());

            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;
                case "customer.subscription.created":
                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event);
                    break;
                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(event);
                    break;
                case "invoice.paid":
                    handleInvoicePaid(event);
                    break;
                case "invoice.payment_failed":
                    handleInvoicePaymentFailed(event);
                    break;
                default:
                    log.debug("Unhandled webhook event type: {}", event.getType());
            }

        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            throw new RuntimeException("Invalid webhook signature", e);
        }
    }

    @Override
    @Transactional
    public void syncSubscriptionStatus(UserSubscription subscription) {
        try {
            if (subscription.getStripeSubscriptionId() == null) {
                log.warn("No Stripe subscription ID to sync");
                return;
            }

            log.info("Syncing subscription status: {}", subscription.getStripeSubscriptionId());

            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());

            // Update subscription status
            subscription.setActive("active".equals(stripeSubscription.getStatus()));
            subscription.setCancelAtPeriodEnd(stripeSubscription.getCancelAtPeriodEnd());

            // Update renewal date
            if (stripeSubscription.getCurrentPeriodEnd() != null) {
                LocalDate renewalDate = Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                subscription.setRenewalDate(renewalDate);
            }

            dataManager.save(subscription);
            log.info("Subscription status synced successfully");

        } catch (StripeException e) {
            log.error("Failed to sync subscription status", e);
            throw new RuntimeException("Failed to sync subscription status: " + e.getMessage(), e);
        }
    }

    @Override
    public String createBillingPortalSession(User user) {
        try {
            UserSubscription subscription = dataManager.load(UserSubscription.class)
                .query("select s from rental_UserSubscription s where s.user.id = :userId")
                .parameter("userId", user.getId())
                .optional()
                .orElseThrow(() -> new IllegalArgumentException("No subscription found for user"));

            if (subscription.getStripeCustomerId() == null) {
                throw new IllegalArgumentException("No Stripe customer ID found");
            }

            log.info("Creating billing portal session for customer: {}", subscription.getStripeCustomerId());

            com.stripe.param.billingportal.SessionCreateParams params = 
                com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(subscription.getStripeCustomerId())
                .setReturnUrl(stripeProperties.getSuccessUrl())
                .build();

            com.stripe.model.billingportal.Session session = 
                com.stripe.model.billingportal.Session.create(params);

            log.info("Billing portal session created: {}", session.getId());
            return session.getUrl();

        } catch (StripeException e) {
            log.error("Failed to create billing portal session", e);
            throw new RuntimeException("Failed to create billing portal session: " + e.getMessage(), e);
        }
    }

    // Private helper methods for webhook handling

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) {
            log.error("Failed to deserialize checkout session");
            return;
        }

        log.info("Checkout session completed: {}", session.getId());

        String userId = session.getMetadata().get("userId");
        String planId = session.getMetadata().get("plan");
        String billingCycleId = session.getMetadata().get("billingCycle");

        if (userId == null || planId == null || billingCycleId == null) {
            log.error("Missing metadata in checkout session");
            return;
        }

        // Update subscription
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.user.id = :userId")
            .parameter("userId", java.util.UUID.fromString(userId))
            .optional()
            .orElse(null);

        if (subscription != null) {
            subscription.setStripeSubscriptionId(session.getSubscription());
            subscription.setStripeCheckoutSessionId(session.getId());
            subscription.setPlan(SubscriptionPlan.fromId(planId));
            subscription.setBillingCycle(BillingCycle.fromId(billingCycleId));
            subscription.setActive(true);
            subscription.setStartDate(LocalDate.now());
            dataManager.save(subscription);
            log.info("Subscription updated after checkout completion");
        }
    }

    private void handleSubscriptionUpdated(Event event) {
        Subscription stripeSubscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeSubscription == null) {
            log.error("Failed to deserialize subscription");
            return;
        }

        log.info("Subscription updated: {}", stripeSubscription.getId());

        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.stripeSubscriptionId = :subId")
            .parameter("subId", stripeSubscription.getId())
            .optional()
            .orElse(null);

        if (subscription != null) {
            subscription.setActive("active".equals(stripeSubscription.getStatus()));
            subscription.setCancelAtPeriodEnd(stripeSubscription.getCancelAtPeriodEnd());
            
            if (stripeSubscription.getCurrentPeriodEnd() != null) {
                LocalDate renewalDate = Instant.ofEpochSecond(stripeSubscription.getCurrentPeriodEnd())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                subscription.setRenewalDate(renewalDate);
            }
            
            dataManager.save(subscription);
            log.info("Subscription record updated");
        }
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription stripeSubscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeSubscription == null) {
            log.error("Failed to deserialize subscription");
            return;
        }

        log.info("Subscription deleted: {}", stripeSubscription.getId());

        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.stripeSubscriptionId = :subId")
            .parameter("subId", stripeSubscription.getId())
            .optional()
            .orElse(null);

        if (subscription != null) {
            subscription.setActive(false);
            subscription.setPlan(SubscriptionPlan.FREE);
            subscription.setStripeSubscriptionId(null);
            dataManager.save(subscription);
            log.info("Subscription downgraded to FREE");
        }
    }

    private void handleInvoicePaid(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (invoice == null) {
            log.error("Failed to deserialize invoice");
            return;
        }

        log.info("Invoice paid: {}", invoice.getId());

        // Find user by customer ID
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.stripeCustomerId = :customerId")
            .parameter("customerId", invoice.getCustomer())
            .optional()
            .orElse(null);

        if (subscription != null) {
            // Create billing history record
            BillingHistory billingHistory = dataManager.create(BillingHistory.class);
            billingHistory.setUser(subscription.getUser());
            billingHistory.setBillingDate(LocalDateTime.now());
            billingHistory.setAmount(BigDecimal.valueOf(invoice.getAmountPaid() / 100.0));
            billingHistory.setDescription("Subscription payment");
            billingHistory.setPlan(subscription.getPlan());
            billingHistory.setBillingCycle(subscription.getBillingCycle());
            billingHistory.setStripeInvoiceId(invoice.getId());
            billingHistory.setStripeInvoiceUrl(invoice.getHostedInvoiceUrl());
            billingHistory.setStatus("paid");
            
            dataManager.save(billingHistory);
            log.info("Billing history record created");
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (invoice == null) {
            log.error("Failed to deserialize invoice");
            return;
        }

        log.warn("Invoice payment failed: {}", invoice.getId());

        // Find user by customer ID
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.stripeCustomerId = :customerId")
            .parameter("customerId", invoice.getCustomer())
            .optional()
            .orElse(null);

        if (subscription != null) {
            // Create billing history record with failed status
            BillingHistory billingHistory = dataManager.create(BillingHistory.class);
            billingHistory.setUser(subscription.getUser());
            billingHistory.setBillingDate(LocalDateTime.now());
            billingHistory.setAmount(BigDecimal.valueOf(invoice.getAmountDue() / 100.0));
            billingHistory.setDescription("Failed subscription payment");
            billingHistory.setPlan(subscription.getPlan());
            billingHistory.setBillingCycle(subscription.getBillingCycle());
            billingHistory.setStripeInvoiceId(invoice.getId());
            billingHistory.setStatus("failed");
            
            dataManager.save(billingHistory);
            log.info("Failed payment recorded in billing history");
        }
    }
}