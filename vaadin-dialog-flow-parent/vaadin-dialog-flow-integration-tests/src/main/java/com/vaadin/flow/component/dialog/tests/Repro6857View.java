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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow-components/issues/6857
 * <p>
 * Escape shortcut registered on a button inside a modal Dialog, plus an Escape
 * shortcut registered on a button inside a Notification opened on top of that
 * dialog. Pressing Escape while the notification is visible is expected to
 * invoke only the notification shortcut.
 */
@Route("repro-6857")
public class Repro6857View extends Div {

    private final Div log = new Div();
    private int counter;

    private Dialog dialog;
    private Notification notification;

    public Repro6857View() {
        log.setId("log");

        NativeButton openDialog = new NativeButton("Open modal dialog",
                e -> openDialog(true));
        openDialog.setId("open-modal-dialog");

        NativeButton openModelessDialog = new NativeButton(
                "Open modeless dialog", e -> openDialog(false));
        openModelessDialog.setId("open-modeless-dialog");

        // Control: notification alone, no dialog behind it
        NativeButton openNotificationOnly = new NativeButton(
                "Open notification only", e -> openNotification());
        openNotificationOnly.setId("open-notification-only");

        NativeButton clearLog = new NativeButton("Clear log", e -> {
            log.removeAll();
            counter = 0;
        });
        clearLog.setId("clear-log");

        add(openDialog, openModelessDialog, openNotificationOnly, clearLog, log);
    }

    private void openDialog(boolean modal) {
        dialog = new Dialog();
        dialog.setModal(modal);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setHeaderTitle("Editor");

        Button dialogClose = new Button("Close dialog");
        dialogClose.setId("dialog-close");
        dialogClose.addClickListener(e -> log("DIALOG close button clicked"
                + " (fromClient=" + e.isFromClient() + ")"));
        dialogClose.addClickShortcut(Key.ESCAPE);
        dialog.getHeader().add(dialogClose);

        Button save = new Button("Save (shows notification)",
                e -> openNotification());
        save.setId("dialog-save");

        dialog.add(new Paragraph("Dialog content, modal=" + modal), save);
        dialog.open();
        log("dialog opened, modal=" + modal);
    }

    private void openNotification() {
        notification = new Notification();
        notification.setDuration(0);
        notification.setPosition(Notification.Position.MIDDLE);

        Button notificationClose = new Button("Close notification");
        notificationClose.setId("notification-close");
        notificationClose.addClickListener(e -> {
            log("NOTIFICATION close button clicked (fromClient="
                    + e.isFromClient() + ")");
            notification.close();
        });
        notificationClose.addClickShortcut(Key.ESCAPE);

        notification.add(new Div(new Paragraph("Person invalid")),
                notificationClose);
        notification.open();
        log("notification opened");
    }

    private void log(String message) {
        counter++;
        Div entry = new Div(counter + ": " + message);
        entry.setClassName("log-entry");
        log.add(entry);
    }
}
