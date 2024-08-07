package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox/validation/basic")
public class CheckboxBasicValidationPage
        extends AbstractValidationPage<Checkbox> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";

    public CheckboxBasicValidationPage() {
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));
    }

    @Override
    protected Checkbox createTestField() {
        return new Checkbox("Checkbox") {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
    }
}
