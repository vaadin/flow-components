/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton.validation;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BasicValidationTest
        extends AbstractBasicValidationTest<RadioButtonGroup<String>, String> {
    protected RadioButtonGroup<String> createTestField() {
        return new RadioButtonGroup<String>();
    }
}
