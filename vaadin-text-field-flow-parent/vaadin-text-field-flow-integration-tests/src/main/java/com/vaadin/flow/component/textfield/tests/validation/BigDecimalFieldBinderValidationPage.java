/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
    public static final String RESET_BEAN_BUTTON = "reset-bean-button";

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
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));

        add(createButton(RESET_BEAN_BUTTON, "Reset bean", event -> {
            binder.setBean(new Bean());
        }));
    }

    @Override
    protected BigDecimalField createTestField() {
        return new BigDecimalField();
    }
}
