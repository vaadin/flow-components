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

import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;

/**
 * Repro for https://github.com/vaadin/web-components/issues/12465
 *
 * Two overlapping modeless dialogs, each with a Select. Opening the Select
 * overlay of the frontmost dialog is expected to make clicks on the frontmost
 * dialog itself land on that dialog, not on the dialog behind it.
 */
@Route("repro-12465")
public class Repro12465View extends Div {

    private final Div log = new Div();
    private int dialogCount;

    public Repro12465View() {
        log.setId("log");

        NativeButton open = new NativeButton("Open dialog",
                e -> openDialog(null));
        open.setId("open-dialog");

        NativeButton clearLog = new NativeButton("Clear log",
                e -> log.removeAll());
        clearLog.setId("clear-log");

        NativeButton pageButton = new NativeButton("Page button",
                e -> logEvent("page-button clicked"));
        pageButton.setId("page-button");

        add(new Div(open, clearLog, pageButton), log);
    }

    private void openDialog(Dialog parent) {
        int index = ++dialogCount;

        Dialog dialog = new Dialog();
        dialog.setId("dialog-" + index);
        dialog.setHeaderTitle("Dialog " + index);
        dialog.setDraggable(true);
        dialog.setModality(ModalityMode.MODELESS);
        dialog.setWidth("360px");
        // Wide gutter so the part='overlay' area not covered by slotted
        // content is easy to hit with the mouse.
        dialog.getElement().getStyle().set("--vaadin-dialog-padding", "48px");
        dialog.setTop((60 + (index - 1) * 260) + "px");
        dialog.setLeft((80 + (index - 1) * 40) + "px");

        Select<String> select = new Select<>();
        select.setId("select-" + index);
        select.setLabel("Select " + index);
        select.setItems("1", "2", "3");
        select.addValueChangeListener(
                e -> logEvent("select-" + index + " value=" + e.getValue()));

        NativeButton openNested = new NativeButton("Open nested dialog",
                e -> openDialog(dialog));
        openNested.setId("open-nested-" + index);

        NativeButton marker = new NativeButton("Button " + index,
                e -> logEvent("button-" + index + " clicked"));
        marker.setId("button-" + index);

        NativeButton close = new NativeButton("Close", e -> dialog.close());
        close.setId("close-" + index);

        // Footer content is slotted directly into the dialog element, unlike
        // the dialog content, which goes through <vaadin-dialog-content>.
        NativeButton footerButton = new NativeButton("Footer " + index,
                e -> logEvent("footer-button-" + index + " clicked"));
        footerButton.setId("footer-button-" + index);
        dialog.getFooter().add(footerButton);

        dialog.add(new Div(select), new Div(openNested, marker, close));

        if (parent != null) {
            // As in the reported example: the nested dialog is a child of the
            // parent dialog.
            parent.add(dialog);
        }
        dialog.open();
    }

    private void logEvent(String message) {
        Div entry = new Div(message);
        entry.setClassName("log-entry");
        log.add(entry);
    }
}
