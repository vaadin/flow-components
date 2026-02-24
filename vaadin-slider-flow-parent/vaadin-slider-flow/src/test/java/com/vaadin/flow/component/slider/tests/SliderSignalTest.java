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
package com.vaadin.flow.component.slider.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.slider.SliderFeatureFlagProvider;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;
import com.vaadin.tests.EnableFeatureFlagRule;

public class SliderSignalTest extends AbstractSignalsUnitTest {

    @Rule
    public EnableFeatureFlagRule featureFlagRule = new EnableFeatureFlagRule(
            SliderFeatureFlagProvider.SLIDER_COMPONENT);

    private Slider slider;
    private ValueSignal<Double> minSignal;
    private ValueSignal<Double> maxSignal;
    private ValueSignal<Double> stepSignal;

    @Before
    public void setup() {
        slider = new Slider();
        minSignal = new ValueSignal<>(0.0);
        maxSignal = new ValueSignal<>(100.0);
        stepSignal = new ValueSignal<>(1.0);
    }

    // ===== MIN BINDING TESTS =====

    @Test
    public void bindMinSignal() {
        slider.bindMin(minSignal);
        ui.add(slider);
        Assert.assertEquals(0.0, slider.getMin(), 0.001);

        minSignal.set(10.0);
        Assert.assertEquals(10.0, slider.getMin(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMinSignal_setMin_throws() {
        slider.bindMin(minSignal);
        ui.add(slider);
        slider.setMin(10.0);
    }

    // ===== MAX BINDING TESTS =====

    @Test
    public void bindMaxSignal() {
        slider.bindMax(maxSignal);
        ui.add(slider);
        Assert.assertEquals(100.0, slider.getMax(), 0.001);

        maxSignal.set(200.0);
        Assert.assertEquals(200.0, slider.getMax(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindMaxSignal_setMax_throws() {
        slider.bindMax(maxSignal);
        ui.add(slider);
        slider.setMax(200.0);
    }

    // ===== STEP BINDING TESTS =====

    @Test
    public void bindStepSignal() {
        slider.bindStep(stepSignal);
        ui.add(slider);
        Assert.assertEquals(1.0, slider.getStep(), 0.001);

        stepSignal.set(5.0);
        Assert.assertEquals(5.0, slider.getStep(), 0.001);
    }

    @Test(expected = BindingActiveException.class)
    public void bindStepSignal_setStep_throws() {
        slider.bindStep(stepSignal);
        ui.add(slider);
        slider.setStep(5.0);
    }
}
