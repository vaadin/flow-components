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

import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
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
    public void minMaxConstructor() {
        RangeSlider slider = new RangeSlider(10, 50);
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(10, 50), slider.getValue());
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
    public void labelMinMaxConstructor() {
        RangeSlider slider = new RangeSlider("Label", 10, 50);
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(new RangeSliderValue(10, 50), slider.getValue());
    }

    @Test
    public void setMin_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setMin(10);

        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(10, slider.getElement().getProperty("min", 0), 0);
    }

    @Test
    public void setMax_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setMax(200);

        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(200, slider.getElement().getProperty("max", 0), 0);
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
        Assert.assertFalse(
                slider.getElement().getProperty("valueAlwaysVisible", false));
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
        Assert.assertFalse(
                slider.getElement().getProperty("minMaxVisible", false));
    }

    @Test
    public void setValueFromClient_null_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", JacksonUtils.nullNode());
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_startNotAlignedWithStep_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", createValueArray(15, 80));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_endNotAlignedWithStep_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", createValueArray(20, 85));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_startBelowMin_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", createValueArray(-10, 50));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_endAboveMax_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", createValueArray(50, 110));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueFromClient_startGreaterThanEnd_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", createValueArray(80, 20));
        Assert.assertEquals(new RangeSliderValue(0, 100), slider.getValue());
    }

    @Test
    public void setValueChangeMode_getValueChangeMode() {
        RangeSlider slider = new RangeSlider();
        Assert.assertEquals(ValueChangeMode.ON_CHANGE,
                slider.getValueChangeMode());

        slider.setValueChangeMode(ValueChangeMode.EAGER);
        Assert.assertEquals(ValueChangeMode.EAGER, slider.getValueChangeMode());
    }

    @Test
    public void setValueChangeTimeout_getValueChangeTimeout() {
        RangeSlider slider = new RangeSlider();
        Assert.assertEquals(HasValueChangeMode.DEFAULT_CHANGE_TIMEOUT,
                slider.getValueChangeTimeout());

        slider.setValueChangeTimeout(500);
        Assert.assertEquals(500, slider.getValueChangeTimeout());
    }

    private ArrayNode createValueArray(double start, double end) {
        ArrayNode array = JacksonUtils.createArrayNode();
        array.add(start);
        array.add(end);
        return array;
    }
}
