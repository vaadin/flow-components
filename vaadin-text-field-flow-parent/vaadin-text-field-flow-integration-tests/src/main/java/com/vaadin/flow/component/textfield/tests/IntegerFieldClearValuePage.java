package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.IntegerField;

@Route("vaadin-integer-field/clear-value")
public class IntegerFieldClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";

    public IntegerFieldClearValuePage() {
        IntegerField integerField = new IntegerField();

        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> integerField.clear());

        add(integerField, clearButton);
    }
}
