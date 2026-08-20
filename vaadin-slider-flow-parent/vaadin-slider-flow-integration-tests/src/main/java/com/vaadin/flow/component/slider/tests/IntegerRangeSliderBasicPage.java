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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.slider.IntegerRangeSlider;
import com.vaadin.flow.component.slider.IntegerRangeSliderValue;
import com.vaadin.flow.router.Route;

@Route("vaadin-slider/integer-range-slider-basic")
public class IntegerRangeSliderBasicPage extends Div {

    public IntegerRangeSliderBasicPage() {
        IntegerRangeSlider rangeSlider = new IntegerRangeSlider();
        rangeSlider.setMin(10);
        rangeSlider.setMax(200);
        rangeSlider.setStep(5);
        rangeSlider.setValue(new IntegerRangeSliderValue(25, 150));
        rangeSlider.setWidth("200px");

        Span serverValue = new Span();
        serverValue.setId("server-value");
        rangeSlider.addValueChangeListener(event -> serverValue.setText(
                event.getValue().start() + "," + event.getValue().end()));

        add(rangeSlider, serverValue);
    }
}
