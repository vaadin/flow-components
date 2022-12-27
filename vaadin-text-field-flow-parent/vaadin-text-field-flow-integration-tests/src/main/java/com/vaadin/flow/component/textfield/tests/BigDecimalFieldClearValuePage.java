package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.BigDecimalField;

@Route("vaadin-big-decimal-field/clear-value")
public class BigDecimalFieldClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";

    public BigDecimalFieldClearValuePage() {
        BigDecimalField bigDecimalField = new BigDecimalField();

        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> bigDecimalField.clear());

        add(bigDecimalField, clearButton);
    }
}
