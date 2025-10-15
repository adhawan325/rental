# 🚀 Stripe Integration - Quick Start Guide

## ✅ What's Already Done

Your Stripe payment integration is **fully implemented and compiled successfully**. Here's what's ready:

- ✅ Stripe Java SDK (v25.3.0) added to dependencies
- ✅ Configuration classes created (StripeProperties, StripeConfig)
- ✅ Service layer fully implemented (StripeService, StripeServiceImpl)
- ✅ Webhook controller ready (StripeWebhookController)
- ✅ Entity enhancements completed (UserSubscription)
- ✅ Tests created (StripeConfigTest)
- ✅ Documentation written (STRIPE_SETUP.md, STRIPE_INTEGRATION_COMPLETE.md)

---

## 🎯 What You Need to Do Now

### Step 1: Get Your Stripe Keys (5 minutes)

1. **Sign up/Login to Stripe**:
   - Go to https://dashboard.stripe.com/
   - Create an account or log in

2. **Get API Keys**:
   - Navigate to **Developers** → **API keys**
   - Copy your **Publishable key** (starts with `pk_test_`)
   - Copy your **Secret key** (starts with `sk_test_`)

3. **Save Keys Securely**:
   ```bash
   # Copy the environment template
   cp .env.example .env
   
   # Edit .env and add your keys
   nano .env
   ```

---

### Step 2: Create Products and Prices (10 minutes)

1. **Create Plus Plan Product**:
   - Go to **Products** → **Add Product**
   - Name: `Plus Plan`
   - Description: `Access to 50 saved analyses and comparisons`
   - Click **Add pricing**:
     - **Monthly**: $9.99/month, recurring
     - **Annual**: $99/year, recurring
   - Save and copy the Price IDs (format: `price_xxxxx`)

2. **Create Pro Plan Product**:
   - Go to **Products** → **Add Product**
   - Name: `Pro Plan`
   - Description: `Unlimited analyses and comparisons`
   - Click **Add pricing**:
     - **Monthly**: $19.99/month, recurring
     - **Annual**: $199/year, recurring
   - Save and copy the Price IDs

3. **Update .env with Price IDs**:
   ```bash
   STRIPE_PRICE_PLUS_MONTHLY=price_xxxxx
   STRIPE_PRICE_PLUS_ANNUAL=price_xxxxx
   STRIPE_PRICE_PRO_MONTHLY=price_xxxxx
   STRIPE_PRICE_PRO_ANNUAL=price_xxxxx
   ```

---

### Step 3: Set Up Webhooks (5 minutes)

#### For Local Development:

1. **Install Stripe CLI**:
   ```bash
   brew install stripe/stripe-cli/stripe
   ```

2. **Login to Stripe CLI**:
   ```bash
   stripe login
   ```

3. **Forward Webhooks** (run this in a separate terminal):
   ```bash
   stripe listen --forward-to localhost:8080/api/stripe/webhook
   ```

4. **Copy Webhook Secret**:
   - The CLI will display a webhook signing secret (starts with `whsec_`)
   - Add it to your `.env`:
     ```bash
     STRIPE_WEBHOOK_SECRET=whsec_xxxxx
     ```

#### For Production:

1. Go to **Developers** → **Webhooks** → **Add endpoint**
2. Enter URL: `https://yourdomain.com/api/stripe/webhook`
3. Select events:
   - `checkout.session.completed`
   - `customer.subscription.created`
   - `customer.subscription.updated`
   - `customer.subscription.deleted`
   - `invoice.paid`
   - `invoice.payment_failed`
4. Copy the webhook signing secret to your production environment

---

### Step 4: Start the Application (2 minutes)

```bash
# Option 1: Using Gradle
./gradlew bootRun

# Option 2: Using Docker
docker-compose up -d

# Option 3: Using Make
make up
```

**Access**: http://localhost:8080

---

## 🧪 Test the Integration

### Test 1: Configuration Loading

```bash
# Check logs for Stripe initialization
tail -f logs/application.log | grep Stripe
```

You should see:
```
Initializing Stripe SDK...
Stripe API key configured: sk_test_****
Stripe SDK initialized successfully
```

### Test 2: Create a Test Checkout Session

Add this to a controller or test:

