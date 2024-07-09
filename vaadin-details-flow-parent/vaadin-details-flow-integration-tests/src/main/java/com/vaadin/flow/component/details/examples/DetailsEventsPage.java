/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.details.examples;

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
