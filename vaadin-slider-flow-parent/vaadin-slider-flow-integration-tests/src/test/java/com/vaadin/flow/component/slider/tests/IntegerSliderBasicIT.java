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

import com.vaadin.flow.component.slider.testbench.IntegerSliderElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-slider/integer-slider-basic")
public class IntegerSliderBasicIT extends AbstractComponentIT {
    // Only covers basic functionality for setting and retrieving properties via
    // the TestBench element. DecimalSliderBasicIT has more comprehensive
    // coverage.

    private IntegerSliderElement slider;
    private TestBenchElement serverValue;

    @Before
    public void init() {
        open();
        slider = $(IntegerSliderElement.class).first();
        serverValue = $("span").id("server-value");
    }

    @Test
    public void basicProperties() {
        Assert.assertEquals(10, (int) slider.getMin());
        Assert.assertEquals(200, (int) slider.getMax());
        Assert.assertEquals(50, (int) slider.getValue());
        Assert.assertEquals(5, (int) slider.getStep());
    }

    @Test
    public void setValue_valueSynchronizedToServer() {
        slider.setValue(100);
        Assert.assertEquals("100", serverValue.getText());
    }
}