```java
@Autowired
private StripeService stripeService;

@Autowired
private CurrentAuthentication currentAuthentication;

public void testCheckout() {
    User user = (User) currentAuthentication.getUser();
    
    String checkoutUrl = stripeService.createCheckoutSession(
        user,
        SubscriptionPlan.PLUS,
        BillingCycle.MONTHLY
    );
    
    System.out.println("Checkout URL: " + checkoutUrl);
    // Redirect user to this URL
}
```

### Test 3: Trigger Webhook Events

```bash
# In terminal with Stripe CLI running
stripe trigger checkout.session.completed
stripe trigger customer.subscription.updated
stripe trigger invoice.paid
```

Check your application logs to see webhook events being processed.

### Test 4: Use Stripe Test Cards

When testing checkout, use these test card numbers:
- **Success**: `4242 4242 4242 4242`
- **Decline**: `4000 0000 0000 0002`
- **3D Secure**: `4000 0025 0000 3155`

Any future expiry date and any 3-digit CVC will work.

---

## 💡 Usage Examples

### Example 1: Subscription Management View

```java
@Route(value = "subscription", layout = MainLayout.class)
@ViewController("SubscriptionManagement")
@ViewDescriptor("subscription-management-view.xml")
public class SubscriptionManagementView extends StandardView {
    
    @Autowired
    private StripeService stripeService;
    
    @Autowired
    private CurrentAuthentication currentAuthentication;
    
    @Autowired
    private DataManager dataManager;
    
    @ViewComponent
    private Html currentPlanLabel;
    
    @ViewComponent
    private Html statusLabel;
    
    @Subscribe
    public void onInit(InitEvent event) {
        loadSubscriptionInfo();
    }
    
    private void loadSubscriptionInfo() {
        User user = (User) currentAuthentication.getUser();
        
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.user.id = :userId")
            .parameter("userId", user.getId())
            .optional()
            .orElse(null);
        
        if (subscription != null) {
            currentPlanLabel.setContent(subscription.getPlan().name());
            statusLabel.setContent(subscription.isActive() ? "Active" : "Inactive");
        }
    }
    
    @Subscribe("upgradeToProBtn")
    public void onUpgradeClick(ClickEvent<Button> event) {
        User user = (User) currentAuthentication.getUser();
        
        try {
            String checkoutUrl = stripeService.createCheckoutSession(
                user,
                SubscriptionPlan.PRO,
                BillingCycle.MONTHLY
            );
            
            // Redirect to Stripe Checkout
            getUI().ifPresent(ui -> ui.getPage().setLocation(checkoutUrl));
            
        } catch (Exception e) {
            notifications.create("Failed to create checkout session")
                .withType(Notifications.Type.ERROR)
                .show();
        }
    }
    
    @Subscribe("manageBillingBtn")
    public void onManageBillingClick(ClickEvent<Button> event) {
        User user = (User) currentAuthentication.getUser();
        
        try {
            String portalUrl = stripeService.createBillingPortalSession(user);
            
            // Redirect to Stripe Billing Portal
            getUI().ifPresent(ui -> ui.getPage().setLocation(portalUrl));
            
        } catch (Exception e) {
            notifications.create("Failed to open billing portal")
                .withType(Notifications.Type.ERROR)
                .show();
        }
    }
    
    @Subscribe("cancelBtn")
    public void onCancelClick(ClickEvent<Button> event) {
        User user = (User) currentAuthentication.getUser();
        
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.user.id = :userId")
            .parameter("userId", user.getId())
            .optional()
            .orElseThrow();
        
        dialogs.createOptionDialog()
            .withHeader("Cancel Subscription")
            .withText("Are you sure you want to cancel? You'll have access until " + 
                     subscription.getRenewalDate())
            .withActions(
                new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                    try {
                        stripeService.cancelSubscription(subscription);
                        notifications.create("Subscription cancelled")
                            .withType(Notifications.Type.SUCCESS)
                            .show();
                        loadSubscriptionInfo();
                    } catch (Exception ex) {
                        notifications.create("Failed to cancel subscription")
                            .withType(Notifications.Type.ERROR)
                            .show();
                    }
                }),
                new DialogAction(DialogAction.Type.NO)
            )
            .open();
    }
}
```

### Example 2: Usage Limit Enforcement

