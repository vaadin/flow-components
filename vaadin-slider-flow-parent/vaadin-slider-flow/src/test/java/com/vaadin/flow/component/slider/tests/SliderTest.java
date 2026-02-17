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

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

public class SliderTest {
    @Test
    public void defaultConstructor() {
        Slider slider = new Slider();
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void minMaxConstructor() {
        Slider slider = new Slider(10, 50);
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    public void labelConstructor() {
        Slider slider = new Slider("Label");
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0, slider.getMin(), 0);
        Assert.assertEquals(100, slider.getMax(), 0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void labelMinMaxConstructor() {
        Slider slider = new Slider("Label", 10, 50);
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(50, slider.getMax(), 0);
        Assert.assertEquals(1, slider.getStep(), 0);
        Assert.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    public void setMin_updatesProperty() {
        Slider slider = new Slider();
        slider.setMin(10);

        Assert.assertEquals(10, slider.getMin(), 0);
        Assert.assertEquals(10, slider.getElement().getProperty("min", 0), 0);
    }

    @Test
    public void setMax_updatesProperty() {
        Slider slider = new Slider();
        slider.setMax(200);

        Assert.assertEquals(200, slider.getMax(), 0);
        Assert.assertEquals(200, slider.getElement().getProperty("max", 0), 0);
    }

    @Test
    public void setStep_updatesProperty() {
        Slider slider = new Slider();
        slider.setStep(0.1);

        Assert.assertEquals(0.1, slider.getStep(), 0);
        Assert.assertEquals(0.1, slider.getElement().getProperty("step", 0.0),
                0);
    }

    @Test
    public void clear_valueResetsToMin() {
        Slider slider = new Slider(10, 50);
        slider.setValue(30.0);
        slider.clear();

        Assert.assertEquals(10, slider.getValue(), 0);
    }

    @Test
    public void implementsHasAriaLabel() {
        Slider slider = new Slider();
        Assert.assertTrue(slider instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        Slider slider = new Slider();
        slider.setAriaLabel("aria-label");

        Assert.assertTrue(slider.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", slider.getAriaLabel().get());

        slider.setAriaLabel(null);
        Assert.assertTrue(slider.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        Slider slider = new Slider();
        slider.setAriaLabelledBy("aria-labelledby");

        Assert.assertTrue(slider.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby",
                slider.getAriaLabelledBy().get());

        slider.setAriaLabelledBy(null);
        Assert.assertTrue(slider.getAriaLabelledBy().isEmpty());
    }

    @Test
    public void setValueAlwaysVisible_defaultFalse() {
        Slider slider = new Slider();
        Assert.assertFalse(slider.isValueAlwaysVisible());
    }

    @Test
    public void setValueAlwaysVisible_updatesProperty() {
        Slider slider = new Slider();
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
        Slider slider = new Slider();
        Assert.assertFalse(slider.isMinMaxVisible());
    }

    @Test
    public void setMinMaxVisible_updatesProperty() {
        Slider slider = new Slider();
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
    public void setValueFromClient_valueNotAlignedWithStep_ignored() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10);
        slider.getElement().setProperty("value", 15.0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void setValueFromClient_valueBelowMin_ignored() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10);
        slider.getElement().setProperty("value", -10.0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void setValueFromClient_valueAboveMax_ignored() {
        Slider slider = new Slider(0, 100);
        slider.setStep(10);
        slider.getElement().setProperty("value", 110.0);
        Assert.assertEquals(0, slider.getValue(), 0);
    }

    @Test
    public void setValueChangeMode_getValueChangeMode() {
        Slider slider = new Slider();
        Assert.assertEquals(ValueChangeMode.ON_CHANGE,
                slider.getValueChangeMode());

        slider.setValueChangeMode(ValueChangeMode.EAGER);
        Assert.assertEquals(ValueChangeMode.EAGER, slider.getValueChangeMode());
    }

    @Test
    public void setValueChangeTimeout_getValueChangeTimeout() {
        Slider slider = new Slider();
        Assert.assertEquals(HasValueChangeMode.DEFAULT_CHANGE_TIMEOUT,
                slider.getValueChangeTimeout());

        slider.setValueChangeTimeout(500);
        Assert.assertEquals(500, slider.getValueChangeTimeout());
    }
}
