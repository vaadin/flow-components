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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/9815
 *
 * Non-modal dialog does not regain focus when clicking its header after losing
 * focus to the background, so Esc no longer closes it.
 */
@Route("repro-9815")
public class Repro9815View extends Div {

    private final Span status = new Span("initial");

    public Repro9815View() {
        status.setId("status");

        Input bgInput = new Input();
        bgInput.setId("bg-input");

        Dialog draggableDialog = createDialog(true, "draggable");
        Dialog plainDialog = createDialog(false, "plain");

        NativeButton openDraggable = new NativeButton("open draggable",
                e -> draggableDialog.open());
        openDraggable.setId("open-draggable");

        NativeButton openPlain = new NativeButton("open plain",
                e -> plainDialog.open());
        openPlain.setId("open-plain");

        add(status, new Div(bgInput), new Div(openDraggable, openPlain));
    }

    private Dialog createDialog(boolean draggable, String key) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Title " + key);
        dialog.setDraggable(draggable);
        dialog.setModal(false);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        Div content = new Div("Content area " + key);
        content.setId(key + "-content");
        content.setHeight("100px");
        content.setWidth("300px");

        Input field = new Input();
        field.setId(key + "-field");

        dialog.add(content, field);

        NativeButton close = new NativeButton("Close", e -> dialog.close());
        close.setId(key + "-close");
        dialog.getFooter().add(close);

        dialog.addOpenedChangeListener(e -> status
                .setText(key + (e.isOpened() ? " opened" : " closed")));
        return dialog;
    }
}
