package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

public class ValidationController<C extends Component & HasValidator<V>, V> {
    private C component;
    private boolean manualValidationEnabled;
    private String customErrorMessage;
    private String validationResultErrorMessage;

    public ValidationController(C component) {
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
        customErrorMessage = errorMessage;
        updateErrorMessage();
    }

    public String getCustomErrorMessage() {
        return customErrorMessage;
    }

    private void setValidationResultErrorMessage(String errorMessage) {
        validationResultErrorMessage = errorMessage;
        updateErrorMessage();
    }

    private void updateErrorMessage() {
        String errorMessage = customErrorMessage;
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = validationResultErrorMessage;
        }
        component.getElement().setProperty("errorMessage", errorMessage);
    }

    public void validate(V value) {
        if (manualValidationEnabled) {
            return;
        }

        ValueContext context = new ValueContext(null, component);
        ValidationResult result = getDefaultValidator().apply(value, context);
        if (result.isError()) {
            setInvalid(true);
            setValidationResultErrorMessage(result.getErrorMessage());
        } else {
            setInvalid(false);
            setValidationResultErrorMessage("");
        }
    }

    private Validator<V> getDefaultValidator() {
        return component.getDefaultValidator();
    }
}
