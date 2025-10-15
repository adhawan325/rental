# 🚀 User Portal - Quick Start Guide

## Overview

This guide helps you quickly implement the user self-service portal for the Rental application.

---

## 📦 What You're Building

A complete self-service portal with:

- ✅ **User Registration** with email verification
- ✅ **Multi-Auth Login** (password + Google/GitHub)
- ✅ **Password Management** (reset, change, policies)
- ✅ **Two-Factor Authentication** (TOTP)
- ✅ **Profile Management** (edit info, preferences)
- ✅ **Session Management** (view/revoke sessions)
- ✅ **Security Features** (lockout, audit, history)

---

## 🎯 Quick Decision Matrix

Before starting, answer these questions:

| Question | Options | Recommendation |
|----------|---------|----------------|
| **Email Provider?** | Gmail, SendGrid, AWS SES, Mailgun | SendGrid (production), Gmail (testing) |
| **Social Login?** | Google, GitHub, Facebook, Microsoft | Google + GitHub (most common) |
| **2FA Mandatory?** | Optional, Mandatory for admins, Mandatory for all | Optional (better UX) |
| **Password Expiry?** | Yes (90 days), No, Configurable | No (NIST recommends against) |
| **GDPR Compliance?** | Yes, No | Yes (if EU users) |

---

## 📋 Prerequisites

### 1. **Jmix Version**
- ✅ Jmix 2.6.2 (you have this)
- ✅ Java 17+ (you have this)
- ✅ PostgreSQL (you have this)

### 2. **External Services**

#### Email Service (Choose One)
```bash
# Option 1: Gmail (for testing)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password  # Generate in Google Account settings

# Option 2: SendGrid (for production)
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
```

#### OAuth2 Providers (Optional)

**Google OAuth2**:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create project → Enable Google+ API
3. Create OAuth 2.0 credentials
4. Add redirect URI: `http://localhost:8080/login/oauth2/code/google`
5. Copy Client ID and Secret

