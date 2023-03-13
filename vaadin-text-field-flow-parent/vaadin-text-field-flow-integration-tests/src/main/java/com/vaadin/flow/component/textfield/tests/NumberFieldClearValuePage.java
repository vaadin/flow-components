package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.NumberField;

@Route("vaadin-number-field/clear-value")
public class NumberFieldClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";

    public NumberFieldClearValuePage() {
        NumberField numberField = new NumberField();

        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> numberField.clear());

        add(numberField, clearButton);
    }
}
