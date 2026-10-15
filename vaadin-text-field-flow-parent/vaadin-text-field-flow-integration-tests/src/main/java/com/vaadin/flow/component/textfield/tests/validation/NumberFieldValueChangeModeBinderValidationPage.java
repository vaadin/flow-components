/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("vaadin-number-field/validation/value-change-mode/binder")
public class NumberFieldValueChangeModeBinderValidationPage
        extends AbstractValueChangeModeValidationPage<NumberField> {
    public static class Bean {
        private Number property;

        public Number getProperty() {
            return property;
        }

        public void setProperty(Number property) {
            this.property = property;
        }
    }

    public NumberFieldValueChangeModeBinderValidationPage() {
        super();

        testField.setValueChangeMode(ValueChangeMode.EAGER);

        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.forField(testField).bind("property");
        binder.addStatusChangeListener(event -> {
            logValidationResult(binder.isValid());
        });
    }

    @Override
    protected NumberField createTestField() {
        return new NumberField();
    }
}
