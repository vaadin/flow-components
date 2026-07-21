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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/flow/issues/24974 (last comment).
 *
 * A Dialog whose element is appended as a child of a Button. Since Vaadin 25 no
 * longer teleports the overlay, the Dialog DOM stays nested inside the Button,
 * so a click inside the Dialog bubbles up to the Button and re-triggers its
 * click listener (opening yet another Dialog).
 */
@Route("repro-24974")
public class Repro24974View extends VerticalLayout {

    private int buttonClicks = 0;
    private int dialogsOpened = 0;

    public Repro24974View() {
        Span clickCount = new Span("Button listener fired: 0");
        clickCount.setId("click-count");

        Span openCount = new Span("Dialogs opened: 0");
        openCount.setId("open-count");

        Button button = new Button("First Dialog", e -> {
            buttonClicks++;
            clickCount.setText("Button listener fired: " + buttonClicks);
            openDialog(e.getSource(), "Dialog " + buttonClicks, openCount);
        });
        button.setId("open-button");

        add(button, clickCount, openCount);
    }

    private void openDialog(Component parent, String title, Span openCount) {
        dialogsOpened++;
        openCount.setText("Dialogs opened: " + dialogsOpened);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setModality(ModalityMode.MODELESS);
        dialog.setDraggable(true);

        // Clickable content inside the dialog to trigger the bubbling.
        Span body = new Span("Click me (" + title + ")");
        body.setId("dialog-body");
        dialog.add(body);

        parent.getElement().appendChild(dialog.getElement());
        dialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                dialog.removeFromParent();
            }
        });
        dialog.open();
    }
}
