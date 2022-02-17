/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
