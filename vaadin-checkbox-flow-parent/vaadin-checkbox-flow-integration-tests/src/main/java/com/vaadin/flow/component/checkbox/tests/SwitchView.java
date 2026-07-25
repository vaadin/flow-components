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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link Switch}.
 */
@Route("vaadin-switch")
public class SwitchView extends Div {

    /**
     * Creates a new instance.
     */
    public SwitchView() {
        Span value = new Span("false");
        value.setId("value");

        Switch field = new Switch("Notifications");
        field.setId("default-switch");
        field.addValueChangeListener(
                event -> value.setText(String.valueOf(event.getValue())));

        add(field, value);
    }
}