**GitHub OAuth2**:
1. Go to [GitHub Settings → Developer Settings](https://github.com/settings/developers)
2. New OAuth App
3. Add callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Copy Client ID and Secret

---

## 🚀 Implementation Steps

### Step 1: Add Dependencies (5 minutes)

Edit `build.gradle`:

```gradle
dependencies {
    // ... existing dependencies ...
    
    // NEW: Email Add-on
    implementation 'io.jmix.email:jmix-email-starter'
    implementation 'io.jmix.email:jmix-email-flowui-starter'
    
    // NEW: OIDC Add-on (for social login)
    implementation 'io.jmix.oidc:jmix-oidc-starter'
    
    // NEW: Audit Add-on
    implementation 'io.jmix.audit:jmix-audit-starter'
    implementation 'io.jmix.audit:jmix-audit-flowui-starter'
    
    // NEW: 2FA Support
    implementation 'com.warrenstrange:googleauth:1.5.0'
    implementation 'com.google.zxing:core:3.5.2'
    implementation 'com.google.zxing:javase:3.5.2'
}
```

Run:
```bash
./gradlew clean build
```

---

### Step 2: Configure Email (10 minutes)

Add to `application.properties`:

```properties
# Email Configuration
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

jmix.email.from-address=${MAIL_FROM:noreply@yourdomain.com}
jmix.email.from-name=Rental Application
```

Create `.env` file:
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@yourdomain.com
```

Test email:
```java
@Autowired
private EmailSender emailSender;

public void testEmail() {
    emailSender.sendEmail(
        EmailInfo.builder()
            .setAddresses("test@example.com")
            .setSubject("Test Email")
            .setBody("Hello from Rental App!")
            .build()
    );
}
```

---

### Step 3: Configure OAuth2 (15 minutes)

Add to `application.properties`:

```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.scope=openid,profile,email

# GitHub OAuth2
spring.security.oauth2.client.registration.github.client-id=${GITHUB_CLIENT_ID}
spring.security.oauth2.client.registration.github.client-secret=${GITHUB_CLIENT_SECRET}
spring.security.oauth2.client.registration.github.scope=read:user,user:email
```

Add to `.env`:
```bash
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
```

---

### Step 4: Create Database Entities (30 minutes)

Use Jmix Studio to create these entities:

#### 1. EmailVerificationToken
```java
@Entity
@Table(name = "EMAIL_VERIFICATION_TOKEN")
public class EmailVerificationToken {
    @Id @GeneratedValue private UUID id;
    @ManyToOne private User user;
    @Column(unique = true) private String token;
    @Column private TokenType tokenType;
    @Column private LocalDateTime expiryDate;
    @Column private Boolean used = false;
}
```

#### 2. PasswordHistory
```java
@Entity
@Table(name = "PASSWORD_HISTORY")
public class PasswordHistory {
    @Id @GeneratedValue private UUID id;
    @ManyToOne private User user;
    @Column private String passwordHash;
    @Column private LocalDateTime changedDate;
}
```

#### 3. TwoFactorAuth
```java
@Entity
@Table(name = "TWO_FACTOR_AUTH")
public class TwoFactorAuth {
    @Id @GeneratedValue private UUID id;
    @OneToOne private User user;
    @Column private String secretKey;
    @Column private Boolean enabled = false;
    @Column private String backupCodes;
}
```

#### 4. UserSession
```java
@Entity
@Table(name = "USER_SESSION")
public class UserSession {
    @Id @GeneratedValue private UUID id;
    @ManyToOne private User user;
    @Column(unique = true) private String sessionId;
    @Column private String ipAddress;
    @Column private String userAgent;
    @Column private LocalDateTime createdDate;
    @Column private Boolean active = true;
}
```

#### 5. LoginHistory
```java
@Entity
@Table(name = "LOGIN_HISTORY")
public class LoginHistory {
    @Id @GeneratedValue private UUID id;
    @ManyToOne private User user;
    @Column private String username;
    @Column private String ipAddress;
    @Column private LocalDateTime loginDate;
    @Column private Boolean success;
    @Column private String failureReason;
}
```

#### 6. Enhance User Entity
Add these fields to existing `User.java`:

```java
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

enum AccountStatus {
    PENDING, ACTIVE, LOCKED, SUSPENDED, DELETED
}
```

Generate Liquibase changelog and run:
```bash
./gradlew update
```

---

### Step 5: Create Core Services (1-2 hours)

#### RegistrationService.java
```java
@Service
public class RegistrationService {
    
    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Transactional
    public User registerUser(RegistrationRequest request) {
        // Validate
        validateRegistration(request);
        
        // Create user
        User user = dataManager.create(User.class);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setAccountStatus(AccountStatus.PENDING);
        user.setEmailVerified(false);
        user.setActive(false);
        
        user = dataManager.save(user);
        
        // Generate verification token
        String token = generateVerificationToken(user);
        
        // Send verification email
        emailService.sendVerificationEmail(user, token);
        
        return user;
    }
    
    private String generateVerificationToken(User user) {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().encodeToString(tokenBytes);
        
        EmailVerificationToken verificationToken = dataManager.create(EmailVerificationToken.class);
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setTokenType(TokenType.EMAIL_VERIFICATION);
        verificationToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        verificationToken.setUsed(false);
        
        dataManager.save(verificationToken);
        
        return token;
    }
}
```

#### PasswordService.java
```java
@Service
public class PasswordService {
    
    @Autowired
    private DataManager dataManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password
        validatePassword(newPassword, user);
        
        // Check password history
        if (isPasswordInHistory(user, newPassword)) {
            throw new IllegalArgumentException("Cannot reuse recent passwords");
        }
        
        // Update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setPasswordChangedDate(LocalDateTime.now());
        dataManager.save(user);
        
        // Save to history
        savePasswordHistory(user, encodedPassword);
        
        // Invalidate all sessions
        invalidateAllSessions(user);
    }
    
    private boolean isPasswordInHistory(User user, String password) {
        List<PasswordHistory> history = dataManager.load(PasswordHistory.class)
            .query("select h from PasswordHistory h where h.user = :user order by h.changedDate desc")
            .parameter("user", user)
            .maxResults(5)
            .list();
        
        return history.stream()
            .anyMatch(h -> passwordEncoder.matches(password, h.getPasswordHash()));
    }
}
```

---

### Step 6: Create Views (2-3 hours)

#### RegistrationView.java
```java
@Route("register")
@ViewController("rental_RegistrationView")
@ViewDescriptor("registration-view.xml")
public class RegistrationView extends StandardView {
    
    @ViewComponent
    private TextField<String> usernameField;
    
    @ViewComponent
    private EmailField emailField;
    
    @ViewComponent
    private PasswordField passwordField;
    
    @ViewComponent
    private PasswordField confirmPasswordField;
    
    @Autowired
    private RegistrationService registrationService;
    
