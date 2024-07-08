/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-big-decimal-field/validation/basic")
public class BigDecimalFieldBasicValidationPage
        extends AbstractValidationPage<BigDecimalField> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public BigDecimalFieldBasicValidationPage() {
        super();

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));
    }

    protected BigDecimalField createTestField() {
        return new BigDecimalField();
    }
}
