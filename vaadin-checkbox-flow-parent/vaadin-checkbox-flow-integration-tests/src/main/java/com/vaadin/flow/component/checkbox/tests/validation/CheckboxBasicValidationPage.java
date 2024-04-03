package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox/validation/basic")
public class CheckboxBasicValidationPage
        extends AbstractValidationPage<Checkbox> {
    public static final String REQUIRED_BUTTON = "required-button";

    public CheckboxBasicValidationPage() {
        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));
    }

    @Override
    protected Checkbox createTestField() {
        Checkbox checkbox = new Checkbox() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };

        return checkbox;
    }
}
