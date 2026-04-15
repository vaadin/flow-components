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

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

class SliderTest {
    @Test
    void defaultConstructor() {
        Slider slider = new Slider();
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void minMaxConstructor() {
        Slider slider = new Slider(10, 50);
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    void labelConstructor() {
        Slider slider = new Slider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin(), 0);
        Assertions.assertEquals(100, slider.getMax(), 0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void labelMinMaxConstructor() {
        Slider slider = new Slider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(50, slider.getMax(), 0);
        Assertions.assertEquals(1, slider.getStep(), 0);
        Assertions.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    void setMin_updatesProperty() {
        Slider slider = new Slider();
        slider.setMin(10.0);

        Assertions.assertEquals(10, slider.getMin(), 0);
        Assertions.assertEquals(10, slider.getElement().getProperty("min", 0),
                0);
    }

    @Test
    void setMin_null_throws() {
        Slider slider = new Slider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setMin(null));
    }

    @Test
    void setMax_updatesProperty() {
        Slider slider = new Slider();
        slider.setMax(200.0);

        Assertions.assertEquals(200, slider.getMax(), 0);
        Assertions.assertEquals(200, slider.getElement().getProperty("max", 0),
                0);
    }

    @Test
    void setMax_null_throws() {
        Slider slider = new Slider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setMax(null));
    }

    @Test
    void setStep_updatesProperty() {
        Slider slider = new Slider();
        slider.setStep(0.1);

        Assertions.assertEquals(0.1, slider.getStep(), 0);
        Assertions.assertEquals(0.1,
                slider.getElement().getProperty("step", 0.0), 0);
    }

    @Test
    void setStep_null_throws() {
        Slider slider = new Slider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setStep(null));
    }

    @Test
    void clear_valueResetsToMin() {
        Slider slider = new Slider(10, 50);
        slider.setValue(30.0);
        slider.clear();

        Assertions.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    void implementsHasAriaLabel() {
        Slider slider = new Slider();
        Assertions.assertTrue(slider instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        Slider slider = new Slider();
        slider.setAriaLabel("aria-label");

        Assertions.assertTrue(slider.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", slider.getAriaLabel().get());

        slider.setAriaLabel(null);
        Assertions.assertTrue(slider.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        Slider slider = new Slider();
        slider.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(slider.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                slider.getAriaLabelledBy().get());

        slider.setAriaLabelledBy(null);
        Assertions.assertTrue(slider.getAriaLabelledBy().isEmpty());
    }

    @Test
    void setValueAlwaysVisible_defaultFalse() {
        Slider slider = new Slider();
        Assertions.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    void setValueAlwaysVisible_updatesProperty() {
        Slider slider = new Slider();
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
        Slider slider = new Slider();
        Assertions.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    void setMinMaxVisible_updatesProperty() {
        Slider slider = new Slider();
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
    void setValue_null_throws() {
        Slider slider = new Slider();
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setValue(null));
    }

    @Test
    void setValueFromClient_valueNotAlignedWithStep_ignored() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10.0);
        slider.getElement().setProperty("value", 15.0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void setValueFromClient_valueBelowMin_ignored() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10.0);
        slider.getElement().setProperty("value", -10.0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void setValueFromClient_valueAboveMax_ignored() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10.0);
        slider.getElement().setProperty("value", 110.0);
        Assertions.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    void setValueChangeMode_getValueChangeMode() {
        Slider slider = new Slider();
        Assertions.assertEquals(ValueChangeMode.ON_CHANGE,
                slider.getValueChangeMode());

        slider.setValueChangeMode(ValueChangeMode.EAGER);
        Assertions.assertEquals(ValueChangeMode.EAGER,
                slider.getValueChangeMode());
    }

    @Test
    void setValueChangeTimeout_getValueChangeTimeout() {
        Slider slider = new Slider();
        Assertions.assertEquals(HasValueChangeMode.DEFAULT_CHANGE_TIMEOUT,
                slider.getValueChangeTimeout());

        slider.setValueChangeTimeout(500);
        Assertions.assertEquals(500, slider.getValueChangeTimeout());
    }
}
