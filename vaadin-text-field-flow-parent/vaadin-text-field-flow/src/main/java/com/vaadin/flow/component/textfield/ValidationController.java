package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public class ValidationController<T> {
    private Component component;
    private boolean manualValidationEnabled;
    private String customErrorMessage;
    private String constraintErrorMessage;

    public ValidationController(Component component) {
        this.component = component;
    }

    public void setManualValidation(boolean enabled) {
        this.manualValidationEnabled = enabled;
    }

    public void setInvalid(boolean invalid) {
        component.getElement().setProperty("invalid", invalid);
    }

    public boolean isInvalid() {
        return component.getElement().getProperty("invalid", false);
    }

    public void setCustomErrorMessage(String errorMessage) {
        this.customErrorMessage = errorMessage;
        updateErrorMessageProperty();
    }

    public String getCustomErrorMessage() {
        return customErrorMessage;
    }

    private void setConstraintErrorMessage(String errorMessage) {
        this.constraintErrorMessage = errorMessage;
        updateErrorMessageProperty();
    }

    private void updateErrorMessageProperty() {
        String errorMessage = customErrorMessage;
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = constraintErrorMessage;
        }
        component.getElement().setProperty("errorMessage", errorMessage);
    }

    protected ValidationResult checkValidity(T value, ValueContext context) {
        return ValidationResult.ok();
    }

    public void validate(T value) {
        if (manualValidationEnabled) {
            return;
        }

        ValueContext context = new ValueContext(null, component);
        ValidationResult result = checkValidity(value, context);
        if (result.isError()) {
            setInvalid(true);
            setConstraintErrorMessage(result.getErrorMessage());
        } else {
            setInvalid(false);
            setConstraintErrorMessage("");
        }
    }

    public Validator<T> getDefaultValidator() {
        return (value, context) -> checkValidity(value, context);
    }
}
