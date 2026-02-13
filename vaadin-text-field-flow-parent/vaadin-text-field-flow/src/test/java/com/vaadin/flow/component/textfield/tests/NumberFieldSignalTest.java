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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class NumberFieldSignalTest extends AbstractSignalsUnitTest {

    private NumberField numberField;
    private ValueSignal<Double> minSignal;
    private ValueSignal<Double> maxSignal;

    @Before
    public void setup() {
        numberField = new NumberField();
        minSignal = new ValueSignal<>(0.0);
        maxSignal = new ValueSignal<>(100.0);
    }

    @After
    public void tearDown() {
        if (numberField != null && numberField.isAttached()) {
            numberField.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    public void bindMin_signalBound_minSynchronizedWhenAttached() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);

        Assert.assertEquals(0.0, numberField.getMin(), 0.001);

        minSignal.set(10.0);
        Assert.assertEquals(10.0, numberField.getMin(), 0.001);

        minSignal.set(5.5);
        Assert.assertEquals(5.5, numberField.getMin(), 0.001);
    }

    @Test
    public void bindMin_signalBound_noEffectWhenDetached() {
        numberField.bindMin(minSignal);
        // Not attached to UI

        double initialMin = numberField.getMin();
        minSignal.set(10.0);
        Assert.assertEquals(initialMin, numberField.getMin(), 0.001);
    }

    @Test
    public void bindMin_signalBound_detachAndReattach() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);
        Assert.assertEquals(0.0, numberField.getMin(), 0.001);

        // Detach
        numberField.removeFromParent();
        minSignal.set(10.0);
        Assert.assertEquals(0.0, numberField.getMin(), 0.001);

        // Reattach
        UI.getCurrent().add(numberField);
        Assert.assertEquals(10.0, numberField.getMin(), 0.001);

        minSignal.set(15.0);
        Assert.assertEquals(15.0, numberField.getMin(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_setMinWhileBound_throwsException() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);

        numberField.setMin(10.0);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_bindAgainWhileBound_throwsException() {
        numberField.bindMin(minSignal);
        UI.getCurrent().add(numberField);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(50.0);
        numberField.bindMin(anotherSignal);
    }

    // ===== MAX BINDING TESTS =====

    @Test
    public void bindMax_signalBound_maxSynchronizedWhenAttached() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);

        Assert.assertEquals(100.0, numberField.getMax(), 0.001);

        maxSignal.set(200.0);
        Assert.assertEquals(200.0, numberField.getMax(), 0.001);

        maxSignal.set(150.5);
        Assert.assertEquals(150.5, numberField.getMax(), 0.001);
    }

    @Test
    public void bindMax_signalBound_noEffectWhenDetached() {
        numberField.bindMax(maxSignal);
        // Not attached to UI

        double initialMax = numberField.getMax();
        maxSignal.set(200.0);
        Assert.assertEquals(initialMax, numberField.getMax(), 0.001);
    }

    @Test
    public void bindMax_signalBound_detachAndReattach() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);
        Assert.assertEquals(100.0, numberField.getMax(), 0.001);

        // Detach
        numberField.removeFromParent();
        maxSignal.set(200.0);
        Assert.assertEquals(100.0, numberField.getMax(), 0.001);

        // Reattach
        UI.getCurrent().add(numberField);
        Assert.assertEquals(200.0, numberField.getMax(), 0.001);

        maxSignal.set(250.0);
        Assert.assertEquals(250.0, numberField.getMax(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_setMaxWhileBound_throwsException() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);

        numberField.setMax(200.0);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_bindAgainWhileBound_throwsException() {
        numberField.bindMax(maxSignal);
        UI.getCurrent().add(numberField);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(500.0);
        numberField.bindMax(anotherSignal);
    }
}
