package com.non.rental.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Stripe payment integration.
 * Initializes the Stripe SDK with the API key.
 */
@Configuration
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    private final StripeProperties stripeProperties;

    public StripeConfig(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    /**
     * Initialize Stripe SDK with the secret API key.
     */
    @PostConstruct
    public void init() {
        String secretKey = stripeProperties.getApi().getSecretKey();
        
        if (secretKey == null || secretKey.isEmpty() || secretKey.equals("sk_test_your_test_key_here")) {
            log.warn("Stripe API key is not configured. Please set STRIPE_SECRET_KEY environment variable or stripe.api.secret-key property.");
            log.warn("Stripe integration will not work until a valid API key is provided.");
        } else {
            Stripe.apiKey = secretKey;
            log.info("Stripe SDK initialized successfully");
            log.debug("Using Stripe API key: {}...", secretKey.substring(0, Math.min(10, secretKey.length())));
        }

        // Log webhook configuration
        if (stripeProperties.getWebhook().isEnabled()) {
            String webhookSecret = stripeProperties.getWebhook().getSecret();
            if (webhookSecret != null && !webhookSecret.isEmpty() && !webhookSecret.equals("whsec_your_webhook_secret_here")) {
                log.info("Stripe webhook handling is enabled");
            } else {
                log.warn("Stripe webhook secret is not configured. Webhook signature verification will fail.");
            }
        } else {
            log.info("Stripe webhook handling is disabled");
        }

        // Log price configuration
        log.debug("Stripe Price IDs configured:");
        log.debug("  Plus Monthly: {}", stripeProperties.getPrice().getPlusMonthly());
        log.debug("  Plus Annual: {}", stripeProperties.getPrice().getPlusAnnual());
        log.debug("  Pro Monthly: {}", stripeProperties.getPrice().getProMonthly());
        log.debug("  Pro Annual: {}", stripeProperties.getPrice().getProAnnual());
    }
}