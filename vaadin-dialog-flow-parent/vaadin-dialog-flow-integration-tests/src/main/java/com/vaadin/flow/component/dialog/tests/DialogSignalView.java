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
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog-signal-view")
public class DialogSignalView extends Div {

    private static final String BUTTON_CAPTION = "Open dialog";

    public DialogSignalView() {
        addBasicDialog();
        addConfirmationDialog();
        addCloseFromServerSideDialog();

        addinitiallyOpenDialog();
    }

    private void addinitiallyOpenDialog() {
        ValueSignal<Boolean> openedSignal = new ValueSignal<>(true);

        Dialog dialog = new Dialog();
        dialog.setModality(ModalityMode.MODELESS);
        dialog.bindOpened(openedSignal);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Span span = new Span("Text inside dialog. Can't be closed.");
        span.setId("nested-component");
        dialog.setId("initially-open-dialog");
        dialog.add(span);
    }

    private void addBasicDialog() {
        ValueSignal<Boolean> openedSignal = new ValueSignal<>(false);

        NativeButton button = new NativeButton(BUTTON_CAPTION);

        Dialog dialog = new Dialog();
        dialog.add(new Text("Close me with the esc-key or an outside click"));
        dialog.bindOpened(openedSignal);

        dialog.setWidth("400px");
        dialog.setHeight("150px");

        button.addClickListener(event -> openedSignal.value(true));

        button.setId("basic-dialog-button");
        dialog.setId("basic-dialog");
        add(button);
    }

    private void addConfirmationDialog() {
        ValueSignal<Boolean> openedSignal = new ValueSignal<>(false);

        NativeButton button = new NativeButton(BUTTON_CAPTION);

        Dialog dialog = new Dialog();
        dialog.bindOpened(openedSignal);

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Span message = new Span();
        Span signalValue = new Span(openedSignal
                .map(value -> value != null && value ? "[Signal: true]" : ""));

        Button confirmButton = new Button("Confirm", event -> {
            message.setText("Confirmed!");
            openedSignal.value(false);
        });
        Button cancelButton = new Button("Cancel", event -> {
            message.setText("Cancelled...");
            openedSignal.value(false);
        });
        dialog.add(confirmButton, cancelButton);
        button.addClickListener(event -> openedSignal.value(true));

        message.setId("confirmation-dialog-message");
        signalValue.setId("confirmation-signal-value");
        button.setId("confirmation-dialog-button");
        dialog.setId("confirmation-dialog");
        add(button, message, signalValue);
    }

    private void addCloseFromServerSideDialog() {
        ValueSignal<Boolean> openedSignal = new ValueSignal<>(false);

        NativeButton button = new NativeButton(BUTTON_CAPTION);
        Span message = new Span();

        Dialog dialog = new Dialog(new Text("Close me with the esc-key"));
        dialog.setCloseOnOutsideClick(false);
        dialog.bindOpened(openedSignal);

        dialog.addDialogCloseActionListener(e -> {
            message.setText("Closed from server-side");
            openedSignal.value(false);
        });

        button.addClickListener(event -> openedSignal.value(true));

        message.setId("server-side-close-dialog-message");
        button.setId("server-side-close-dialog-button");
        dialog.setId("server-side-close-dialog");
        add(button, message);
    }

}
