package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.NumberField;

@Route("vaadin-number-field/clear-value")
public class NumberFieldClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";
    public static final String CLEAR_AND_SET_VALUE_BUTTON = "clear-and-set-value-button";

    public NumberFieldClearValuePage() {
        NumberField numberField = new NumberField();

        NativeButton clearButton = new NativeButton("Clear value", event -> {
            numberField.clear();
        });
        clearButton.setId(CLEAR_BUTTON);

        NativeButton clearAndSetValueButton = new NativeButton(
                "Clear and set value", event -> {
                    numberField.clear();
                    numberField.setValue(Double.valueOf(1234));
                });
        clearAndSetValueButton.setId(CLEAR_AND_SET_VALUE_BUTTON);

        add(numberField, clearButton, clearAndSetValueButton);
    }
}
