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
package com.vaadin.flow.component.progressbar.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ProgressBarSignalTest extends AbstractSignalsUnitTest {

    private ProgressBar progressBar;
    private ValueSignal<Double> minSignal;
    private ValueSignal<Double> maxSignal;
    private ValueSignal<Double> valueSignal;
    private ValueSignal<Boolean> indeterminateSignal;

    @Before
    public void setup() {
        progressBar = new ProgressBar();
        minSignal = new ValueSignal<>(0.0);
        maxSignal = new ValueSignal<>(100.0);
        valueSignal = new ValueSignal<>(50.0);
        indeterminateSignal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (progressBar != null && progressBar.isAttached()) {
            progressBar.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    public void bindMin_signalBound_minSynchronizedWhenAttached() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);

        Assert.assertEquals(0.0, progressBar.getMin(), 0.001);

        minSignal.set(10.0);
        Assert.assertEquals(10.0, progressBar.getMin(), 0.001);

        minSignal.set(5.5);
        Assert.assertEquals(5.5, progressBar.getMin(), 0.001);
    }

    @Test
    public void bindMin_signalBound_noEffectWhenDetached() {
        progressBar.bindMin(minSignal);
        // Not attached to UI

        double initialMin = progressBar.getMin();
        minSignal.set(10.0);
        Assert.assertEquals(initialMin, progressBar.getMin(), 0.001);
    }

    @Test
    public void bindMin_signalBound_detachAndReattach() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(0.0, progressBar.getMin(), 0.001);

        // Detach
        progressBar.removeFromParent();
        minSignal.set(10.0);
        Assert.assertEquals(0.0, progressBar.getMin(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(10.0, progressBar.getMin(), 0.001);

        minSignal.set(15.0);
        Assert.assertEquals(15.0, progressBar.getMin(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_setMinWhileBound_throwsException() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);

        progressBar.setMin(10.0);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMin_bindAgainWhileBound_throwsException() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(50.0);
        progressBar.bindMin(anotherSignal);
    }

    // ===== MAX BINDING TESTS =====

    @Test
    public void bindMax_signalBound_maxSynchronizedWhenAttached() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);

        Assert.assertEquals(100.0, progressBar.getMax(), 0.001);

        maxSignal.set(200.0);
        Assert.assertEquals(200.0, progressBar.getMax(), 0.001);

        maxSignal.set(150.5);
        Assert.assertEquals(150.5, progressBar.getMax(), 0.001);
    }

    @Test
    public void bindMax_signalBound_noEffectWhenDetached() {
        progressBar.bindMax(maxSignal);
        // Not attached to UI

        double initialMax = progressBar.getMax();
        maxSignal.set(200.0);
        Assert.assertEquals(initialMax, progressBar.getMax(), 0.001);
    }

    @Test
    public void bindMax_signalBound_detachAndReattach() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(100.0, progressBar.getMax(), 0.001);

        // Detach
        progressBar.removeFromParent();
        maxSignal.set(200.0);
        Assert.assertEquals(100.0, progressBar.getMax(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(200.0, progressBar.getMax(), 0.001);

        maxSignal.set(250.0);
        Assert.assertEquals(250.0, progressBar.getMax(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_setMaxWhileBound_throwsException() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);

        progressBar.setMax(200.0);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMax_bindAgainWhileBound_throwsException() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(500.0);
        progressBar.bindMax(anotherSignal);
    }

    // ===== VALUE BINDING TESTS =====

    @Test
    public void bindValue_signalBound_valueSynchronizedWhenAttached() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);

        Assert.assertEquals(50.0, progressBar.getValue(), 0.001);

        valueSignal.set(75.0);
        Assert.assertEquals(75.0, progressBar.getValue(), 0.001);

        valueSignal.set(25.5);
        Assert.assertEquals(25.5, progressBar.getValue(), 0.001);
    }

    @Test
    public void bindValue_signalBound_noEffectWhenDetached() {
        progressBar.bindValue(valueSignal);
        // Not attached to UI

        double initialValue = progressBar.getValue();
        valueSignal.set(75.0);
        Assert.assertEquals(initialValue, progressBar.getValue(), 0.001);
    }

    @Test
    public void bindValue_signalBound_detachAndReattach() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(50.0, progressBar.getValue(), 0.001);

        // Detach
        progressBar.removeFromParent();
        valueSignal.set(75.0);
        Assert.assertEquals(50.0, progressBar.getValue(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(75.0, progressBar.getValue(), 0.001);

        valueSignal.set(90.0);
        Assert.assertEquals(90.0, progressBar.getValue(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindValue_setValueWhileBound_throwsException() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);

        progressBar.setValue(75.0);
    }

    @Test(expected = BindingActiveException.class)
    public void bindValue_bindAgainWhileBound_throwsException() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(80.0);
        progressBar.bindValue(anotherSignal);
    }

    // ===== INDETERMINATE BINDING TESTS =====

    @Test
    public void bindIndeterminate_signalBound_indeterminateSynchronizedWhenAttached() {
        progressBar.bindIndeterminate(indeterminateSignal);
        UI.getCurrent().add(progressBar);

        Assert.assertFalse(progressBar.isIndeterminate());

        indeterminateSignal.set(true);
        Assert.assertTrue(progressBar.isIndeterminate());

        indeterminateSignal.set(false);
        Assert.assertFalse(progressBar.isIndeterminate());
    }

    @Test
    public void bindIndeterminate_signalBound_noEffectWhenDetached() {
        progressBar.bindIndeterminate(indeterminateSignal);
        // Not attached to UI

        boolean initial = progressBar.isIndeterminate();
        indeterminateSignal.set(true);
        Assert.assertEquals(initial, progressBar.isIndeterminate());
    }

    @Test
    public void bindIndeterminate_signalBound_detachAndReattach() {
        progressBar.bindIndeterminate(indeterminateSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertFalse(progressBar.isIndeterminate());

        // Detach
        progressBar.removeFromParent();
        indeterminateSignal.set(true);
        Assert.assertFalse(progressBar.isIndeterminate());

        // Reattach
        UI.getCurrent().add(progressBar);
        Assert.assertTrue(progressBar.isIndeterminate());

        indeterminateSignal.set(false);
        Assert.assertFalse(progressBar.isIndeterminate());
    }

    @Test(expected = BindingActiveException.class)
    public void bindIndeterminate_setIndeterminateWhileBound_throwsException() {
        progressBar.bindIndeterminate(indeterminateSignal);
        UI.getCurrent().add(progressBar);

        progressBar.setIndeterminate(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindIndeterminate_bindAgainWhileBound_throwsException() {
        progressBar.bindIndeterminate(indeterminateSignal);
        UI.getCurrent().add(progressBar);

        ValueSignal<Boolean> anotherSignal = new ValueSignal<>(true);
        progressBar.bindIndeterminate(anotherSignal);
    }
}
