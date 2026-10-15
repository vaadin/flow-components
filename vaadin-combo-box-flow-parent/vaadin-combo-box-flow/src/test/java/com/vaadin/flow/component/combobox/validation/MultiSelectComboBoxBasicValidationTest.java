/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.combobox.validation;

import java.util.Set;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.tests.validation.AbstractBasicValidationTest;

public class MultiSelectComboBoxBasicValidationTest extends
        AbstractBasicValidationTest<MultiSelectComboBox<String>, Set<String>> {
    protected MultiSelectComboBox<String> createTestField() {
        return new MultiSelectComboBox<String>();
    }
}
