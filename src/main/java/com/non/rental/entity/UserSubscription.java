package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing a user's subscription.
 */
@JmixEntity
@Entity(name = "rental_UserSubscription")
@Table(name = "USER_SUBSCRIPTION", indexes = {
        @Index(name = "IDX_USER_SUBSCRIPTION_USER", columnList = "USER_ID")
})
public class UserSubscription {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @NotNull
    @Column(name = "PLAN", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Column(name = "BILLING_CYCLE")
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "RENEWAL_DATE")
    private LocalDate renewalDate;

    @Column(name = "CANCEL_AT_PERIOD_END")
    private Boolean cancelAtPeriodEnd = false;

    @Column(name = "STRIPE_CUSTOMER_ID")
    private String stripeCustomerId;

    @Column(name = "STRIPE_SUBSCRIPTION_ID")
    private String stripeSubscriptionId;

    @Column(name = "STRIPE_PAYMENT_METHOD_ID")
    private String stripePaymentMethodId;

    @Column(name = "PAYMENT_METHOD_LAST4")
    private String paymentMethodLast4;

    @Column(name = "PAYMENT_METHOD_BRAND")
    private String paymentMethodBrand;

    @Column(name = "ACTIVE")
    private Boolean active = true;

    // Usage tracking
    @Column(name = "SAVED_ANALYSES_COUNT")
    private Integer savedAnalysesCount = 0;

    @Column(name = "COMPARISONS_COUNT")
    private Integer comparisonsCount = 0;

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDate renewalDate) {
        this.renewalDate = renewalDate;
    }

    public Boolean getCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(Boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public String getStripePaymentMethodId() {
        return stripePaymentMethodId;
    }

    public void setStripePaymentMethodId(String stripePaymentMethodId) {
        this.stripePaymentMethodId = stripePaymentMethodId;
    }

    public String getPaymentMethodLast4() {
        return paymentMethodLast4;
    }

    public void setPaymentMethodLast4(String paymentMethodLast4) {
        this.paymentMethodLast4 = paymentMethodLast4;
    }

    public String getPaymentMethodBrand() {
        return paymentMethodBrand;
    }

    public void setPaymentMethodBrand(String paymentMethodBrand) {
        this.paymentMethodBrand = paymentMethodBrand;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getSavedAnalysesCount() {
        return savedAnalysesCount;
    }

    public void setSavedAnalysesCount(Integer savedAnalysesCount) {
        this.savedAnalysesCount = savedAnalysesCount;
    }

    public Integer getComparisonsCount() {
        return comparisonsCount;
    }

    public void setComparisonsCount(Integer comparisonsCount) {
        this.comparisonsCount = comparisonsCount;
    }

    @InstanceName
    public String getInstanceName() {
        return plan != null ? plan.getDisplayName() : "Subscription";
    }

    /**
     * Get the limit for saved analyses based on the plan.
     */
    public int getSavedAnalysesLimit() {
        if (plan == null) return 3;
        switch (plan) {
            case FREE:
                return 3;
            case PLUS:
                return 25;
            case PRO:
                return -1; // Unlimited
            default:
                return 3;
        }
    }

    /**
     * Get the limit for comparisons based on the plan.
     */
    public int getComparisonsLimit() {
        if (plan == null) return 2;
        switch (plan) {
            case FREE:
                return 2;
            case PLUS:
                return 10;
            case PRO:
                return -1; // Unlimited
            default:
                return 2;
        }
    }

    /**
     * Check if the user can save more analyses.
     */
    public boolean canSaveMoreAnalyses() {
        int limit = getSavedAnalysesLimit();
        return limit == -1 || (savedAnalysesCount != null && savedAnalysesCount < limit);
    }

    /**
     * Check if the user can create more comparisons.
     */
    public boolean canCreateMoreComparisons() {
        int limit = getComparisonsLimit();
        return limit == -1 || (comparisonsCount != null && comparisonsCount < limit);
    }
}