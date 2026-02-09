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

import java.util.Collections;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a <code>&lt;vaadin-range-slider&gt;</code>
 * element.
 */
@Element("vaadin-range-slider")
public class RangeSliderElement extends SliderBaseElement {

    /**
     * Sets the value of the range slider, emulating user input. The emulation
     * is done by setting the value property to the given values and then
     * triggering synthetic {@code input} and {@code change} DOM events to
     * synchronize the value with the server side.
     *
     * @param start
     *            the start value
     * @param end
     *            the end value
     */
    public void setValue(double start, double end) {
        executeScript("""
                    const [element, start, end] = arguments;
                    element.value = [start, end];
                """, this, start, end);
        dispatchEvent("input", Collections.singletonMap("bubbles", true));
        dispatchEvent("change", Collections.singletonMap("bubbles", true));
    }

    /**
     * Sets the start value of the range slider, emulating user input. The
     * emulation is done by setting the value property and then triggering
     * synthetic {@code input} and {@code change} DOM events to synchronize the
     * value with the server side.
     *
     * @param start
     *            the start value
     */
    public void setStartValue(double start) {
        setValue(start, getEndValue());
    }

    /**
     * Sets the end value of the range slider, emulating user input. The
     * emulation is done by setting the value property and then triggering
     * synthetic {@code input} and {@code change} DOM events to synchronize the
     * value with the server side.
     *
     * @param end
     *            the end value
     */
    public void setEndValue(double end) {
        setValue(getStartValue(), end);
    }

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
     * Gets the draggable thumb element for the start value of the range
     * slider.
     *
     * @return the start draggable thumb element
     */
    public TestBenchElement getStartThumb() {
        return $("input").first();
    }

    /**
     * Gets the draggable thumb element for the end value of the range
     * slider.
     *
     * @return the end draggable thumb element
     */
    public TestBenchElement getEndThumb() {
        return $("input").last();
    }
}
