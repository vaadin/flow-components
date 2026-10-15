/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.validation;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class TextFieldBasicValidationTest
        extends AbstractBasicValidationTest<TextField, String> {
    protected TextField createTestField() {
        return new TextField();
    }
}
