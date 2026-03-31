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

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

public abstract class AbstractNumberSliderTest<TComponent extends NumberSlider<TComponent, TValue>, TValue extends Number> {

    protected abstract TComponent createSlider();

    protected abstract TComponent createSlider(int min, int max);

    protected abstract TComponent createSlider(String label);

    protected abstract TComponent createSlider(String label, int min, int max);

    protected abstract TValue fromDouble(double value);

    @Test
    void defaultConstructor() {
        var slider = createSlider();
        Assertions.assertEquals(0, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(100, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(0, slider.getValue().doubleValue(), 0);
    }

    @Test
    void minMaxConstructor() {
        var slider = createSlider(10, 50);
        Assertions.assertEquals(10, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(50, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(1, slider.getStep().doubleValue(), 0);
        Assertions.assertEquals(10, slider.getValue().doubleValue(), 0);
    }

    @Test
    void labelConstructor() {
        var slider = createSlider("Label");
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(0, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(100, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(0, slider.getValue().doubleValue(), 0);
    }

    @Test
    void labelMinMaxConstructor() {
        var slider = createSlider("Label", 10, 50);
        Assertions.assertEquals("Label", slider.getLabel());
        Assertions.assertEquals(10, slider.getMin().doubleValue(), 0);
        Assertions.assertEquals(50, slider.getMax().doubleValue(), 0);
        Assertions.assertEquals(1, slider.getStep().doubleValue(), 0);
        Assertions.assertEquals(10, slider.getValue().doubleValue(), 0);
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
    void clear_valueResetsToMin() {
        var slider = createSlider(10, 50);
        slider.setValue(fromDouble(30));
        slider.clear();

        Assertions.assertEquals(10, slider.getValue().doubleValue(), 0);
    }

    @Test
    void implementsHasAriaLabel() {
        var slider = createSlider();
        Assertions.assertTrue(slider instanceof HasAriaLabel);
    }

    @Test
    void setAriaLabel() {
        var slider = createSlider();
        slider.setAriaLabel("aria-label");

        Assertions.assertTrue(slider.getAriaLabel().isPresent());
        Assertions.assertEquals("aria-label", slider.getAriaLabel().get());

        slider.setAriaLabel(null);
        Assertions.assertTrue(slider.getAriaLabel().isEmpty());
    }

    @Test
    void setAriaLabelledBy() {
        var slider = createSlider();
        slider.setAriaLabelledBy("aria-labelledby");

        Assertions.assertTrue(slider.getAriaLabelledBy().isPresent());
        Assertions.assertEquals("aria-labelledby",
                slider.getAriaLabelledBy().get());

        slider.setAriaLabelledBy(null);
        Assertions.assertTrue(slider.getAriaLabelledBy().isEmpty());
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
    void setValueFromClient_valueNotAlignedWithStep_ignored() {
        var slider = createSlider(0, 100);
        slider.setStep(fromDouble(10));
        slider.getElement().setProperty("value", 15.0);
        Assertions.assertEquals(0, slider.getValue().doubleValue(), 0);
    }

    @Test
    void setValueFromClient_valueBelowMin_ignored() {
        var slider = createSlider(0, 100);
        slider.setStep(fromDouble(10));
        slider.getElement().setProperty("value", -10.0);
        Assertions.assertEquals(0, slider.getValue().doubleValue(), 0);
    }

    @Test
    void setValueFromClient_valueAboveMax_ignored() {
        var slider = createSlider(0, 100);
        slider.setStep(fromDouble(10));
        slider.getElement().setProperty("value", 110.0);
        Assertions.assertEquals(0, slider.getValue().doubleValue(), 0);
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
}
