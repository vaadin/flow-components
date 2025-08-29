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
package com.vaadin.flow.component.confirmdialog.tests;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/open-change")
public class OpenChangeListenerView extends Div {

    int eventCounter;

    public OpenChangeListenerView() {
        NativeButton button = new NativeButton("Show dialog");
        button.setId("dialog-open");

        Div message = new Div();
        message.setId("message");

        Div eventCounterMessage = new Div();
        eventCounterMessage.setId("event-counter-message");

        Div eventSourceMessage = new Div();
        eventSourceMessage.setId("event-source-message");

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId("dialog");
        dialog.addClosedListener(e -> dialog.close());
        dialog.setText("There is an opened change listener for this dialog");

        message.setText("The open state of the dialog is " + dialog.isOpened());

        button.addClickListener(event -> dialog.open());

        eventCounter = 0;
        dialog.addOpenedChangeListener(event -> {
            message.setText(
                    "The open state of the dialog is " + dialog.isOpened());
            eventCounterMessage
                    .setText("Number of events is " + ++eventCounter);
            eventSourceMessage.setText("The event came from "
                    + (event.isFromClient() ? "client" : "server"));
        });
        add(button, message, eventCounterMessage, eventSourceMessage, dialog);
    }
}
