/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

@Route("vaadin-text-field/text-area-pattern")
public class TextAreaPatternPage extends Div {

    public TextAreaPatternPage() {
        TextArea textArea = new TextArea();
        TextFieldTestPageUtil.addInvalidCheck(this, textArea);

        Div currentValue = new Div();
        currentValue.setId("current-value");
        textArea.addValueChangeListener(
                event -> currentValue.setText(event.getValue()));

        NativeButton setNumberPattern = new NativeButton("Set number pattern",
                e -> textArea.setPattern("[0-9]*"));
        setNumberPattern.setId("set-number-pattern");

        NativeButton setInvalidPattern = new NativeButton("Set invalid pattern",
                e -> textArea.setPattern("[0-9"));
        setInvalidPattern.setId("set-invalid-pattern");

        NativeButton clearPattern = new NativeButton("Clear pattern",
                e -> textArea.setPattern(null));
        clearPattern.setId("clear-pattern");

        NativeButton enablePreventInvalidInput = new NativeButton(
                "Enable prevent invalid input",
                e -> textArea.setPreventInvalidInput(true));
        enablePreventInvalidInput.setId("enable-prevent-invalid-input");

        add(textArea);
        add(currentValue);
        add(new Div(setNumberPattern, setInvalidPattern, clearPattern,
                enablePreventInvalidInput));
    }

}
