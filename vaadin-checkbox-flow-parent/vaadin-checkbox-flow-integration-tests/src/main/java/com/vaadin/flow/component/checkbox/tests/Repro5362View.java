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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/5362
 */
@Route("repro-5362")
public class Repro5362View extends Div {

    private final Div log = new Div();

    public Repro5362View() {
        log.setId("log");

        // Reporter's example: checkbox without click listener, shortcut "1"
        Checkbox checkbox1 = new Checkbox("Should trigger when clicking 1");
        checkbox1.setId("checkbox1");
        checkbox1.addValueChangeListener(e -> log("checkbox1 value="
                + e.getValue() + " fromClient=" + e.isFromClient()));
        checkbox1.addClickShortcut(Key.DIGIT_1);

        // Reporter's example: checkbox with click listener, shortcut "2"
        Checkbox checkbox2 = new Checkbox("Should trigger when clicking 2");
        checkbox2.setId("checkbox2");
        checkbox2.addClickListener(e -> log("checkbox2 click fromClient="
                + e.isFromClient() + " value=" + checkbox2.getValue()));
        checkbox2.addValueChangeListener(e -> log("checkbox2 value="
                + e.getValue() + " fromClient=" + e.isFromClient()));
        checkbox2.addClickShortcut(Key.DIGIT_2);

        // Control: button click shortcut, shortcut "3"
        Button button = new Button(
                "Should trigger notification when clicking 3");
        button.setId("button");
        button.addClickListener(
                e -> log("button click fromClient=" + e.isFromClient()));
        button.addClickShortcut(Key.DIGIT_3);

        // Sibling component sharing the same ClickNotifier API, shortcut "4"
        Switch switchComponent = new Switch("Should toggle when clicking 4");
        switchComponent.setId("switch");
        switchComponent.addClickListener(e -> log("switch click fromClient="
                + e.isFromClient() + " value=" + switchComponent.getValue()));
        switchComponent.addValueChangeListener(e -> log("switch value="
                + e.getValue() + " fromClient=" + e.isFromClient()));
        switchComponent.addClickShortcut(Key.DIGIT_4);

        add(new Div(checkbox1), new Div(checkbox2), new Div(button),
                new Div(switchComponent), log);
    }

    private void log(String message) {
        Div line = new Div(message);
        line.setClassName("log-line");
        log.add(line);
    }
}
