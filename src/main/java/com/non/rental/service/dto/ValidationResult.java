package com.non.rental.service.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object representing the result of rental analysis validation.
 * Contains validation status, error messages, and warning messages.
 * 
 * <p>This class distinguishes between:</p>
 * <ul>
 *   <li><b>Errors:</b> Critical issues that prevent calculations (missing required fields, invalid values)</li>
 *   <li><b>Warnings:</b> Potential concerns that don't prevent calculations (unusual values, risky metrics)</li>
 * </ul>
 * 
 * @author Rental Analysis System
 * @version 1.0
 */
public class ValidationResult {

    /**
     * Indicates whether the analysis passed validation (no errors)
     * True = valid (may have warnings), False = invalid (has errors)
     */
    private final boolean valid;

    /**
     * List of error messages describing validation failures
     * Empty if validation passed
     */
    private final List<String> errors;

    /**
     * List of warning messages describing potential concerns
     * Empty if no warnings
     */
    private final List<String> warnings;

    /**
     * Creates a new validation result.
     * 
     * @param valid whether validation passed
     * @param errors list of error messages (will be copied to prevent modification)
     * @param warnings list of warning messages (will be copied to prevent modification)
     */
    public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
    }

    /**
     * Creates a valid validation result with no errors or warnings.
     * 
     * @return valid result
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Creates a valid validation result with warnings.
     * 
     * @param warnings list of warning messages
     * @return valid result with warnings
     */
    public static ValidationResult validWithWarnings(List<String> warnings) {
        return new ValidationResult(true, Collections.emptyList(), warnings);
    }

    /**
     * Creates an invalid validation result with errors.
     * 
     * @param errors list of error messages
     * @return invalid result
     */
    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, errors, Collections.emptyList());
    }

    /**
     * Creates an invalid validation result with errors and warnings.
     * 
     * @param errors list of error messages
     * @param warnings list of warning messages
     * @return invalid result with errors and warnings
     */
    public static ValidationResult invalid(List<String> errors, List<String> warnings) {
        return new ValidationResult(false, errors, warnings);
    }

    /**
     * Checks if the validation passed (no errors).
     * 
     * @return true if valid, false if invalid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the list of error messages.
     * 
     * @return unmodifiable list of errors (empty if valid)
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Gets the list of warning messages.
     * 
     * @return unmodifiable list of warnings (empty if no warnings)
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Checks if there are any errors.
     * 
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Checks if there are any warnings.
     * 
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Gets the total number of errors.
     * 
     * @return error count
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Gets the total number of warnings.
     * 
     * @return warning count
     */
    public int getWarningCount() {
        return warnings.size();
    }

    /**
     * Gets a summary message describing the validation result.
     * 
     * @return summary string
     */
    public String getSummary() {
        if (valid && !hasWarnings()) {
            return "Validation passed with no issues";
        } else if (valid && hasWarnings()) {
            return String.format("Validation passed with %d warning(s)", warnings.size());
        } else {
            return String.format("Validation failed with %d error(s) and %d warning(s)", 
                               errors.size(), warnings.size());
        }
    }

    /**
     * Gets all messages (errors and warnings) combined.
     * 
     * @return list of all messages
     */
    public List<String> getAllMessages() {
        List<String> allMessages = new ArrayList<>();
        errors.forEach(error -> allMessages.add("ERROR: " + error));
        warnings.forEach(warning -> allMessages.add("WARNING: " + warning));
        return allMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid &&
               Objects.equals(errors, that.errors) &&
               Objects.equals(warnings, that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errors, warnings);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult{");
        sb.append("valid=").append(valid);
        
        if (!errors.isEmpty()) {
            sb.append(", errors=[");
            sb.append(String.join("; ", errors));
            sb.append("]");
        }
        
        if (!warnings.isEmpty()) {
            sb.append(", warnings=[");
            sb.append(String.join("; ", warnings));
            sb.append("]");
        }
        
        sb.append("}");
        return sb.toString();
    }

    /**
     * Builder class for constructing ValidationResult instances.
     * Provides a fluent API for adding errors and warnings.
     */
    public static class Builder {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        /**
         * Adds an error message.
         * 
         * @param error the error message
         * @return this builder
         */
        public Builder addError(String error) {
            if (error != null && !error.trim().isEmpty()) {
                errors.add(error);
            }
            return this;
        }

        /**
         * Adds multiple error messages.
         * 
         * @param errors the error messages
         * @return this builder
         */
        public Builder addErrors(List<String> errors) {
            if (errors != null) {
                errors.forEach(this::addError);
            }
            return this;
        }

        /**
         * Adds a warning message.
         * 
         * @param warning the warning message
         * @return this builder
         */
        public Builder addWarning(String warning) {
            if (warning != null && !warning.trim().isEmpty()) {
                warnings.add(warning);
            }
            return this;
        }

        /**
         * Adds multiple warning messages.
         * 
         * @param warnings the warning messages
         * @return this builder
         */
        public Builder addWarnings(List<String> warnings) {
            if (warnings != null) {
                warnings.forEach(this::addWarning);
            }
            return this;
        }

        /**
         * Builds the ValidationResult.
         * Result is valid if there are no errors.
         * 
         * @return the validation result
         */
        public ValidationResult build() {
            boolean valid = errors.isEmpty();
            return new ValidationResult(valid, errors, warnings);
        }
    }
}