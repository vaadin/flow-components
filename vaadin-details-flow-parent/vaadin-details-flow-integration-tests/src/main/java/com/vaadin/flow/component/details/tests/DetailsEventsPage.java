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
