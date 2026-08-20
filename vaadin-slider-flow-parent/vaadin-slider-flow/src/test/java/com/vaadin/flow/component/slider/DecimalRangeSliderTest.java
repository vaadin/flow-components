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

class DecimalRangeSliderTest extends
        AbstractRangeSliderTest<DecimalRangeSlider, DecimalRangeSliderValue, Double> {

    @Override
    DecimalRangeSlider createSlider() {
        return new DecimalRangeSlider();
    }

    @Test
    void defaultConstructor() {
        DecimalRangeSlider slider = new DecimalRangeSlider();
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void minMaxConstructor() {
        DecimalRangeSlider slider = new DecimalRangeSlider(10, 50);
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(new DecimalRangeSliderValue(10.0, 50.0),
                slider.getValue());
    }

    @Test
    void labelConstructor() {
        DecimalRangeSlider slider = new DecimalRangeSlider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void labelMinMaxConstructor() {
        DecimalRangeSlider slider = new DecimalRangeSlider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(new DecimalRangeSliderValue(10.0, 50.0),
                slider.getValue());
    }

    @Test
    void decimalRangeSliderValue_nullStart_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new DecimalRangeSliderValue(null, 100.0));
    }

    @Test
    void decimalRangeSliderValue_nullEnd_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new DecimalRangeSliderValue(0.0, null));
    }

    @Test
    void setMin_updatesProperty() {
        slider.setMin(10.0);

        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(10, slider.getElement().getProperty("min", 0),
                0);
    }

    @Test
    void setMax_updatesProperty() {
        slider.setMax(200.0);

        Assertions.assertEquals(200, slider.getMax(), 0);
        Assertions.assertEquals(200, slider.getElement().getProperty("max", 0),
                0);
    }

    @Test
    void setStep_updatesProperty() {
        slider.setStep(0.1);

        Assertions.assertEquals(0.1, slider.getStep(), 0);
        Assertions.assertEquals(0.1,
                slider.getElement().getProperty("step", 0.0), 0);
    }

    @Test
    void setValueFromClient_null_ignored() {
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", JacksonUtils.nullNode());
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startNotAlignedWithStep_ignored() {
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", createValueArray(15, 80));
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_endNotAlignedWithStep_ignored() {
        slider.setStep(10.0);
        slider.getElement().setPropertyJson("value", createValueArray(20, 85));
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startBelowMin_ignored() {
        slider.getElement().setPropertyJson("value", createValueArray(-10, 50));
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_endAboveMax_ignored() {
        slider.getElement().setPropertyJson("value", createValueArray(50, 110));
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }

    @Test
    void setValueFromClient_startGreaterThanEnd_ignored() {
        slider.getElement().setPropertyJson("value", createValueArray(80, 20));
        Assertions.assertEquals(new DecimalRangeSliderValue(0.0, 100.0),
                slider.getValue());
    }
}
