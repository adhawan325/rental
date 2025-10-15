package com.non.rental.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for storing email verification and password reset tokens.
 * Follows Jmix best practices with proper annotations and lifecycle management.
 */
@JmixEntity
@Entity
@Table(name = "EMAIL_VERIFICATION_TOKEN", indexes = {
        @Index(name = "IDX_EMAIL_VERIFICATION_TOKEN_TOKEN", columnList = "TOKEN", unique = true),
        @Index(name = "IDX_EMAIL_VERIFICATION_TOKEN_USER", columnList = "USER_ID")
})
public class EmailVerificationToken {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @NotNull
    @Column(name = "TOKEN", nullable = false, unique = true, length = 255)
    private String token;

    @NotNull
    @Column(name = "TOKEN_TYPE", nullable = false, length = 50)
    private String tokenType; // EMAIL_VERIFICATION, PASSWORD_RESET

    @NotNull
    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;

    @NotNull
    @Column(name = "USED", nullable = false)
    private Boolean used = false;

    @Column(name = "USED_DATE")
    private LocalDateTime usedDate;

    @NotNull
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public LocalDateTime getUsedDate() {
        return usedDate;
    }

    public void setUsedDate(LocalDateTime usedDate) {
        this.usedDate = usedDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if token is valid (not used and not expired)
     */
    public boolean isValid() {
        return !used && !isExpired();
    }
}