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
package com.vaadin.flow.component.textfield.validation;

import java.math.BigDecimal;

import org.junit.Ignore;

import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.function.SerializablePredicate;

public class BigDecimalFieldBinderValidationTest
        extends AbstractBinderValidationTest<BigDecimal, BigDecimalField> {

    @Override
    protected void initField() {
        field = new BigDecimalField();
    }

    @Override
    protected void setValidValue() {
        field.setValue(new BigDecimal("5"));
    }

    @Override
    protected void setComponentInvalidValue() {

    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue(new BigDecimal("11"));
    }

    @Override
    protected SerializablePredicate<? super BigDecimal> getValidator() {
        return value -> value == null || value.compareTo(BigDecimal.TEN) < 0;
    }

    @Override
    @Ignore("Component doesn't have validation constraints")
    public void elementWithConstraints_componentValidationNotMet_elementValidationFails() {
    }
}
