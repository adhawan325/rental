package com.non.rental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Stripe payment integration.
 * Maps properties from application.properties with prefix "stripe".
 */
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private Api api = new Api();
    private Webhook webhook = new Webhook();
    private Price price = new Price();
    private String successUrl;
    private String cancelUrl;

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    /**
     * Stripe API configuration.
     */
    public static class Api {
        private String secretKey;
        private String publicKey;

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }
    }

    /**
     * Stripe Webhook configuration.
     */
    public static class Webhook {
        private String secret;
        private boolean enabled = true;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Stripe Price IDs for subscription plans.
     */
    public static class Price {
        private String plusMonthly;
        private String plusAnnual;
        private String proMonthly;
        private String proAnnual;

        public String getPlusMonthly() {
            return plusMonthly;
        }

        public void setPlusMonthly(String plusMonthly) {
            this.plusMonthly = plusMonthly;
        }

        public String getPlusAnnual() {
            return plusAnnual;
        }

        public void setPlusAnnual(String plusAnnual) {
            this.plusAnnual = plusAnnual;
        }

        public String getProMonthly() {
            return proMonthly;
        }

        public void setProMonthly(String proMonthly) {
            this.proMonthly = proMonthly;
        }

        public String getProAnnual() {
            return proAnnual;
        }

        public void setProAnnual(String proAnnual) {
            this.proAnnual = proAnnual;
        }

        /**
         * Get the Stripe Price ID for a given plan and billing cycle.
         */
        public String getPriceId(com.non.rental.entity.SubscriptionPlan plan, 
                                 com.non.rental.entity.BillingCycle billingCycle) {
            if (plan == null || billingCycle == null) {
                return null;
            }

            switch (plan) {
                case PLUS:
                    return billingCycle == com.non.rental.entity.BillingCycle.MONTHLY 
                        ? plusMonthly : plusAnnual;
                case PRO:
                    return billingCycle == com.non.rental.entity.BillingCycle.MONTHLY 
                        ? proMonthly : proAnnual;
                default:
                    return null;
            }
        }
    }
}