package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.List;

@Route("vaadin-checkbox-group/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<CheckboxGroup<String>> {
    public static final String REQUIRED_BUTTON = "required-button";

    public BasicValidationPage() {
        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));
    }

    @Override
    protected CheckboxGroup<String> createTestField() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
        checkboxGroup.setItems(List.of("foo", "bar", "baz"));

        return checkboxGroup;
    }
}
