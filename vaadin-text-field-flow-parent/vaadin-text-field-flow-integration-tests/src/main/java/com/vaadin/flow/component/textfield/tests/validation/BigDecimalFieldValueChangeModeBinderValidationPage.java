/*
 * Copyright 2000-2024 Vaadin Ltd.
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
