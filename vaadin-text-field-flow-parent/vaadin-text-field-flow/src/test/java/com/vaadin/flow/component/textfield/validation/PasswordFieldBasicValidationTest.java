/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.validation;

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class PasswordFieldBasicValidationTest
        extends AbstractBasicValidationTest<PasswordField, String> {
    protected PasswordField createTestField() {
        return new PasswordField();
    }
}
