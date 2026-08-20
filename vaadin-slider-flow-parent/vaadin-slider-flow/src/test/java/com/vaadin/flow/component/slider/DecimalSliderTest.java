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

class DecimalSliderTest extends AbstractSliderTest<DecimalSlider, Double> {

    @Override
    DecimalSlider createSlider() {
        return new DecimalSlider();
    }

    @Test
    void defaultConstructor() {
        DecimalSlider slider = new DecimalSlider();
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void minMaxConstructor() {
        DecimalSlider slider = new DecimalSlider(10, 50);
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    void labelConstructor() {
        DecimalSlider slider = new DecimalSlider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void labelMinMaxConstructor() {
        DecimalSlider slider = new DecimalSlider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(10, slider.getValue(), 0);
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
    void clear_valueResetsToMin() {
        slider.setMin(10.0);
        slider.setValue(30.0);
        slider.clear();

        Assertions.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    void setValueFromClient_valueNotAlignedWithStep_ignored() {
        slider.setStep(10.0);
        slider.getElement().setProperty("value", 15.0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void setValueFromClient_valueBelowMin_ignored() {
        slider.getElement().setProperty("value", -10.0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void setValueFromClient_valueAboveMax_ignored() {
        slider.getElement().setProperty("value", 110.0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }
}
