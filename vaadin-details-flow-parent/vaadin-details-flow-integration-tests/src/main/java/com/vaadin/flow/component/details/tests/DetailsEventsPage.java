/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.details.tests;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-details/events")
public class DetailsEventsPage extends Div {
    public DetailsEventsPage() {
        Details details = new Details();

        Div output = new Div();
        output.setId("output");
        output.getStyle().set("white-space", "pre");
        details.addOpenedChangeListener(e -> {
            output.setText(output.getText() + String.format(
                    "Opened changed: opened=%s, isFromClient=%s\n",
                    e.isOpened(), e.isFromClient()));
        });

        NativeButton toggle = new NativeButton("Toggle", e -> {
            details.setOpened(!details.isOpened());
        });
        toggle.setId("toggle");

        add(details, new Div(toggle), output);
    }
}
