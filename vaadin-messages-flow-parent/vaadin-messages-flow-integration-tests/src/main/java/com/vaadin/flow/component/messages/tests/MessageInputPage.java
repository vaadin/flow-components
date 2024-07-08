/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
