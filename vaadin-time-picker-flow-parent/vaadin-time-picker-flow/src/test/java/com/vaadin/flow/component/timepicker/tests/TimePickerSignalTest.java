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
package com.vaadin.flow.component.timepicker.tests;

import java.time.LocalTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class TimePickerSignalTest extends AbstractSignalsTest {

    private TimePicker timePicker;
    private ValueSignal<LocalTime> minSignal;
    private ValueSignal<LocalTime> maxSignal;

    @BeforeEach
    void setup() {
        timePicker = new TimePicker();
        minSignal = new ValueSignal<>(LocalTime.of(9, 0));
        maxSignal = new ValueSignal<>(LocalTime.of(17, 0));
    }

    @AfterEach
    void tearDown() {
        if (timePicker != null && timePicker.isAttached()) {
            timePicker.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    void bindMin_signalBound_minSynchronizedWhenAttached() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);

        Assertions.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        minSignal.set(LocalTime.of(10, 0));
        Assertions.assertEquals(LocalTime.of(10, 0), timePicker.getMin());

        minSignal.set(LocalTime.of(8, 30));
        Assertions.assertEquals(LocalTime.of(8, 30), timePicker.getMin());
    }

    @Test
    void bindMin_signalBound_noEffectWhenDetached() {
        timePicker.bindMin(minSignal);
        // Not attached to UI

        LocalTime initialMin = timePicker.getMin();
        minSignal.set(LocalTime.of(10, 0));
        Assertions.assertEquals(initialMin, timePicker.getMin());
    }

    @Test
    void bindMin_signalBound_detachAndReattach() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);
        Assertions.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        // Detach
        timePicker.removeFromParent();
        minSignal.set(LocalTime.of(10, 0));
        Assertions.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        // Reattach
        UI.getCurrent().add(timePicker);
        Assertions.assertEquals(LocalTime.of(10, 0), timePicker.getMin());

        minSignal.set(LocalTime.of(11, 0));
        Assertions.assertEquals(LocalTime.of(11, 0), timePicker.getMin());
    }

    @Test
    void bindMin_setMinWhileBound_throwsException() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);

        Assertions.assertThrows(BindingActiveException.class,
                () -> timePicker.setMin(LocalTime.of(10, 0)));
    }

    @Test
    void bindMin_bindAgainWhileBound_throwsException() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);

        ValueSignal<LocalTime> anotherSignal = new ValueSignal<>(
                LocalTime.of(12, 0));
        Assertions.assertThrows(BindingActiveException.class,
                () -> timePicker.bindMin(anotherSignal));
    }

    // ===== MAX BINDING TESTS =====

    @Test
    void bindMax_signalBound_maxSynchronizedWhenAttached() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);

        Assertions.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        maxSignal.set(LocalTime.of(18, 0));
        Assertions.assertEquals(LocalTime.of(18, 0), timePicker.getMax());

        maxSignal.set(LocalTime.of(16, 30));
        Assertions.assertEquals(LocalTime.of(16, 30), timePicker.getMax());
    }

    @Test
    void bindMax_signalBound_noEffectWhenDetached() {
        timePicker.bindMax(maxSignal);
        // Not attached to UI

        LocalTime initialMax = timePicker.getMax();
        maxSignal.set(LocalTime.of(18, 0));
        Assertions.assertEquals(initialMax, timePicker.getMax());
    }

    @Test
    void bindMax_signalBound_detachAndReattach() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);
        Assertions.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        // Detach
        timePicker.removeFromParent();
        maxSignal.set(LocalTime.of(18, 0));
        Assertions.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        // Reattach
        UI.getCurrent().add(timePicker);
        Assertions.assertEquals(LocalTime.of(18, 0), timePicker.getMax());

        maxSignal.set(LocalTime.of(19, 0));
        Assertions.assertEquals(LocalTime.of(19, 0), timePicker.getMax());
    }

    @Test
    void bindMax_setMaxWhileBound_throwsException() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);

        Assertions.assertThrows(BindingActiveException.class,
                () -> timePicker.setMax(LocalTime.of(18, 0)));
    }

    @Test
    void bindMax_bindAgainWhileBound_throwsException() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);

        ValueSignal<LocalTime> anotherSignal = new ValueSignal<>(
                LocalTime.of(20, 0));
        Assertions.assertThrows(BindingActiveException.class,
                () -> timePicker.bindMax(anotherSignal));
    }
}
