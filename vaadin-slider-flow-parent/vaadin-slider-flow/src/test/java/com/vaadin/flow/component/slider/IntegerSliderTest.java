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

class IntegerSliderTest extends AbstractSliderTest<IntegerSlider, Integer> {

    @Override
    IntegerSlider createSlider() {
        return new IntegerSlider();
    }

    @Test
    void defaultConstructor() {
        IntegerSlider slider = new IntegerSlider();
        Assertions.assertEquals(0, slider.getMin());
        Assertions.assertEquals(100, slider.getMax());
        Assertions.assertEquals(0, slider.getValue());
    }

    @Test
    void minMaxConstructor() {
        IntegerSlider slider = new IntegerSlider(10, 50);
        Assertions.assertEquals(10, slider.getMin());
        Assertions.assertEquals(50, slider.getMax());
        Assertions.assertEquals(1, slider.getStep());
        Assertions.assertEquals(10, slider.getValue());
    }

    @Test
    void labelConstructor() {
        IntegerSlider slider = new IntegerSlider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin());
        Assertions.assertEquals(100, slider.getMax());
        Assertions.assertEquals(0, slider.getValue());
    }

    @Test
    void labelMinMaxConstructor() {
        IntegerSlider slider = new IntegerSlider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin());
        Assertions.assertEquals(50, slider.getMax());
        Assertions.assertEquals(1, slider.getStep());
        Assertions.assertEquals(10, slider.getValue());
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
    void clear_valueResetsToMin() {
        slider.setMin(10);
        slider.setValue(30);
        slider.clear();

        Assertions.assertEquals(10, slider.getValue());
    }

    @Test
    void setValueFromClient_valueNotAlignedWithStep_ignored() {
        slider.setStep(10);
        slider.getElement().setProperty("value", 15.0);
        Assertions.assertEquals(0, slider.getValue());
    }

    @Test
    void setValueFromClient_valueBelowMin_ignored() {
        slider.getElement().setProperty("value", -10.0);
        Assertions.assertEquals(0, slider.getValue());
    }

    @Test
    void setValueFromClient_valueAboveMax_ignored() {
        slider.getElement().setProperty("value", 110.0);
        Assertions.assertEquals(0, slider.getValue());
    }
}
