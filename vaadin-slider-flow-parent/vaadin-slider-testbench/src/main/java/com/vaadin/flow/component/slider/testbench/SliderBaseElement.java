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

import java.util.function.Function;

import com.vaadin.testbench.HasHelper;
import com.vaadin.testbench.HasLabel;
import com.vaadin.testbench.HasValidation;
import com.vaadin.testbench.TestBenchElement;

/**
 * Base class for slider TestBench elements, containing shared functionality.
 */
abstract class SliderBaseElement<TNumber extends Number>
        extends TestBenchElement implements HasLabel, HasHelper, HasValidation {

    final Function<Double, TNumber> fromDouble;
    final Function<TNumber, Double> toDouble;

    SliderBaseElement(Function<Double, TNumber> fromDouble,
            Function<TNumber, Double> toDouble) {
        this.fromDouble = fromDouble;
        this.toDouble = toDouble;
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    public TNumber getMin() {
        return fromDouble.apply(getPropertyDouble("min"));
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public TNumber getMax() {
        return fromDouble.apply(getPropertyDouble("max"));
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    public TNumber getStep() {
        return fromDouble.apply(getPropertyDouble("step"));
    }
}
