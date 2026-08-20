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

abstract class NumberSliderElement<TNumber extends Number>
        extends SliderBaseElement<TNumber> {

    NumberSliderElement(Function<Double, TNumber> fromDouble,
            Function<TNumber, Double> toDouble) {
        super(fromDouble, toDouble);
    }

    /**
     * Sets the value of the slider, emulating user input. The emulation is done
     * by setting the value property to the given value and then triggering
     * synthetic {@code input} and {@code change} DOM events to synchronize the
     * value with the server side.
     */
    public void setValue(TNumber value) {
        setProperty("value", toDouble.apply(value));
        dispatchEvent("input", Collections.singletonMap("bubbles", true));
        dispatchEvent("change", Collections.singletonMap("bubbles", true));
    }

    /**
     * Gets the current value of the slider.
     *
     * @return the current value
     */
    public TNumber getValue() {
        return fromDouble.apply(getPropertyDouble("value"));
    }

    /**
     * Gets the input element of the slider.
     *
     * @return the input element
     */
    public TestBenchElement getInputElement() {
        return $("input").first();
    }
}
