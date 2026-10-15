/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.select.validation;

import com.vaadin.flow.component.select.Select;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class BasicValidationTest
        extends AbstractBasicValidationTest<Select<String>, String> {
    protected Select<String> createTestField() {
        return new Select<String>();
    }
}
