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
import java.util.function.Function;

import com.vaadin.testbench.TestBenchElement;

abstract class NumberRangeSliderElement<TNumber extends Number>
        extends SliderBaseElement<TNumber> {

    NumberRangeSliderElement(Function<Double, TNumber> fromDouble,
            Function<TNumber, Double> toDouble) {
        super(fromDouble, toDouble);
    }

    /**
     * Gets the start value of the range slider.
     *
     * @return the start value
     */
    public TNumber getStartValue() {
        return fromDouble.apply(getPropertyDouble("value", "0"));
    }

    /**
     * Gets the end value of the range slider.
     *
     * @return the end value
     */
    public TNumber getEndValue() {
        return fromDouble.apply(getPropertyDouble("value", "1"));
    }

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
    public void setValue(TNumber start, TNumber end) {
        Double startDouble = toDouble.apply(start);
        Double endDouble = toDouble.apply(end);
        executeScript("""
                    const [element, start, end] = arguments;
                    element.value = [start, end];
                """, this, startDouble, endDouble);
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
    public void setStartValue(TNumber start) {
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
    public void setEndValue(TNumber end) {
        setValue(getStartValue(), end);
    }

    /**
     * Gets the input element that controls the start value of the range slider.
     *
     * @return the start input element
     */
    public TestBenchElement getStartInputElement() {
        return $("input").first();
    }

    /**
     * Gets the input element that controls the end value of the range slider.
     *
     * @return the end input element
     */
    public TestBenchElement getEndInputElement() {
        return $("input").last();
    }
}
