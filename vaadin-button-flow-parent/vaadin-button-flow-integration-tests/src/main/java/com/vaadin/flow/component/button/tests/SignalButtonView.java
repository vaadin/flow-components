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
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * View for {@link Button} demo.
 */
@Route("vaadin-button/signal-button")
public class SignalButtonView extends Div {
    public SignalButtonView() {
        ValueSignal<String> textSignal = new ValueSignal<>("initial text");
        Signal<String> computedSignal = Signal
                .computed(() -> textSignal.get() + " computed");

        var computedSignalButton = new Button(computedSignal);
        computedSignalButton.setId("computed-signal-button");
        add(new Text("computed-signal-button"), computedSignalButton,
                new HtmlComponent("br"));

        var signalIconButton = new Button(textSignal,
                new Icon(VaadinIcon.SIGNAL));
        signalIconButton.setId("signal-icon-button");
        add(new Text("signal-icon-button"), signalIconButton,
                new HtmlComponent("br"));

        var signalIconClickListenerButton = new Button(textSignal,
                new Icon(VaadinIcon.SIGNAL), event -> {
                    textSignal.set("signal-icon-click-button clicked");
                });
        signalIconClickListenerButton.setIconAfterText(true);
        signalIconClickListenerButton.setId("signal-icon-click-button");
        add(new Text("signal-icon-click-button"), signalIconClickListenerButton,
                new HtmlComponent("br"));

        var signalClickListenerButton = new Button(textSignal, event -> {
            textSignal.set("signal-click-button clicked");
        });
        signalClickListenerButton.setId("signal-click-button");
        add(new Text("signal-click-button"), signalClickListenerButton,
                new HtmlComponent("br"));

        var clearTextButton = new Button("Clear text", event -> {
            textSignal.set("");
        });
        clearTextButton.setId("clear-text-button");
        add(clearTextButton, new HtmlComponent("br"));

        var textSignalButton = new Button(textSignal);
        textSignalButton.setId("text-signal-button");

        var removeTextSignalButton = new Button("Remove text signal button",
                event -> {
                    remove(textSignalButton);
                });
        removeTextSignalButton.setId("remove-text-signal-button");
        add(new Text("remove-text-signal-button"), removeTextSignalButton,
                new HtmlComponent("br"));

        var addTextSignalButton = new Button("Add text signal button",
                event -> {
                    add(textSignalButton);
                });
        addTextSignalButton.setId("add-text-signal-button");
        add(new Text("add-text-signal-button"), addTextSignalButton,
                new HtmlComponent("br"));

        add(new Text("text-signal-button"), textSignalButton);
    }
}
