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
package com.vaadin.flow.component.slider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;
import com.vaadin.tests.EnableFeatureFlagExtension;

class SliderSignalTest extends AbstractSignalsTest {

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            SliderFeatureFlagProvider.SLIDER_COMPONENT);

    private DecimalSlider slider;
    private ValueSignal<Double> minSignal;
    private ValueSignal<Double> maxSignal;
    private ValueSignal<Double> stepSignal;

    @BeforeEach
    void setup() {
        slider = new DecimalSlider();
        minSignal = new ValueSignal<>(0.0);
        maxSignal = new ValueSignal<>(100.0);
        stepSignal = new ValueSignal<>(1.0);
    }

    // ===== MIN BINDING TESTS =====

    @Test
    void bindMinSignal() {
        slider.bindMin(minSignal);
        ui.add(slider);
        Assertions.assertEquals(0.0, slider.getMin(), 0.001);

        minSignal.set(10.0);
        Assertions.assertEquals(10.0, slider.getMin(), 0.001);
    }

    @Test
    void bindMinSignal_setMin_throws() {
        slider.bindMin(minSignal);
        ui.add(slider);
        Assertions.assertThrows(BindingActiveException.class,
                () -> slider.setMin(10.0));
    }

    // ===== MAX BINDING TESTS =====

    @Test
    void bindMaxSignal() {
        slider.bindMax(maxSignal);
        ui.add(slider);
        Assertions.assertEquals(100.0, slider.getMax(), 0.001);

        maxSignal.set(200.0);
        Assertions.assertEquals(200.0, slider.getMax(), 0.001);
    }

    @Test
    void bindMaxSignal_setMax_throws() {
        slider.bindMax(maxSignal);
        ui.add(slider);
        Assertions.assertThrows(BindingActiveException.class,
                () -> slider.setMax(200.0));
    }

    // ===== STEP BINDING TESTS =====

    @Test
    void bindStepSignal() {
        slider.bindStep(stepSignal);
        ui.add(slider);
        Assertions.assertEquals(1.0, slider.getStep(), 0.001);

        stepSignal.set(5.0);
        Assertions.assertEquals(5.0, slider.getStep(), 0.001);
    }

    @Test
    void bindStepSignal_setStep_throws() {
        slider.bindStep(stepSignal);
        ui.add(slider);
        Assertions.assertThrows(BindingActiveException.class,
                () -> slider.setStep(5.0));
    }
}
