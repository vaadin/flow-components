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
package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/1034 — a Dialog
 * opened from a focus listener closes again immediately.
 */
@Route("repro-1034")
public class Repro1034View extends Div {

    public Repro1034View() {
        add(createCustomFieldCase());
        add(createPlainTextAreaCase());
        add(createNoCloseOnOutsideClickCase());
    }

    /**
     * Faithful port of the reporter's example: TextArea inside a CustomField.
     */
    private Div createCustomFieldCase() {
        Div log = new Div();
        log.setId("custom-field-log");
        log.setText("custom-field log:");

        Dialog dialog = new Dialog();
        dialog.add("Dialog opened from CustomField focus");
        dialog.setId("custom-field-dialog");
        logOpenedChanges(dialog, log, "custom-field");

        TextArea textArea = new TextArea();
        textArea.setId("custom-field-textarea");
        textArea.addFocusListener(e -> dialog.open());

        CustomField<String> customField = new CustomField<>() {
            {
                add(textArea);
            }

            @Override
            protected String generateModelValue() {
                return textArea.getValue();
            }

            @Override
            protected void setPresentationValue(String newPresentationValue) {
            }
        };
        customField.setId("custom-field");

        return new Div(customField, log);
    }

    /** Control: plain TextArea, no CustomField wrapper. */
    private Div createPlainTextAreaCase() {
        Div log = new Div();
        log.setId("plain-log");
        log.setText("plain log:");

        Dialog dialog = new Dialog();
        dialog.add("Dialog opened from plain TextArea focus");
        dialog.setId("plain-dialog");
        logOpenedChanges(dialog, log, "plain");

        TextArea textArea = new TextArea();
        textArea.setId("plain-textarea");
        textArea.addFocusListener(e -> dialog.open());

        return new Div(textArea, log);
    }

    /**
     * Control: same as the plain case but with close-on-outside-click disabled,
     * which the issue comments name as the workaround.
     */
    private Div createNoCloseOnOutsideClickCase() {
        Div log = new Div();
        log.setId("no-outside-log");
        log.setText("no-outside log:");

        Dialog dialog = new Dialog();
        dialog.add("Dialog with closeOnOutsideClick=false");
        dialog.setId("no-outside-dialog");
        dialog.setCloseOnOutsideClick(false);
        logOpenedChanges(dialog, log, "no-outside");

        TextArea textArea = new TextArea();
        textArea.setId("no-outside-textarea");
        textArea.addFocusListener(e -> dialog.open());

        NativeButton close = new NativeButton("Close no-outside dialog",
                e -> dialog.close());
        close.setId("no-outside-close");

        return new Div(textArea, close, log);
    }

    private void logOpenedChanges(Dialog dialog, Div log, String name) {
        dialog.addOpenedChangeListener(e -> log
                .setText(log.getText() + " [" + name + " opened=" + e.isOpened()
                        + " fromClient=" + e.isFromClient() + "]"));
    }
}
