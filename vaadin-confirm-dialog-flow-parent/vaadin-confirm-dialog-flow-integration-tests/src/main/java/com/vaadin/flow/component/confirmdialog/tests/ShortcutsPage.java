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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/shortcuts")
public class ShortcutsPage extends Div {
    public ShortcutsPage() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button shortcutButton = new Button("Confirm");
        shortcutButton.setId("shortcut-button");
        shortcutButton.addClickShortcut(Key.KEY_X);
        shortcutButton.addClickListener(e -> dialog.close());
        dialog.add(shortcutButton);

        Button openDialog = new Button("Open");
        openDialog.setId("open-dialog-button");
        openDialog.addClickListener(e -> dialog.open());
        add(openDialog);
    }
}
