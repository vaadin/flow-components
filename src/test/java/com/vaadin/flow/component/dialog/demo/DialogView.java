/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog.demo;

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Dialog} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog")
@HtmlImport("frontend://bower_components/vaadin-dialog/theme/lumo/vaadin-dialog.html")
public class DialogView extends DemoView {

    private static final String BUTTON_CAPTION = "Open dialog";

    @Override
    public void initView() {
        addBasicDialog();
        addConfirmationDialog();
    }

    private void addBasicDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Basic dialog
        Dialog dialog = new Dialog();
        dialog.add(new Label("Close me with the esc-key or an outside click"));

        button.addClickListener(event -> dialog.open());
        // end-source-example

        button.setId("basic-dialog-button");
        addCard("Basic dialog", button, dialog);
    }

    private void addConfirmationDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Confirmation dialog
        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Label messageLabel = new Label();

        NativeButton confirmButton = new NativeButton("Confirm", event -> {
            messageLabel.setText("Confirmed!");
            dialog.close();
        });
        NativeButton cancelButton = new NativeButton("Cancel", event -> {
            messageLabel.setText("Cancelled...");
            dialog.close();
        });
        dialog.add(confirmButton, cancelButton);
        // end-source-example
        button.addClickListener(event -> dialog.open());

        button.setId("confirmation-dialog-button");
        addCard("Confirmation dialog", button, dialog, messageLabel);
    }
}
