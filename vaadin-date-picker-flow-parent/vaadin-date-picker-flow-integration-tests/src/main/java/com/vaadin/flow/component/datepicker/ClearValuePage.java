package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/clear-value")
public class ClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";

    public ClearValuePage() {
        DatePicker datePicker = new DatePicker();

        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> datePicker.clear());

        add(datePicker, clearButton);
    }
}
