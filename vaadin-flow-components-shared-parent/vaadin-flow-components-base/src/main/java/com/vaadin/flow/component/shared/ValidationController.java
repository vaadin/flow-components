package com.vaadin.flow.component.shared;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
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
public class ValidationController<C extends Component & HasValidator<V> & HasValidation, V>
        implements Serializable {
    private C component;
    private boolean manualValidationEnabled;
    private String lastValidationResultErrorMessage;

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
            setComponentInvalid(true);
            setComponentErrorMessage(result.getErrorMessage());
        } else {
            setComponentInvalid(false);
            setComponentErrorMessage("");
        }
    }

    private void setComponentInvalid(boolean invalid) {
        component.setInvalid(invalid);
    }

    private void setComponentErrorMessage(String errorMessage) {
        boolean hasCustomErrorMessage = component.getErrorMessage() != null
                && !component.getErrorMessage().isEmpty()
                && !component.getErrorMessage()
                        .equals(lastValidationResultErrorMessage);

        // Avoid overwriting a custom error message that might have been
        // set by the developer using the same component API.
        if (!hasCustomErrorMessage) {
            component.setErrorMessage(errorMessage);
        }

        lastValidationResultErrorMessage = errorMessage;
    }
}
