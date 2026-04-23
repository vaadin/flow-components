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

import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element for testing an {@code IntegerSlider} component.
 * <p>
 * See {@link DecimalSliderElement} for testing {@code DecimalSlider}
 * components.
 */
@Element("vaadin-slider")
public class IntegerSliderElement extends NumberSliderElement<Integer> {
    public IntegerSliderElement() {
        super(v -> v != null ? v.intValue() : null,
                v -> v != null ? v.doubleValue() : null);
    }
}
