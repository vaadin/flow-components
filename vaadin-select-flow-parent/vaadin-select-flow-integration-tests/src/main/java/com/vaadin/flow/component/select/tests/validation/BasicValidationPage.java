package com.vaadin.flow.component.select.tests.validation;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.List;

@Route("vaadin-select/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<Select<String>> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";

    public BasicValidationPage() {
        testField.setI18n(new Select.SelectI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));
    }

    @Override
    protected Select<String> createTestField() {
        Select<String> select = new Select<>() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
        select.setItems(List.of("foo", "bar", "baz"));
        select.setEmptySelectionAllowed(true);
        return select;
    }
}
