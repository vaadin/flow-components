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

import java.util.List;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/5943.
 *
 * A dialog is opened from a value change listener of a text field. Committing
 * the value by clicking somewhere else with the mouse makes the dialog close
 * again right after it opened. Committing it with Tab keeps it open.
 *
 * Query parameters:
 * <ul>
 * <li>{@code delay} — milliseconds to sleep on the server before opening the
 * dialog, to widen or narrow the window between mousedown and mouseup.</li>
 * </ul>
 */
@Route("repro-5943")
public class Repro5943View extends Div implements BeforeEnterObserver {

    private final Div log = new Div();
    private long delay = 0;
    private long base = System.currentTimeMillis();

    public Repro5943View() {
        log.setId("log");

        add(variant("close-on-outside-click", true));
        add(variant("no-close-on-outside-click", false));

        Dialog plain = createDialog("button", true);
        NativeButton openFromButton = new NativeButton(
                "Open from button (control)", e -> plain.open());
        openFromButton.setId("open-from-button");

        NativeButton clear = new NativeButton("Clear log", e -> {
            log.setText("");
            base = System.currentTimeMillis();
        });
        clear.setId("clear-log");

        add(new Div(openFromButton, clear), log);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        List<String> values = event.getLocation().getQueryParameters()
                .getParameters().get("delay");
        delay = values == null ? 0 : Long.parseLong(values.get(0));
        base = System.currentTimeMillis();
    }

    private Div variant(String id, boolean closeOnOutsideClick) {
        TextField first = new TextField("Change value here (" + id + ")");
        first.setId(id + "-first");
        first.setWidth("240px");

        TextField second = new TextField("Then click here");
        second.setId(id + "-second");
        second.setWidth("240px");

        first.addValueChangeListener(event -> {
            record(id, "value-change value=" + event.getValue() + " fromClient="
                    + event.isFromClient());
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            createDialog(id, closeOnOutsideClick).open();
        });

        Div wrapper = new Div(first, second);
        wrapper.getStyle().set("display", "flex").set("gap", "1rem")
                .set("padding", "0.5rem 0");
        return wrapper;
    }

    private Dialog createDialog(String id, boolean closeOnOutsideClick) {
        Dialog dialog = new Dialog(new Span("I'm a dialog (" + id + ")"));
        dialog.setWidth("400px");
        dialog.setHeight("150px");
        dialog.setCloseOnOutsideClick(closeOnOutsideClick);
        dialog.addOpenedChangeListener(event -> record(id, "opened="
                + event.isOpened() + " fromClient=" + event.isFromClient()));
        return dialog;
    }

    private void record(String id, String message) {
        long time = System.currentTimeMillis() - base;
        log.add(new Div(new Span("[" + time + "ms] " + id + ": " + message)));
    }
}
