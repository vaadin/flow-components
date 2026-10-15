/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests.validation;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class CheckboxBasicValidationTest
        extends AbstractBasicValidationTest<Checkbox, Boolean> {
    protected Checkbox createTestField() {
        return new Checkbox();
    }
}
