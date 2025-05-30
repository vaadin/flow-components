/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;

/**
 * Page created for testing purposes. Not suitable for demos.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog-view")
public class DialogView extends Div {

    private static final String BUTTON_CAPTION = "Open dialog";

    public DialogView() {
        addBasicDialog();
        addConfirmationDialog();
        addCloseFromServerSideDialog();
        addDialogWithFocusedElement();
        addStyledDialogContent();
    }

    private void addBasicDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        Dialog dialog = new Dialog();
        dialog.add(new Text("Close me with the esc-key or an outside click"));

        dialog.setWidth("400px");
        dialog.setHeight("150px");

        button.addClickListener(event -> dialog.open());

        button.setId("basic-dialog-button");
        add(button);
    }

    private void addConfirmationDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Span message = new Span();

        Button confirmButton = new Button("Confirm", event -> {
            message.setText("Confirmed!");
            dialog.close();
        });
        Button cancelButton = new Button("Cancel", event -> {
            message.setText("Cancelled...");
            dialog.close();
        });
        dialog.add(confirmButton, cancelButton);
        button.addClickListener(event -> dialog.open());

        message.setId("confirmation-dialog-message");
        button.setId("confirmation-dialog-button");
        add(button, message);
    }

    private void addCloseFromServerSideDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        Span message = new Span();

        Dialog dialog = new Dialog(new Text("Close me with the esc-key"));
        dialog.setCloseOnOutsideClick(false);

        dialog.addDialogCloseActionListener(e -> {
            message.setText("Closed from server-side");
            dialog.close();
        });

        button.addClickListener(event -> dialog.open());

        message.setId("server-side-close-dialog-message");
        button.setId("server-side-close-dialog-button");
        add(button, message);
    }

    private void addDialogWithFocusedElement() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        Dialog dialog = new Dialog();
        Input input = new Input();

        dialog.add(input);

        button.addClickListener(event -> {
            dialog.open();
            input.focus();
        });

        button.setId("focus-dialog-button");
        add(button);
    }

    private void addStyledDialogContent() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        Dialog dialog = new Dialog();
        Div content = new Div();
        content.addClassName("my-style");

        content.setText("This component is styled using global styles");
        dialog.add(content);

        String styles = ".my-style { " + "  color: red;" + " }";

        /*
         * The code below register the style file dynamically. Normally you
         * use @StyleSheet annotation for the component class. This way is
         * chosen just to show the style file source code.
         */
        StreamRegistration resource = UI.getCurrent().getSession()
                .getResourceRegistry()
                .registerResource(new StreamResource("styles.css", () -> {
                    byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(bytes);
                }));
        UI.getCurrent().getPage().addStyleSheet(
                "base://" + resource.getResourceUri().toString());

        dialog.setWidth("400px");
        dialog.setHeight("150px");

        button.addClickListener(event -> dialog.open());

        button.setId("styled-content-dialog-button");
        add(button);
    }
}
