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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class IntegerFieldSignalTest extends AbstractSignalsTest {

    private IntegerField integerField;
    private ValueSignal<Integer> minSignal;
    private ValueSignal<Integer> maxSignal;

    @BeforeEach
    void setup() {
        integerField = new IntegerField();
        minSignal = new ValueSignal<>(0);
        maxSignal = new ValueSignal<>(100);
    }

    @AfterEach
    void tearDown() {
        if (integerField != null && integerField.isAttached()) {
            integerField.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    void bindMin_signalBound_minSynchronizedWhenAttached() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);

        Assertions.assertEquals(0, integerField.getMin());

        minSignal.set(10);
        Assertions.assertEquals(10, integerField.getMin());

        minSignal.set(5);
        Assertions.assertEquals(5, integerField.getMin());
    }

    @Test
    void bindMin_signalBound_noEffectWhenDetached() {
        integerField.bindMin(minSignal);
        // Not attached to UI

        int initialMin = integerField.getMin();
        minSignal.set(10);
        Assertions.assertEquals(initialMin, integerField.getMin());
    }

    @Test
    void bindMin_signalBound_detachAndReattach() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);
        Assertions.assertEquals(0, integerField.getMin());

        // Detach
        integerField.removeFromParent();
        minSignal.set(10);
        Assertions.assertEquals(0, integerField.getMin());

        // Reattach
        UI.getCurrent().add(integerField);
        Assertions.assertEquals(10, integerField.getMin());

        minSignal.set(15);
        Assertions.assertEquals(15, integerField.getMin());
    }

    @Test
    void bindMin_thenSetStep_stepValidationUsesMin() {
        minSignal.set(1);
        integerField.bindMin(minSignal);
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

    @Test
    void bindMin_setMinWhileBound_throwsException() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);

        Assertions.assertThrows(BindingActiveException.class,
                () -> integerField.setMin(10));
    }

    @Test
    void bindMin_bindAgainWhileBound_throwsException() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);

        ValueSignal<Integer> anotherSignal = new ValueSignal<>(50);
        Assertions.assertThrows(BindingActiveException.class,
                () -> integerField.bindMin(anotherSignal));
    }

    // ===== MAX BINDING TESTS =====

    @Test
    void bindMax_signalBound_maxSynchronizedWhenAttached() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);

        Assertions.assertEquals(100, integerField.getMax());

        maxSignal.set(200);
        Assertions.assertEquals(200, integerField.getMax());

        maxSignal.set(150);
        Assertions.assertEquals(150, integerField.getMax());
    }

    @Test
    void bindMax_signalBound_noEffectWhenDetached() {
        integerField.bindMax(maxSignal);
        // Not attached to UI

        int initialMax = integerField.getMax();
        maxSignal.set(200);
        Assertions.assertEquals(initialMax, integerField.getMax());
    }

    @Test
    void bindMax_signalBound_detachAndReattach() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);
        Assertions.assertEquals(100, integerField.getMax());

        // Detach
        integerField.removeFromParent();
        maxSignal.set(200);
        Assertions.assertEquals(100, integerField.getMax());

        // Reattach
        UI.getCurrent().add(integerField);
        Assertions.assertEquals(200, integerField.getMax());

        maxSignal.set(250);
        Assertions.assertEquals(250, integerField.getMax());
    }

    @Test
    void bindMax_setMaxWhileBound_throwsException() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);

        Assertions.assertThrows(BindingActiveException.class,
                () -> integerField.setMax(200));
    }

    @Test
    void bindMax_bindAgainWhileBound_throwsException() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);

        ValueSignal<Integer> anotherSignal = new ValueSignal<>(500);
        Assertions.assertThrows(BindingActiveException.class,
                () -> integerField.bindMax(anotherSignal));
    }
}
