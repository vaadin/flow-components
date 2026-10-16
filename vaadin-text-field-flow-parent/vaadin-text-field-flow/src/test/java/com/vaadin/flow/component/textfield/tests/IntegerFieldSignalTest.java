/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class IntegerFieldSignalTest extends AbstractSignalsTest {

    @TestFactory
    Stream<DynamicTest> bindMin() {
        return generateBindingTests(IntegerField::new, IntegerField::bindMin,
                IntegerField::getMin, IntegerField::setMin, 0, 10);
    }

    @TestFactory
    Stream<DynamicTest> bindMax() {
        return generateBindingTests(IntegerField::new, IntegerField::bindMax,
                IntegerField::getMax, IntegerField::setMax, 100, 200);
    }

    @Test
    void bindMin_thenSetStep_stepValidationUsesMin() {
        var integerField = new IntegerField();
        integerField.bindMin(new ValueSignal<>(1));
        UI.getCurrent().add(integerField);

        integerField.setStep(3);

        // With min=1 and step=3: valid values are 1, 4, 7, 10, ...
        // Value 4 = 1 + 1*3, should be valid
        integerField.setValue(4);
        ValidationResult result = integerField.getDefaultValidator()
                .apply(integerField.getValue(), null);
        Assertions.assertFalse(result.isError(),
                "Value 4 should be valid (1 + 1*3)");

        // Value 3 = not aligned with step from min=1, should be invalid.
        integerField.setValue(3);
        result = integerField.getDefaultValidator()
                .apply(integerField.getValue(), null);
        Assertions.assertTrue(result.isError(),
                "Value 3 should be invalid (not aligned with step 3 from min 1).");
    }
}
