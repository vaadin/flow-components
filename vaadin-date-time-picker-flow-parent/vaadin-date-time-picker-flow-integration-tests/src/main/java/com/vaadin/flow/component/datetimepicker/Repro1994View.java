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
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1994.
 * DateTimePickerElement.setDateTime sets the value property to
 * LocalDateTime.toString(), which can carry more than 3 fractional-second
 * digits; the web component's ISO parser rejects such strings, so the value
 * silently does not change. The browser test mimics the TestBench element's
 * exact mechanism (set value property + dispatch change event).
 */
@Route("repro-1994")
public class Repro1994View extends Div {

    public Repro1994View() {
        DateTimePicker picker = new DateTimePicker("Date and time");
        picker.setId("picker");

        Span log = new Span("no value change");
        log.setId("log");
        picker.addValueChangeListener(event -> log.setText("value: "
                + event.getValue() + ", fromClient: " + event.isFromClient()));

        add(picker, log);
    }
}
