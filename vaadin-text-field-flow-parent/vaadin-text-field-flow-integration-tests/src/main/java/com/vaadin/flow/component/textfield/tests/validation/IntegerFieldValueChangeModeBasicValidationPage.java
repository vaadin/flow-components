/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;

@Route("vaadin-integer-field/validation/value-change-mode/basic")
public class IntegerFieldValueChangeModeBasicValidationPage
        extends AbstractValueChangeModeValidationPage<IntegerField> {
    @Override
    protected IntegerField createTestField() {
        return new IntegerField() {
            @Override
            protected void validate() {
                super.validate();
                logValidationResult(!isInvalid());
            }
        };
    }
}
