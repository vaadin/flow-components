/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox.validation;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class ComboBoxBasicValidationTest
        extends AbstractBasicValidationTest<ComboBox<String>, String> {
    protected ComboBox<String> createTestField() {
        return new ComboBox<String>();
    }
}
