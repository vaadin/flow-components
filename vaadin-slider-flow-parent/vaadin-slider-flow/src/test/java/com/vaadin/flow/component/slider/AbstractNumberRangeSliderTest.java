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
import org.junit.jupiter.api.Test;

import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;

public abstract class AbstractNumberRangeSliderTest<TComponent extends NumberRangeSlider<TComponent, TRange, TValue>, TRange extends Range<TValue>, TValue extends Number> {

    protected abstract TComponent createSlider();

    protected abstract TComponent createSlider(int min, int max);

    protected abstract TComponent createSlider(String label);

    protected abstract TComponent createSlider(String label, int min, int max);

    protected abstract TRange createRange(double start, double end);

    protected abstract TValue fromDouble(double value);

    @Test
    void defaultConstructor() {
        var slider = createSlider();
        Assertions.assertEquals(0, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(100, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void minMaxConstructor() {
        var slider = createSlider(10, 50);
        Assertions.assertEquals(10, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(50, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(1, slider.getStep().doubleValue(), 0);
        Assertions.assertEquals(createRange(10, 50), slider.getValue());
    }

    @Test
    void labelConstructor() {
        var slider = createSlider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(100, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void labelMinMaxConstructor() {
        var slider = createSlider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(50, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(1, slider.getStep().doubleValue(), 0);
        Assertions.assertEquals(createRange(10, 50), slider.getValue());
    }

    @Test
    void setMin_updatesProperty() {
        var slider = createSlider();
        slider.setMin(fromDouble(10));

        Assertions.assertEquals(10, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(10, slider.getElement().getProperty("min", 0),
                0);
    }

    @Test
    void setMax_updatesProperty() {
        var slider = createSlider();
        slider.setMax(fromDouble(200));

        Assertions.assertEquals(200, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(200, slider.getElement().getProperty("max", 0),
                0);
    }

    @Test
    void setStep_updatesProperty() {
        var slider = createSlider();
        slider.setStep(fromDouble(5));

        Assertions.assertEquals(5, slider.getStep().doubleValue(), 0);
        Assertions.assertEquals(5, slider.getElement().getProperty("step", 0.0),
                0);
    }

    @Test
    void setAccessibleNameStart() {
        var slider = createSlider();
        slider.setAccessibleNameStart("Start");

        Assertions.assertTrue(slider.getAccessibleNameStart().isPresent());
        Assertions.assertEquals("Start", slider.getAccessibleNameStart().get());
        Assertions.assertEquals("Start",
                slider.getElement().getProperty("accessibleNameStart"));

        slider.setAccessibleNameStart(null);
        Assertions.assertTrue(slider.getAccessibleNameStart().isEmpty());
    }

    @Test
    void setAccessibleNameEnd() {
        var slider = createSlider();
        slider.setAccessibleNameEnd("End");

        Assertions.assertTrue(slider.getAccessibleNameEnd().isPresent());
        Assertions.assertEquals("End", slider.getAccessibleNameEnd().get());
        Assertions.assertEquals("End",
                slider.getElement().getProperty("accessibleNameEnd"));

        slider.setAccessibleNameEnd(null);
        Assertions.assertTrue(slider.getAccessibleNameEnd().isEmpty());
    }

    @Test
    void setValueAlwaysVisible_defaultFalse() {
        var slider = createSlider();
        Assertions.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    void setValueAlwaysVisible_updatesProperty() {
        var slider = createSlider();
        slider.setValueAlwaysVisible(true);

        Assertions.assertTrue(slider.isValueAlwaysVisible());
        Assertions.assertTrue(
                slider.getElement().getProperty("valueAlwaysVisible", false));

        slider.setValueAlwaysVisible(false);
        Assertions.assertFalse(slider.isValueAlwaysVisible());
        Assertions.assertFalse(
                slider.getElement().getProperty("valueAlwaysVisible", false));
    }

    @Test
    void setMinMaxVisible_defaultFalse() {
        var slider = createSlider();
        Assertions.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    void setMinMaxVisible_updatesProperty() {
        var slider = createSlider();
        slider.setMinMaxVisible(true);

        Assertions.assertTrue(slider.isMinMaxVisible());
        Assertions.assertTrue(
                slider.getElement().getProperty("minMaxVisible", false));

        slider.setMinMaxVisible(false);
        Assertions.assertFalse(slider.isMinMaxVisible());
        Assertions.assertFalse(
                slider.getElement().getProperty("minMaxVisible", false));
    }

    @Test
    void setValueFromClient_null_ignored() {
        var slider = createSlider();
        slider.setStep(fromDouble(10));
        slider.getElement().setPropertyJson("value", JacksonUtils.nullNode());
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void setValueFromClient_startNotAlignedWithStep_ignored() {
        var slider = createSlider();
        slider.setStep(fromDouble(10));
        slider.getElement().setPropertyJson("value", createValueArray(15, 80));
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void setValueFromClient_endNotAlignedWithStep_ignored() {
        var slider = createSlider();
        slider.setStep(fromDouble(10));
        slider.getElement().setPropertyJson("value", createValueArray(20, 85));
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void setValueFromClient_startBelowMin_ignored() {
        var slider = createSlider();
        slider.setStep(fromDouble(10));
        slider.getElement().setPropertyJson("value", createValueArray(-10, 50));
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void setValueFromClient_endAboveMax_ignored() {
        var slider = createSlider();
        slider.setStep(fromDouble(10));
        slider.getElement().setPropertyJson("value", createValueArray(50, 110));
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void setValueFromClient_startGreaterThanEnd_ignored() {
        var slider = createSlider();
        slider.setStep(fromDouble(10));
        slider.getElement().setPropertyJson("value", createValueArray(80, 20));
        Assertions.assertEquals(createRange(0, 100), slider.getValue());
    }

    @Test
    void setValueChangeMode_getValueChangeMode() {
        var slider = createSlider();
        Assertions.assertEquals(ValueChangeMode.ON_CHANGE,
                slider.getValueChangeMode());

        slider.setValueChangeMode(ValueChangeMode.EAGER);
        Assertions.assertEquals(ValueChangeMode.EAGER,
                slider.getValueChangeMode());
    }

    @Test
    void setValueChangeTimeout_getValueChangeTimeout() {
        var slider = createSlider();
        Assertions.assertEquals(HasValueChangeMode.DEFAULT_CHANGE_TIMEOUT,
                slider.getValueChangeTimeout());

        slider.setValueChangeTimeout(500);
        Assertions.assertEquals(500, slider.getValueChangeTimeout());
    }

    protected ArrayNode createValueArray(double start, double end) {
        ArrayNode array = JacksonUtils.createArrayNode();
        array.add(start);
        array.add(end);
        return array;
    }
}
