package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

@Route("vaadin-time-picker/clear-value")
public class ClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";

    public ClearValuePage() {
        TimePicker timePicker = new TimePicker();

        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> timePicker.clear());

        add(timePicker, clearButton);
    }
}
