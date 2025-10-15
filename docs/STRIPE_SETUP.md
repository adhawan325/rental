# Stripe Payment Integration Setup Guide

## Overview

This guide covers the Stripe payment integration for the Rental Yield Calculator application using Jmix 2.6.2.

## ✅ Completed Setup

### 1. Dependencies Added

**File**: `build.gradle`

```gradle
implementation 'com.stripe:stripe-java:26.13.0'
```

### 2. Configuration Properties

**File**: `src/main/resources/application.properties`

```properties
# Stripe Payment Configuration
stripe.api.secret-key=${STRIPE_SECRET_KEY:sk_test_your_test_key_here}
stripe.api.public-key=${STRIPE_PUBLIC_KEY:pk_test_your_test_key_here}
stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET:whsec_your_webhook_secret_here}
stripe.webhook.enabled=true

# Stripe Price IDs (configure these in Stripe Dashboard)
stripe.price.plus-monthly=${STRIPE_PRICE_PLUS_MONTHLY:price_plus_monthly}
stripe.price.plus-annual=${STRIPE_PRICE_PLUS_ANNUAL:price_plus_annual}
stripe.price.pro-monthly=${STRIPE_PRICE_PRO_MONTHLY:price_pro_monthly}
stripe.price.pro-annual=${STRIPE_PRICE_PRO_ANNUAL:price_pro_annual}

# Stripe Success/Cancel URLs
stripe.success-url=${STRIPE_SUCCESS_URL:http://localhost:8080/subscription/success}
stripe.cancel-url=${STRIPE_CANCEL_URL:http://localhost:8080/subscription/cancel}
```

### 3. Configuration Classes Created

#### StripeProperties.java
- Maps Stripe configuration from application.properties
- Nested classes for API, Webhook, and Price configuration
- Helper method to get Price ID by plan and billing cycle

#### StripeConfig.java
- Initializes Stripe SDK with API key
- Validates configuration on startup
- Logs configuration status

### 4. Entity Enhancements

#### UserSubscription.java
Added fields:
- `stripePriceId` - The Stripe Price ID used for the subscription
- `stripeCheckoutSessionId` - The Checkout Session ID

### 5. Service Layer

#### StripeService.java (Interface)
Methods:
- `createCheckoutSession()` - Create Stripe Checkout Session
- `getOrCreateCustomer()` - Get or create Stripe Customer
- `cancelSubscription()` - Cancel at period end
- `cancelSubscriptionImmediately()` - Cancel immediately
- `reactivateSubscription()` - Reactivate cancelled subscription
- `updatePaymentMethod()` - Update payment method
- `handleWebhook()` - Process webhook events
- `syncSubscriptionStatus()` - Sync from Stripe
- `createBillingPortalSession()` - Create billing portal URL

#### StripeServiceImpl.java (Implementation)
Handles:
- Checkout session creation
- Customer management
- Subscription lifecycle (create, cancel, reactivate)
- Payment method updates
- Webhook event processing
- Billing history creation

### 6. REST API

#### StripeWebhookController.java
- Endpoint: `POST /api/stripe/webhook`
- Handles Stripe webhook events
- Validates webhook signatures

### 7. Testing

#### StripeConfigTest.java
Tests:
- Configuration properties loading
- API key configuration
- Webhook configuration
- Price ID configuration
- URL configuration

## 🚀 Getting Started

### Step 1: Get Stripe API Keys

