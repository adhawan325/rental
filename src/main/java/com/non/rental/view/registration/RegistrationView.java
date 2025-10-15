package com.non.rental.view.registration;

import com.non.rental.service.RegistrationService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User Registration View - The Jmix Way
 * Uses Vaadin Flow UI components with server-side logic.
 * No REST APIs needed - direct service calls from the view.
 */
@Route("register")
@ViewController("rental_RegistrationView")
@ViewDescriptor("registration-view.xml")
@AnonymousAllowed
public class RegistrationView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(RegistrationView.class);

    @Autowired
    private RegistrationService registrationService;

    @ViewComponent
    private TextField usernameField;

    @ViewComponent
    private EmailField emailField;

    @ViewComponent
    private PasswordField passwordField;

    @ViewComponent
    private PasswordField confirmPasswordField;

    @ViewComponent
    private TextField firstNameField;

    @ViewComponent
    private TextField lastNameField;

    @ViewComponent
    private Button registerButton;

    @ViewComponent
    private Button loginButton;

    @Subscribe
    public void onInit(InitEvent event) {
        log.info("Registration view initialized");
    }

    @Subscribe("registerButton")
    public void onRegisterButtonClick(ClickEvent<Button> event) {
        try {
            // Validate input
            if (!validateInput()) {
                return;
            }

            // Register user (direct service call - the Jmix way!)
            registrationService.registerUser(
                    usernameField.getValue(),
                    emailField.getValue(),
                    passwordField.getValue(),
                    firstNameField.getValue(),
                    lastNameField.getValue()
            );

            // Show success message
            Notification notification = Notification.show(
                    "Registration successful! Please check your email to verify your account.",
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clear form
            clearForm();

            log.info("User registered successfully: {}", usernameField.getValue());

        } catch (IllegalArgumentException e) {
            log.warn("Registration validation failed: {}", e.getMessage());
            showError(e.getMessage());
        } catch (Exception e) {
            log.error("Registration failed", e);
            showError("Registration failed. Please try again later.");
        }
    }

    @Subscribe("loginButton")
    public void onLoginButtonClick(ClickEvent<Button> event) {
        // Navigate to login view
        getUI().ifPresent(ui -> ui.navigate("login"));
    }

    private boolean validateInput() {
        // Username validation
        if (usernameField.getValue() == null || usernameField.getValue().trim().isEmpty()) {
            showError("Username is required");
            usernameField.focus();
            return false;
        }

        if (usernameField.getValue().length() < 3) {
            showError("Username must be at least 3 characters");
            usernameField.focus();
            return false;
        }

        // Email validation
        if (emailField.getValue() == null || emailField.getValue().trim().isEmpty()) {
            showError("Email is required");
            emailField.focus();
            return false;
        }

        if (!emailField.getValue().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            emailField.focus();
            return false;
        }

        // Password validation
        if (passwordField.getValue() == null || passwordField.getValue().isEmpty()) {
            showError("Password is required");
            passwordField.focus();
            return false;
        }

        if (passwordField.getValue().length() < 8) {
            showError("Password must be at least 8 characters");
            passwordField.focus();
            return false;
        }

        // Confirm password validation
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            showError("Passwords do not match");
            confirmPasswordField.focus();
            return false;
        }

        // First name validation
        if (firstNameField.getValue() == null || firstNameField.getValue().trim().isEmpty()) {
            showError("First name is required");
            firstNameField.focus();
            return false;
        }

        // Last name validation
        if (lastNameField.getValue() == null || lastNameField.getValue().trim().isEmpty()) {
            showError("Last name is required");
            lastNameField.focus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Notification notification = Notification.show(
                message,
                3000,
                Notification.Position.TOP_CENTER
        );
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void clearForm() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        firstNameField.clear();
        lastNameField.clear();
    }
}