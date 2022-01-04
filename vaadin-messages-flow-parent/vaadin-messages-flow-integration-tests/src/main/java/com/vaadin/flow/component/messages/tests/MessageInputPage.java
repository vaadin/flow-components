/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.messages.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.router.Route;

@Route("vaadin-messages/message-input-test")
public class MessageInputPage extends Div {

    private final MessageInput messageInput = new MessageInput();

    public MessageInputPage() {
        add(messageInput);

        Input input = new Input();
        input.setId("verify-field");
        add(input);

        messageInput.addSubmitListener(
                submitEvent -> input.setValue(submitEvent.getValue()));

        NativeButton toggleEnabledButton = new NativeButton("toggle-enabled",
                e -> messageInput.setEnabled(!messageInput.isEnabled()));
        toggleEnabledButton.setId("toggle-enabled");
        add(toggleEnabledButton);

        NativeButton setI18nButton = new NativeButton("set-i18n",
                e -> messageInput.setI18n(new MessageInputI18n()
                        .setMessage("Viesti").setSend("Lähetä")));
        setI18nButton.setId("set-i18n");
        add(setI18nButton);
    }
}
