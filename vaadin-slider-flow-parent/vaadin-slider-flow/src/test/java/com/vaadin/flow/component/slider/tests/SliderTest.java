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

import com.vaadin.flow.component.slider.Slider;

public class SliderTest {
    @Test
    public void defaultConstructor() {
        Slider slider = new Slider();
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void listenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider(e -> listenerInvoked.set(true));
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(0, slider.getValue(), 0);

        slider.setValue(50.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void minMaxValueConstructor() {
        Slider slider = new Slider(10, 50, 25);
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);
    }

    @Test
    public void minMaxValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider(10, 50, 25, e -> listenerInvoked.set(true));
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);

        slider.setValue(30.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void minMaxStepValueConstructor() {
        Slider slider = new Slider(10, 50, 5, 25);
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);
    }

    @Test
    public void minMaxStepValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider(10, 50, 5, 25,
                e -> listenerInvoked.set(true));
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);

        slider.setValue(30.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelConstructor() {
        Slider slider = new Slider("Label");
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void labelListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider("Label", e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(0, slider.getValue(), 0);

        slider.setValue(50.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelMinMaxValueConstructor() {
        Slider slider = new Slider("Label", 10, 50, 25);
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);
    }

    @Test
    public void labelMinMaxValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider("Label", 10, 50, 25,
                e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);

        slider.setValue(30.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelMinMaxStepValueConstructor() {
        Slider slider = new Slider("Label", 10, 50, 5, 25);
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);
    }

    @Test
    public void labelMinMaxStepValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider("Label", 10, 50, 5, 25,
                e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(25, slider.getValue(), 0);

        slider.setValue(30.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_lessThanMin_throws() {
        Slider slider = new Slider(0, 100, 1, 0);
        slider.setValue(-150.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_greaterThanMax_throws() {
        Slider slider = new Slider(0, 100, 1, 0);
        slider.setValue(150.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_notAlignedWithStep_throws() {
        Slider slider = new Slider(0, 100, 10, 0);
        slider.setValue(15.0);
    }

    @Test(expected = NullPointerException.class)
    public void setValue_null_throws() {
        Slider slider = new Slider();
        slider.setValue(null);
    }

    @Test
    public void setValue_minMaxValue_updatesProperties() {
        Slider slider = new Slider(0, 100, 1, 0);

        slider.setValue(-10, 200, 50.0);

        Assert.assertEquals(-10, slider.getMin(), 0);
        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(50, slider.getValue(), 0);
    }

    @Test
    public void setValue_minMaxStepValue_updatesProperties() {
        Slider slider = new Slider(0, 100, 1, 0);

        slider.setValue(-10, 200, 5, 50.0);

        Assert.assertEquals(-10, slider.getMin(), 0);
        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(50, slider.getValue(), 0);
    }

    @Test
    public void setInvalidMin_throws() {
        Slider slider = new Slider();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setMin(slider.getMax() + 1));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setValue(slider.getMax() + 1, slider.getMax(),
                        slider.getMin()));
    }

    @Test
    public void setInvalidMax_throws() {
        Slider slider = new Slider();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setMax(slider.getMin() - 1));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setValue(slider.getMin(), slider.getMin() - 1,
                        slider.getMin()));
    }

    @Test
    public void setInvalidStep_throws() {
        Slider slider = new Slider();
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setStep(0));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setStep(-5));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setValue(slider.getMin(), slider.getMax(), 0,
                        slider.getMin()));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> slider.setValue(slider.getMin(), slider.getMax(), -5,
                        slider.getMin()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_valueNotAlignedWithStep_throws() {
        Slider slider = new Slider();
        slider.setValue(0, 100, 10, 15.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_valueLessThanMin_throws() {
        Slider slider = new Slider();
        slider.setValue(10, 100, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_valueGreaterThanMax_throws() {
        Slider slider = new Slider();
        slider.setValue(0, 40, 50.0);
    }

    @Test
    public void setMin_updatesProperty() {
        Slider slider = new Slider();
        slider.setMin(10);

        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(10, slider.getElement().getProperty("min", 0), 0);
    }

    @Test
    public void setMin_valueBelowNewMin_adjustsValue() {
        Slider slider = new Slider();
        slider.setMin(50);

        Assert.assertEquals(50, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getValue(), 0);
    }

    @Test
    public void setMax_updatesProperty() {
        Slider slider = new Slider();
        slider.setMax(200);

        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(200, slider.getElement().getProperty("max", 0), 0);
    }

    @Test
    public void setMax_valueAboveNewMax_adjustsValue() {
        Slider slider = new Slider(0, 100, 1, 80);
        slider.setMax(50);

        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(50, slider.getValue(), 0);
    }

    @Test
    public void setStep_updatesProperty() {
        Slider slider = new Slider();
        slider.setStep(5);

        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(5, slider.getElement().getProperty("step", 0), 0);
    }

    @Test
    public void setStep_valueNotAligned_adjustsValue() {
        Slider slider = new Slider(0, 100, 1, 53);
        slider.setStep(10);

        Assert.assertEquals(10, slider.getStep(), 0);
        Assert.assertEquals(50, slider.getValue(), 0);
    }

    @Test
    public void setStep_valueNotAligned_adjustsToClosestValue() {
        Slider slider = new Slider(0, 100, 1, 57);
        slider.setStep(10);

        Assert.assertEquals(60, slider.getValue(), 0);
    }
}
