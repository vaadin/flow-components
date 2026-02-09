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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("vaadin-range-slider/basic")
public class RangeSliderBasicPage extends Div {

    public RangeSliderBasicPage() {
        RangeSlider rangeSlider = new RangeSlider(10, 200, 5,
                new RangeSliderValue(25, 150));
        rangeSlider.setWidth("200px");

        Span serverValue = new Span();
        serverValue.setId("server-value");
        rangeSlider.addValueChangeListener(event -> serverValue.setText(
                event.getValue().start() + "," + event.getValue().end()));

        NativeButton setEagerMode = new NativeButton("Set eager mode",
                e -> rangeSlider.setValueChangeMode(ValueChangeMode.EAGER));
        setEagerMode.setId("set-eager-mode");

        NativeButton setLazyMode = new NativeButton("Set lazy mode", e -> {
            rangeSlider.setValueChangeMode(ValueChangeMode.LAZY);
            rangeSlider.setValueChangeTimeout(1500);
        });
        setLazyMode.setId("set-lazy-mode");

        add(rangeSlider, serverValue, setEagerMode, setLazyMode);
    }
}
