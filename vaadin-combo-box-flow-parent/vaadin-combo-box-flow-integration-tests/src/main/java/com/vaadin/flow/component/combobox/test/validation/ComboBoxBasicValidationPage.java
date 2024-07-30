package com.vaadin.flow.component.combobox.test.validation;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

import java.util.List;

@Route("vaadin-combo-box/validation/basic")
public class ComboBoxBasicValidationPage
        extends AbstractValidationPage<ComboBox<String>> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String ENABLE_CUSTOM_VALUE_BUTTON = "enable-custom-value-button";

    public ComboBoxBasicValidationPage() {
        testField.setI18n(new ComboBox.ComboBoxI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));

        add(createButton(ENABLE_CUSTOM_VALUE_BUTTON, "Enable custom values",
                event -> {
                    testField.setAllowCustomValue(true);
                    testField.addCustomValueSetListener(e -> {
                        testField.setValue(e.getDetail());
                    });
                }));
    }

    @Override
    protected ComboBox<String> createTestField() {
        ComboBox<String> comboBox = new ComboBox<>() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
        comboBox.setItems(List.of("foo", "bar", "baz"));

        return comboBox;
    }
}
