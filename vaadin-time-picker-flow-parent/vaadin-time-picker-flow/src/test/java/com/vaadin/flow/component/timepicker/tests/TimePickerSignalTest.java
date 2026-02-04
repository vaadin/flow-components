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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class TimePickerSignalTest extends AbstractSignalsUnitTest {

    private TimePicker timePicker;
    private ValueSignal<LocalTime> minSignal;
    private ValueSignal<LocalTime> maxSignal;

    @Before
    public void setup() {
        timePicker = new TimePicker();
        minSignal = new ValueSignal<>(LocalTime.of(9, 0));
        maxSignal = new ValueSignal<>(LocalTime.of(17, 0));
    }

    @After
    public void tearDown() {
        if (timePicker != null && timePicker.isAttached()) {
            timePicker.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    public void bindMin_signalBound_minSynchronizedWhenAttached() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);

        Assert.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        minSignal.value(LocalTime.of(10, 0));
        Assert.assertEquals(LocalTime.of(10, 0), timePicker.getMin());

        minSignal.value(LocalTime.of(8, 30));
        Assert.assertEquals(LocalTime.of(8, 30), timePicker.getMin());
    }

    @Test
    public void bindMin_signalBound_noEffectWhenDetached() {
        timePicker.bindMin(minSignal);
        // Not attached to UI

        LocalTime initialMin = timePicker.getMin();
        minSignal.value(LocalTime.of(10, 0));
        Assert.assertEquals(initialMin, timePicker.getMin());
    }

    @Test
    public void bindMin_signalBound_detachAndReattach() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        // Detach
        timePicker.removeFromParent();
        minSignal.value(LocalTime.of(10, 0));
        Assert.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        // Reattach
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(10, 0), timePicker.getMin());

        minSignal.value(LocalTime.of(11, 0));
        Assert.assertEquals(LocalTime.of(11, 0), timePicker.getMin());
    }

    @Test
    public void bindMin_nullUnbindsSignal() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        timePicker.bindMin(null);
        minSignal.value(LocalTime.of(10, 0));
        Assert.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        // Should be able to set manually after unbinding
        timePicker.setMin(LocalTime.of(11, 0));
        Assert.assertEquals(LocalTime.of(11, 0), timePicker.getMin());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_setMinWhileBound_throwsException() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);

        timePicker.setMin(LocalTime.of(10, 0));
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_bindAgainWhileBound_throwsException() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);

        ValueSignal<LocalTime> anotherSignal = new ValueSignal<>(
                LocalTime.of(12, 0));
        timePicker.bindMin(anotherSignal);
    }

    @Test
    public void bindMin_nullSignalValue_setsEmptyString() {
        timePicker.bindMin(minSignal);
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(9, 0), timePicker.getMin());

        minSignal.value(null);
        Assert.assertNull(timePicker.getMin());
        Assert.assertEquals("", timePicker.getElement().getProperty("min"));
    }

    // ===== MAX BINDING TESTS =====

    @Test
    public void bindMax_signalBound_maxSynchronizedWhenAttached() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);

        Assert.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        maxSignal.value(LocalTime.of(18, 0));
        Assert.assertEquals(LocalTime.of(18, 0), timePicker.getMax());

        maxSignal.value(LocalTime.of(16, 30));
        Assert.assertEquals(LocalTime.of(16, 30), timePicker.getMax());
    }

    @Test
    public void bindMax_signalBound_noEffectWhenDetached() {
        timePicker.bindMax(maxSignal);
        // Not attached to UI

        LocalTime initialMax = timePicker.getMax();
        maxSignal.value(LocalTime.of(18, 0));
        Assert.assertEquals(initialMax, timePicker.getMax());
    }

    @Test
    public void bindMax_signalBound_detachAndReattach() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        // Detach
        timePicker.removeFromParent();
        maxSignal.value(LocalTime.of(18, 0));
        Assert.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        // Reattach
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(18, 0), timePicker.getMax());

        maxSignal.value(LocalTime.of(19, 0));
        Assert.assertEquals(LocalTime.of(19, 0), timePicker.getMax());
    }

    @Test
    public void bindMax_nullUnbindsSignal() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        timePicker.bindMax(null);
        maxSignal.value(LocalTime.of(18, 0));
        Assert.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        // Should be able to set manually after unbinding
        timePicker.setMax(LocalTime.of(19, 0));
        Assert.assertEquals(LocalTime.of(19, 0), timePicker.getMax());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_setMaxWhileBound_throwsException() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);

        timePicker.setMax(LocalTime.of(18, 0));
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_bindAgainWhileBound_throwsException() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);

        ValueSignal<LocalTime> anotherSignal = new ValueSignal<>(
                LocalTime.of(20, 0));
        timePicker.bindMax(anotherSignal);
    }

    @Test
    public void bindMax_nullSignalValue_setsEmptyString() {
        timePicker.bindMax(maxSignal);
        UI.getCurrent().add(timePicker);
        Assert.assertEquals(LocalTime.of(17, 0), timePicker.getMax());

        maxSignal.value(null);
        Assert.assertNull(timePicker.getMax());
        Assert.assertEquals("", timePicker.getElement().getProperty("max"));
    }
}
