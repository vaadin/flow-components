/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import java.math.BigDecimal;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-big-decimal-field/validation/value-change-mode/binder")
public class BigDecimalFieldValueChangeModeBinderValidationPage
        extends AbstractValueChangeModeValidationPage<BigDecimalField> {
    public static class Bean {
        private BigDecimal property;

        public BigDecimal getProperty() {
            return property;
        }

        public void setProperty(BigDecimal property) {
            this.property = property;
        }
    }

    public BigDecimalFieldValueChangeModeBinderValidationPage() {
        super();

        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.forField(testField).bind("property");
        binder.addStatusChangeListener(event -> {
            logValidationResult(binder.isValid());
        });
    }

    @Override
    protected BigDecimalField createTestField() {
        return new BigDecimalField();
    }
}
