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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class ProgressBarSignalTest extends AbstractSignalsTest {

    private ProgressBar progressBar;
    private ValueSignal<Double> minSignal;
    private ValueSignal<Double> maxSignal;
    private ValueSignal<Double> valueSignal;

    @BeforeEach
    void setup() {
        progressBar = new ProgressBar();
        minSignal = new ValueSignal<>(0.0);
        maxSignal = new ValueSignal<>(100.0);
        valueSignal = new ValueSignal<>(50.0);
    }

    @AfterEach
    void tearDown() {
        if (progressBar != null && progressBar.isAttached()) {
            progressBar.removeFromParent();
        }
    }

    // ===== MIN BINDING TESTS =====

    @Test
    void bindMin_signalBound_minSynchronizedWhenAttached() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);

        Assertions.assertEquals(0.0, progressBar.getMin(), 0.001);

        minSignal.set(10.0);
        Assertions.assertEquals(10.0, progressBar.getMin(), 0.001);

        minSignal.set(5.5);
        Assertions.assertEquals(5.5, progressBar.getMin(), 0.001);
    }

    @Test
    void bindMin_signalBound_noEffectWhenDetached() {
        progressBar.bindMin(minSignal);
        // Not attached to UI

        double initialMin = progressBar.getMin();
        minSignal.set(10.0);
        Assertions.assertEquals(initialMin, progressBar.getMin(), 0.001);
    }

    @Test
    void bindMin_signalBound_detachAndReattach() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);
        Assertions.assertEquals(0.0, progressBar.getMin(), 0.001);

        // Detach
        progressBar.removeFromParent();
        minSignal.set(10.0);
        Assertions.assertEquals(0.0, progressBar.getMin(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assertions.assertEquals(10.0, progressBar.getMin(), 0.001);

        minSignal.set(15.0);
        Assertions.assertEquals(15.0, progressBar.getMin(), 0.001);
    }

    @Test
    void bindMin_setMinWhileBound_throwsException() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);

        Assertions.assertThrows(BindingActiveException.class,
                () -> progressBar.setMin(10.0));
    }

    @Test
    void bindMin_bindAgainWhileBound_throwsException() {
        progressBar.bindMin(minSignal);
        UI.getCurrent().add(progressBar);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(50.0);
        Assertions.assertThrows(BindingActiveException.class,
                () -> progressBar.bindMin(anotherSignal));
    }

    // ===== MAX BINDING TESTS =====

    @Test
    void bindMax_signalBound_maxSynchronizedWhenAttached() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);

        Assertions.assertEquals(100.0, progressBar.getMax(), 0.001);

        maxSignal.set(200.0);
        Assertions.assertEquals(200.0, progressBar.getMax(), 0.001);

        maxSignal.set(150.5);
        Assertions.assertEquals(150.5, progressBar.getMax(), 0.001);
    }

    @Test
    void bindMax_signalBound_noEffectWhenDetached() {
        progressBar.bindMax(maxSignal);
        // Not attached to UI

        double initialMax = progressBar.getMax();
        maxSignal.set(200.0);
        Assertions.assertEquals(initialMax, progressBar.getMax(), 0.001);
    }

    @Test
    void bindMax_signalBound_detachAndReattach() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);
        Assertions.assertEquals(100.0, progressBar.getMax(), 0.001);

        // Detach
        progressBar.removeFromParent();
        maxSignal.set(200.0);
        Assertions.assertEquals(100.0, progressBar.getMax(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assertions.assertEquals(200.0, progressBar.getMax(), 0.001);

        maxSignal.set(250.0);
        Assertions.assertEquals(250.0, progressBar.getMax(), 0.001);
    }

    @Test
    void bindMax_setMaxWhileBound_throwsException() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);

        Assertions.assertThrows(BindingActiveException.class,
                () -> progressBar.setMax(200.0));
    }

    @Test
    void bindMax_bindAgainWhileBound_throwsException() {
        progressBar.bindMax(maxSignal);
        UI.getCurrent().add(progressBar);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(500.0);
        Assertions.assertThrows(BindingActiveException.class,
                () -> progressBar.bindMax(anotherSignal));
    }

    // ===== VALUE BINDING TESTS =====

    @Test
    void bindValue_signalBound_valueSynchronizedWhenAttached() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);

        Assertions.assertEquals(50.0, progressBar.getValue(), 0.001);

        valueSignal.set(75.0);
        Assertions.assertEquals(75.0, progressBar.getValue(), 0.001);

        valueSignal.set(25.5);
        Assertions.assertEquals(25.5, progressBar.getValue(), 0.001);
    }

    @Test
    void bindValue_signalBound_noEffectWhenDetached() {
        progressBar.bindValue(valueSignal);
        // Not attached to UI

        double initialValue = progressBar.getValue();
        valueSignal.set(75.0);
        Assertions.assertEquals(initialValue, progressBar.getValue(), 0.001);
    }

    @Test
    void bindValue_signalBound_detachAndReattach() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);
        Assertions.assertEquals(50.0, progressBar.getValue(), 0.001);

        // Detach
        progressBar.removeFromParent();
        valueSignal.set(75.0);
        Assertions.assertEquals(50.0, progressBar.getValue(), 0.001);

        // Reattach
        UI.getCurrent().add(progressBar);
        Assertions.assertEquals(75.0, progressBar.getValue(), 0.001);

        valueSignal.set(90.0);
        Assertions.assertEquals(90.0, progressBar.getValue(), 0.001);
    }

    @Test
    void bindValue_setValueWhileBound_throwsException() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);

        Assertions.assertThrows(BindingActiveException.class,
                () -> progressBar.setValue(75.0));
    }

    @Test
    void bindValue_bindAgainWhileBound_throwsException() {
        progressBar.bindValue(valueSignal);
        UI.getCurrent().add(progressBar);

        ValueSignal<Double> anotherSignal = new ValueSignal<>(80.0);
        Assertions.assertThrows(BindingActiveException.class,
                () -> progressBar.bindValue(anotherSignal));
    }
}
