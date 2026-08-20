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
import com.vaadin.flow.component.slider.IntegerSlider;
import com.vaadin.flow.router.Route;

@Route("vaadin-slider/integer-slider-basic")
public class IntegerSliderBasicPage extends Div {

    public IntegerSliderBasicPage() {
        IntegerSlider slider = new IntegerSlider();
        slider.setMin(10);
        slider.setMax(200);
        slider.setStep(5);
        slider.setValue(50);
        slider.setWidth("200px");

        Span serverValue = new Span();
        serverValue.setId("server-value");
        slider.addValueChangeListener(
                event -> serverValue.setText(String.valueOf(event.getValue())));

        add(slider, serverValue);
    }
}
