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
import org.junit.Test;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.slider.SliderBase;

public class SliderBaseTest {

    @Tag("test-slider")
    private static class TestSlider extends SliderBase<TestSlider, Double> {
        public TestSlider(double min, double max, double value) {
            super(min, max, value);
        }
    }

    @Test
    public void setMin_getMin() {
        TestSlider slider = new TestSlider(0.0, 100.0, 0.0);
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(0.0, slider.getElement().getProperty("min", 0.0),
                0.0);

        slider.setMin(10.0);
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(10.0, slider.getElement().getProperty("min", 0.0),
                0.0);
    }

    @Test
    public void setMax_getMax() {
        TestSlider slider = new TestSlider(0.0, 100.0, 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(100.0, slider.getElement().getProperty("max", 0.0),
                0.0);

        slider.setMax(200.0);
        Assert.assertEquals(200.0, slider.getMax(), 0.0);
        Assert.assertEquals(200.0, slider.getElement().getProperty("max", 0.0),
                0.0);
    }

    @Test
    public void setStep_getStep() {
        TestSlider slider = new TestSlider(0.0, 100.0, 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(1.0, slider.getElement().getProperty("step", 1.0),
                0.0);

        slider.setStep(5.0);
        Assert.assertEquals(5.0, slider.getStep(), 0.0);
        Assert.assertEquals(5.0, slider.getElement().getProperty("step", 1.0),
                0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMin_greaterThanMax_throws() {
        TestSlider slider = new TestSlider(0.0, 100.0, 0.0);
        slider.setMin(150.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMax_lessThanMin_throws() {
        TestSlider slider = new TestSlider(50.0, 100.0, 50.0);
        slider.setMax(25.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setStep_notPositive_throws() {
        TestSlider slider = new TestSlider(0.0, 100.0, 0.0);
        slider.setStep(0);
    }
}
