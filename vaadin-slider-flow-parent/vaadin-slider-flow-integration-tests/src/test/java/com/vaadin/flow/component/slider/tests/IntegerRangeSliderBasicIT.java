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

import com.vaadin.flow.component.slider.testbench.IntegerRangeSliderElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-slider/integer-range-slider-basic")
public class IntegerRangeSliderBasicIT extends AbstractComponentIT {
    // Only covers basic functionality for setting and retrieving properties via
    // the TestBench element. DecimalRangeSliderBasicIT has more comprehensive
    // coverage.

    private IntegerRangeSliderElement rangeSlider;
    private TestBenchElement serverValue;

    @Before
    public void init() {
        open();
        rangeSlider = $(IntegerRangeSliderElement.class).first();
        serverValue = $("span").id("server-value");
    }

    @Test
    public void basicProperties() {
        Assert.assertEquals(10, (int) rangeSlider.getMin());
        Assert.assertEquals(200, (int) rangeSlider.getMax());
        Assert.assertEquals(5, (int) rangeSlider.getStep());
        Assert.assertEquals(25, (int) rangeSlider.getStartValue());
        Assert.assertEquals(150, (int) rangeSlider.getEndValue());
    }

    @Test
    public void setValue_valueSynchronizedToServer() {
        rangeSlider.setValue(50, 100);
        Assert.assertEquals("50,100", serverValue.getText());
    }

    @Test
    public void setStartValue_valueSynchronizedToServer() {
        rangeSlider.setStartValue(50);
        Assert.assertEquals("50,150", serverValue.getText());
    }

    @Test
    public void setEndValue_valueSynchronizedToServer() {
        rangeSlider.setEndValue(100);
        Assert.assertEquals("25,100", serverValue.getText());
    }
}
