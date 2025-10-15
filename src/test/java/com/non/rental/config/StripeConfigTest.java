package com.non.rental.config;

import com.stripe.Stripe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Stripe configuration.
 */
@SpringBootTest
class StripeConfigTest {

    @Autowired
    private StripeProperties stripeProperties;

    @Autowired
    private StripeConfig stripeConfig;

    @Test
    void testStripePropertiesLoaded() {
        assertNotNull(stripeProperties, "StripeProperties should be loaded");
        assertNotNull(stripeProperties.getApi(), "API properties should be loaded");
        assertNotNull(stripeProperties.getWebhook(), "Webhook properties should be loaded");
        assertNotNull(stripeProperties.getPrice(), "Price properties should be loaded");
    }

    @Test
    void testStripeApiKeyConfiguration() {
        assertNotNull(stripeProperties.getApi().getSecretKey(), "Secret key should be configured");
        assertNotNull(stripeProperties.getApi().getPublicKey(), "Public key should be configured");
    }

    @Test
    void testStripeWebhookConfiguration() {
        assertNotNull(stripeProperties.getWebhook().getSecret(), "Webhook secret should be configured");
        assertTrue(stripeProperties.getWebhook().isEnabled(), "Webhook should be enabled by default");
    }

    @Test
    void testStripePriceConfiguration() {
        assertNotNull(stripeProperties.getPrice().getPlusMonthly(), "Plus monthly price should be configured");
        assertNotNull(stripeProperties.getPrice().getPlusAnnual(), "Plus annual price should be configured");
        assertNotNull(stripeProperties.getPrice().getProMonthly(), "Pro monthly price should be configured");
        assertNotNull(stripeProperties.getPrice().getProAnnual(), "Pro annual price should be configured");
    }

    @Test
    void testStripeUrlConfiguration() {
        assertNotNull(stripeProperties.getSuccessUrl(), "Success URL should be configured");
        assertNotNull(stripeProperties.getCancelUrl(), "Cancel URL should be configured");
    }

    @Test
    void testStripeApiKeySet() {
        // Note: This will be the test key or placeholder from application.properties
        assertNotNull(Stripe.apiKey, "Stripe API key should be set after initialization");
    }
}