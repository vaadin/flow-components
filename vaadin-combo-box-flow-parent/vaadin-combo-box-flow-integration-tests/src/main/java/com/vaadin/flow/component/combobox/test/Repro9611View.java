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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * Reproduces #9611. The push()/setValue()/push() sequence runs inside a button
 * click handler, i.e. a live synchronous server round-trip with an active push
 * channel — matching the reporter's setup where ui.push() is called before
 * setValue() on a live UI.
 */
@Route("repro-9611")
public class Repro9611View extends Div {
    public Repro9611View() {
        UI ui = UI.getCurrentOrThrow();
        ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        Div log = new Div();
        log.setId("log");

        NativeButton run = new NativeButton("Run repro", e -> {
            MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
            comboBox.setId("combo");
            comboBox.addValueChangeListener(ev -> {
                Div entry = new Div();
                entry.setText("value-change"
                        + (ev.isFromClient() ? " from client" : "")
                        + " | old: [" + String.join(",", ev.getOldValue())
                        + "] | new: [" + String.join(",", ev.getValue())
                        + "] | getValue(): ["
                        + String.join(",", comboBox.getValue()) + "]");
                log.add(entry);
            });

            comboBox.setItems(List.of("1", "2", "3"));
            add(comboBox);

            ui.push(); // first push (before setValue)

            comboBox.setValue(Set.of("1"));
            ui.push(); // second push (after setValue)

            System.out.println(
                    "# value: " + String.join(",", comboBox.getValue()));
        });
        run.setId("run");

        add(run, log);
    }
}
