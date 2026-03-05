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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class MessageListSignalTest extends AbstractSignalsUnitTest {

    private MessageList messageList;

    @Before
    public void setup() {
        messageList = new MessageList();
    }

    @Test
    public void signalConstructor_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        messageList = new MessageList(listSignal);
        ui.add(messageList);

        Assert.assertEquals(2, messageList.getItems().size());
        Assert.assertEquals("Hello", messageList.getItems().get(0).getText());
        Assert.assertEquals("World", messageList.getItems().get(1).getText());
    }

    @Test
    public void signalConstructor_updatesWhenSignalChanges() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList = new MessageList(listSignal);
        ui.add(messageList);

        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        listSignal.set(List.of(item1Signal, item2Signal));

        Assert.assertEquals(2, messageList.getItems().size());
    }

    @Test(expected = BindingActiveException.class)
    public void signalConstructor_setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList = new MessageList(listSignal);
        ui.add(messageList);

        messageList.setItems(List.of(new MessageListItem("World")));
    }

    @Test
    public void bindItems_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assert.assertEquals(2, messageList.getItems().size());
        Assert.assertEquals("Hello", messageList.getItems().get(0).getText());
        Assert.assertEquals("World", messageList.getItems().get(1).getText());
    }

    @Test
    public void bindItems_updatesWhenListSignalChanges() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assert.assertEquals(1, messageList.getItems().size());

        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        var item3Signal = new ValueSignal<>(new MessageListItem("Foo"));
        listSignal.set(List.of(item1Signal, item2Signal, item3Signal));

        Assert.assertEquals(3, messageList.getItems().size());
        Assert.assertEquals("Foo", messageList.getItems().get(2).getText());
    }

    @Test
    public void bindItems_updatesWhenItemSignalChanges() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        Assert.assertEquals("Hello", messageList.getItems().get(0).getText());

        item1Signal.set(new MessageListItem("Updated Hello"));

        Assert.assertEquals("Updated Hello",
                messageList.getItems().get(0).getText());
    }

    @Test
    public void bindItems_notAttached_bindingInactiveUntilAttach() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var item2Signal = new ValueSignal<>(new MessageListItem("World"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        messageList.bindItems(listSignal);

        Assert.assertEquals(0, messageList.getItems().size());

        ui.add(messageList);

        Assert.assertEquals(2, messageList.getItems().size());
    }

    @Test(expected = BindingActiveException.class)
    public void setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        messageList.setItems(List.of(new MessageListItem("World")));
    }

    @Test(expected = BindingActiveException.class)
    public void addItemWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        ui.add(messageList);

        messageList.addItem(new MessageListItem("World"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_calledTwice_throws() {
        var item1Signal = new ValueSignal<>(new MessageListItem("Hello"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        messageList.bindItems(listSignal);
        messageList.bindItems(listSignal);
    }

    @Test(expected = NullPointerException.class)
    public void bindItems_nullSignal_throws() {
        messageList.bindItems(null);
    }
}
