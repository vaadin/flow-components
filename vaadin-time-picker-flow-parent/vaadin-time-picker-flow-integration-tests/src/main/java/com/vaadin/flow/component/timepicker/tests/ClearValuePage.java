package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.Route;

@Route("vaadin-time-picker/clear-value")
public class ClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";

    public ClearValuePage() {
        TimePicker timePicker = new TimePicker();

        Label label1 = new Label(timePicker.getLocale().getDisplayCountry());
        label1.setId("country");
        Label label2 = new Label(timePicker.getLocale().getDisplayLanguage());
        label2.setId("language");
        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> timePicker.clear());

        add(timePicker, label1,label2,clearButton);
    }
}
