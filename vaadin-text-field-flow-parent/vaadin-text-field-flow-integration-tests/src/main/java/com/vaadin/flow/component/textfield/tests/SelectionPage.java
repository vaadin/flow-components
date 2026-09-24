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

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.shared.SelectionRange;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * Test view for the {@code HasSelection} mixin on text fields.
 */
@Route("vaadin-text-field/selection-test")
public class SelectionPage extends Div {

    public SelectionPage() {
        TextField textField = new TextField();
        textField.setId("text-field");
        textField.setValue("Hello world");

        TextArea textArea = new TextArea();
        textArea.setId("text-area");
        textArea.setValue("Lorem ipsum dolor sit amet");

        PasswordField passwordField = new PasswordField();
        passwordField.setId("password-field");
        passwordField.setValue("secret123");

        Div info = new Div();
        info.setId("selection-info");
        info.setText("(no selection)");
        Signal.effect(this,
                ctx -> info.setText(format(textField.selectionSignal().get())));

        Div areaInfo = new Div();
        areaInfo.setId("area-selection-info");
        areaInfo.setText("(no selection)");
        Signal.effect(this, ctx -> areaInfo
                .setText(format(textArea.selectionSignal().get())));

        add(textField, textArea, passwordField, info, areaInfo);

        addButton("select-all", "Select all", () -> textField.selectAll());
        addButton("set-range", "setSelectionRange(2, 7)",
                () -> textField.setSelectionRange(2, 7));
        addButton("set-cursor", "setCursorPosition(4)",
                () -> textField.setCursorPosition(4));
        addButton("deselect", "Deselect", () -> textField.deselect());
        addButton("focus", "Focus", () -> textField.focus());
        addButton("blur", "Blur",
                () -> textField.getElement().executeJs("this.blur()"));

        addButton("area-select-range", "Area setSelectionRange(0, 5)",
                () -> textArea.setSelectionRange(0, 5));
        addButton("area-focus", "Area focus", () -> textArea.focus());

        // Mirrors UC6: server-side click handler reads the current selection
        // synchronously via selectionSignal().peek() and replaces it in place.
        // Proves the timing the PR #3194 thread was worried about: when the
        // user selects text and clicks a server button, the handler sees the
        // selection that was active at click time, with no extra roundtrip.
        Div transformInfo = new Div();
        transformInfo.setId("transform-info");
        transformInfo.setText("(no transform yet)");
        AtomicInteger transformCount = new AtomicInteger();
        addButton("area-uppercase", "Uppercase area selection", () -> {
            SelectionRange sel = textArea.selectionSignal().peek();
            int n = transformCount.incrementAndGet();
            if (sel.isEmpty()) {
                transformInfo.setText("#" + n + " empty");
                return;
            }
            String value = textArea.getValue();
            String replaced = sel.content().toUpperCase();
            textArea.setValue(value.substring(0, sel.start()) + replaced
                    + value.substring(sel.end()));
            textArea.setSelectionRange(sel.start(),
                    sel.start() + replaced.length());
            transformInfo.setText("#" + n + " " + sel.start() + "-" + sel.end()
                    + ":" + sel.content());
        });
        add(transformInfo);

        addButton("password-select-all", "Password selectAll",
                () -> passwordField.selectAll());
        addButton("password-focus", "Password focus",
                () -> passwordField.focus());
    }

    private void addButton(String id, String label, Runnable action) {
        NativeButton button = new NativeButton(label, e -> action.run());
        button.setId(id);
        add(button);
    }

    private static String format(SelectionRange r) {
        return r.start() + "-" + r.end() + ":" + r.length() + ":" + r.content();
    }
}
