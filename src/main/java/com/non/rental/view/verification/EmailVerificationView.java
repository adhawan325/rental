package com.non.rental.view.verification;

import com.non.rental.service.RegistrationService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Email Verification View - The Jmix Way
 * Handles email verification via URL parameter (no REST API needed).
 * Example: http://localhost:8080/verify?token=abc123
 */
@Route("verify")
@ViewController("rental_EmailVerificationView")
@ViewDescriptor("email-verification-view.xml")
@AnonymousAllowed
public class EmailVerificationView extends StandardView implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationView.class);

    @Autowired
    private RegistrationService registrationService;

    @ViewComponent
    private VerticalLayout contentLayout;

    @ViewComponent
    private Icon statusIcon;

    @ViewComponent
    private H2 statusTitle;

    @ViewComponent
    private Paragraph statusMessage;

    @ViewComponent
    private Button actionButton;

    private String token;
    private boolean verificationSuccess = false;

    /**
     * This method is called before the view is displayed.
     * We extract the token from URL query parameters and verify it.
     * This is the Jmix way - no REST API needed!
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parameters = queryParameters.getParameters();

        if (parameters.containsKey("token")) {
            token = parameters.get("token").get(0);
            log.info("Email verification requested with token");
            verifyToken();
        } else {
            log.warn("Email verification accessed without token");
            showError("No verification token provided");
        }
    }

    @Subscribe
    public void onInit(InitEvent event) {
        // Initial state will be updated in beforeEnter
    }

    private void verifyToken() {
        try {
            // Call service directly (the Jmix way - no REST API!)
            boolean success = registrationService.verifyEmail(token);

            if (success) {
                showSuccess();
            } else {
                showError("Invalid or expired verification token");
            }
        } catch (Exception e) {
            log.error("Email verification failed", e);
            showError("Verification failed. Please try again or contact support.");
        }
    }

    private void showSuccess() {
        verificationSuccess = true;

        statusIcon.setIcon(VaadinIcon.CHECK_CIRCLE);
        statusIcon.setColor("var(--lumo-success-color)");
        statusIcon.setSize("64px");

        statusTitle.setText("Email Verified Successfully!");
        statusTitle.getStyle().set("color", "var(--lumo-success-color)");

        statusMessage.setText(
                "Your email has been verified and your account is now active. " +
                "You can now sign in and start using yldz.io."
        );

        actionButton.setText("Go to Login");
        actionButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        actionButton.setVisible(true);

        log.info("Email verification successful");
    }

    private void showError(String message) {
        verificationSuccess = false;

        statusIcon.setIcon(VaadinIcon.CLOSE_CIRCLE);
        statusIcon.setColor("var(--lumo-error-color)");
        statusIcon.setSize("64px");

        statusTitle.setText("Verification Failed");
        statusTitle.getStyle().set("color", "var(--lumo-error-color)");

        statusMessage.setText(message);

        actionButton.setText("Back to Home");
        actionButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        actionButton.setVisible(true);

        log.warn("Email verification failed: {}", message);
    }

    @Subscribe("actionButton")
    public void onActionButtonClick(ClickEvent<Button> event) {
        if (verificationSuccess) {
            // Navigate to login
            getUI().ifPresent(ui -> ui.navigate("login"));
        } else {
            // Navigate to home/registration
            getUI().ifPresent(ui -> ui.navigate("register"));
        }
    }
}