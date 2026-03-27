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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

import tools.jackson.databind.node.ArrayNode;

class MessageListSignalTest extends AbstractSignalsTest {

    private MessageList messageList;

    @BeforeEach
    void setup() {
        messageList = new MessageList();
    }

    @Test
    void signalConstructor_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        messageList = new MessageList(listSignal);
        ui.add(messageList);

        Assertions.assertEquals(2, messageList.getItems().size());
        Assertions.assertEquals("Hello",
                messageList.getItems().get(0).getText());
        Assertions.assertEquals("World",
                messageList.getItems().get(1).getText());

        assertFullUpdate();
    }

    @Test
    void signalConstructor_updatesWhenSignalChanges() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList = new MessageList(listSignal);
        ui.add(messageList);
        assertFullUpdate();

        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        listSignal.set(List.of(item1Signal, item2Signal));

        Assertions.assertEquals(2, messageList.getItems().size());
        assertFullUpdate();
    }

    @Test
    void signalConstructor_setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList = new MessageList(listSignal);
        ui.add(messageList);

        Assertions.assertThrows(BindingActiveException.class, () -> messageList
                .setItems(List.of(new MessageListItem("World"))));
    }

    @Test
    void bindItems_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assertions.assertEquals(2, messageList.getItems().size());
        Assertions.assertEquals("Hello",
                messageList.getItems().get(0).getText());
        Assertions.assertEquals("World",
                messageList.getItems().get(1).getText());
        assertFullUpdate();
    }

    @Test
    void bindItems_updatesWhenListSignalChanges() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assertions.assertEquals(1, messageList.getItems().size());
        assertFullUpdate();

        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        var item3Signal = new ValueSignal<>(new MessageListItem("Foo"));
        listSignal.set(List.of(item1Signal, item2Signal, item3Signal));

        Assertions.assertEquals(3, messageList.getItems().size());
        Assertions.assertEquals("Foo", messageList.getItems().get(2).getText());
        assertFullUpdate();
    }

    @Test
    void bindItems_updatesWhenItemSignalChanges() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assertions.assertEquals("Hello",
                messageList.getItems().get(0).getText());
        assertFullUpdate();

        item1Signal.set(new MessageListItem("Updated Hello"));

        Assertions.assertEquals("Updated Hello",
                messageList.getItems().get(0).getText());
        assertFullUpdate();
    }

    @Test
    void setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assertions.assertThrows(BindingActiveException.class, () -> messageList
                .setItems(List.of(new MessageListItem("World"))));
    }

    @Test
    void addItemWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assertions.assertThrows(BindingActiveException.class,
                () -> messageList.addItem(new MessageListItem("World")));
    }

    @Test
    void bindItems_calledTwice_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> messageList.bindItems(listSignal));
    }

    @Test
    void bindItems_nullSignal_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageList.bindItems(null));
    }

    /**
     * Asserts that the only pending JavaScript invocation is a full update
     * (setItems) and that the parameters of the invocation match the items in
     * the message list.
     */
    private void assertFullUpdate() {
        var pendingInvocations = ui.dumpPendingJavaScriptInvocations();
        // Expect only one pending invocation
        Assertions.assertEquals(1, pendingInvocations.size());

        var invocation = pendingInvocations.getFirst();
        // Expect the only invocation to be setItems
        Assertions.assertTrue(invocation.getInvocation().getExpression()
                .contains("setItems"));

        // Expect the parameters to equal the items in the message list
        var parameterItems = (ArrayNode) invocation.getInvocation()
                .getParameters().getFirst();
        var expectedItems = JacksonUtils.listToJson(messageList.getItems());
        Assertions.assertTrue(
                JacksonUtils.jsonEquals(expectedItems, parameterItems));
    }
}
