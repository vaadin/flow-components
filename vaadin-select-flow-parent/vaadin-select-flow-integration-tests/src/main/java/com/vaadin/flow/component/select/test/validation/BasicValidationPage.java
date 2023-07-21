package com.vaadin.flow.component.select.test.validation;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.Arrays;
import java.util.List;

@Route("vaadin-select/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<Select<String>> {
    public static final String REQUIRED_BUTTON = "required-button";

    public BasicValidationPage() {
        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));
    }

    @Override
    protected Select<String> createTestField() {
        Select<String> select = new Select<>();
        select.setItems(Arrays.asList("foo", "bar", "baz"));
        select.setEmptySelectionAllowed(true);
        
        return select;
    }
}
