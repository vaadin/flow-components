package com.vaadin.flow.component.radiobutton.tests.validation;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.List;

@Route("vaadin-radio-button-group/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<RadioButtonGroup<String>> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String SET_INVALID_BUTTON = "set-invalid-button";

    public BasicValidationPage() {
        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));

        add(createButton(SET_INVALID_BUTTON, "Set invalid state", event -> {
            testField.setInvalid(true);
        }));
    }

    @Override
    protected RadioButtonGroup<String> createTestField() {
        RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
        radioButtonGroup.setItems(List.of("foo", "bar", "baz"));
        radioButtonGroup.setLabel("Radio Button Group");

        return radioButtonGroup;
    }
}
