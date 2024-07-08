/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import java.math.BigDecimal;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-big-decimal-field/validation/binder")
public class BigDecimalFieldBinderValidationPage
        extends AbstractValidationPage<BigDecimalField> {
    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public static class Bean {
        private BigDecimal property;

        public BigDecimal getProperty() {
            return property;
        }

        public void setProperty(BigDecimal property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    public BigDecimalFieldBinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .bind("property");

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));
    }

    protected BigDecimalField createTestField() {
        return new BigDecimalField();
    }
}
