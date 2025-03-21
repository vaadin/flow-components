/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.validation;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.function.SerializablePredicate;

public class IntegerFieldBinderValidationTest
        extends AbstractBinderValidationTest<Integer, IntegerField> {

    @Override
    protected void initField() {
        field = new IntegerField();
        field.setMax(10);
    }

    @Override
    protected void setValidValue() {
        field.setValue(5);
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue(15);
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue(1);
    }

    @Override
    protected SerializablePredicate<? super Integer> getValidator() {
        return value -> value == null || value > 2;
    }
}
