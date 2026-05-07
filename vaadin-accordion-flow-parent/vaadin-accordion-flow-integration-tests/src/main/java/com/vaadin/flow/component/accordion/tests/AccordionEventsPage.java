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
package com.vaadin.flow.component.accordion.tests;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-accordion/events")
public class AccordionEventsPage extends Div {
    public AccordionEventsPage() {
        Accordion accordion = new Accordion();
        accordion.add("Panel 0", new Span("Content 0"));
        accordion.add("Panel 1", new Span("Content 1"));
        accordion.add("Panel 2", new Span("Content 2"));

        Div output = new Div();
        output.setId("output");
        output.getStyle().set("white-space", "pre");
        accordion.addOpenedChangeListener(e -> {
            String index = e.getOpenedIndex().isPresent()
                    ? String.valueOf(e.getOpenedIndex().getAsInt())
                    : "null";
            output.setText(output.getText() + String.format(
                    "Opened changed: index=%s, isFromClient=%s\n", index,
                    e.isFromClient()));
        });

        NativeButton openSecond = new NativeButton("Open second",
                e -> accordion.open(1));
        openSecond.setId("open-second");

        NativeButton closeAccordion = new NativeButton("Close",
                e -> accordion.close());
        closeAccordion.setId("close");

        add(accordion, new Div(openSecond, closeAccordion), output);
    }
}