1. Sign up at [https://stripe.com](https://stripe.com)
2. Go to **Developers** → **API keys**
3. Copy your **Publishable key** (starts with `pk_test_`)
4. Copy your **Secret key** (starts with `sk_test_`)

### Step 2: Create Stripe Products and Prices

1. Go to **Products** in Stripe Dashboard
2. Create products for your subscription plans:

#### Plus Plan
- **Product Name**: "Plus Plan"
- **Monthly Price**: $9.99 USD
  - Recurring: Monthly
  - Copy the Price ID (e.g., `price_1ABC123...`)
- **Annual Price**: $99.99 USD
  - Recurring: Yearly
  - Copy the Price ID

#### Pro Plan
- **Product Name**: "Pro Plan"
- **Monthly Price**: $19.99 USD
  - Recurring: Monthly
  - Copy the Price ID
- **Annual Price**: $199.99 USD
  - Recurring: Yearly
  - Copy the Price ID

### Step 3: Configure Environment Variables

Update your `.env` file or environment variables:

```bash
# Stripe API Keys
STRIPE_SECRET_KEY=sk_test_your_actual_secret_key
STRIPE_PUBLIC_KEY=pk_test_your_actual_public_key

# Stripe Price IDs (from Step 2)
STRIPE_PRICE_PLUS_MONTHLY=price_1ABC123...
STRIPE_PRICE_PLUS_ANNUAL=price_1DEF456...
STRIPE_PRICE_PRO_MONTHLY=price_1GHI789...
STRIPE_PRICE_PRO_ANNUAL=price_1JKL012...

# URLs (adjust for your domain)
STRIPE_SUCCESS_URL=http://localhost:8080/subscription/success
STRIPE_CANCEL_URL=http://localhost:8080/subscription/cancel
```

### Step 4: Set Up Webhooks (Optional but Recommended)

1. Go to **Developers** → **Webhooks** in Stripe Dashboard
2. Click **Add endpoint**
3. Enter your webhook URL: `https://yourdomain.com/api/stripe/webhook`
4. Select events to listen to:
   - `checkout.session.completed`
   - `customer.subscription.created`
   - `customer.subscription.updated`
   - `customer.subscription.deleted`
   - `invoice.paid`
   - `invoice.payment_failed`
5. Copy the **Signing secret** (starts with `whsec_`)
6. Add to environment variables:
   ```bash
   STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
   ```

### Step 5: Build and Run

```bash
# Install dependencies
./gradlew build

# Run the application
./gradlew bootRun

# Or with Docker
docker-compose up -d
```

### Step 6: Test the Integration

```bash
# Run tests
./gradlew test

# Specific test
./gradlew test --tests StripeConfigTest
```

## 📋 Usage Examples

### Create a Checkout Session

```java
@Autowired
private StripeService stripeService;

public void upgradeSubscription(User user) {
    Session session = stripeService.createCheckoutSession(
        user, 
        SubscriptionPlan.PLUS, 
        BillingCycle.MONTHLY
    );
    
    // Redirect user to session.getUrl()
    String checkoutUrl = session.getUrl();
}
```

### Cancel a Subscription

```java
@Autowired
private StripeService stripeService;

public void cancelUserSubscription(UserSubscription subscription) {
    // Cancel at end of billing period
    stripeService.cancelSubscription(subscription);
    
    // Or cancel immediately
    stripeService.cancelSubscriptionImmediately(subscription);
}
```

### Create Billing Portal Session

```java
@Autowired
private StripeService stripeService;

public String getBillingPortalUrl(User user) {
    return stripeService.createBillingPortalSession(user);
}
```

## 🔒 Security Best Practices

### 1. Never Commit API Keys
- Use environment variables
- Add `.env` to `.gitignore`
- Use different keys for dev/prod

### 2. Validate Webhook Signatures
- Always verify webhook signatures
- Use the webhook secret from Stripe
- Reject invalid signatures

### 3. Use HTTPS in Production
- Stripe requires HTTPS for webhooks
- Use SSL certificates
- Configure proper CORS policies

### 4. Implement Idempotency
- Handle duplicate webhook events
- Use Stripe's idempotency keys
- Check for existing records before creating

## 🧪 Testing with Stripe Test Mode

### Test Card Numbers

```
Success: 4242 4242 4242 4242
Decline: 4000 0000 0000 0002
Requires Authentication: 4000 0025 0000 3155
```

### Test Webhooks Locally

Use Stripe CLI:

```bash
# Install Stripe CLI
brew install stripe/stripe-cli/stripe

# Login
stripe login

# Forward webhooks to local server
stripe listen --forward-to localhost:8080/api/stripe/webhook

# Trigger test events
stripe trigger checkout.session.completed
stripe trigger invoice.paid
```

## 📊 Monitoring and Logging

### Application Logs

```properties
# Enable Stripe logging
logging.level.com.non.rental.service.stripe=debug
logging.level.com.stripe=info
```

### Stripe Dashboard

Monitor in real-time:
- **Payments** - View all transactions
- **Subscriptions** - Track active subscriptions
- **Customers** - Manage customer data
- **Logs** - View API requests and webhooks

## 🐛 Troubleshooting

### Issue: "No API key provided"

**Solution**: Ensure `STRIPE_SECRET_KEY` is set correctly

```bash
echo $STRIPE_SECRET_KEY
```

### Issue: "Invalid webhook signature"

**Solution**: Verify webhook secret matches Stripe Dashboard

```bash
echo $STRIPE_WEBHOOK_SECRET
```

### Issue: "Price not found"

**Solution**: Verify Price IDs in Stripe Dashboard match configuration

### Issue: "Customer not found"

**Solution**: Check that customer was created successfully and ID is stored

## 🔄 Next Steps

After this setup, you can implement:

1. **Frontend Integration**
   - Add Stripe.js to frontend
   - Create checkout UI components
   - Handle success/cancel redirects

2. **Subscription Management UI**
   - Display current plan
   - Upgrade/downgrade options
   - Cancel subscription button
   - Billing history view

3. **Usage Tracking**
   - Enforce plan limits
   - Track API usage
   - Implement metering (if needed)

4. **Email Notifications**
   - Payment confirmations
   - Failed payment alerts
   - Subscription expiry warnings

5. **Admin Dashboard**
   - View all subscriptions
   - Manual subscription management
   - Revenue analytics

## 📚 Resources

- [Stripe Documentation](https://stripe.com/docs)
- [Stripe Java SDK](https://github.com/stripe/stripe-java)
- [Stripe Testing](https://stripe.com/docs/testing)
- [Stripe Webhooks](https://stripe.com/docs/webhooks)
- [Stripe Checkout](https://stripe.com/docs/payments/checkout)

## ✅ Verification Checklist

- [ ] Stripe dependency added to `build.gradle`
- [ ] Configuration properties added to `application.properties`
- [ ] StripeProperties class created
- [ ] StripeConfig class created
- [ ] UserSubscription entity enhanced
- [ ] StripeService interface and implementation created
- [ ] StripeWebhookController created
- [ ] Environment variables configured in `.env`
- [ ] Stripe API keys obtained
- [ ] Stripe products and prices created
- [ ] Webhook endpoint configured (optional)
- [ ] Tests passing
- [ ] Application builds successfully
- [ ] Stripe SDK initializes correctly

## 🎉 Setup Complete!

Your Stripe payment integration is now ready. The application will:
- ✅ Initialize Stripe SDK on startup
- ✅ Load configuration from properties
- ✅ Handle checkout sessions
- ✅ Process webhook events
- ✅ Manage subscriptions
- ✅ Track billing history

**Next**: Implement the frontend UI for subscription management!