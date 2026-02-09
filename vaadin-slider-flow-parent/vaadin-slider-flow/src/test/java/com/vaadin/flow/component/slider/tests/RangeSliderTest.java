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

import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;

public class RangeSliderTest {

    @Test
    public void defaultConstructor() {
        RangeSlider slider = new RangeSlider();
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void listenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider(e -> listenerInvoked.set(true));
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());

        slider.setValue(new RangeSliderValue(25, 75));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void minMaxValueConstructor() {
        RangeSlider slider = new RangeSlider(10, 50,
                new RangeSliderValue(15, 45));
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(15, 45), slider.getValue());
    }

    @Test
    public void minMaxValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider(10, 50,
                new RangeSliderValue(15, 45), e -> listenerInvoked.set(true));
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(15, 45), slider.getValue());

        slider.setValue(new RangeSliderValue(20, 40));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void minMaxStepValueConstructor() {
        RangeSlider slider = new RangeSlider(0, 1, 0.1,
                new RangeSliderValue(0.2, 0.8));
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(1, slider.getMax(), 0);
        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(0.2, 0.8), slider.getValue());
    }

    @Test
    public void minMaxStepValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider(10, 50, 5,
                new RangeSliderValue(15, 45), e -> listenerInvoked.set(true));
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(15, 45), slider.getValue());

        slider.setValue(new RangeSliderValue(20, 40));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelConstructor() {
        RangeSlider slider = new RangeSlider("Label");
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void labelListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider("Label",
                e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());

        slider.setValue(new RangeSliderValue(25, 75));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelMinMaxValueConstructor() {
        RangeSlider slider = new RangeSlider("Label", 10, 50,
                new RangeSliderValue(15, 45));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(15, 45), slider.getValue());
    }

    @Test
    public void labelMinMaxValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider("Label", 10, 50,
                new RangeSliderValue(15, 45), e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(15, 45), slider.getValue());

        slider.setValue(new RangeSliderValue(20, 40));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelMinMaxStepValueConstructor() {
        RangeSlider slider = new RangeSlider("Label", 10, 50, 5,
                new RangeSliderValue(15, 45));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(15, 45), slider.getValue());
    }

    @Test
    public void labelMinMaxStepValueListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        RangeSlider slider = new RangeSlider("Label", 10, 50, 5,
                new RangeSliderValue(15, 45), e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(5, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(15, 45), slider.getValue());

        slider.setValue(new RangeSliderValue(20, 40));
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void setValue_minMaxValue_updatesProperties() {
        RangeSlider slider = new RangeSlider();
        slider.setValue(new RangeSliderValue(20, 80), -10, 200);

        Assert.assertEquals(-10, slider.getMin(), 0);
        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(new RangeSliderValue(20, 80), slider.getValue());
    }

    @Test
    public void setValue_minMaxStepValue_updatesProperties() {
        RangeSlider slider = new RangeSlider();
        slider.setValue(new RangeSliderValue(0.2, 0.8), 0.1, 0.9, 0.1);

        Assert.assertEquals(0.1, slider.getMin(), 0);
        Assert.assertEquals(0.9, slider.getMax(), 0);
        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(0.2, 0.8), slider.getValue());
    }

    @Test
    public void setValue_invalidRange_throws() {
        RangeSlider slider = new RangeSlider();

        Assert.assertThrows("setValue should throw when max < min",
                IllegalArgumentException.class,
                () -> slider.setValue(new RangeSliderValue(0, 0),
                        slider.getMin(), slider.getMin() - 0.5, 0.5));

        Assert.assertThrows("setValue should throw when min > max",
                IllegalArgumentException.class,
                () -> slider.setValue(new RangeSliderValue(0, 0),
                        slider.getMax() + 0.5, slider.getMax(), 0.5));
    }

    @Test
    public void setValue_invalidStep_throws() {
        RangeSlider slider = new RangeSlider();

        Assert.assertThrows("setValue should throw when step = 0",
                IllegalArgumentException.class,
                () -> slider.setValue(new RangeSliderValue(0, 100), 0, 100, 0));

        Assert.assertThrows("setValue should throw when step < 0",
                IllegalArgumentException.class, () -> slider
                        .setValue(new RangeSliderValue(0, 100), 0, 100, -0.5));
    }

    @Test
    public void setValue_invalidValue_throws() {
        RangeSlider slider = new RangeSlider();

        Assert.assertThrows(
                "setValue should throw when value is not aligned with step",
                IllegalArgumentException.class, () -> slider
                        .setValue(new RangeSliderValue(15, 85), 0, 100, 10));

        Assert.assertThrows("setValue should throw when start < min",
                IllegalArgumentException.class,
                () -> slider.setValue(new RangeSliderValue(5, 50), 10, 100));

        Assert.assertThrows("setValue should throw when end > max",
                IllegalArgumentException.class,
                () -> slider.setValue(new RangeSliderValue(0, 50), 0, 40));

        Assert.assertThrows("setValue should throw when start > end",
                IllegalArgumentException.class,
                () -> new RangeSliderValue(75, 25));

        Assert.assertThrows("setValue should throw when value is null",
                NullPointerException.class,
                () -> slider.setValue(null, 0, 100, 10));

        Assert.assertThrows("setValue should throw when value is null",
                NullPointerException.class, () -> slider.setValue(null));
    }

    @Test
    public void setMin_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setMin(10);

        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(10, slider.getElement().getProperty("min", 0), 0);
    }

    @Test
    public void setMin_invalidMin_throws() {
        RangeSlider slider = new RangeSlider();

        Assert.assertThrows("setMin should throw when min > current max",
                IllegalArgumentException.class,
                () -> slider.setMin(slider.getMax() + 0.5));
    }

    @Test
    public void setMin_valueBelowNewMin_adjustsValue() {
        RangeSlider slider = new RangeSlider(0, 100,
                new RangeSliderValue(10, 90));

        slider.setMin(50);
        Assert.assertEquals(new RangeSliderValue(50, 90), slider.getValue());

        slider.setMin(100);
        Assert.assertEquals(new RangeSliderValue(100, 100), slider.getValue());
    }

    @Test
    public void setMax_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setMax(200);

        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(200, slider.getElement().getProperty("max", 0), 0);
    }

    @Test
    public void setMax_invalidMax_throws() {
        RangeSlider slider = new RangeSlider();

        Assert.assertThrows("setMax should throw when max < current min",
                IllegalArgumentException.class,
                () -> slider.setMax(slider.getMin() - 0.5));
    }

    @Test
    public void setMax_valueAboveNewMax_adjustsValue() {
        RangeSlider slider = new RangeSlider(0, 100,
                new RangeSliderValue(10, 90));

        slider.setMax(50);
        Assert.assertEquals(new RangeSliderValue(10, 50), slider.getValue());

        slider.setMax(0);
        Assert.assertEquals(new RangeSliderValue(0, 0), slider.getValue());
    }

    @Test
    public void setStep_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(0.1);

        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(0.1, slider.getElement().getProperty("step", 0.0),
                0);
    }

    @Test
    public void setStep_invalidStep_throws() {
        RangeSlider slider = new RangeSlider();

        Assert.assertThrows("setStep should throw when step = 0",
                IllegalArgumentException.class, () -> slider.setStep(0));

        Assert.assertThrows("setStep should throw when step < 0",
                IllegalArgumentException.class, () -> slider.setStep(-5));
    }

    @Test
    public void setStep_valueNotAligned_roundsValueDownToNearestStep() {
        RangeSlider slider = new RangeSlider(0, 100, 1,
                new RangeSliderValue(10, 90));
        slider.setStep(50);

        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setStep_valueNotAligned_roundsValueUpToNearestStep() {
        RangeSlider slider = new RangeSlider(0, 100, 1,
                new RangeSliderValue(30, 70));
        slider.setStep(50);

        Assert.assertEquals(new RangeSliderValue(50, 50), slider.getValue());
    }

    @Test
    public void setStep_valueNotAligned_roundsValueWithoutPrecisionErrors() {
        RangeSlider slider = new RangeSlider(0.1, 1.0, 0.01,
                new RangeSliderValue(0.25, 0.75));
        slider.setStep(0.1);

        Assert.assertEquals(new RangeSliderValue(0.3, 0.8), slider.getValue());
    }

    @Test
    public void setAccessibleNameStart() {
        RangeSlider slider = new RangeSlider();
        slider.setAccessibleNameStart("Start");

        Assert.assertTrue(slider.getAccessibleNameStart().isPresent());
        Assert.assertEquals("Start", slider.getAccessibleNameStart().get());
        Assert.assertEquals("Start",
                slider.getElement().getProperty("accessibleNameStart"));

        slider.setAccessibleNameStart(null);
        Assert.assertTrue(slider.getAccessibleNameStart().isEmpty());
    }

    @Test
    public void setAccessibleNameEnd() {
        RangeSlider slider = new RangeSlider();
        slider.setAccessibleNameEnd("End");

        Assert.assertTrue(slider.getAccessibleNameEnd().isPresent());
        Assert.assertEquals("End", slider.getAccessibleNameEnd().get());
        Assert.assertEquals("End",
                slider.getElement().getProperty("accessibleNameEnd"));

        slider.setAccessibleNameEnd(null);
        Assert.assertTrue(slider.getAccessibleNameEnd().isEmpty());
    }

    @Test
    public void setValueAlwaysVisible_defaultFalse() {
        RangeSlider slider = new RangeSlider();
        Assert.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    public void setValueAlwaysVisible_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setValueAlwaysVisible(true);

        Assert.assertTrue(slider.isValueAlwaysVisible());
        Assert.assertTrue(
                slider.getElement().getProperty("valueAlwaysVisible", false));

        slider.setValueAlwaysVisible(false);
        Assert.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    public void setMinMaxVisible_defaultFalse() {
        RangeSlider slider = new RangeSlider();
        Assert.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    public void setMinMaxVisible_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setMinMaxVisible(true);

        Assert.assertTrue(slider.isMinMaxVisible());
        Assert.assertTrue(
                slider.getElement().getProperty("minMaxVisible", false));

        slider.setMinMaxVisible(false);
        Assert.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    public void setValueFromClient_null_ignored() {
        RangeSlider slider = new RangeSlider(0, 100, 10,
                new RangeSliderValue(0, 100));
        slider.getElement().setPropertyJson("value", JacksonUtils.nullNode());
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_startNotAlignedWithStep_ignored() {
        RangeSlider slider = new RangeSlider(0, 100, 10,
                new RangeSliderValue(0, 100));
        slider.getElement().setPropertyJson("value", createValueArray(15, 80));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_endNotAlignedWithStep_ignored() {
        RangeSlider slider = new RangeSlider(0, 100, 10,
                new RangeSliderValue(0, 100));
        slider.getElement().setPropertyJson("value", createValueArray(20, 85));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_startBelowMin_ignored() {
        RangeSlider slider = new RangeSlider(0, 100, 10,
                new RangeSliderValue(0, 100));
        slider.getElement().setPropertyJson("value", createValueArray(-10, 50));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_endAboveMax_ignored() {
        RangeSlider slider = new RangeSlider(0, 100, 10,
                new RangeSliderValue(0, 100));
        slider.getElement().setPropertyJson("value", createValueArray(50, 110));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_startGreaterThanEnd_ignored() {
        RangeSlider slider = new RangeSlider(0, 100, 10,
                new RangeSliderValue(0, 100));
        slider.getElement().setPropertyJson("value", createValueArray(80, 20));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    private ArrayNode createValueArray(double start, double end) {
        ArrayNode array = JacksonUtils.createArrayNode();
        array.add(start);
        array.add(end);
        return array;
    }
}
