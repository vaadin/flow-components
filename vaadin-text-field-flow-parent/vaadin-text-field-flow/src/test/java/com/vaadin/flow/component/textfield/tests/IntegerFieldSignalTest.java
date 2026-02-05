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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class IntegerFieldSignalTest extends AbstractSignalsUnitTest {

    private IntegerField integerField;
    private ValueSignal<Integer> minSignal;
    private ValueSignal<Integer> maxSignal;

    @Before
    public void setup() {
        integerField = new IntegerField();
        minSignal = new ValueSignal<>(0);
        maxSignal = new ValueSignal<>(100);
    }

    @After
    public void tearDown() {
        if (integerField != null && integerField.isAttached()) {
            integerField.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    public void bindMin_signalBound_minSynchronizedWhenAttached() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);

        Assert.assertEquals(0, integerField.getMin());

        minSignal.value(10);
        Assert.assertEquals(10, integerField.getMin());

        minSignal.value(5);
        Assert.assertEquals(5, integerField.getMin());
    }

    @Test
    public void bindMin_signalBound_noEffectWhenDetached() {
        integerField.bindMin(minSignal);
        // Not attached to UI

        int initialMin = integerField.getMin();
        minSignal.value(10);
        Assert.assertEquals(initialMin, integerField.getMin());
    }

    @Test
    public void bindMin_signalBound_detachAndReattach() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);
        Assert.assertEquals(0, integerField.getMin());

        // Detach
        integerField.removeFromParent();
        minSignal.value(10);
        Assert.assertEquals(0, integerField.getMin());

        // Reattach
        UI.getCurrent().add(integerField);
        Assert.assertEquals(10, integerField.getMin());

        minSignal.value(15);
        Assert.assertEquals(15, integerField.getMin());
    }

    @Test
    public void bindMin_nullUnbindsSignal() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);
        Assert.assertEquals(0, integerField.getMin());

        integerField.bindMin(null);
        minSignal.value(10);
        Assert.assertEquals(0, integerField.getMin());

        // Should be able to set manually after unbinding
        integerField.setMin(20);
        Assert.assertEquals(20, integerField.getMin());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_setMinWhileBound_throwsException() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);

        integerField.setMin(10);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_bindAgainWhileBound_throwsException() {
        integerField.bindMin(minSignal);
        UI.getCurrent().add(integerField);

        ValueSignal<Integer> anotherSignal = new ValueSignal<>(50);
        integerField.bindMin(anotherSignal);
    }

    // ===== MAX BINDING TESTS =====

    @Test
    public void bindMax_signalBound_maxSynchronizedWhenAttached() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);

        Assert.assertEquals(100, integerField.getMax());

        maxSignal.value(200);
        Assert.assertEquals(200, integerField.getMax());

        maxSignal.value(150);
        Assert.assertEquals(150, integerField.getMax());
    }

    @Test
    public void bindMax_signalBound_noEffectWhenDetached() {
        integerField.bindMax(maxSignal);
        // Not attached to UI

        int initialMax = integerField.getMax();
        maxSignal.value(200);
        Assert.assertEquals(initialMax, integerField.getMax());
    }

    @Test
    public void bindMax_signalBound_detachAndReattach() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);
        Assert.assertEquals(100, integerField.getMax());

        // Detach
        integerField.removeFromParent();
        maxSignal.value(200);
        Assert.assertEquals(100, integerField.getMax());

        // Reattach
        UI.getCurrent().add(integerField);
        Assert.assertEquals(200, integerField.getMax());

        maxSignal.value(250);
        Assert.assertEquals(250, integerField.getMax());
    }

    @Test
    public void bindMax_nullUnbindsSignal() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);
        Assert.assertEquals(100, integerField.getMax());

        integerField.bindMax(null);
        maxSignal.value(200);
        Assert.assertEquals(100, integerField.getMax());

        // Should be able to set manually after unbinding
        integerField.setMax(300);
        Assert.assertEquals(300, integerField.getMax());
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_setMaxWhileBound_throwsException() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);

        integerField.setMax(200);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_bindAgainWhileBound_throwsException() {
        integerField.bindMax(maxSignal);
        UI.getCurrent().add(integerField);

        ValueSignal<Integer> anotherSignal = new ValueSignal<>(500);
        integerField.bindMax(anotherSignal);
    }
}
