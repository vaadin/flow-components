/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-text-field/text-selection")
public class TextSelectionPage extends Div {

    public TextSelectionPage() {
        TextField textField = new TextField();
        textField.setId("text-field");
        textField.setValue("Hello world");

        Div textFieldFocusFromClient = new Div();
        textFieldFocusFromClient.setId("text-field-focus-from-client");
        textField.addFocusListener(e -> textFieldFocusFromClient
                .setText("Is focus from client: " + e.isFromClient()));

        NativeButton textFieldSelectAll = new NativeButton("selectAll()",
                e -> textField.selectAll());
        textFieldSelectAll.setId("text-field-select-all");

        NativeButton textFieldSetRange = new NativeButton(
                "setSelectionRange(2, 7)",
                e -> textField.setSelectionRange(2, 7));
        textFieldSetRange.setId("text-field-set-range");

        NativeButton textFieldSetCursor = new NativeButton(
                "setCursorPosition(4)", e -> textField.setCursorPosition(4));
        textFieldSetCursor.setId("text-field-set-cursor");

        NativeButton textFieldEnableAutoselect = new NativeButton(
                "Enable autoselect", e -> textField.setAutoselect(true));
        textFieldEnableAutoselect.setId("text-field-enable-autoselect");

        add(new H2("TextField"), textField,
                new Div(textFieldSelectAll, textFieldSetRange,
                        textFieldSetCursor, textFieldEnableAutoselect),
                textFieldFocusFromClient);

        TextArea textArea = new TextArea();
        textArea.setId("text-area");
        textArea.setValue("Lorem ipsum dolor sit amet");

        NativeButton textAreaSetRange = new NativeButton(
                "setSelectionRange(2, 7)",
                e -> textArea.setSelectionRange(2, 7));
        textAreaSetRange.setId("text-area-set-range");

        add(new H2("TextArea"), textArea, new Div(textAreaSetRange));

        PasswordField passwordField = new PasswordField();
        passwordField.setId("password-field");
        passwordField.setValue("secret123");

        NativeButton passwordFieldSetRange = new NativeButton(
                "setSelectionRange(2, 7)",
                e -> passwordField.setSelectionRange(2, 7));
        passwordFieldSetRange.setId("password-field-set-range");

        add(new H2("PasswordField"), passwordField,
                new Div(passwordFieldSetRange));
    }
}
