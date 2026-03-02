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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

@Route("vaadin-messages/message-list-signal-test")
public class MessageListSignalPage extends Div {

    private final List<ValueSignal<MessageListItem>> itemSignals = new ArrayList<>();
    private final ValueSignal<List<ValueSignal<MessageListItem>>> listSignal;

    public MessageListSignalPage() {
        itemSignals.add(new ValueSignal<>(
                new MessageListItem("Hello, world!", "Alice")));
        itemSignals.add(
                new ValueSignal<>(new MessageListItem("How are you?", "Bob")));
        itemSignals.add(new ValueSignal<>(
                new MessageListItem("Fine, thanks!", "Charlie")));
        listSignal = new ValueSignal<>(List.copyOf(itemSignals));

        var messageList = new MessageList(listSignal);

        var addButton = new NativeButton("Add message", e -> {
            var name = "User " + (itemSignals.size() + 1);
            itemSignals.add(new ValueSignal<>(
                    new MessageListItem("Message from " + name, name)));
            listSignal.set(List.copyOf(itemSignals));
        });
        addButton.setId("addMessage");

        var removeLastButton = new NativeButton("Remove last", e -> {
            if (!itemSignals.isEmpty()) {
                itemSignals.removeLast();
                listSignal.set(List.copyOf(itemSignals));
            }
        });
        removeLastButton.setId("removeLast");

        var updateFirstButton = new NativeButton("Update first message", e -> {
            if (!itemSignals.isEmpty()) {
                var current = itemSignals.getFirst().peek();
                itemSignals.getFirst()
                        .set(new MessageListItem(
                                current.getText() + " (edited)",
                                current.getUserName()));
            }
        });
        updateFirstButton.setId("updateFirst");

        add(messageList,
                new Div(addButton, removeLastButton, updateFirstButton));
    }
}
