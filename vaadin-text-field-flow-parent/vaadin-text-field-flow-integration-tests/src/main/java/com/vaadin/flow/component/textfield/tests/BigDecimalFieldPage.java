
package com.vaadin.flow.component.textfield.tests;

import java.math.BigDecimal;
import java.util.Locale;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;

@Route("vaadin-text-field/big-decimal-field-test")
public class BigDecimalFieldPage extends Div {

    private Div messageContainer;

    public BigDecimalFieldPage() {
        messageContainer = new Div();
        messageContainer.setId("messages");

        BigDecimalField field = new BigDecimalField();
        field.addValueChangeListener(this::logValueChangeEvent);

        NativeButton setValueWithScale = new NativeButton("set value",
                e -> field.setValue(new BigDecimal("1.2").setScale(3)));
        setValueWithScale.setId("set-value-with-scale");

        NativeButton toggleReadOnly = new NativeButton("toggle read-only",
                e -> field.setReadOnly(!field.isReadOnly()));
        toggleReadOnly.setId("toggle-read-only");

        NativeButton toggleRequired = new NativeButton("toggle required",
                e -> field.setRequiredIndicatorVisible(
                        !field.isRequiredIndicatorVisible()));
        toggleRequired.setId("toggle-required");

        NativeButton toggleEnabled = new NativeButton("toggle enabled",
                e -> field.setEnabled(!field.isEnabled()));
        toggleEnabled.setId("toggle-enabled");

        NativeButton setFrenchLocale = new NativeButton("Set French locale",
                e -> field.setLocale(Locale.FRENCH));
        setFrenchLocale.setId("set-french-locale");

        BigDecimalField fieldWithClearButton = new BigDecimalField();
        fieldWithClearButton.setClearButtonVisible(true);
        fieldWithClearButton.setId("clear-big-decimal-field");
        fieldWithClearButton.addValueChangeListener(this::logValueChangeEvent);

        Div buttons = new Div(setValueWithScale, toggleReadOnly, toggleRequired,
                toggleEnabled, setFrenchLocale);
        add(field, buttons, fieldWithClearButton, messageContainer);
    }

    private void logValueChangeEvent(
            ComponentValueChangeEvent<BigDecimalField, BigDecimal> event) {
        String text = String.format("Old value: '%s'. New value: '%s'.",
                event.getOldValue(), event.getValue());
        Paragraph paragraph = new Paragraph(text);
        messageContainer.add(paragraph);
    }
}
