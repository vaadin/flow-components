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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-combo-box/validation/initial-value")
public class ComboBoxInitialValueValidationPage
        extends AbstractValidationPage<ComboBox<String>> {
    public static final String SET_NULL = "set-null-button";
    public static final String SET_EMPTY_STRING = "set-empty-string-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";

    public ComboBoxInitialValueValidationPage() {
        testField.setI18n(new ComboBox.ComboBoxI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(SET_NULL, "Set null", event -> {
            testField.setValue(null);
        }));

        add(createButton(SET_EMPTY_STRING, "Set empty string", event -> {
            testField.setValue("");
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
        comboBox.setRequired(true);

        return comboBox;
    }
}
