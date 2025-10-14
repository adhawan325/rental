package com.non.rental.view.subscription;

import com.non.rental.entity.*;
import com.non.rental.service.SubscriptionService;
import com.non.rental.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Subscription management view for users to view and manage their subscription plans.
 */
@Route(value = "subscription", layout = MainView.class)
@ViewController("SubscriptionView")
@ViewDescriptor("subscription-view.xml")
public class SubscriptionView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionView.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private Notifications notifications;

    @ViewComponent
    private VerticalLayout currentPlanSection;

    @ViewComponent
    private VerticalLayout pricingPlansSection;

    @ViewComponent
    private VerticalLayout billingSection;

    @ViewComponent
    private DataGrid<BillingHistory> billingHistoryGrid;

    @ViewComponent
    private Checkbox billingCycleToggle;

    @ViewComponent
    private MessageBundle messageBundle;

    private UserSubscription currentSubscription;
    private BillingCycle selectedBillingCycle = BillingCycle.MONTHLY;

    @Subscribe
    public void onInit(InitEvent event) {
        log.debug("Initializing SubscriptionView");
        setupBillingHistoryGrid();
    }

    @Subscribe
    public void onReady(ReadyEvent event) {
        log.debug("SubscriptionView ready");
        loadSubscriptionData();
        renderCurrentPlanSection();
        renderPricingPlansSection();
        renderBillingSection();
    }

    @Subscribe("billingCycleToggle")
    public void onBillingCycleToggleValueChange(Checkbox.ValueChangeEvent event) {
        selectedBillingCycle = Boolean.TRUE.equals(event.getValue()) 
                ? BillingCycle.ANNUAL 
                : BillingCycle.MONTHLY;
        renderPricingPlansSection();
    }

    /**
     * Load current subscription data.
     */
    private void loadSubscriptionData() {
        try {
            currentSubscription = subscriptionService.getCurrentUserSubscription();
            log.debug("Loaded subscription: {}", currentSubscription.getPlan());
        } catch (Exception e) {
            log.error("Error loading subscription", e);
            notifications.create(messageBundle.getMessage("error.loadingSubscription"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Render the current plan section.
     */
    private void renderCurrentPlanSection() {
        currentPlanSection.removeAll();

        H2 title = new H2(messageBundle.getMessage("section.currentPlan.title"));
        currentPlanSection.add(title);

        if (currentSubscription == null) {
            currentPlanSection.add(new Span(messageBundle.getMessage("error.noSubscription")));
            return;
        }

        // Current plan card
        Div planCard = createCard();
        planCard.addClassName("current-plan-card");

        // Plan name and badge
        HorizontalLayout planHeader = new HorizontalLayout();
        planHeader.setWidthFull();
        planHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        planHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        H3 planName = new H3(currentSubscription.getPlan().getDisplayName() + " Plan");
        Span badge = new Span(messageBundle.getMessage("label.currentBadge"));
        badge.getElement().getThemeList().add("badge success");
        badge.getStyle().set("font-size", "0.75rem");
        badge.getStyle().set("padding", "0.25rem 0.5rem");
        badge.getStyle().set("border-radius", "4px");
        badge.getStyle().set("background-color", "#10b981");
        badge.getStyle().set("color", "white");

        planHeader.add(planName, badge);
        planCard.add(planHeader);

        // Plan features
        H4 featuresTitle = new H4(messageBundle.getMessage("section.features.title"));
        planCard.add(featuresTitle);

        List<String> features = subscriptionService.getPlanFeatures(currentSubscription.getPlan());
        UnorderedList featureList = new UnorderedList();
        for (String feature : features) {
            ListItem item = new ListItem(feature);
            item.getStyle().set("margin-bottom", "0.5rem");
            featureList.add(item);
        }
        planCard.add(featureList);

        // Usage statistics
        H4 usageTitle = new H4(messageBundle.getMessage("section.usage.title"));
        planCard.add(usageTitle);

        Div usageStats = new Div();
        int analysesLimit = currentSubscription.getSavedAnalysesLimit();
        String analysesText = analysesLimit == -1 
                ? String.format("%d %s (%s)", 
                        currentSubscription.getSavedAnalysesCount(),
                        messageBundle.getMessage("label.savedAnalyses"),
                        messageBundle.getMessage("label.unlimited"))
                : String.format("%d %s %d %s", 
                        currentSubscription.getSavedAnalysesCount(),
                        messageBundle.getMessage("label.of"),
                        analysesLimit,
                        messageBundle.getMessage("label.savedAnalyses"));
        usageStats.add(new Paragraph(analysesText));

        int comparisonsLimit = currentSubscription.getComparisonsLimit();
        String comparisonsText = comparisonsLimit == -1
                ? String.format("%d %s (%s)", 
                        currentSubscription.getComparisonsCount(),
                        messageBundle.getMessage("label.comparisons"),
                        messageBundle.getMessage("label.unlimited"))
                : String.format("%d %s %d %s", 
                        currentSubscription.getComparisonsCount(),
                        messageBundle.getMessage("label.of"),
                        comparisonsLimit,
                        messageBundle.getMessage("label.comparisons"));
        usageStats.add(new Paragraph(comparisonsText));
        planCard.add(usageStats);

        // Renewal date
        if (currentSubscription.getRenewalDate() != null) {
            Paragraph renewalInfo = new Paragraph(
                    messageBundle.getMessage("field.renewalDate") + ": " + 
                    currentSubscription.getRenewalDate().format(DATE_FORMATTER)
            );
            renewalInfo.getStyle().set("font-weight", "bold");
            planCard.add(renewalInfo);
        }

        // Cancellation notice
        if (Boolean.TRUE.equals(currentSubscription.getCancelAtPeriodEnd())) {
            Div cancelNotice = new Div();
            cancelNotice.getStyle()
                    .set("background-color", "#fef3c7")
                    .set("border", "1px solid #fbbf24")
                    .set("border-radius", "4px")
                    .set("padding", "1rem")
                    .set("margin-top", "1rem");
            cancelNotice.add(new Paragraph(messageBundle.getMessage("notice.subscriptionCanceled")));
            planCard.add(cancelNotice);
        }

        // Manage subscription button
        if (currentSubscription.getPlan() != SubscriptionPlan.FREE) {
            Button manageButton = new Button(messageBundle.getMessage("button.manageSubscription"));
            manageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            manageButton.addClickListener(e -> showManageSubscriptionDialog());
            planCard.add(manageButton);
        }

        currentPlanSection.add(planCard);
    }

    /**
     * Render the pricing plans comparison section.
     */
    private void renderPricingPlansSection() {
        pricingPlansSection.removeAll();

        H2 title = new H2(messageBundle.getMessage("section.pricingPlans.title"));
        pricingPlansSection.add(title);

        // Billing cycle toggle
        HorizontalLayout toggleLayout = new HorizontalLayout();
        toggleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        toggleLayout.setSpacing(true);
        
        Span monthlyLabel = new Span(messageBundle.getMessage("label.monthly"));
        Span annualLabel = new Span(messageBundle.getMessage("label.annual"));
        Span savingsLabel = new Span(messageBundle.getMessage("label.savings"));
        savingsLabel.getStyle().set("color", "#10b981").set("font-weight", "bold");
        
        toggleLayout.add(monthlyLabel, billingCycleToggle, annualLabel, savingsLabel);
        pricingPlansSection.add(toggleLayout);

        // Plans grid
        HorizontalLayout plansGrid = new HorizontalLayout();
        plansGrid.setWidthFull();
        plansGrid.setSpacing(true);

        // Create plan cards
        for (SubscriptionPlan plan : SubscriptionPlan.values()) {
            Div planCard = createPlanCard(plan);
            plansGrid.add(planCard);
        }

        pricingPlansSection.add(plansGrid);
    }

    /**
     * Create a pricing plan card.
     */
    private Div createPlanCard(SubscriptionPlan plan) {
        Div card = createCard();
        card.setWidth("100%");
        card.getStyle().set("flex", "1");

        // Highlight current plan
        if (currentSubscription != null && currentSubscription.getPlan() == plan) {
            card.getStyle().set("border", "2px solid #3b82f6");
        }

        // Plan name
        H3 planName = new H3(plan.getDisplayName());
        planName.getStyle().set("text-align", "center");
        card.add(planName);

        // Price
        Double price = selectedBillingCycle == BillingCycle.MONTHLY 
                ? plan.getMonthlyPrice() 
                : plan.getAnnualPrice();
        
        Div priceDiv = new Div();
        priceDiv.getStyle().set("text-align", "center").set("margin", "1rem 0");
        
        if (price == 0.0) {
            H2 priceText = new H2(messageBundle.getMessage("label.free"));
            priceDiv.add(priceText);
        } else {
            H2 priceText = new H2(CURRENCY_FORMAT.format(price));
            Span periodText = new Span(messageBundle.getMessage("label.periodSeparator") + selectedBillingCycle.getDisplayName().toLowerCase());
            periodText.getStyle().set("color", "#6b7280");
            priceDiv.add(priceText, periodText);
        }
        card.add(priceDiv);

        // Features
        List<String> features = subscriptionService.getPlanFeatures(plan);
        UnorderedList featureList = new UnorderedList();
        featureList.getStyle().set("list-style", "none").set("padding", "0");
        
        for (String feature : features) {
            ListItem item = new ListItem();
            HorizontalLayout featureLayout = new HorizontalLayout();
            featureLayout.setSpacing(true);
            featureLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            
            Span icon = new Span(messageBundle.getMessage("label.checkmark"));
            icon.getStyle().set("color", "#10b981").set("font-weight", "bold");
            Span text = new Span(feature);
            
            featureLayout.add(icon, text);
            item.add(featureLayout);
            item.getStyle().set("margin-bottom", "0.5rem");
            featureList.add(item);
        }
        card.add(featureList);

        // Action button
        Button actionButton;
        if (currentSubscription != null && currentSubscription.getPlan() == plan) {
            actionButton = new Button(messageBundle.getMessage("button.currentPlan"));
            actionButton.setEnabled(false);
        } else {
            actionButton = new Button(messageBundle.getMessage("button.selectPlan"));
            actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            actionButton.addClickListener(e -> handlePlanSelection(plan));
        }
        actionButton.setWidthFull();
        card.add(actionButton);

        return card;
    }

    /**
     * Render the billing information section.
     */
    private void renderBillingSection() {
        billingSection.removeAll();

        H2 title = new H2(messageBundle.getMessage("section.billing.title"));
        billingSection.add(title);

        if (currentSubscription == null || currentSubscription.getPlan() == SubscriptionPlan.FREE) {
            billingSection.add(new Paragraph(messageBundle.getMessage("message.noBillingInfo")));
            return;
        }

        // Payment method card
        Div paymentCard = createCard();
        H4 paymentTitle = new H4(messageBundle.getMessage("section.paymentMethod.title"));
        paymentCard.add(paymentTitle);

        if (currentSubscription.getPaymentMethodLast4() != null) {
            String brand = currentSubscription.getPaymentMethodBrand() != null 
                    ? currentSubscription.getPaymentMethodBrand() 
                    : messageBundle.getMessage("label.card");
            Paragraph paymentInfo = new Paragraph(
                    String.format(messageBundle.getMessage("message.paymentMethodFormat"), 
                            brand,
                            currentSubscription.getPaymentMethodLast4())
            );
            paymentCard.add(paymentInfo);
        } else {
            paymentCard.add(new Paragraph(messageBundle.getMessage("message.noPaymentMethod")));
        }

        Button updatePaymentButton = new Button(messageBundle.getMessage("button.updatePaymentMethod"));
        updatePaymentButton.addClickListener(e -> showUpdatePaymentMethodDialog());
        paymentCard.add(updatePaymentButton);

        billingSection.add(paymentCard);

        // Billing history
        H4 historyTitle = new H4(messageBundle.getMessage("section.billingHistory.title"));
        billingSection.add(historyTitle);

        loadBillingHistory();
        billingSection.add(billingHistoryGrid);
    }

    /**
     * Setup billing history grid.
     */
    private void setupBillingHistoryGrid() {
        billingHistoryGrid.addColumn("billingDate")
                .setHeader(messageBundle.getMessage("field.date"))
                .setAutoWidth(true);
        
        billingHistoryGrid.addColumn("description")
                .setHeader(messageBundle.getMessage("field.description"))
                .setAutoWidth(true);
        
        billingHistoryGrid.addColumn(history -> CURRENCY_FORMAT.format(history.getAmount()))
                .setHeader(messageBundle.getMessage("field.amount"))
                .setAutoWidth(true);
        
        billingHistoryGrid.addColumn("status")
                .setHeader(messageBundle.getMessage("field.status"))
                .setAutoWidth(true);

        // Add download invoice button column
        billingHistoryGrid.addComponentColumn(history -> {
            if (history.getStripeInvoiceUrl() != null) {
                Button downloadButton = new Button(messageBundle.getMessage("button.download"));
                downloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                downloadButton.addClickListener(e -> downloadInvoice(history));
                return downloadButton;
            }
            return new Span(messageBundle.getMessage("label.placeholder"));
        }).setHeader(messageBundle.getMessage("field.invoice")).setAutoWidth(true);
    }

    /**
     * Load billing history.
     */
    private void loadBillingHistory() {
        try {
            List<BillingHistory> history = subscriptionService.getBillingHistory();
            billingHistoryGrid.setItems(history);
        } catch (Exception e) {
            log.error("Error loading billing history", e);
            notifications.create(messageBundle.getMessage("error.loadingBillingHistory"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Handle plan selection.
     */
    private void handlePlanSelection(SubscriptionPlan plan) {
        if (plan == SubscriptionPlan.FREE) {
            showDowngradeConfirmationDialog();
        } else {
            showUpgradeConfirmationDialog(plan);
        }
    }

    /**
     * Show upgrade confirmation dialog.
     */
    private void showUpgradeConfirmationDialog(SubscriptionPlan newPlan) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(messageBundle.getMessage("dialog.upgrade.title"));

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Double price = selectedBillingCycle == BillingCycle.MONTHLY
                ? newPlan.getMonthlyPrice()
                : newPlan.getAnnualPrice();

        Paragraph message = new Paragraph(
                String.format(messageBundle.getMessage("dialog.upgrade.message"),
                        newPlan.getDisplayName(),
                        CURRENCY_FORMAT.format(price),
                        selectedBillingCycle.getDisplayName().toLowerCase())
        );
        content.add(message);

        HorizontalLayout buttons = new HorizontalLayout();
        Button confirmButton = new Button(messageBundle.getMessage("button.confirm"));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.addClickListener(e -> {
            upgradePlan(newPlan);
            dialog.close();
        });

        Button cancelButton = new Button(messageBundle.getMessage("button.cancel"));
        cancelButton.addClickListener(e -> dialog.close());

        buttons.add(confirmButton, cancelButton);
        content.add(buttons);

        dialog.add(content);
        dialog.open();
    }

    /**
     * Show downgrade confirmation dialog.
     */
    private void showDowngradeConfirmationDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(messageBundle.getMessage("dialog.downgrade.title"));

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Paragraph message = new Paragraph(messageBundle.getMessage("dialog.downgrade.message"));
        content.add(message);

        HorizontalLayout buttons = new HorizontalLayout();
        Button confirmButton = new Button(messageBundle.getMessage("button.confirm"));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        confirmButton.addClickListener(e -> {
            downgradePlan();
            dialog.close();
        });

        Button cancelButton = new Button(messageBundle.getMessage("button.cancel"));
        cancelButton.addClickListener(e -> dialog.close());

        buttons.add(confirmButton, cancelButton);
        content.add(buttons);

        dialog.add(content);
        dialog.open();
    }

    /**
     * Show manage subscription dialog.
     */
    private void showManageSubscriptionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(messageBundle.getMessage("dialog.manage.title"));

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        if (Boolean.TRUE.equals(currentSubscription.getCancelAtPeriodEnd())) {
            // Show reactivate option
            Paragraph message = new Paragraph(messageBundle.getMessage("dialog.manage.reactivateMessage"));
            content.add(message);

            Button reactivateButton = new Button(messageBundle.getMessage("button.reactivate"));
            reactivateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            reactivateButton.addClickListener(e -> {
                reactivateSubscription();
                dialog.close();
            });
            content.add(reactivateButton);
        } else {
            // Show cancel option
            Paragraph message = new Paragraph(messageBundle.getMessage("dialog.manage.cancelMessage"));
            content.add(message);

            Button cancelButton = new Button(messageBundle.getMessage("button.cancelSubscription"));
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            cancelButton.addClickListener(e -> {
                cancelSubscription();
                dialog.close();
            });
            content.add(cancelButton);
        }

        Button closeButton = new Button(messageBundle.getMessage("button.close"));
        closeButton.addClickListener(e -> dialog.close());
        content.add(closeButton);

        dialog.add(content);
        dialog.open();
    }

    /**
     * Show update payment method dialog.
     */
    private void showUpdatePaymentMethodDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(messageBundle.getMessage("dialog.updatePayment.title"));

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Paragraph message = new Paragraph(messageBundle.getMessage("dialog.updatePayment.message"));
        content.add(message);

        // TODO: In production, integrate with Stripe Elements for secure card input
        Paragraph note = new Paragraph(messageBundle.getMessage("message.stripeIntegrationNote"));
        note.getStyle().set("font-style", "italic").set("color", "#6b7280");
        content.add(note);

        Button closeButton = new Button(messageBundle.getMessage("button.close"));
        closeButton.addClickListener(e -> dialog.close());
        content.add(closeButton);

        dialog.add(content);
        dialog.open();
    }

    /**
     * Upgrade to a new plan.
     */
    private void upgradePlan(SubscriptionPlan newPlan) {
        try {
            currentSubscription = subscriptionService.changePlan(newPlan, selectedBillingCycle);
            notifications.create(messageBundle.getMessage("success.planUpgraded"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            refreshView();
        } catch (Exception e) {
            log.error("Error upgrading plan", e);
            notifications.create(messageBundle.getMessage("error.upgradingPlan"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Downgrade to free plan.
     */
    private void downgradePlan() {
        try {
            currentSubscription = subscriptionService.changePlan(SubscriptionPlan.FREE, null);
            notifications.create(messageBundle.getMessage("success.planDowngraded"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            refreshView();
        } catch (Exception e) {
            log.error("Error downgrading plan", e);
            notifications.create(messageBundle.getMessage("error.downgradingPlan"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Cancel subscription.
     */
    private void cancelSubscription() {
        try {
            currentSubscription = subscriptionService.cancelSubscription();
            notifications.create(messageBundle.getMessage("success.subscriptionCanceled"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            refreshView();
        } catch (Exception e) {
            log.error("Error canceling subscription", e);
            notifications.create(messageBundle.getMessage("error.cancelingSubscription"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Reactivate subscription.
     */
    private void reactivateSubscription() {
        try {
            currentSubscription = subscriptionService.reactivateSubscription();
            notifications.create(messageBundle.getMessage("success.subscriptionReactivated"))
                    .withType(Notifications.Type.SUCCESS)
                    .show();
            refreshView();
        } catch (Exception e) {
            log.error("Error reactivating subscription", e);
            notifications.create(messageBundle.getMessage("error.reactivatingSubscription"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    /**
     * Download invoice.
     */
    private void downloadInvoice(BillingHistory history) {
        if (history.getStripeInvoiceUrl() != null) {
            // TODO: In production, open invoice URL or download PDF
            getUI().ifPresent(ui -> ui.getPage().open(history.getStripeInvoiceUrl(), "_blank"));
        }
    }

    /**
     * Refresh the view.
     */
    private void refreshView() {
        loadSubscriptionData();
        renderCurrentPlanSection();
        renderPricingPlansSection();
        renderBillingSection();
    }

    /**
     * Create a styled card div.
     */
    private Div createCard() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "8px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
                .set("margin-bottom", "1rem");
        return card;
    }
}