/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/basicuse")
public class BasicUseView extends Div {

    private Log log = new Log();

    boolean expanded = true;

    public BasicUseView() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("My header");
        confirmDialog.setText("Here is my text");
        Button showDialogButton = new Button("Show dialog",
                e -> confirmDialog.open());
        showDialogButton.setId("open-dialog");

        NativeButton logButton = new NativeButton("Log",
                event -> log.log("Clicked"));
        logButton.setId("log");

        add(confirmDialog, showDialogButton, logButton, new Hr(), log);
    }

    public static class Log extends Div {

        public static final String LOG_ID = "log-output";

        private int logCount;

        public Log() {
            setId(LOG_ID);
        }

        public void log(String msg) {
            Div div = new Div();
            div.addClassName("log");
            logCount++;
            div.setText(logCount + ". " + msg);
            add(div);
        }
    }
}
