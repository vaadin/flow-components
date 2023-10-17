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

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-integer-field/validation/eager-binder")
public class IntegerFieldEagerBinderValidationPage
        extends AbstractValidationPage<IntegerField> {
    public static class Bean {
        private Integer property;

        public Integer getProperty() {
            return property;
        }

        public void setProperty(Integer property) {
            this.property = property;
        }
    }

    public IntegerFieldEagerBinderValidationPage() {
        super();

        testField.setValueChangeMode(ValueChangeMode.EAGER);

        Binder<Bean> binder = new Binder<>(Bean.class);
        binder.forField(testField).bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });
    }

    protected IntegerField createTestField() {
        return new IntegerField();
    }
}
