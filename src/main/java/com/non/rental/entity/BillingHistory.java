package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing billing history/invoices.
 */
@JmixEntity
@Entity(name = "rental_BillingHistory")
@Table(name = "BILLING_HISTORY", indexes = {
        @Index(name = "IDX_BILLING_HISTORY_USER", columnList = "USER_ID"),
        @Index(name = "IDX_BILLING_HISTORY_DATE", columnList = "BILLING_DATE")
})
public class BillingHistory {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @NotNull
    @Column(name = "BILLING_DATE", nullable = false)
    private LocalDateTime billingDate;

    @NotNull
    @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PLAN")
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan;

    @Column(name = "BILLING_CYCLE")
    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Column(name = "STRIPE_INVOICE_ID")
    private String stripeInvoiceId;

    @Column(name = "STRIPE_INVOICE_URL")
    private String stripeInvoiceUrl;

    @Column(name = "STATUS")
    private String status; // paid, pending, failed, refunded

    @Column(name = "PAYMENT_METHOD_LAST4")
    private String paymentMethodLast4;

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

    public LocalDateTime getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDateTime billingDate) {
        this.billingDate = billingDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStripeInvoiceId() {
        return stripeInvoiceId;
    }

    public void setStripeInvoiceId(String stripeInvoiceId) {
        this.stripeInvoiceId = stripeInvoiceId;
    }

    public String getStripeInvoiceUrl() {
        return stripeInvoiceUrl;
    }

    public void setStripeInvoiceUrl(String stripeInvoiceUrl) {
        this.stripeInvoiceUrl = stripeInvoiceUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethodLast4() {
        return paymentMethodLast4;
    }

    public void setPaymentMethodLast4(String paymentMethodLast4) {
        this.paymentMethodLast4 = paymentMethodLast4;
    }

    @InstanceName
    public String getInstanceName() {
        return String.format("%s - $%.2f", 
            billingDate != null ? billingDate.toString() : "N/A",
            amount != null ? amount : BigDecimal.ZERO);
    }
}