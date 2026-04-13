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

import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;

class RangeSliderTest {

    @Test
    void defaultConstructor() {
        RangeSlider slider = new RangeSlider();
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void minMaxConstructor() {
        RangeSlider slider = new RangeSlider(10, 50);
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(new RangeSliderValue(10.0, 50.0),
                slider.getValue());
    }

    @Test
    void labelConstructor() {
        RangeSlider slider = new RangeSlider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void labelMinMaxConstructor() {
        RangeSlider slider = new RangeSlider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(new RangeSliderValue(10.0, 50.0),
                slider.getValue());
    }

    @Test
    void rangeSliderValue_nullStart_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new RangeSliderValue(null, 100.0));
    }

    @Test
    void rangeSliderValue_nullEnd_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new RangeSliderValue(0.0, null));
    }

    @Test
    void setMin_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setMin(10.0);

        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(10, slider.getElement().getProperty("min", 0),
                0);
    }

    @Test
    void setMin_null_throws() {
        RangeSlider slider = new RangeSlider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setMin(null));
    }

    @Test
    void setMax_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setMax(200.0);

        Assertions.assertEquals(200, slider.getMax(), 0);
        Assertions.assertEquals(200, slider.getElement().getProperty("max", 0),
                0);
    }

    @Test
    void setMax_null_throws() {
        RangeSlider slider = new RangeSlider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setMax(null));
    }

    @Test
    void setStep_updatesProperty() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(0.1);

        Assertions.assertEquals(0.1, slider.getStep(), 0);
        Assertions.assertEquals(0.1,
                slider.getElement().getProperty("step", 0.0), 0);
    }

    @Test
    void setStep_null_throws() {
        RangeSlider slider = new RangeSlider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setStep(null));
    }

    @Test
    void setAccessibleNameStart() {
        RangeSlider slider = new RangeSlider();
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
        RangeSlider slider = new RangeSlider();
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
        RangeSlider slider = new RangeSlider();
        Assertions.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    void setValueAlwaysVisible_updatesProperty() {
        RangeSlider slider = new RangeSlider();
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
        RangeSlider slider = new RangeSlider();
        Assertions.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    void setMinMaxVisible_updatesProperty() {
        RangeSlider slider = new RangeSlider();
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
        RangeSlider slider = new RangeSlider();
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", JacksonUtils.nullNode());
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startNotAlignedWithStep_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", createValueArray(15, 80));
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_endNotAlignedWithStep_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", createValueArray(20, 85));
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startBelowMin_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", createValueArray(-10, 50));
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_endAboveMax_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", createValueArray(50, 110));
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startGreaterThanEnd_ignored() {
        RangeSlider slider = new RangeSlider();
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", createValueArray(80, 20));
        Assertions.assertEquals(new RangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValue_null_throws() {
        RangeSlider slider = new RangeSlider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setValue(null));
    }

    @Test
    void setValueChangeMode_getValueChangeMode() {
        RangeSlider slider = new RangeSlider();
        Assertions.assertEquals(ValueChangeMode.ON_CHANGE,
                slider.getValueChangeMode());

        slider.setValueChangeMode(ValueChangeMode.EAGER);
        Assertions.assertEquals(ValueChangeMode.EAGER,
                slider.getValueChangeMode());
    }

    @Test
    void setValueChangeTimeout_getValueChangeTimeout() {
        RangeSlider slider = new RangeSlider();
        Assertions.assertEquals(HasValueChangeMode.DEFAULT_CHANGE_TIMEOUT,
                slider.getValueChangeTimeout());

        slider.setValueChangeTimeout(500);
        Assertions.assertEquals(500, slider.getValueChangeTimeout());
    }

    private ArrayNode createValueArray(double start, double end) {
        ArrayNode array = JacksonUtils.createArrayNode();
        array.add(start);
        array.add(end);
        return array;
    }
}
