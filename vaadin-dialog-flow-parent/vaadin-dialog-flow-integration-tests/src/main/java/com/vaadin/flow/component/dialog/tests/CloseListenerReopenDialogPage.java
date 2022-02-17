/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

@Route("vaadin-dialog/close-listener-reopen-dialog")
public class CloseListenerReopenDialogPage extends Div {

    public CloseListenerReopenDialogPage() {
        Dialog dialog = new Dialog();
        NativeButton close = new NativeButton("Close dialog",
                event -> dialog.close());
        close.setId("close");
        dialog.add(close);
        Registration registration = dialog.addDialogCloseActionListener(
                event -> addInfo("main", "Main dialog is closed"));
        NativeButton open = new NativeButton("Open dialog",
                event -> dialog.open());
        open.setId("open");
        add(open);

        NativeButton button = new NativeButton("Remove close listener",
                event -> registration.remove());
        button.setId("remove");
        add(button);

        Dialog subDialog = new Dialog();
        subDialog.add(new Text("Subdialog"));
        dialog.add(subDialog);
        subDialog.addDialogCloseActionListener(event -> {
            addInfo("sub", "Subdialog is closed");
            subDialog.close();
        });

        NativeButton openSubDialog = new NativeButton("Open subdialog",
                event -> subDialog.open());
        openSubDialog.setId("open-sub");
        dialog.add(openSubDialog);
    }

    private void addInfo(String style, String text) {
        Div div = new Div();
        div.setText(text);
        div.addClassName(style);
        add(div);
    }
}
