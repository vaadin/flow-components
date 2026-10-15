/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;

@Route("vaadin-number-field/validation/value-change-mode/basic")
public class NumberFieldValueChangeModeBasicValidationPage
        extends AbstractValueChangeModeValidationPage<NumberField> {
    @Override
    protected NumberField createTestField() {
        return new NumberField() {
            @Override
            protected void validate() {
                super.validate();
                logValidationResult(!isInvalid());
            }
        };
    }
}
