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

@Route(value = "vaadin-confirm-dialog/events")
public class EventView extends Div {

    public EventView() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button showDialogButton = new Button("Show dialog", e -> dialog.open());
        showDialogButton.setId("open-dialog");

        Button toggleCloseOnEscButton = new Button("Toggle close on Esc",
                e -> dialog.setCloseOnEsc(!dialog.isCloseOnEsc()));
        toggleCloseOnEscButton.setId("toggle-close-on-esc");
        dialog.add(toggleCloseOnEscButton);

        add(dialog, showDialogButton);
    }
}
