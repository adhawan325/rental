package com.non.rental.service;

import com.non.rental.entity.EmailVerificationToken;
import com.non.rental.entity.User;
import com.non.rental.security.RentalUserRole;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.email.Emailer;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Service for user registration following Jmix best practices.
 * Uses DataManager for data access and Jmix Email add-on for sending emails.
 */
@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final DataManager dataManager;
    private final PasswordEncoder passwordEncoder;
    private final Emailer emailer;
    private final SystemAuthenticator systemAuthenticator;

    @Value("${rental.registration.token-expiry-hours:168}")
    private int tokenExpiryHours;

    @Value("${rental.registration.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${jmix.email.from-address}")
    private String fromAddress;

    public RegistrationService(DataManager dataManager,
                               PasswordEncoder passwordEncoder,
                               Emailer emailer,
                               SystemAuthenticator systemAuthenticator) {
        this.dataManager = dataManager;
        this.passwordEncoder = passwordEncoder;
        this.emailer = emailer;
        this.systemAuthenticator = systemAuthenticator;
    }

    /**
     * Register a new user with email verification.
     * This is the main entry point for user registration.
     */
    @Transactional
    public User registerUser(String username, String email, String password, 
                            String firstName, String lastName) {
        
        log.info("Starting registration for user: {}, email: {}", username, email);

        // Validate input
        validateRegistrationInput(username, email, password);

        // Execute registration with system privileges to bypass security constraints
        return systemAuthenticator.withSystem(() -> {
            // Check for duplicates
            checkDuplicateUser(username, email);

            // Create user entity (the Jmix way)
            User user = dataManager.create(User.class);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setActive(false); // Not active until email verified
            user.setEmailVerified(false);
            user.setAccountStatus("PENDING");
            user.setRegistrationDate(LocalDateTime.now());

            // Save user using DataManager (Jmix way)
            user = dataManager.save(user);
            log.info("User created with ID: {}", user.getId());

            // Generate verification token
            String token = generateVerificationToken(user);
            log.info("Verification token generated for user: {}", username);

            // Send verification email
            sendVerificationEmail(user, token);
            log.info("Verification email sent to: {}", email);

            return user;
        });
    }

    /**
     * Verify email using token.
     */
    @Transactional
    public boolean verifyEmail(String token) {
        log.info("Attempting to verify email with token");

        // Execute with system privileges to bypass security constraints
        return systemAuthenticator.withSystem(() -> {
            // Find token using DataManager (Jmix way)
            List<EmailVerificationToken> tokens = dataManager.load(EmailVerificationToken.class)
                    .query("select e from EmailVerificationToken e where e.token = :token " +
                           "and e.tokenType = 'EMAIL_VERIFICATION'")
                    .parameter("token", token)
                    .list();

            if (tokens.isEmpty()) {
                log.warn("Token not found: {}", token);
                return false;
            }

            EmailVerificationToken verificationToken = tokens.get(0);

            // Validate token
            if (!verificationToken.isValid()) {
                log.warn("Token is invalid (used or expired): {}", token);
                return false;
            }

            // Mark token as used
            verificationToken.setUsed(true);
            verificationToken.setUsedDate(LocalDateTime.now());
            dataManager.save(verificationToken);

            // Activate user account
            User user = verificationToken.getUser();
            user.setEmailVerified(true);
            user.setEmailVerifiedDate(LocalDateTime.now());
            user.setAccountStatus("ACTIVE");
            user.setActive(true);
            dataManager.save(user);

            // Assign default roles to the user
            assignDefaultRoles(user);

            log.info("Email verified successfully for user: {}", user.getUsername());
            return true;
        });
    }

    /**
     * Resend verification email.
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        // Execute with system privileges to bypass security constraints
        systemAuthenticator.withSystem(() -> {
            // Find user by email
            List<User> users = dataManager.load(User.class)
                    .query("select u from rental_User u where u.email = :email")
                    .parameter("email", email)
                    .list();

            if (users.isEmpty()) {
                throw new IllegalArgumentException("User not found with email: " + email);
            }

            User user = users.get(0);

            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new IllegalStateException("Email already verified");
            }

            // Invalidate old tokens
            invalidateOldTokens(user);

            // Generate new token
            String token = generateVerificationToken(user);

            // Send email
            sendVerificationEmail(user, token);
            log.info("Verification email resent to: {}", email);
            
            return null;
        });
    }

    // ========== Private Helper Methods ==========

    private void validateRegistrationInput(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void checkDuplicateUser(String username, String email) {
        // Check username (Jmix DataManager way)
        Long usernameCount = dataManager.loadValue("select count(u) from rental_User u where u.username = :username", Long.class)
                .parameter("username", username)
                .one();

        if (usernameCount != null && usernameCount > 0) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check email
        Long emailCount = dataManager.loadValue("select count(u) from rental_User u where u.email = :email", Long.class)
                .parameter("email", email)
                .one();

        if (emailCount != null && emailCount > 0) {
            throw new IllegalArgumentException("Email already registered");
        }
    }

    private String generateVerificationToken(User user) {
        // Generate cryptographically secure token
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // Create token entity
        EmailVerificationToken verificationToken = dataManager.create(EmailVerificationToken.class);
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setTokenType("EMAIL_VERIFICATION");
        verificationToken.setCreatedDate(LocalDateTime.now());
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(tokenExpiryHours));
        verificationToken.setUsed(false);

        // Save using DataManager
        dataManager.save(verificationToken);

        return token;
    }

    private void sendVerificationEmail(User user, String token) {
        String verificationUrl = baseUrl + "/verify?token=" + token;

        String emailBody = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #2c3e50;">Welcome to yldz.io!</h2>
                        
                        <p>Hi %s,</p>
                        
                        <p>Thank you for registering with yldz.io. To complete your registration and activate your account, please verify your email address by clicking the button below:</p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #3498db; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                                Verify Email Address
                            </a>
                        </div>
                        
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="background-color: #f4f4f4; padding: 10px; border-radius: 5px; word-break: break-all;">
                            %s
                        </p>
                        
                        <p><strong>This link will expire in %d hours.</strong></p>
                        
                        <p>If you didn't create an account with us, please ignore this email.</p>
                        
                        <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                        
                        <p style="font-size: 12px; color: #777;">
                            This is an automated email. Please do not reply to this message.
                        </p>
                    </div>
                </body>
                </html>
                """,
                user.getFirstName() != null ? user.getFirstName() : user.getUsername(),
                verificationUrl,
                verificationUrl,
                tokenExpiryHours
        );

        // Use Jmix Emailer (the Jmix way)
        EmailInfo emailInfo = EmailInfoBuilder.create()
                .setAddresses(user.getEmail())
                .setSubject("Verify Your Email - yldz.io")
                .setFrom(fromAddress)
                .setBody(emailBody)
                .build();

        try {
            // Send email asynchronously using Jmix email queue
            emailer.sendEmailAsync(emailInfo);
            log.info("Verification email queued for: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private void invalidateOldTokens(User user) {
        // Find all unused tokens for this user
        List<EmailVerificationToken> oldTokens = dataManager.load(EmailVerificationToken.class)
                .query("select e from EmailVerificationToken e where e.user = :user " +
                       "and e.tokenType = 'EMAIL_VERIFICATION' and e.used = false")
                .parameter("user", user)
                .list();

        // Mark them as used
        for (EmailVerificationToken token : oldTokens) {
            token.setUsed(true);
            token.setUsedDate(LocalDateTime.now());
            dataManager.save(token);
        }
    }

    /**
     * Assign default roles to a newly registered user.
     * This method assigns:
     * - RentalUserRole: Grants access to all rental application features
     * - ui-minimal: Basic UI access role (required for login)
     */
    private void assignDefaultRoles(User user) {
        log.info("Assigning default roles to user: {}", user.getUsername());

        // Assign Rental User Role (application-specific role)
        assignRole(user, RentalUserRole.CODE, "resource");

        // Assign UI Minimal Role (required for UI login)
        assignRole(user, "ui-minimal", "resource");

        log.info("Default roles assigned successfully to user: {}", user.getUsername());
    }

    /**
     * Assign a specific role to a user.
     */
    private void assignRole(User user, String roleCode, String roleType) {
        // Check if role assignment already exists
        Long existingCount = dataManager.loadValue(
                "select count(ra) from sec_RoleAssignmentEntity ra " +
                "where ra.username = :username and ra.roleCode = :roleCode",
                Long.class)
                .parameter("username", user.getUsername())
                .parameter("roleCode", roleCode)
                .one();

        if (existingCount != null && existingCount > 0) {
            log.debug("Role {} already assigned to user: {}", roleCode, user.getUsername());
            return;
        }

        // Create new role assignment
        RoleAssignmentEntity roleAssignment = dataManager.create(RoleAssignmentEntity.class);
        roleAssignment.setUsername(user.getUsername());
        roleAssignment.setRoleCode(roleCode);
        roleAssignment.setRoleType(roleType);

        dataManager.save(roleAssignment);
        log.info("Assigned role {} to user: {}", roleCode, user.getUsername());
    }
}