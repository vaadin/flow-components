/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test.validation;

import java.util.List;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBoxI18n;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-multi-select-combo-box/validation/basic")
public class MultiSelectComboBoxBasicValidationPage
        extends AbstractValidationPage<MultiSelectComboBox<String>> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String ENABLE_CUSTOM_VALUE_BUTTON = "enable-custom-value-button";

    public MultiSelectComboBoxBasicValidationPage() {
        testField.setI18n(new MultiSelectComboBoxI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));

        add(createButton(ENABLE_CUSTOM_VALUE_BUTTON, "Enable custom values",
                event -> {
                    testField.setAllowCustomValue(true);
                    testField.addCustomValueSetListener(e -> {
                        testField.select(e.getDetail());
                    });
                }));
    }

    @Override
    protected MultiSelectComboBox<String> createTestField() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>() {
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
