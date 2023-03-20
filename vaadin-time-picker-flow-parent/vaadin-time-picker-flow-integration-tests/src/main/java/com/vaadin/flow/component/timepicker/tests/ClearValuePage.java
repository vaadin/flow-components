package com.vaadin.flow.component.timepicker.tests;

import java.time.LocalTime;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

@Route("vaadin-time-picker/clear-value")
public class ClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";
    public static final String CLEAR_AND_SET_VALUE_BUTTON = "clear-and-set-value-button";

    public ClearValuePage() {
        TimePicker timePicker = new TimePicker();

        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> timePicker.clear());

        NativeButton clearAndSetValueButton = new NativeButton(
                "Clear and set value", event -> {
                    timePicker.clear();
                    timePicker.setValue(LocalTime.of(10, 0));
                });
        clearAndSetValueButton.setId(CLEAR_AND_SET_VALUE_BUTTON);

        add(timePicker, clearButton, clearAndSetValueButton);
    }
}
