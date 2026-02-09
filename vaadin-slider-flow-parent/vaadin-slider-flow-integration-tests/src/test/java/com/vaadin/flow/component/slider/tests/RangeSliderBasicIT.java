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
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.slider.testbench.RangeSliderElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-range-slider/basic")
public class RangeSliderBasicIT extends AbstractComponentIT {

    private RangeSliderElement rangeSlider;
    private TestBenchElement serverValue;

    @Before
    public void init() {
        open();
        rangeSlider = $(RangeSliderElement.class).first();
        serverValue = $("span").id("server-value");
    }

    @Test
    public void basicProperties() {
        Assert.assertEquals(10, rangeSlider.getMin(), 0);
        Assert.assertEquals(200, rangeSlider.getMax(), 0);
        Assert.assertEquals(5, rangeSlider.getStep(), 0);
        Assert.assertEquals(25, rangeSlider.getStartValue(), 0);
        Assert.assertEquals(150, rangeSlider.getEndValue(), 0);
    }

    @Test
    public void setValue_valueSynchronizedToServer() {
        rangeSlider.setValue(50, 100);
        Assert.assertEquals("50.0,100.0", serverValue.getText());
    }

    @Test
    public void setStartValue_valueSynchronizedToServer() {
        rangeSlider.setStartValue(50);
        Assert.assertEquals("50.0,150.0", serverValue.getText());
    }

    @Test
    public void setEndValue_valueSynchronizedToServer() {
        rangeSlider.setEndValue(100);
        Assert.assertEquals("25.0,100.0", serverValue.getText());
    }

    @Test
    public void valueChangeMode_defaultMode_valueUpdatedOnThumbRelease() {
        new Actions(getDriver())
                .moveToElement(rangeSlider.getStartInputElement())
                .clickAndHold().moveByOffset(50, 0).perform();
        Assert.assertEquals("Value should not be updated while dragging thumb",
                "", serverValue.getText());

        new Actions(getDriver()).release().perform();
        Assert.assertNotEquals("Value should be updated after releasing thumb",
                "", serverValue.getText());
    }

    @Test
    public void valueChangeMode_eagerMode_valueUpdatedOnThumbDrag() {
        $("button").id("set-eager-mode").click();

        new Actions(getDriver())
                .moveToElement(rangeSlider.getStartInputElement())
                .clickAndHold().moveByOffset(50, 0).perform();
        Assert.assertNotEquals("Value should be updated while dragging thumb",
                "", serverValue.getText());

        new Actions(getDriver()).release().perform();
        Assert.assertNotEquals("Value should be updated after releasing thumb",
                "", serverValue.getText());
    }

    @Test
    public void valueChangeMode_lazyMode_valueUpdatedOnThumbDragAfterTimeout() {
        $("button").id("set-lazy-mode").click();

        long start = System.currentTimeMillis();

        new Actions(getDriver())
                .moveToElement(rangeSlider.getStartInputElement())
                .clickAndHold().moveByOffset(50, 0).perform();
        Assert.assertEquals("Value should not be updated before timeout", "",
                serverValue.getText());

        waitUntil(driver -> !serverValue.getText().isEmpty());

        long elapsed = System.currentTimeMillis() - start;
        Assert.assertTrue(
                "Value should be updated after the timeout (elapsed: %d ms)"
                        .formatted(elapsed),
                elapsed >= 1500);
    }
}
