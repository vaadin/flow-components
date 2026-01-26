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
package com.vaadin.flow.component.slider.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-range-slider&gt;</code>
 * element.
 */
@Element("vaadin-range-slider")
public class RangeSliderElement extends TestBenchElement {

    /**
     * Gets the start value of the range slider.
     *
     * @return the start value
     */
    public double getStartValue() {
        return getPropertyDouble("value", "0");
    }

    /**
     * Gets the end value of the range slider.
     *
     * @return the end value
     */
    public double getEndValue() {
        return getPropertyDouble("value", "1");
    }

    /**
     * Gets the minimum value of the range slider.
     *
     * @return the minimum value
     */
    public double getMin() {
        return getPropertyDouble("min");
    }

    /**
     * Gets the maximum value of the range slider.
     *
     * @return the maximum value
     */
    public double getMax() {
        return getPropertyDouble("max");
    }

    /**
     * Gets the step value of the range slider.
     *
     * @return the step value
     */
    public double getStep() {
        return getPropertyDouble("step");
    }
}
