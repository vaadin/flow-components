/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;

@Route("vaadin-big-decimal-field/validation/value-change-mode/basic")
public class BigDecimalFieldValueChangeModeBasicValidationPage
        extends AbstractValueChangeModeValidationPage<BigDecimalField> {
    @Override
    protected BigDecimalField createTestField() {
        return new BigDecimalField() {
            @Override
            protected void validate() {
                super.validate();
                logValidationResult(!isInvalid());
            }
        };
    }
}
