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

import com.vaadin.flow.component.slider.testbench.RangeSliderElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-range-slider/basic")
public class RangeSliderBasicIT extends AbstractComponentIT {

    private RangeSliderElement rangeSlider;

    @Before
    public void init() {
        open();
        rangeSlider = $(RangeSliderElement.class).first();
    }

    @Test
    public void basicProperties() {
        Assert.assertEquals(10, rangeSlider.getMin(), 0);
        Assert.assertEquals(200, rangeSlider.getMax(), 0);
        Assert.assertEquals(5, rangeSlider.getStep(), 0);
        Assert.assertEquals(25, rangeSlider.getStartValue(), 0);
        Assert.assertEquals(150, rangeSlider.getEndValue(), 0);
    }
}
