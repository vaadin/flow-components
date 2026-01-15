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

import com.vaadin.flow.component.slider.Slider;

public class SliderTest {
    @Test
    public void defaultConstructor() {
        Slider slider = new Slider();
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(0.0, slider.getValue(), 0.0);
    }

    @Test
    public void minMaxConstructor() {
        Slider slider = new Slider(10.0, 50.0);
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(10.0, slider.getValue(), 0.0);
    }

    @Test
    public void minMaxValueConstructor() {
        Slider slider = new Slider(10.0, 50.0, 25.0);
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(25.0, slider.getValue(), 0.0);
    }

    @Test
    public void setMin_getMin() {
        Slider slider = new Slider();
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
        Slider slider = new Slider();
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
        Slider slider = new Slider();
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
        Slider slider = new Slider(0, 100);
        slider.setMin(150.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMax_lessThanMin_throws() {
        Slider slider = new Slider(50, 100);
        slider.setMax(25.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_lessThanMin_throws() {
        Slider slider = new Slider(10, 100);
        slider.setValue(5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_greaterThanMax_throws() {
        Slider slider = new Slider(0, 100);
        slider.setValue(150.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setStep_notPositive_throws() {
        Slider slider = new Slider();
        slider.setStep(0);
    }
}
