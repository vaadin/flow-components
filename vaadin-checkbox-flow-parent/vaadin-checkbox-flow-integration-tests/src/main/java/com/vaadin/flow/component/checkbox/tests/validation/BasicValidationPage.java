package com.vaadin.flow.component.checkbox.tests.validation;

import java.util.Arrays;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox-group/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<CheckboxGroup<String>> {
    public static final String REQUIRED_BUTTON = "required-button";

    public BasicValidationPage() {
        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));
    }

    @Override
    protected CheckboxGroup<String> createTestField() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        checkboxGroup.setItems(Arrays.asList("foo", "bar", "baz"));

        return checkboxGroup;
    }
}
