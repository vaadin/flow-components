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

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;

public class RangeSliderTest {

    @Test
    public void defaultConstructor() {
        RangeSlider slider = new RangeSlider();
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    public void listenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider(e -> listenerInvoked.set(true));
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());

        slider.setValue(new RangeSliderValue(25.0, 75.0));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void minMaxValueConstructor() {
        RangeSlider slider = new RangeSlider(10.0, 50.0,
                new RangeSliderValue(15.0, 45.0));
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());
    }

    @Test
    public void minMaxValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider(10.0, 50.0,
                new RangeSliderValue(15.0, 45.0),
                e -> listenerInvoked.set(true));
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());

        slider.setValue(new RangeSliderValue(20.0, 40.0));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void minMaxStepValueConstructor() {
        RangeSlider slider = new RangeSlider(10.0, 50.0, 5.0,
                new RangeSliderValue(15.0, 45.0));
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(5.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());
    }

    @Test
    public void minMaxStepValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider(10.0, 50.0, 5.0,
                new RangeSliderValue(15.0, 45.0),
                e -> listenerInvoked.set(true));
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(5.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());

        slider.setValue(new RangeSliderValue(20.0, 40.0));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelConstructor() {
        RangeSlider slider = new RangeSlider("Label");
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    public void labelListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider("Label",
                e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());

        slider.setValue(new RangeSliderValue(25.0, 75.0));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelMinMaxValueConstructor() {
        RangeSlider slider = new RangeSlider("Label", 10.0, 50.0,
                new RangeSliderValue(15.0, 45.0));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());
    }

    @Test
    public void labelMinMaxValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider("Label", 10.0, 50.0,
                new RangeSliderValue(15.0, 45.0),
                e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(1.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());

        slider.setValue(new RangeSliderValue(20.0, 40.0));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelMinMaxStepValueConstructor() {
        RangeSlider slider = new RangeSlider("Label", 10.0, 50.0, 5.0,
                new RangeSliderValue(15.0, 45.0));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(5.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());
    }

    @Test
    public void labelMinMaxStepValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider("Label", 10.0, 50.0, 5.0,
                new RangeSliderValue(15.0, 45.0),
                e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(5.0, slider.getStep(), 0.0);
        Assert.assertEquals(new RangeSliderValue(15.0, 45.0),
                slider.getValue());

        slider.setValue(new RangeSliderValue(20.0, 40.0));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_startLessThanMin_throws() {
        RangeSlider slider = new RangeSlider(0.0, 100.0, 1.0,
                new RangeSliderValue(0.0, 100.0));
        slider.setValue(new RangeSliderValue(-10.0, 50.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_endGreaterThanMax_throws() {
        RangeSlider slider = new RangeSlider(0.0, 100.0, 1.0,
                new RangeSliderValue(0.0, 100.0));
        slider.setValue(new RangeSliderValue(50.0, 150.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_startGreaterThanEnd_throws() {
        new RangeSliderValue(75.0, 25.0);
    }

    @Test(expected = NullPointerException.class)
    public void setValue_null_throws() {
        RangeSlider slider = new RangeSlider();
        slider.setValue(null);
    }

    @Test
    public void implementsHasSizeInterface() {
        RangeSlider slider = new RangeSlider();
        Assert.assertTrue(slider instanceof HasSize);
    }

    @Test
    public void implementsFocusableInterface() {
        RangeSlider slider = new RangeSlider();
        Assert.assertTrue(slider instanceof Focusable);
    }

    @Test
    public void implementsKeyNotifierInterface() {
        RangeSlider slider = new RangeSlider();
        Assert.assertTrue(slider instanceof KeyNotifier);
    }

    @Test
    public void setMin_getMin() {
        RangeSlider slider = new RangeSlider(0.0, 100.0, 1.0,
                new RangeSliderValue(0.0, 100.0));
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
        RangeSlider slider = new RangeSlider(0.0, 100.0, 1.0,
                new RangeSliderValue(0.0, 100.0));
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
        RangeSlider slider = new RangeSlider(0.0, 100.0, 1.0,
                new RangeSliderValue(0.0, 100.0));
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
        RangeSlider slider = new RangeSlider(0.0, 100.0, 1.0,
                new RangeSliderValue(0.0, 100.0));
        slider.setMin(150.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMax_lessThanMin_throws() {
        RangeSlider slider = new RangeSlider(50.0, 100.0, 10.0,
                new RangeSliderValue(50.0, 100.0));
        slider.setMax(25.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setStep_notPositive_throws() {
        RangeSlider slider = new RangeSlider(0.0, 100.0, 1.0,
                new RangeSliderValue(0.0, 100.0));
        slider.setStep(0.0);
    }
}
