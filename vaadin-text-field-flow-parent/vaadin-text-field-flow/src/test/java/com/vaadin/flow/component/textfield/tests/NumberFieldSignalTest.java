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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class NumberFieldSignalTest extends AbstractSignalsTest {

    private NumberField numberField;
    private ValueSignal<Double> minSignal;
    private ValueSignal<Double> maxSignal;

    @BeforeEach
    void setup() {
        numberField = new NumberField();
        minSignal = new ValueSignal<>(0.0);
        maxSignal = new ValueSignal<>(100.0);
    }

    @AfterEach
    void tearDown() {
        if (numberField != null && numberField.isAttached()) {
            numberField.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    void bindMin_signalBound_minSynchronizedWhenAttached() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);

        Assertions.assertEquals(0.0, numberField.getMin(), 0.001);

        minSignal.set(10.0);
        Assertions.assertEquals(10.0, numberField.getMin(), 0.001);

        minSignal.set(5.5);
        Assertions.assertEquals(5.5, numberField.getMin(), 0.001);
    }

    @Test
    void bindMin_signalBound_noEffectWhenDetached() {
        numberField.bindMin(minSignal);
        // Not attached to UI

        double initialMin = numberField.getMin();
        minSignal.set(10.0);
        Assertions.assertEquals(initialMin, numberField.getMin(), 0.001);
    }

    @Test
    void bindMin_signalBound_detachAndReattach() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);
        Assertions.assertEquals(0.0, numberField.getMin(), 0.001);

        // Detach
        numberField.removeFromParent();
        minSignal.set(10.0);
        Assertions.assertEquals(0.0, numberField.getMin(), 0.001);

        // Reattach
        UI.getCurrent().add(numberField);
        Assertions.assertEquals(10.0, numberField.getMin(), 0.001);

        minSignal.set(15.0);
        Assertions.assertEquals(15.0, numberField.getMin(), 0.001);
    }

    @Test
    void bindMin_thenSetStep_stepValidationUsesMin() {
        minSignal.set(1.0);
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);

        numberField.setStep(3.0);

        // With min=1 and step=3: valid values are 1, 4, 7, 10, ...
        // Value 4 = 1 + 1*3, should be valid
        numberField.setValue(4.0);
        ValidationResult result = numberField.getDefaultValidator()
                .apply(numberField.getValue(), null);
        Assertions.assertFalse(result.isError(),
                "Value 4 should be valid (1 + 1*3)");

        // Value 3 = not aligned with step from min=1, should be invalid.
        numberField.setValue(3.0);
        result = numberField.getDefaultValidator().apply(numberField.getValue(),
                null);
        Assertions.assertTrue(result.isError(),
                "Value 3 should be invalid (not aligned with step 3 from min 1).");
    }

    @Test
    void bindMin_setMinWhileBound_throwsException() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);

        Assertions.assertThrows(BindingActiveException.class,
                () -> numberField.setMin(10.0));
    }

    @Test
    void bindMin_bindAgainWhileBound_throwsException() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(50.0);
        Assertions.assertThrows(BindingActiveException.class,
                () -> numberField.bindMin(anotherSignal));
    }

    // ===== MAX BINDING TESTS =====

    @Test
    void bindMax_signalBound_maxSynchronizedWhenAttached() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);

        Assertions.assertEquals(100.0, numberField.getMax(), 0.001);

        maxSignal.set(200.0);
        Assertions.assertEquals(200.0, numberField.getMax(), 0.001);

        maxSignal.set(150.5);
        Assertions.assertEquals(150.5, numberField.getMax(), 0.001);
    }

    @Test
    void bindMax_signalBound_noEffectWhenDetached() {
        numberField.bindMax(maxSignal);
        // Not attached to UI

        double initialMax = numberField.getMax();
        maxSignal.set(200.0);
        Assertions.assertEquals(initialMax, numberField.getMax(), 0.001);
    }

    @Test
    void bindMax_signalBound_detachAndReattach() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);
        Assertions.assertEquals(100.0, numberField.getMax(), 0.001);

        // Detach
        numberField.removeFromParent();
        maxSignal.set(200.0);
        Assertions.assertEquals(100.0, numberField.getMax(), 0.001);

        // Reattach
        UI.getCurrent().add(numberField);
        Assertions.assertEquals(200.0, numberField.getMax(), 0.001);

        maxSignal.set(250.0);
        Assertions.assertEquals(250.0, numberField.getMax(), 0.001);
    }

    @Test
    void bindMax_setMaxWhileBound_throwsException() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);

        Assertions.assertThrows(BindingActiveException.class,
                () -> numberField.setMax(200.0));
    }

    @Test
    void bindMax_bindAgainWhileBound_throwsException() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(500.0);
        Assertions.assertThrows(BindingActiveException.class,
                () -> numberField.bindMax(anotherSignal));
    }
}
