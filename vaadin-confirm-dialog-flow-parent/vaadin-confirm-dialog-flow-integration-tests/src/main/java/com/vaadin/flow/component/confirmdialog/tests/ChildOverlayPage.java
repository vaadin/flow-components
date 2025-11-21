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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/child-overlay")
public class ChildOverlayPage extends Div {
    public ChildOverlayPage() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId("parent-dialog");
        dialog.setText("This is the parent dialog");

        ConfirmDialog dialog2 = new ConfirmDialog();
        dialog2.setText("This is a child dialog");
        // Just so the issue is more visible
        dialog2.getElement().executeJs("this.$.overlay.style.top='300px'");

        Button openDialog = new Button("Open dialogs", e -> {
            dialog.open();
            dialog2.open();
        });
        openDialog.setId("open-dialogs");

        add(new Div(openDialog));
    }
}
