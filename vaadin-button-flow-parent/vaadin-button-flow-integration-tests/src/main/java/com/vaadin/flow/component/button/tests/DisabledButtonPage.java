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
package com.vaadin.flow.component.button.tests;

import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import elemental.json.Json;
import elemental.json.JsonObject;

@Route("vaadin-button/disabled-button")
public class DisabledButtonPage extends Div {
    private AtomicInteger blurListenerCounter = new AtomicInteger(0);
    private AtomicInteger focusListenerCounter = new AtomicInteger(0);
    private AtomicInteger clickListenerCounter = new AtomicInteger(0);
    private AtomicInteger doubleClickListenerCounter = new AtomicInteger(0);

    private Div listenerCounters;

    public DisabledButtonPage() {
        Button button = new Button("Disabled button");
        button.setEnabled(false);

        button.addFocusShortcut(Key.KEY_A, KeyModifier.ALT);
        button.addBlurListener(
                event -> incrementListenerCounter(blurListenerCounter));
        button.addFocusListener(
                event -> incrementListenerCounter(focusListenerCounter));
        button.addClickListener(
                event -> incrementListenerCounter(clickListenerCounter));
        button.addDoubleClickListener(
                event -> incrementListenerCounter(doubleClickListenerCounter));

        listenerCounters = new Div();
        listenerCounters.setId("listener-counters");

        NativeButton enableButton = new NativeButton("Enable button", event -> {
            button.setEnabled(true);
        });
        enableButton.setId("enable-button");

        add(button, listenerCounters, enableButton);
    }

    private void incrementListenerCounter(AtomicInteger listenerCounter) {
        listenerCounter.incrementAndGet();
        updateListenerCounters();
    }

    private void updateListenerCounters() {
        JsonObject json = Json.createObject();
        json.put("blur", blurListenerCounter.get());
        json.put("focus", focusListenerCounter.get());
        json.put("click", clickListenerCounter.get());
        json.put("doubleClick", doubleClickListenerCounter.get());
        listenerCounters.setText(json.toJson());
    }
}
