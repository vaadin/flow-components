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

import com.vaadin.flow.component.slider.AbstractNumberRangeSliderTest;
import com.vaadin.flow.component.slider.IntegerRangeSlider;
import com.vaadin.flow.component.slider.IntegerRangeSliderValue;

class IntegerRangeSliderTest extends
        AbstractNumberRangeSliderTest<IntegerRangeSlider, IntegerRangeSliderValue, Integer> {

    @Override
    protected IntegerRangeSlider createSlider() {
        return new IntegerRangeSlider();
    }

    @Override
    protected IntegerRangeSlider createSlider(int min, int max) {
        return new IntegerRangeSlider(min, max);
    }

    @Override
    protected IntegerRangeSlider createSlider(String label) {
        return new IntegerRangeSlider(label);
    }

    @Override
    protected IntegerRangeSlider createSlider(String label, int min, int max) {
        return new IntegerRangeSlider(label, min, max);
    }

    @Override
    protected IntegerRangeSliderValue createRange(double start, double end) {
        return new IntegerRangeSliderValue((int) start, (int) end);
    }

    @Override
    protected Integer fromDouble(double value) {
        return (int) value;
    }
}
