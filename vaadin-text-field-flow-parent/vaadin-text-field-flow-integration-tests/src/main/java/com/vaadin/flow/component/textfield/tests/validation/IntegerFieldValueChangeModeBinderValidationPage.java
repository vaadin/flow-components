/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("vaadin-integer-field/validation/value-change-mode/binder")
public class IntegerFieldValueChangeModeBinderValidationPage
        extends AbstractValueChangeModeValidationPage<IntegerField> {
    public static class Bean {
        private Integer property;

        public Integer getProperty() {
            return property;
        }

        public void setProperty(Integer property) {
            this.property = property;
        }
    }

    public IntegerFieldValueChangeModeBinderValidationPage() {
        super();

        testField.setValueChangeMode(ValueChangeMode.EAGER);

        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.forField(testField).bind("property");
        binder.addStatusChangeListener(event -> {
            logValidationResult(binder.isValid());
        });
    }

    @Override
    protected IntegerField createTestField() {
        return new IntegerField();
    }
}