```java
@Service
public class AnalysisService {
    
    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private CurrentAuthentication currentAuthentication;
    
    public boolean canCreateAnalysis() {
        User user = (User) currentAuthentication.getUser();
        
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.user.id = :userId")
            .parameter("userId", user.getId())
            .optional()
            .orElse(null);
        
        if (subscription == null || !subscription.isActive()) {
            return false; // No active subscription
        }
        
        switch (subscription.getPlan()) {
            case FREE:
                return subscription.getSavedAnalysesCount() < 5;
            case PLUS:
                return subscription.getSavedAnalysesCount() < 50;
            case PRO:
                return true; // Unlimited
            default:
                return false;
        }
    }
    
    public void incrementAnalysisCount(User user) {
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.user.id = :userId")
            .parameter("userId", user.getId())
            .optional()
            .orElseThrow();
        
        subscription.setSavedAnalysesCount(subscription.getSavedAnalysesCount() + 1);
        dataManager.save(subscription);
    }
}
```

### Example 3: Subscription Status Check

```java
@Service
public class SubscriptionGuard {
    
    @Autowired
    private DataManager dataManager;
    
    public boolean hasActiveSubscription(User user) {
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.user.id = :userId")
            .parameter("userId", user.getId())
            .optional()
            .orElse(null);
        
        return subscription != null && 
               subscription.isActive() && 
               subscription.getPlan() != SubscriptionPlan.FREE;
    }
    
    public boolean hasPlan(User user, SubscriptionPlan plan) {
        UserSubscription subscription = dataManager.load(UserSubscription.class)
            .query("select s from rental_UserSubscription s where s.user.id = :userId")
            .parameter("userId", user.getId())
            .optional()
            .orElse(null);
        
        return subscription != null && 
               subscription.isActive() && 
               subscription.getPlan() == plan;
    }
}
```

---

## 🔍 Troubleshooting

### Issue: "Stripe API key not set"

**Solution**: Check your `.env` file or environment variables:
```bash
echo $STRIPE_SECRET_KEY
```

If empty, set it:
```bash
export STRIPE_SECRET_KEY=sk_test_your_key_here
```

---

### Issue: "Invalid webhook signature"

**Solution**: 
1. Make sure webhook secret is correct in `.env`
2. For local testing, use Stripe CLI to forward webhooks
3. Check that the webhook endpoint is accessible

---

### Issue: "Price ID not found"

**Solution**: 
1. Verify Price IDs in Stripe Dashboard
2. Update `.env` with correct Price IDs
3. Restart the application

---

### Issue: "Customer not found"

**Solution**: 
1. Check that user has a subscription record
2. Verify Stripe Customer ID is stored correctly
3. Use `getOrCreateCustomer()` to ensure customer exists

---

## 📊 Monitoring

### Check Stripe Dashboard

1. **Payments**: View all successful/failed payments
2. **Subscriptions**: Monitor active subscriptions
3. **Customers**: See customer details
4. **Events**: View webhook event history
5. **Logs**: Debug API requests

### Application Logs

```bash
# Watch Stripe-related logs
tail -f logs/application.log | grep -i stripe

# Watch webhook events
tail -f logs/application.log | grep -i webhook
```

---

## 🎉 You're Ready!

Your Stripe integration is complete and ready to use. Here's what you can do now:

1. ✅ **Create checkout sessions** for new subscriptions
2. ✅ **Manage subscriptions** (cancel, reactivate, upgrade)
3. ✅ **Update payment methods** via Billing Portal
4. ✅ **Handle webhook events** automatically
5. ✅ **Track billing history** in your database
6. ✅ **Enforce usage limits** based on subscription plan

---

## 📚 Additional Resources

- **Full Setup Guide**: See `STRIPE_SETUP.md` for comprehensive documentation
- **Implementation Details**: See `STRIPE_INTEGRATION_COMPLETE.md`
- **Stripe Docs**: https://stripe.com/docs
- **Stripe Java SDK**: https://github.com/stripe/stripe-java
- **Test Cards**: https://stripe.com/docs/testing

---

## 🆘 Need Help?

1. Check `STRIPE_SETUP.md` for detailed troubleshooting
2. Review Stripe Dashboard logs
3. Check application logs for errors
4. Verify environment variables are set correctly
5. Test with Stripe CLI for local development

**Happy coding! 🚀**