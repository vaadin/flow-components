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

import com.vaadin.flow.internal.JacksonUtils;

class IntegerRangeSliderTest extends
        AbstractRangeSliderTest<IntegerRangeSlider, IntegerRangeSliderValue, Integer> {

    @Override
    IntegerRangeSlider createSlider() {
        return new IntegerRangeSlider();
    }

    @Test
    void defaultConstructor() {
        IntegerRangeSlider slider = new IntegerRangeSlider();
        Assertions.assertEquals(0, slider.getMin());
        Assertions.assertEquals(100, slider.getMax());
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }

    @Test
    void minMaxConstructor() {
        IntegerRangeSlider slider = new IntegerRangeSlider(10, 50);
        Assertions.assertEquals(10, slider.getMin());
        Assertions.assertEquals(50, slider.getMax());
        Assertions.assertEquals(1, slider.getStep());
        Assertions.assertEquals(new IntegerRangeSliderValue(10, 50),
                slider.getValue());
    }

    @Test
    void labelConstructor() {
        IntegerRangeSlider slider = new IntegerRangeSlider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin());
        Assertions.assertEquals(100, slider.getMax());
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }

    @Test
    void labelMinMaxConstructor() {
        IntegerRangeSlider slider = new IntegerRangeSlider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin());
        Assertions.assertEquals(50, slider.getMax());
        Assertions.assertEquals(1, slider.getStep());
        Assertions.assertEquals(new IntegerRangeSliderValue(10, 50),
                slider.getValue());
    }

    @Test
    void integerRangeSliderValue_nullStart_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new IntegerRangeSliderValue(null, 100));
    }

    @Test
    void integerRangeSliderValue_nullEnd_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new IntegerRangeSliderValue(0, null));
    }

    @Test
    void setMin_updatesProperty() {
        slider.setMin(10);

        Assertions.assertEquals(10, slider.getMin());
        Assertions.assertEquals(10, slider.getElement().getProperty("min", 0));
    }

    @Test
    void setMax_updatesProperty() {
        slider.setMax(200);

        Assertions.assertEquals(200, slider.getMax());
        Assertions.assertEquals(200, slider.getElement().getProperty("max", 0));
    }

    @Test
    void setStep_updatesProperty() {
        slider.setStep(10);

        Assertions.assertEquals(10, slider.getStep());
        Assertions.assertEquals(10, slider.getElement().getProperty("step", 0));
    }

    @Test
    void setValueFromClient_null_ignored() {
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", JacksonUtils.nullNode());
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startNotAlignedWithStep_ignored() {
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", createValueArray(15, 80));
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }

    @Test
    void setValueFromClient_endNotAlignedWithStep_ignored() {
        slider.setStep(10);
        slider.getElement().setPropertyJson("value", createValueArray(20, 85));
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startBelowMin_ignored() {
        slider.getElement().setPropertyJson("value", createValueArray(-10, 50));
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }

    @Test
    void setValueFromClient_endAboveMax_ignored() {
        slider.getElement().setPropertyJson("value", createValueArray(50, 110));
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startGreaterThanEnd_ignored() {
        slider.getElement().setPropertyJson("value", createValueArray(80, 20));
        Assertions.assertEquals(new IntegerRangeSliderValue(0, 100),
                slider.getValue());
    }
}
