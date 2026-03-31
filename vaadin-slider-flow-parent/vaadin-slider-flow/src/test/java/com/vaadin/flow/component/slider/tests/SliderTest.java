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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.slider.AbstractNumberSliderTest;
import com.vaadin.flow.component.slider.Slider;

class SliderTest extends AbstractNumberSliderTest<Slider, Double> {

    @Override
    protected Slider createSlider() {
        return new Slider();
    }

    @Override
    protected Slider createSlider(int min, int max) {
        return new Slider(min, max);
    }

    @Override
    protected Slider createSlider(String label) {
        return new Slider(label);
    }

    @Override
    protected Slider createSlider(String label, int min, int max) {
        return new Slider(label, min, max);
    }

    @Override
    protected Double fromDouble(double value) {
        return value;
    }

    @Test
    void setStep_fractional_updatesProperty() {
        Slider slider = new Slider();
        slider.setStep(0.1);

        Assertions.assertEquals(0.1, slider.getStep(), 0);
        Assertions.assertEquals(0.1,
                slider.getElement().getProperty("step", 0.0), 0);
    }
}
