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
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

/**
 * Page created for testing purposes. Not suitable for demos.
 * 
 * @author Vaadin Ltd.
 *
 */
@Route("dialog-test")
public class DialogTestPage extends Div {

    private static final String BUTTON_CAPTION = "Open dialog";

    public DialogTestPage() {
        createDialogWithAddOpenedChangeListener();
        createDialogWithoutAddingToTheUi();
    }

    private void createDialogWithAddOpenedChangeListener() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        button.setId("dialog-open");
        Div message = new Div();
        message.setId("message");

        Dialog dialog = new Dialog();
        message.setText("The open state of the dialog is " + dialog.isOpened());
        dialog.add(
                new Label("There is a opened change listener for this dialog"));
        button.addClickListener(event -> dialog.open());

        dialog.addOpenedChangeListener(event -> message.setText(
                "The open state of the dialog is " + dialog.isOpened()));
        add(button, message, dialog);
    }

    private void createDialogWithoutAddingToTheUi() {
        NativeButton open = new NativeButton("Open dialog not attached");
        open.setId("dialog-outside-ui-open");
        NativeButton close = new NativeButton("Close dialog");
        close.setId("dialog-outside-ui-close");

        Dialog dialog = new Dialog();
        dialog.setId("dialog-outside-ui");
        dialog.add(new Label("Hei! Moika! Moi!"), close);

        open.addClickListener(event -> dialog.open());
        close.addClickListener(event -> dialog.close());
        add(open);
    }

}