    @Subscribe("registerBtn")
    public void onRegisterBtnClick(ClickEvent<Button> event) {
        try {
            // Validate
            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                notifications.create("Passwords do not match")
                    .withType(Notifications.Type.ERROR)
                    .show();
                return;
            }
            
            // Register
            RegistrationRequest request = new RegistrationRequest();
            request.setUsername(usernameField.getValue());
            request.setEmail(emailField.getValue());
            request.setPassword(passwordField.getValue());
            
            registrationService.registerUser(request);
            
            // Success
            notifications.create("Registration successful! Please check your email.")
                .withType(Notifications.Type.SUCCESS)
                .show();
            
            // Redirect to login
            ViewNavigators.view(this, LoginView.class).navigate();
            
        } catch (Exception e) {
            notifications.create("Registration failed: " + e.getMessage())
                .withType(Notifications.Type.ERROR)
                .show();
        }
    }
}
```

---

## 🧪 Testing

### Test Email Configuration
```bash
# Start application
./gradlew bootRun

# Check logs for email configuration
# Should see: "Mail server configured: smtp.gmail.com:587"
```

### Test Registration Flow
1. Navigate to `http://localhost:8080/register`
2. Fill in registration form
3. Submit
4. Check email for verification link
5. Click link to verify
6. Login with credentials

### Test OAuth2 Login
1. Navigate to `http://localhost:8080/login`
2. Click "Login with Google"
3. Authorize on Google
4. Should be logged in

---

## 📊 Implementation Timeline

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| **Phase 1: Foundation** | 1-2 weeks | Add-ons installed, DB schema, email working |
| **Phase 2: Registration** | 1 week | Registration + email verification |
| **Phase 3: Authentication** | 1 week | Multi-auth login, social login |
| **Phase 4: Password Mgmt** | 1 week | Reset, change, policies |
| **Phase 5: 2FA** | 1 week | TOTP setup and login |
| **Phase 6: Profile/Sessions** | 1 week | Profile management, session control |
| **Phase 7: Testing** | 1 week | Security audit, UAT |
| **Total** | **7-8 weeks** | **Production-ready portal** |

---

## 🔒 Security Checklist

Before going to production:

- [ ] HTTPS enabled
- [ ] Strong password policy enforced
- [ ] Account lockout configured
- [ ] Email verification required
- [ ] Session timeout configured
- [ ] CSRF protection enabled
- [ ] Security headers configured
- [ ] Rate limiting implemented
- [ ] Audit logging enabled
- [ ] Error messages don't reveal user existence
- [ ] Tokens are cryptographically secure
- [ ] Passwords are hashed with BCrypt
- [ ] OAuth2 redirect URIs validated
- [ ] 2FA available (optional or mandatory)
- [ ] Session management tested under load

---

## 📚 Resources

### Documentation
- **Full Design**: [USER_PORTAL_DESIGN.md](USER_PORTAL_DESIGN.md)
- **Jmix Email**: https://docs.jmix.io/jmix/email/
- **Jmix OIDC**: https://docs.jmix.io/jmix/oidc/
- **Jmix Security**: https://docs.jmix.io/jmix/security/

### External Services
- **Google OAuth2**: https://console.cloud.google.com/
- **GitHub OAuth2**: https://github.com/settings/developers
- **SendGrid**: https://sendgrid.com/
- **Mailtrap** (testing): https://mailtrap.io/

---

## 🆘 Troubleshooting

### Email Not Sending
```bash
# Check SMTP settings
spring.mail.host=smtp.gmail.com
spring.mail.port=587

# For Gmail, enable "Less secure app access" or use App Password
# https://myaccount.google.com/apppasswords

# Check logs
logging.level.org.springframework.mail=DEBUG
```

### OAuth2 Not Working
```bash
# Verify redirect URI matches exactly
# Google Console: http://localhost:8080/login/oauth2/code/google
# GitHub Settings: http://localhost:8080/login/oauth2/code/github

# Check client ID and secret
echo $GOOGLE_CLIENT_ID
echo $GOOGLE_CLIENT_SECRET
```

### Database Migration Fails
```bash
# Check Liquibase changelog
./gradlew update --info

# If needed, rollback and retry
./gradlew liquibaseRollback --liquibaseCommandValue=1
./gradlew update
```

---

## ✅ Next Steps

1. **Review the full design**: [USER_PORTAL_DESIGN.md](USER_PORTAL_DESIGN.md)
2. **Set up email service** (Gmail for testing, SendGrid for production)
3. **Configure OAuth2 providers** (Google, GitHub)
4. **Start Phase 1**: Add dependencies and create database schema
5. **Implement incrementally**: One phase at a time

---

## 💬 Questions?

Common questions answered in [USER_PORTAL_DESIGN.md](USER_PORTAL_DESIGN.md):

- Which email provider should I use?
- Should 2FA be mandatory?
- Do I need GDPR compliance?
- How do I handle password expiry?
- What about rate limiting?

---

**Ready to start?** Begin with Step 1: Add Dependencies! 🚀

---

**Last Updated**: 2024  
**Status**: ✅ READY TO IMPLEMENT  
**Estimated Time**: 7-8 weeks