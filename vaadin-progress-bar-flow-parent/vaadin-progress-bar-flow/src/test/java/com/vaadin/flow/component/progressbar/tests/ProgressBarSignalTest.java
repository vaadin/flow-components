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
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ProgressBarSignalTest extends AbstractSignalsUnitTest {

    private ProgressBar progressBar;
    private ValueSignal<Double> minSignal;
    private ValueSignal<Double> maxSignal;

    @Before
    public void setup() {
        progressBar = new ProgressBar();
        minSignal = new ValueSignal<>(0.0);
        maxSignal = new ValueSignal<>(100.0);
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

        minSignal.value(10.0);
        Assert.assertEquals(10.0, progressBar.getMin(), 0.001);

        minSignal.value(5.5);
        Assert.assertEquals(5.5, progressBar.getMin(), 0.001);
    }

    @Test
    public void bindMin_signalBound_noEffectWhenDetached() {
        progressBar.bindMin(minSignal);
        // Not attached to UI

        double initialMin = progressBar.getMin();
        minSignal.value(10.0);
        Assert.assertEquals(initialMin, progressBar.getMin(), 0.001);
    }

    @Test
    public void bindMin_signalBound_detachAndReattach() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(0.0, progressBar.getMin(), 0.001);

        // Detach
        progressBar.removeFromParent();
        minSignal.value(10.0);
        Assert.assertEquals(0.0, progressBar.getMin(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(10.0, progressBar.getMin(), 0.001);

        minSignal.value(15.0);
        Assert.assertEquals(15.0, progressBar.getMin(), 0.001);
    }

    @Test
    public void bindMin_nullUnbindsSignal() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(0.0, progressBar.getMin(), 0.001);

        progressBar.bindMin(null);
        minSignal.value(10.0);
        Assert.assertEquals(0.0, progressBar.getMin(), 0.001);

        // Should be able to set manually after unbinding
        progressBar.setMin(20.0);
        Assert.assertEquals(20.0, progressBar.getMin(), 0.001);
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

        maxSignal.value(200.0);
        Assert.assertEquals(200.0, progressBar.getMax(), 0.001);

        maxSignal.value(150.5);
        Assert.assertEquals(150.5, progressBar.getMax(), 0.001);
    }

    @Test
    public void bindMax_signalBound_noEffectWhenDetached() {
        progressBar.bindMax(maxSignal);
        // Not attached to UI

        double initialMax = progressBar.getMax();
        maxSignal.value(200.0);
        Assert.assertEquals(initialMax, progressBar.getMax(), 0.001);
    }

    @Test
    public void bindMax_signalBound_detachAndReattach() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(100.0, progressBar.getMax(), 0.001);

        // Detach
        progressBar.removeFromParent();
        maxSignal.value(200.0);
        Assert.assertEquals(100.0, progressBar.getMax(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(200.0, progressBar.getMax(), 0.001);

        maxSignal.value(250.0);
        Assert.assertEquals(250.0, progressBar.getMax(), 0.001);
    }

    @Test
    public void bindMax_nullUnbindsSignal() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);
        Assert.assertEquals(100.0, progressBar.getMax(), 0.001);

        progressBar.bindMax(null);
        maxSignal.value(200.0);
        Assert.assertEquals(100.0, progressBar.getMax(), 0.001);

        // Should be able to set manually after unbinding
        progressBar.setMax(300.0);
        Assert.assertEquals(300.0, progressBar.getMax(), 0.001);
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
}
