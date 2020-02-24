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

    private int eventCounter;

    public DialogTestPage() {
        createDialogWithAddOpenedChangeListener();
        createDialogWithoutAddingToTheUi();
        createDialogAddingToTheUiAfterOpening();
        createEmptyDialog();
        createDialogAndAddComponentAtIndex();
        createDivInDialog();
        createResizableDialog();
    }

    private void createDialogWithAddOpenedChangeListener() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);
        button.setId("dialog-open");

        Div message = new Div();
        message.setId("message");

        Div eventCounterMessage = new Div();
        eventCounterMessage.setId("event-counter-message");

        Div eventSourceMessage = new Div();
        eventSourceMessage.setId("event-source-message");

        Dialog dialog = new Dialog();
        dialog.setId("dialog");
        dialog.addDialogCloseActionListener(e -> dialog.close());

        message.setText("The open state of the dialog is " + dialog.isOpened());
        dialog.add(
                new Label("There is a opened change listener for this dialog"));
        button.addClickListener(event -> dialog.open());

        eventCounter = 0;
        dialog.addOpenedChangeListener(event -> {
            message.setText(
                    "The open state of the dialog is " + dialog.isOpened());
            eventCounterMessage.setText("Number of event is " + eventCounter++);
            eventSourceMessage.setText("The event came from "
                    + (event.isFromClient() ? "client" : "server"));
        });
        add(button, message, eventCounterMessage, eventSourceMessage, dialog);
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

    private void createDialogAddingToTheUiAfterOpening() {
        NativeButton open = new NativeButton("Open and attach dialog");
        open.setId("dialog-in-ui-after-opened-open");
        NativeButton close = new NativeButton("Close dialog");
        close.setId("dialog-in-ui-after-opened-close");

        Dialog dialog = new Dialog();
        dialog.setId("dialog-in-ui-after-opened");
        dialog.add(new Label("Hei! Moika! Moi!"), close);

        open.addClickListener(event -> {
            dialog.setOpened(true);
            if(getChildren().noneMatch(child -> child.equals(dialog))){
                add(dialog);
            }
        });
        close.addClickListener(event -> dialog.close());
        add(open);
    }

    private void createEmptyDialog() {
        Dialog dialog = new Dialog();
        dialog.setId("empty-dialog");

        NativeButton button = new NativeButton("empty-dialog",
                event -> dialog.open());
        button.setId("open-button");

        add(button);
    }

    private void createDialogAndAddComponentAtIndex() {
        Dialog dialog = new Dialog();
        dialog.setId("dialog-add-component-at-index");
        dialog.add(new NativeButton(), new NativeButton(), new NativeButton());

        NativeButton button = new NativeButton("open Dialog",
                event -> dialog.open());
        button.setId("open-dialog-add-component-at-index");

        NativeButton addedButton = new NativeButton("Added Button");
        addedButton.setId("added-button");
        NativeButton addFirst = new NativeButton("Add to the first", event -> {
            dialog.addComponentAsFirst(addedButton);
            dialog.open();
        });
        addFirst.setId("button-to-first");

        NativeButton addAtSecond = createTestButton(dialog, addedButton,
                "button-to-second", 1);

        add(button, addFirst, addAtSecond);
    }

    private NativeButton createTestButton(Dialog dialog,
            NativeButton addedButton, String buttonId, int index) {
        NativeButton button = new NativeButton(buttonId, event -> {
            dialog.addComponentAtIndex(index, addedButton);
            dialog.open();
        });
        button.setId(buttonId);
        return button;
    }

    private void createDivInDialog() {
        Div div = new Div();
        div.setId("div-in-dialog");

        Dialog dialog = new Dialog(div);

        NativeButton button = new NativeButton("open Dialog",
                event -> dialog.open());
        button.setId("button-for-dialog-with-div");

        dialog.setSizeFull();
        div.setSizeFull();
        add(button);
    }

    private void createResizableDialog() {
        Dialog dialog = new Dialog();
        dialog.setId("dialog-resizable");
        dialog.setResizable(true);
        dialog.setWidth("200px");
        dialog.setHeight("200px");

        Div message = new Div();
        message.setId("dialog-resizable-message");

        dialog.addResizeListener(e ->
                message.setText("Rezise listener called with width (" +
                e.getWidth() + ") and height (" + e.getHeight() + ")"));

        dialog.addOpenedChangeListener(e ->
                message.setText("Initial size with width (" +
                dialog.getWidth() + ") and height (" + dialog.getHeight() + ")"));            

        NativeButton closeButton = new NativeButton("close",
                e -> dialog.close());
        closeButton.setId("dialog-resizable-close-button");
        dialog.add(closeButton);

        NativeButton openDialog = new NativeButton("open resizable dialog",
                e -> dialog.open());
        openDialog.setId("dialog-resizable-open-button");

        add(openDialog, message);
    }
}
