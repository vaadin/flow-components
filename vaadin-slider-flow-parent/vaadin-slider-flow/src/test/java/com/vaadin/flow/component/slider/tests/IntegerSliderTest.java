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

import com.vaadin.flow.component.slider.AbstractNumberSliderTest;
import com.vaadin.flow.component.slider.IntegerSlider;

class IntegerSliderTest
        extends AbstractNumberSliderTest<IntegerSlider, Integer> {

    @Override
    protected IntegerSlider createSlider() {
        return new IntegerSlider();
    }

    @Override
    protected IntegerSlider createSlider(int min, int max) {
        return new IntegerSlider(min, max);
    }

    @Override
    protected IntegerSlider createSlider(String label) {
        return new IntegerSlider(label);
    }

    @Override
    protected IntegerSlider createSlider(String label, int min, int max) {
        return new IntegerSlider(label, min, max);
    }

    @Override
    protected Integer fromDouble(double value) {
        return (int) value;
    }
}
