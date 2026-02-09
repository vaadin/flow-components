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

import com.vaadin.flow.component.HasAriaLabel;
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
        Slider slider = new Slider(0.1, 1.0, 0.1, 0.5);
        Assert.assertEquals(0.1, slider.getMin(), 0);
        Assert.assertEquals(1.0, slider.getMax(), 0);
        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(0.5, slider.getValue(), 0);
    }

    @Test
    public void minMaxStepValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider(0.1, 1.0, 0.1, 0.5,
                e -> listenerInvoked.set(true));
        Assert.assertEquals(0.1, slider.getMin(), 0);
        Assert.assertEquals(1.0, slider.getMax(), 0);
        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(0.5, slider.getValue(), 0);

        slider.setValue(0.6);
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

    @Test
    public void setValue_minMaxValue_updatesProperties() {
        Slider slider = new Slider();
        slider.setValue(50.0, -10, 200);

        Assert.assertEquals(-10, slider.getMin(), 0);
        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(50, slider.getValue(), 0);
    }

    @Test
    public void setValue_minMaxStepValue_updatesProperties() {
        Slider slider = new Slider();
        slider.setValue(0.5, 0.1, 0.9, 0.1);

        Assert.assertEquals(0.1, slider.getMin(), 0);
        Assert.assertEquals(0.9, slider.getMax(), 0);
        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(0.5, slider.getValue(), 0);
    }

    @Test
    public void setValue_invalidRange_throws() {
        Slider slider = new Slider();

        Assert.assertThrows("setValue should throw when max < min",
                IllegalArgumentException.class,
                () -> slider.setValue(slider.getMin(), slider.getMin(),
                        slider.getMin() - 0.5, 0.5));

        Assert.assertThrows("setValue should throw when min > max",
                IllegalArgumentException.class,
                () -> slider.setValue(slider.getMin(), slider.getMax() + 0.5,
                        slider.getMax(), 0.5));
    }

    @Test
    public void setValue_invalidStep_throws() {
        Slider slider = new Slider();

        Assert.assertThrows("setValue should throw when step = 0",
                IllegalArgumentException.class,
                () -> slider.setValue(0.0, 0, 100, 0));

        Assert.assertThrows("setValue should throw when step < 0",
                IllegalArgumentException.class,
                () -> slider.setValue(0.0, 0, 100, -0.5));
    }

    @Test
    public void setValue_invalidValue_throws() {
        Slider slider = new Slider();

        Assert.assertThrows(
                "setValue should throw when value is not aligned with step",
                IllegalArgumentException.class,
                () -> slider.setValue(15.0, 0, 100, 10));

        Assert.assertThrows("setValue should throw when value < min",
                IllegalArgumentException.class,
                () -> slider.setValue(5.0, 10, 100));

        Assert.assertThrows("setValue should throw when value > max",
                IllegalArgumentException.class,
                () -> slider.setValue(50.0, 0, 40));

        Assert.assertThrows("setValue should throw when value is null",
                NullPointerException.class,
                () -> slider.setValue(null, 0, 100, 10));

        Assert.assertThrows("setValue should throw when value is null",
                NullPointerException.class, () -> slider.setValue(null));
    }

    @Test
    public void setMin_updatesProperty() {
        Slider slider = new Slider();
        slider.setMin(10);

        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(10, slider.getElement().getProperty("min", 0), 0);
    }

    @Test
    public void setMin_invalidMin_throws() {
        Slider slider = new Slider();

        Assert.assertThrows("setMin should throw when min > current max",
                IllegalArgumentException.class,
                () -> slider.setMin(slider.getMax() + 0.5));
    }

    @Test
    public void setMin_valueBelowNewMin_adjustsValue() {
        Slider slider = new Slider(0, 100, 0);
        slider.setMin(50);

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
    public void setMax_invalidMax_throws() {
        Slider slider = new Slider();

        Assert.assertThrows("setMax should throw when max < current min",
                IllegalArgumentException.class,
                () -> slider.setMax(slider.getMin() - 0.5));
    }

    @Test
    public void setMax_valueAboveNewMax_adjustsValue() {
        Slider slider = new Slider(0, 100, 100);
        slider.setMax(50);

        Assert.assertEquals(50, slider.getValue(), 0);
    }

    @Test
    public void setStep_updatesProperty() {
        Slider slider = new Slider();
        slider.setStep(0.1);

        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(0.1, slider.getElement().getProperty("step", 0.0),
                0);
    }

    @Test
    public void setStep_invalidStep_throws() {
        Slider slider = new Slider();

        Assert.assertThrows("setStep should throw when step = 0",
                IllegalArgumentException.class, () -> slider.setStep(0));

        Assert.assertThrows("setStep should throw when step < 0",
                IllegalArgumentException.class, () -> slider.setStep(-5));
    }

    @Test
    public void setStep_valueNotAligned_roundsValueDownToNearestStep() {
        Slider slider = new Slider(0.0, 1.0, 0.1, 0.1);
        slider.setStep(0.5);

        Assert.assertEquals(0.0, slider.getValue(), 0);
    }

    @Test
    public void setStep_valueNotAligned_roundsValueUpToNearestStep() {
        Slider slider = new Slider(0.0, 1.0, 0.1, 0.4);
        slider.setStep(0.5);

        Assert.assertEquals(0.5, slider.getValue(), 0);
    }

    @Test
    public void setStep_valueNotAligned_roundsValueWithoutPrecisionErrors() {
        Slider slider = new Slider(0.1, 1.0, 0.01, 0.25);
        slider.setStep(0.1);

        Assert.assertEquals(0.3, slider.getValue(), 0);
    }

    @Test
    public void clear_valueResetsToMin() {
        Slider slider = new Slider(10, 50, 30);
        slider.clear();

        Assert.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    public void implementsHasAriaLabel() {
        Slider slider = new Slider();
        Assert.assertTrue(slider instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        Slider slider = new Slider();
        slider.setAriaLabel("aria-label");

        Assert.assertTrue(slider.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", slider.getAriaLabel().get());

        slider.setAriaLabel(null);
        Assert.assertTrue(slider.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        Slider slider = new Slider();
        slider.setAriaLabelledBy("aria-labelledby");

        Assert.assertTrue(slider.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby",
                slider.getAriaLabelledBy().get());

        slider.setAriaLabelledBy(null);
        Assert.assertTrue(slider.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void setValueAlwaysVisible_defaultFalse() {
        Slider slider = new Slider();
        Assert.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    public void setValueAlwaysVisible_updatesProperty() {
        Slider slider = new Slider();
        slider.setValueAlwaysVisible(true);

        Assert.assertTrue(slider.isValueAlwaysVisible());
        Assert.assertTrue(
                slider.getElement().getProperty("valueAlwaysVisible", false));

        slider.setValueAlwaysVisible(false);
        Assert.assertFalse(slider.isValueAlwaysVisible());
        Assert.assertFalse(
                slider.getElement().getProperty("valueAlwaysVisible", false));
    }

    @Test
    public void setMinMaxVisible_defaultFalse() {
        Slider slider = new Slider();
        Assert.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    public void setMinMaxVisible_updatesProperty() {
        Slider slider = new Slider();
        slider.setMinMaxVisible(true);

        Assert.assertTrue(slider.isMinMaxVisible());
        Assert.assertTrue(
                slider.getElement().getProperty("minMaxVisible", false));

        slider.setMinMaxVisible(false);
        Assert.assertFalse(slider.isMinMaxVisible());
        Assert.assertFalse(
                slider.getElement().getProperty("minMaxVisible", false));
    }

    @Test
    public void setValueFromClient_valueNotAlignedWithStep_ignored() {
        Slider slider = new Slider(0, 100, 10, 0);
        slider.getElement().setProperty("value", 15.0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void setValueFromClient_valueBelowMin_ignored() {
        Slider slider = new Slider(0, 100, 10, 0);
        slider.getElement().setProperty("value", -10.0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void setValueFromClient_valueAboveMax_ignored() {
        Slider slider = new Slider(0, 100, 10, 0);
        slider.getElement().setProperty("value", 110.0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }
}
