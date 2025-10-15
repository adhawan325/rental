package com.non.rental.controller;

import com.non.rental.service.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling Stripe webhook events.
 */
@RestController
@RequestMapping("/api/stripe/webhook")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeService stripeService;

    public StripeWebhookController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    /**
     * Handle Stripe webhook events.
     * 
     * @param payload   The webhook payload
     * @param signature The Stripe-Signature header
     * @return Response indicating success or failure
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        try {
            log.info("Received Stripe webhook");
            stripeService.handleWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            log.error("Failed to process webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Webhook processing failed: " + e.getMessage());
        }
    }
}