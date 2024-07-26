package com.vaadin.flow.component.shared;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;

/**
 * An internal controller for managing the validation state of a component. Not
 * intended to be used publicly.
 *
 * @param <C>
 *            Type of the component that uses this controller.
 * @param <V>
 *            Type of the value of the extending component.
 */
public class ValidationController<C extends Component & HasValidator<V>, V>
        implements Serializable {
    private C component;
    private boolean manualValidationEnabled;
    private String customErrorMessage;
    private String validationResultErrorMessage;

    public ValidationController(C component) {
        this.component = component;
    }

    /**
     * Sets whether manual validation mode is enabled.
     * <p>
     * When enabled, the {@link #validate(V)} method skips validation, allowing
     * the invalid state and error messages to be controlled manually.
     *
     * @param enabled
     *            true to enable manual validation, false to disable
     */
    public void setManualValidation(boolean enabled) {
        this.manualValidationEnabled = enabled;
    }

    /**
     * Sets the invalid state for the component.
     *
     * @param invalid
     *            true to set the component's value as invalid, false otherwise.
     */
    public void setInvalid(boolean invalid) {
        component.getElement().setProperty("invalid", invalid);
    }

    /**
     * Gets the invalid state of the component.
     *
     * @return true if the component's value is invalid, false otherwise.
     */
    public boolean isInvalid() {
        return component.getElement().getProperty("invalid", false);
    }

    /**
     * Sets a custom error message to display for all validation failures.
     * <p>
     * This error message will override error messages from the component's
     * validator. If the parameter is set to {@code null}, the custom error
     * message will be cleared, and error messages from the validator will be
     * used again.
     *
     * @param errorMessage
     *            the error message to set, or {@code null} to clear
     */
    public void setCustomErrorMessage(String errorMessage) {
        customErrorMessage = errorMessage;
        updateErrorMessageProperty();
    }

    /**
     * Gets the custom error message displayed for all validation failures.
     *
     * @return the error message
     */
    public String getCustomErrorMessage() {
        return customErrorMessage;
    }

    private void setValidationResultErrorMessage(String errorMessage) {
        validationResultErrorMessage = errorMessage;
        updateErrorMessageProperty();
    }

    private void updateErrorMessageProperty() {
        String errorMessage = customErrorMessage;
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = validationResultErrorMessage;
        }
        component.getElement().setProperty("errorMessage", errorMessage);
    }

    /**
     * Validates the given value using the component's validator and sets the
     * {@code invalid} and {@code errorMessage} properties based on the result.
     * If a custom error message is provided with
     * {@link #setErrorMessage(String)}, it is used. Otherwise, the error
     * message from the validator is used.
     * <p>
     * The method skips validation if the manual validation mode is enabled, see
     * {@link #setManualValidation(boolean)}.
     *
     * @param value
     *            the value to validate
     */
    public void validate(V value) {
        if (manualValidationEnabled) {
            return;
        }

        Validator<V> validator = component.getDefaultValidator();
        ValidationResult result = validator.apply(value, null);
        if (result.isError()) {
            setInvalid(true);
            setValidationResultErrorMessage(result.getErrorMessage());
        } else {
            setInvalid(false);
            setValidationResultErrorMessage("");
        }
    }
}
