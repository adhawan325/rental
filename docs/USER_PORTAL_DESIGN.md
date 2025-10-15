# 🔐 User Self-Service Portal Design

## Executive Summary

This document outlines a comprehensive design for a user self-service portal for the Rental application using Jmix 2.6.2 add-ons and industry-standard security practices. The portal will enable users to:

- **Sign up** with email verification
- **Login** with username/password or social providers (Google, GitHub)
- **Reset/Change passwords** securely
- **Manage profile** information
- **Enable Two-Factor Authentication (2FA)**
- **Manage sessions** and view login history
- **Delete account** with proper data handling

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Jmix Add-ons Required](#jmix-add-ons-required)
3. [Security Features](#security-features)
4. [User Registration Flow](#user-registration-flow)
5. [Authentication Flows](#authentication-flows)
6. [Password Management](#password-management)
7. [Profile Management](#profile-management)
8. [Session Management](#session-management)
9. [Database Schema](#database-schema)
10. [Implementation Plan](#implementation-plan)
11. [Security Best Practices](#security-best-practices)
12. [Configuration](#configuration)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    User Self-Service Portal                  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Registration │  │    Login     │  │   Profile    │      │
│  │   & Signup   │  │ (Multi-Auth) │  │  Management  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Password   │  │   Session    │  │     2FA      │      │
│  │  Management  │  │  Management  │  │   (TOTP)     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼──────┐  ┌────────▼────────┐
│ Jmix Security  │  │ Jmix Email  │  │  Jmix OIDC      │
│    Add-on      │  │   Add-on    │  │   Add-on        │
└────────────────┘  └─────────────┘  └─────────────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                ┌───────────▼───────────┐
                │   Spring Security     │
                │   + PostgreSQL DB     │
                └───────────────────────┘
```

---

## 📦 Jmix Add-ons Required

### 1. **Jmix Email Add-on** ✅ (Recommended)
**Purpose**: Email verification, password reset, notifications

```gradle
implementation 'io.jmix.email:jmix-email-starter'
implementation 'io.jmix.email:jmix-email-flowui-starter'
```

**Features**:
- Email template management
- Asynchronous email sending
- Email queue with retry mechanism
- Support for HTML emails
- Attachment support

### 2. **Jmix OIDC Add-on** ✅ (For Social Login)
**Purpose**: OAuth2/OIDC integration (Google, GitHub, etc.)

```gradle
implementation 'io.jmix.oidc:jmix-oidc-starter'
```

**Features**:
- OAuth2/OIDC authentication
- Multiple provider support
- User mapping from external providers
- Token management

### 3. **Jmix Audit Add-on** ✅ (Recommended)
**Purpose**: Track user activities, login history

```gradle
implementation 'io.jmix.audit:jmix-audit-starter'
implementation 'io.jmix.audit:jmix-audit-flowui-starter'
```

**Features**:
- Entity change tracking
- Login/logout tracking
- User action logging
- Audit trail reports

### 4. **Jmix Notifications Add-on** (Optional)
**Purpose**: In-app notifications for security events

```gradle
implementation 'io.jmix.notifications:jmix-notifications-starter'
implementation 'io.jmix.notifications:jmix-notifications-flowui-starter'
```

**Features**:
- In-app notification center
- Real-time notifications
- Notification templates
- Multiple channels (UI, Email, etc.)

### 5. **Already Included** ✅
- `jmix-security-starter` - Core security
- `jmix-security-flowui-starter` - UI security
- `jmix-security-data-starter` - Data security

---

## 🔒 Security Features

### 1. **Password Security**
- ✅ BCrypt password hashing (Spring Security default)
- ✅ Password strength validation
- ✅ Password history (prevent reuse of last 5 passwords)
- ✅ Password expiration (configurable, e.g., 90 days)
- ✅ Account lockout after failed attempts
- ✅ Secure password reset with time-limited tokens

### 2. **Two-Factor Authentication (2FA)**
- ✅ TOTP-based (Google Authenticator, Authy)
- ✅ Backup codes generation
- ✅ Optional enforcement per user or role
- ✅ QR code generation for easy setup

### 3. **Session Security**
- ✅ Secure session management
- ✅ Session timeout (configurable)
- ✅ Concurrent session control
- ✅ Session invalidation on password change
- ✅ "Remember Me" with secure token

### 4. **Account Security**
- ✅ Email verification on signup
- ✅ Account activation workflow
- ✅ Account lockout mechanism
- ✅ Suspicious activity detection
- ✅ IP-based access control (optional)

### 5. **CSRF & XSS Protection**
- ✅ CSRF tokens (Spring Security)
- ✅ XSS prevention (Vaadin built-in)
- ✅ Content Security Policy headers
- ✅ Secure headers configuration

---

## 📝 User Registration Flow

### Flow Diagram

```
┌─────────────┐
│   User      │
│ Visits Site │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  Registration Form  │
│  - Email            │
│  - Username         │
│  - Password         │
│  - First/Last Name  │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Validation         │
│  - Email unique     │
│  - Username unique  │
│  - Password strong  │
│  - Terms accepted   │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Create User        │
│  - Status: PENDING  │
│  - Generate Token   │
│  - Send Email       │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Email Sent         │
│  "Verify your email"│
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  User Clicks Link   │
│  /verify?token=xxx  │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Verify Token       │
│  - Check expiry     │
│  - Activate account │
│  - Status: ACTIVE   │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Success Message    │
│  "Account activated"│
│  Redirect to Login  │
└─────────────────────┘
```

### Implementation Components

1. **RegistrationView.java** - Registration form UI
2. **RegistrationService.java** - Business logic
3. **EmailVerificationToken** entity - Token storage
4. **VerificationController.java** - Token verification endpoint
5. **Email templates** - Welcome and verification emails

---

## 🔑 Authentication Flows

### 1. **Username/Password Login**

```
┌─────────────┐
│   User      │
│ Enters      │
│ Credentials │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  Validate           │
│  - Username exists  │
│  - Password correct │
│  - Account active   │
│  - Not locked       │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  2FA Enabled?       │
└──────┬──────────────┘
       │
   ┌───┴───┐
   │       │
  Yes     No
   │       │
   │       └──────────────┐
   │                      │
   ▼                      ▼
┌─────────────┐    ┌─────────────┐
│ Prompt 2FA  │    │   Success   │
│    Code     │    │   Login     │
└──────┬──────┘    └─────────────┘
       │
       ▼
┌─────────────┐
│ Verify Code │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Success   │
│   Login     │
└─────────────┘
```

### 2. **Social Login (OAuth2/OIDC)**

```
┌─────────────┐
│   User      │
│ Clicks      │
│ "Login with │
│  Google"    │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  Redirect to        │
│  Google OAuth       │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  User Authorizes    │
│  on Google          │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Callback with      │
│  Authorization Code │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Exchange for Token │
│  Get User Info      │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  User Exists?       │
└──────┬──────────────┘
       │
   ┌───┴───┐
   │       │
  Yes     No
   │       │
   │       └──────────────┐
   │                      │
   ▼                      ▼
┌─────────────┐    ┌─────────────┐
│   Login     │    │   Create    │
│   User      │    │   Account   │
└─────────────┘    └──────┬──────┘
                          │
                          ▼
                   ┌─────────────┐
                   │   Login     │
                   │   User      │
                   └─────────────┘
```

### 3. **Password Reset Flow**

```
┌─────────────┐
│   User      │
│ Clicks      │
│ "Forgot     │
│  Password?" │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│  Enter Email        │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Validate Email     │
│  - Email exists?    │
│  - Account active?  │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Generate Token     │
│  - 1 hour expiry    │
│  - One-time use     │
│  - Send email       │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  User Clicks Link   │
│  /reset?token=xxx   │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Verify Token       │
│  - Valid?           │
│  - Not expired?     │
│  - Not used?        │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Enter New Password │
│  - Strength check   │
│  - Confirm match    │
│  - Not in history   │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Update Password    │
│  - Hash password    │
│  - Invalidate token │
│  - Invalidate       │
│    sessions         │
│  - Send email       │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Success Message    │
│  Redirect to Login  │
└─────────────────────┘
```

---

## 🔐 Password Management

### Password Policy Configuration

```java
@Configuration
public class PasswordPolicyConfiguration {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength: 12
    }
    
    @Bean
    public PasswordValidator passwordValidator() {
        return PasswordValidator.builder()
            .minLength(12)
            .maxLength(128)
            .requireUppercase(true)
            .requireLowercase(true)
            .requireDigit(true)
            .requireSpecialChar(true)
            .preventCommonPasswords(true)
            .preventUserInfoInPassword(true)
            .build();
    }
}
```

### Password Strength Requirements

| Requirement | Value | Rationale |
|-------------|-------|-----------|
| Minimum Length | 12 characters | NIST recommendation |
| Maximum Length | 128 characters | Prevent DoS attacks |
| Uppercase | Required | Complexity |
| Lowercase | Required | Complexity |
| Digit | Required | Complexity |
| Special Character | Required | Complexity |
| Password History | Last 5 passwords | Prevent reuse |
| Password Expiry | 90 days (optional) | Configurable |
| Max Failed Attempts | 5 | Account lockout |
| Lockout Duration | 30 minutes | Security vs. UX |

### Password Change Flow

```java
public interface PasswordService {
    
    /**
     * Change password for authenticated user
     */
    void changePassword(String currentPassword, String newPassword);
    
    /**
     * Reset password using token
     */
    void resetPassword(String token, String newPassword);
    
    /**
     * Request password reset
     */
    void requestPasswordReset(String email);
    
    /**
     * Validate password strength
     */
    PasswordValidationResult validatePassword(String password, User user);
    
    /**
     * Check if password is in history
     */
    boolean isPasswordInHistory(User user, String password);
}
```

---

## 👤 Profile Management

### User Profile Features

1. **Basic Information**
   - First Name, Last Name
   - Email (with verification on change)
   - Username (read-only after creation)
   - Time Zone
   - Profile Picture (optional)

2. **Security Settings**
   - Change Password
   - Enable/Disable 2FA
   - View Active Sessions
   - View Login History
   - Manage Backup Codes

3. **Notification Preferences**
   - Email notifications
   - Security alerts
   - Marketing emails (opt-in)

4. **Account Actions**
   - Download personal data (GDPR)
   - Delete account
   - Export data

### Profile View Structure

```java
@Route(value = "profile", layout = MainView.class)
@ViewController("rental_ProfileView")
@ViewDescriptor("profile-view.xml")
public class ProfileView extends StandardView {
    
    @Autowired
    private CurrentAuthentication currentAuthentication;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    // Tabs: Profile, Security, Sessions, Preferences
}
```

---

## 🖥️ Session Management

### Session Features

1. **Active Sessions View**
   - Device information
   - IP address
   - Location (approximate)
   - Last activity
   - Current session indicator
   - Revoke session button

2. **Session Security**
   - Concurrent session limit (configurable)
   - Session timeout (30 minutes idle)
   - Absolute timeout (12 hours)
   - Invalidate all sessions on password change
   - "Remember Me" (14 days, secure token)

3. **Login History**
   - Timestamp
   - IP address
   - Device/Browser
   - Location
   - Success/Failure status
   - Failure reason

### Session Configuration

```properties
# Session Management
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

# Remember Me
spring.security.remember-me.key=${REMEMBER_ME_KEY:change-this-in-production}
spring.security.remember-me.token-validity-seconds=1209600

# Concurrent Sessions
spring.security.concurrent-sessions.max-sessions=3
spring.security.concurrent-sessions.error-if-maximum-exceeded=false
```

---

## 🗄️ Database Schema

### New Entities Required

#### 1. **EmailVerificationToken**

```java
@Entity
@Table(name = "EMAIL_VERIFICATION_TOKEN")
public class EmailVerificationToken {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
    
    @Column(name = "TOKEN", nullable = false, unique = true)
    private String token;
    
    @Column(name = "TOKEN_TYPE", nullable = false)
    private TokenType tokenType; // EMAIL_VERIFICATION, PASSWORD_RESET
    
    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "USED")
    private Boolean used = false;
    
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
```

#### 2. **PasswordHistory**

```java
@Entity
@Table(name = "PASSWORD_HISTORY")
public class PasswordHistory {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
    
    @Column(name = "PASSWORD_HASH", nullable = false)
    private String passwordHash;
    
    @Column(name = "CHANGED_DATE", nullable = false)
    private LocalDateTime changedDate;
}
```

#### 3. **TwoFactorAuth**

```java
@Entity
@Table(name = "TWO_FACTOR_AUTH")
public class TwoFactorAuth {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;
    
    @Column(name = "SECRET_KEY", nullable = false)
    private String secretKey;
    
    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled = false;
    
    @Column(name = "BACKUP_CODES")
    private String backupCodes; // JSON array
    
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    
    @Column(name = "LAST_USED_DATE")
    private LocalDateTime lastUsedDate;
}
```

#### 4. **UserSession**

```java
@Entity
@Table(name = "USER_SESSION")
public class UserSession {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
    
    @Column(name = "SESSION_ID", nullable = false, unique = true)
    private String sessionId;
    
    @Column(name = "IP_ADDRESS")
    private String ipAddress;
    
    @Column(name = "USER_AGENT")
    private String userAgent;
    
    @Column(name = "DEVICE_INFO")
    private String deviceInfo;
    
    @Column(name = "LOCATION")
    private String location;
    
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "LAST_ACTIVITY_DATE")
    private LocalDateTime lastActivityDate;
    
    @Column(name = "ACTIVE")
    private Boolean active = true;
}
```

#### 5. **LoginHistory**

```java
@Entity
@Table(name = "LOGIN_HISTORY")
public class LoginHistory {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
    
    @Column(name = "USERNAME")
    private String username;
    
    @Column(name = "IP_ADDRESS")
    private String ipAddress;
    
    @Column(name = "USER_AGENT")
    private String userAgent;
    
    @Column(name = "LOGIN_DATE", nullable = false)
    private LocalDateTime loginDate;
    
    @Column(name = "SUCCESS", nullable = false)
    private Boolean success;
    
    @Column(name = "FAILURE_REASON")
    private String failureReason;
    
    @Column(name = "LOCATION")
    private String location;
}
```

#### 6. **SocialLoginConnection**

```java
@Entity
@Table(name = "SOCIAL_LOGIN_CONNECTION")
public class SocialLoginConnection {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;
    
    @Column(name = "PROVIDER", nullable = false)
    private String provider; // GOOGLE, GITHUB, etc.
    
    @Column(name = "PROVIDER_USER_ID", nullable = false)
    private String providerUserId;
    
    @Column(name = "DISPLAY_NAME")
    private String displayName;
    
    @Column(name = "EMAIL")
    private String email;
    
    @Column(name = "PROFILE_URL")
    private String profileUrl;
    
    @Column(name = "CONNECTED_DATE", nullable = false)
    private LocalDateTime connectedDate;
    
    @Column(name = "LAST_USED_DATE")
    private LocalDateTime lastUsedDate;
}
```

#### 7. **User Entity Enhancements**

```java
@Entity
@Table(name = "RENTAL_USER")
public class User implements JmixUserDetails, HasTimeZone {
    
    // ... existing fields ...
    
    @Column(name = "EMAIL_VERIFIED")
    private Boolean emailVerified = false;
    
    @Column(name = "ACCOUNT_STATUS")
    private AccountStatus accountStatus = AccountStatus.PENDING;
    
    @Column(name = "FAILED_LOGIN_ATTEMPTS")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "LOCKED_UNTIL")
    private LocalDateTime lockedUntil;
    
    @Column(name = "PASSWORD_CHANGED_DATE")
    private LocalDateTime passwordChangedDate;
    
    @Column(name = "LAST_LOGIN_DATE")
    private LocalDateTime lastLoginDate;
    
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    
    @Column(name = "TERMS_ACCEPTED")
    private Boolean termsAccepted = false;
    
    @Column(name = "TERMS_ACCEPTED_DATE")
    private LocalDateTime termsAcceptedDate;
    
    @Column(name = "PROFILE_PICTURE_URL")
    private String profilePictureUrl;
}

enum AccountStatus {
    PENDING,        // Email not verified
    ACTIVE,         // Normal active account
    LOCKED,         // Temporarily locked
    SUSPENDED,      // Admin suspended
    DELETED         // Soft deleted
}
```

---

## 📅 Implementation Plan

### Phase 1: Foundation (Week 1-2)

**Goal**: Set up add-ons and basic infrastructure

1. **Add Jmix Dependencies**
   - Add Email add-on
   - Add OIDC add-on
   - Add Audit add-on
   - Configure build.gradle

2. **Database Schema**
   - Create new entities
   - Enhance User entity
   - Generate Liquibase changelogs
   - Run migrations

3. **Email Configuration**
   - Configure SMTP settings
   - Create email templates
   - Test email sending

4. **Security Configuration**
   - Configure password encoder
   - Set up session management
   - Configure CSRF protection

**Deliverables**:
- ✅ All add-ons installed
- ✅ Database schema updated
- ✅ Email system working
- ✅ Basic security configured

---

### Phase 2: User Registration (Week 3)

**Goal**: Implement complete registration flow

1. **Registration View**
   - Create registration form
   - Add validation
   - Terms & conditions checkbox

2. **Registration Service**
   - User creation logic
   - Email verification token generation
   - Send verification email

3. **Email Verification**
   - Verification endpoint
   - Token validation
   - Account activation

4. **Testing**
   - Unit tests
   - Integration tests
   - UI tests

**Deliverables**:
- ✅ Registration form
- ✅ Email verification working
- ✅ Tests passing

---

### Phase 3: Authentication (Week 4)

**Goal**: Implement login flows

1. **Enhanced Login View**
   - Username/password form
   - Social login buttons
   - "Forgot password" link
   - "Remember me" checkbox

2. **Social Login Integration**
   - Configure Google OAuth
   - Configure GitHub OAuth
   - User mapping logic
   - Account linking

3. **Login History**
   - Track login attempts
   - Record device info
   - Geo-location (optional)

4. **Account Lockout**
   - Failed attempt tracking
   - Automatic lockout
   - Unlock mechanism

**Deliverables**:
- ✅ Multi-auth login working
- ✅ Social login integrated
- ✅ Login history tracked
- ✅ Account lockout working

---

### Phase 4: Password Management (Week 5)

**Goal**: Implement password features

1. **Password Reset**
   - "Forgot password" view
   - Token generation
   - Reset email
   - Reset form
   - Token validation

2. **Password Change**
   - Change password view
   - Current password verification
   - Password strength indicator
   - Password history check

3. **Password Policy**
   - Strength validation
   - History tracking
   - Expiry (optional)

**Deliverables**:
- ✅ Password reset working
- ✅ Password change working
- ✅ Password policy enforced

---

### Phase 5: Two-Factor Authentication (Week 6)

**Goal**: Implement 2FA

1. **2FA Setup**
   - Generate secret key
   - QR code generation
   - Verification code input
   - Backup codes generation

2. **2FA Login**
   - 2FA prompt after password
   - Code verification
   - Backup code support
   - "Trust this device" option

3. **2FA Management**
   - Enable/disable 2FA
   - Regenerate backup codes
   - View 2FA status

**Deliverables**:
- ✅ 2FA setup working
- ✅ 2FA login working
- ✅ 2FA management UI

---

### Phase 6: Profile & Session Management (Week 7)

**Goal**: Implement profile and session features

1. **Profile View**
   - View/edit profile
   - Change email (with verification)
   - Upload profile picture
   - Notification preferences

2. **Session Management**
   - Active sessions view
   - Session details
   - Revoke session
   - Login history view

3. **Account Actions**
   - Download data
   - Delete account
   - Export data

**Deliverables**:
- ✅ Profile management working
- ✅ Session management working
- ✅ Account actions working

---

### Phase 7: Testing & Security Audit (Week 8)

**Goal**: Comprehensive testing and security review

1. **Security Testing**
   - Penetration testing
   - OWASP Top 10 check
   - Vulnerability scanning
   - Code review

2. **Performance Testing**
   - Load testing
   - Stress testing
   - Session management under load

3. **User Acceptance Testing**
   - End-to-end flows
   - Edge cases
   - Error handling
   - UX review

4. **Documentation**
   - User guide
   - Admin guide
   - API documentation
   - Security documentation

**Deliverables**:
- ✅ Security audit passed
- ✅ Performance acceptable
- ✅ UAT completed
- ✅ Documentation complete

---

## 🔐 Security Best Practices

### 1. **Password Security**

```java
// ✅ DO: Use BCrypt with appropriate strength
PasswordEncoder encoder = new BCryptPasswordEncoder(12);

// ✅ DO: Validate password strength
public boolean isPasswordStrong(String password) {
    return password.length() >= 12 &&
           password.matches(".*[A-Z].*") &&
           password.matches(".*[a-z].*") &&
           password.matches(".*\\d.*") &&
           password.matches(".*[!@#$%^&*].*");
}

// ❌ DON'T: Store passwords in plain text
// ❌ DON'T: Use weak hashing (MD5, SHA1)
// ❌ DON'T: Allow weak passwords
```

### 2. **Token Security**

```java
// ✅ DO: Use cryptographically secure random tokens
SecureRandom random = new SecureRandom();
byte[] tokenBytes = new byte[32];
random.nextBytes(tokenBytes);
String token = Base64.getUrlEncoder().encodeToString(tokenBytes);

// ✅ DO: Set appropriate expiry times
LocalDateTime expiry = LocalDateTime.now().plusHours(1); // Password reset
LocalDateTime expiry = LocalDateTime.now().plusDays(7);  // Email verification

// ✅ DO: Invalidate tokens after use
token.setUsed(true);

// ❌ DON'T: Use predictable tokens (UUID alone)
// ❌ DON'T: Allow tokens to be reused
// ❌ DON'T: Set long expiry times for sensitive operations
```

### 3. **Session Security**

```java
// ✅ DO: Configure secure session cookies
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict

// ✅ DO: Implement session timeout
server.servlet.session.timeout=30m

// ✅ DO: Invalidate sessions on security events
sessionRegistry.getAllSessions(user, false)
    .forEach(SessionInformation::expireNow);

// ❌ DON'T: Store sensitive data in sessions
// ❌ DON'T: Allow unlimited concurrent sessions
// ❌ DON'T: Use long session timeouts
```

### 4. **Email Security**

```java
// ✅ DO: Use HTTPS links in emails
String verificationLink = "https://yourdomain.com/verify?token=" + token;

// ✅ DO: Include security warnings
"If you didn't request this, please ignore this email."

// ✅ DO: Rate limit email sending
@RateLimiter(name = "email", fallbackMethod = "emailRateLimitFallback")
public void sendEmail(String to, String subject, String body) { }

// ❌ DON'T: Include sensitive data in emails
// ❌ DON'T: Use HTTP links
// ❌ DON'T: Allow unlimited email sending
```

### 5. **Input Validation**

```java
// ✅ DO: Validate all inputs
@Email
@NotBlank
@Size(max = 255)
private String email;

// ✅ DO: Sanitize user input
String sanitized = StringEscapeUtils.escapeHtml4(userInput);

// ✅ DO: Use parameterized queries
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);

// ❌ DON'T: Trust user input
// ❌ DON'T: Use string concatenation for queries
// ❌ DON'T: Skip validation on server side
```

### 6. **Error Handling**

```java
// ✅ DO: Use generic error messages
throw new AuthenticationException("Invalid credentials");

// ✅ DO: Log detailed errors securely
log.error("Login failed for user: {} from IP: {}", username, ipAddress);

// ✅ DO: Implement rate limiting
@RateLimiter(name = "login", fallbackMethod = "loginRateLimitFallback")
public void login(String username, String password) { }

// ❌ DON'T: Reveal user existence
// "User not found" vs "Invalid credentials"

// ❌ DON'T: Expose stack traces to users
// ❌ DON'T: Allow unlimited login attempts
```

### 7. **HTTPS & Headers**

```java
// ✅ DO: Enforce HTTPS
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.requiresChannel()
            .anyRequest()
            .requiresSecure();
        return http.build();
    }
}

// ✅ DO: Set security headers
http.headers()
    .contentSecurityPolicy("default-src 'self'")
    .and()
    .xssProtection()
    .and()
    .frameOptions().deny();

// ❌ DON'T: Allow HTTP in production
// ❌ DON'T: Skip security headers
```

### 8. **Audit Logging**

```java
// ✅ DO: Log security events
@Audited
public class User { }

// ✅ DO: Log authentication events
log.info("User {} logged in from {}", username, ipAddress);
log.warn("Failed login attempt for {} from {}", username, ipAddress);

// ✅ DO: Log sensitive operations
log.info("Password changed for user: {}", username);
log.info("2FA enabled for user: {}", username);

// ❌ DON'T: Log sensitive data (passwords, tokens)
// ❌ DON'T: Skip logging security events
```

---

## ⚙️ Configuration

### 1. **build.gradle**

```gradle
dependencies {
    // Existing dependencies
    implementation 'io.jmix.core:jmix-core-starter'
    implementation 'io.jmix.data:jmix-eclipselink-starter'
    implementation 'io.jmix.security:jmix-security-starter'
    implementation 'io.jmix.security:jmix-security-flowui-starter'
    implementation 'io.jmix.security:jmix-security-data-starter'
    implementation 'io.jmix.flowui:jmix-flowui-starter'
    
    // NEW: Email Add-on
    implementation 'io.jmix.email:jmix-email-starter'
    implementation 'io.jmix.email:jmix-email-flowui-starter'
    
    // NEW: OIDC Add-on (Social Login)
    implementation 'io.jmix.oidc:jmix-oidc-starter'
    
    // NEW: Audit Add-on
    implementation 'io.jmix.audit:jmix-audit-starter'
    implementation 'io.jmix.audit:jmix-audit-flowui-starter'
    
    // NEW: Notifications Add-on (Optional)
    implementation 'io.jmix.notifications:jmix-notifications-starter'
    implementation 'io.jmix.notifications:jmix-notifications-flowui-starter'
    
    // NEW: Google Authenticator for 2FA
    implementation 'com.warrenstrange:googleauth:1.5.0'
    
    // NEW: QR Code generation
    implementation 'com.google.zxing:core:3.5.2'
    implementation 'com.google.zxing:javase:3.5.2'
    
    // Existing
    implementation 'com.stripe:stripe-java:25.3.0'
    implementation 'org.postgresql:postgresql'
}
```

### 2. **application.properties**

```properties
# ============================================
# USER PORTAL CONFIGURATION
# ============================================

# Email Configuration
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Email Templates
jmix.email.from-address=${MAIL_FROM:noreply@yourdomain.com}
jmix.email.from-name=Rental Application

# Session Management
server.servlet.session.timeout=30m
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
server.servlet.session.cookie.name=RENTAL_SESSION

# Remember Me
spring.security.remember-me.key=${REMEMBER_ME_KEY:change-this-in-production-to-random-string}
spring.security.remember-me.token-validity-seconds=1209600

# Concurrent Sessions
spring.security.concurrent-sessions.max-sessions=3
spring.security.concurrent-sessions.error-if-maximum-exceeded=false

# Password Policy
rental.security.password.min-length=12
rental.security.password.max-length=128
rental.security.password.require-uppercase=true
rental.security.password.require-lowercase=true
rental.security.password.require-digit=true
rental.security.password.require-special-char=true
rental.security.password.history-count=5
rental.security.password.expiry-days=90

# Account Lockout
rental.security.lockout.max-attempts=5
rental.security.lockout.duration-minutes=30

# Token Expiry
rental.security.token.email-verification-hours=168
rental.security.token.password-reset-hours=1

# OAuth2/OIDC - Google
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:your-client-id}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:your-client-secret}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# OAuth2/OIDC - GitHub
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID:your-client-id}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET:your-client-secret}
spring.security.oauth2.client.registration.github.scope=read:user,user:email
spring.security.oauth2.client.registration.github.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# 2FA Configuration
rental.security.2fa.issuer=Rental Application
rental.security.2fa.backup-codes-count=10

# Application URLs
rental.app.base-url=${APP_BASE_URL:http://localhost:8080}
rental.app.frontend-url=${APP_FRONTEND_URL:http://localhost:8080}

# Rate Limiting
rental.security.rate-limit.login-attempts-per-minute=5
rental.security.rate-limit.email-per-hour=10
rental.security.rate-limit.password-reset-per-day=3

# Audit Logging
jmix.audit.enabled=true
jmix.audit.entity-log.enabled=true

# Logging
logging.level.com.non.rental.security=DEBUG
logging.level.org.springframework.security=INFO
logging.level.io.jmix.security=INFO
```

### 3. **.env.example**

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost/rental
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-password

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@yourdomain.com

# OAuth2 - Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# OAuth2 - GitHub
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# Security
REMEMBER_ME_KEY=change-this-to-random-string-in-production

# Application
APP_BASE_URL=http://localhost:8080
APP_FRONTEND_URL=http://localhost:8080

# Stripe (existing)
STRIPE_SECRET_KEY=sk_test_your_key
STRIPE_PUBLIC_KEY=pk_test_your_key
STRIPE_WEBHOOK_SECRET=whsec_your_secret
```

---

## 📊 Success Metrics

### Security Metrics
- ✅ Zero password breaches
- ✅ 100% HTTPS coverage
- ✅ All OWASP Top 10 addressed
- ✅ Regular security audits passed

### User Experience Metrics
- ✅ Registration completion rate > 80%
- ✅ Email verification rate > 90%
- ✅ Password reset success rate > 95%
- ✅ 2FA adoption rate > 30%

### Performance Metrics
- ✅ Login time < 2 seconds
- ✅ Registration time < 3 seconds
- ✅ Email delivery < 30 seconds
- ✅ Session management overhead < 5%

---

## 🎯 Next Steps

### Immediate Actions

1. **Review & Approve Design**
   - Review this document with stakeholders
   - Get approval for implementation
   - Prioritize features if needed

2. **Set Up Development Environment**
   - Create feature branch
   - Set up test database
   - Configure email testing (Mailtrap, etc.)

3. **Begin Phase 1**
   - Add Jmix dependencies
   - Create database entities
   - Configure email system

### Questions to Answer

1. **Email Provider**: Which email service will you use?
   - Gmail (for testing)
   - SendGrid
   - AWS SES
   - Mailgun
   - Other?

2. **Social Login**: Which providers do you want?
   - Google ✅
   - GitHub ✅
   - Facebook?
   - Microsoft?
   - Apple?

3. **2FA**: Should it be optional or mandatory?
   - Optional for all users
   - Mandatory for admin users
   - Mandatory for all users

4. **Password Expiry**: Do you want password expiration?
   - Yes, every 90 days
   - No, only on security events
   - Configurable per user/role

5. **GDPR Compliance**: Do you need GDPR features?
   - Data export
   - Data deletion
   - Consent management
   - Privacy policy acceptance

---

## 📚 Resources

### Jmix Documentation
- [Jmix Email Add-on](https://docs.jmix.io/jmix/email/index.html)
- [Jmix OIDC Add-on](https://docs.jmix.io/jmix/oidc/index.html)
- [Jmix Audit Add-on](https://docs.jmix.io/jmix/audit/index.html)
- [Jmix Security](https://docs.jmix.io/jmix/security/index.html)

### Security Standards
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [NIST Password Guidelines](https://pages.nist.gov/800-63-3/)
- [OAuth 2.0 Best Practices](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics)
- [GDPR Compliance](https://gdpr.eu/)

### Libraries
- [Google Authenticator](https://github.com/wstrange/GoogleAuth)
- [ZXing QR Code](https://github.com/zxing/zxing)
- [Spring Security](https://spring.io/projects/spring-security)

---

## ✅ Summary

This design provides a **comprehensive, secure, and user-friendly** self-service portal using:

1. **Jmix Add-ons**
   - Email for verification and notifications
   - OIDC for social login
   - Audit for tracking and compliance

2. **Security Best Practices**
   - Strong password policies
   - Two-factor authentication
   - Secure session management
   - Account lockout protection
   - Audit logging

3. **User Features**
   - Easy registration with email verification
   - Multiple login options (password + social)
   - Self-service password reset
   - Profile management
   - Session management
   - 2FA for enhanced security

4. **Developer-Friendly**
   - Clean architecture
   - Well-documented
   - Testable
   - Maintainable
   - Extensible

**Ready to implement?** Let's start with Phase 1! 🚀

---

**Last Updated**: 2024  
**Status**: 📋 DESIGN COMPLETE - READY FOR IMPLEMENTATION  
**Estimated Timeline**: 8 weeks  
**Team Size**: 2-3 developers