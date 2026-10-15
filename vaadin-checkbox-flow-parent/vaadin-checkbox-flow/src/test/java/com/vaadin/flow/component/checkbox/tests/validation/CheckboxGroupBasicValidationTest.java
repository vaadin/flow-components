/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests.validation;

import java.util.Set;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class CheckboxGroupBasicValidationTest extends
        AbstractBasicValidationTest<CheckboxGroup<String>, Set<String>> {
    protected CheckboxGroup<String> createTestField() {
        return new CheckboxGroup<String>();
    }
}
