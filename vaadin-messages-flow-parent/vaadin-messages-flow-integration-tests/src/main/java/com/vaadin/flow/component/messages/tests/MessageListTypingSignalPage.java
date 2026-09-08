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
package com.vaadin.flow.component.messages.tests;

import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.messages.MessageListUser;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.signals.local.ValueSignal;

@Route("vaadin-messages/message-list-typing-signal-test")
public class MessageListTypingSignalPage extends Div {

    public MessageListTypingSignalPage() {
        var alice = new ValueSignal<>(new MessageListUser("Alice"));
        var typingUsers = new ValueSignal<List<ValueSignal<MessageListUser>>>(
                List.of());

        var messageList = new MessageList(new MessageListItem("foo", "sender"));
        messageList.bindTypingUsers(typingUsers);
        add(messageList);

        addButton("showTyping", () -> typingUsers.set(List.of(alice)));
        addButton("hideTyping", () -> typingUsers.set(List.of()));
        addButton("renameTypingUser",
                () -> alice.set(new MessageListUser("Alice Smith")));
    }

    private void addButton(String id, Command action) {
        NativeButton button = new NativeButton(id, e -> action.execute());
        button.setId(id);
        add(button);
    }
}
