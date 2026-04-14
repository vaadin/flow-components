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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

abstract class AbstractSliderTest<TComponent extends NumberSlider<TComponent, TValue>, TValue extends Number> {

    abstract TComponent createSlider();

    TComponent slider;

    @BeforeEach
    void setup() {
        slider = createSlider();
    }

    @Test
    void implementsHasAriaLabel() {
        Assertions.assertTrue(slider instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        slider.setAriaLabel("aria-label");

        Assertions.assertTrue(slider.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", slider.getAriaLabel().get());

        slider.setAriaLabel(null);
        Assertions.assertTrue(slider.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        slider.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(slider.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                slider.getAriaLabelledBy().get());

        slider.setAriaLabelledBy(null);
        Assertions.assertTrue(slider.getAriaLabelledBy().isEmpty());
    }

    @Test
    void setValueAlwaysVisible_defaultFalse() {
        Assertions.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    void setValueAlwaysVisible_updatesProperty() {
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
        Assertions.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    void setMinMaxVisible_updatesProperty() {
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
    void setValueChangeMode_getValueChangeMode() {
        Assertions.assertEquals(ValueChangeMode.ON_CHANGE,
                slider.getValueChangeMode());

        slider.setValueChangeMode(ValueChangeMode.EAGER);
        Assertions.assertEquals(ValueChangeMode.EAGER,
                slider.getValueChangeMode());
    }

    @Test
    void setValueChangeTimeout_getValueChangeTimeout() {
        Assertions.assertEquals(HasValueChangeMode.DEFAULT_CHANGE_TIMEOUT,
                slider.getValueChangeTimeout());

        slider.setValueChangeTimeout(500);
        Assertions.assertEquals(500, slider.getValueChangeTimeout());
    }

    @Test
    void setMin_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setMin(null));
    }

    @Test
    void setMax_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setMax(null));
    }

    @Test
    void setStep_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setStep(null));
    }

    @Test
    void setValue_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> slider.setValue(null));
    }
}
