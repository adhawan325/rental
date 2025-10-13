package com.non.rental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for RentCast API integration.
 * Maps properties from application.properties with prefix "rentcast".
 */
@Component
@ConfigurationProperties(prefix = "rentcast")
public class RentCastProperties {

    private Api api = new Api();
    private Cache cache = new Cache();

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public static class Api {
        private String key;
        private String baseUrl;
        private int timeout = 30000;
        private int maxRetries = 3;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
    }

    public static class Cache {
        private int ttlDays = 30;
        private boolean enabled = true;

        public int getTtlDays() {
            return ttlDays;
        }

        public void setTtlDays(int ttlDays) {
            this.ttlDays = ttlDays;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}