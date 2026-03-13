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

import com.vaadin.flow.component.messages.testbench.MessageElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-messages/message-list-signal-test")
public class MessageListSignalIT extends AbstractComponentIT {

    private MessageListElement messageList;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).first();
    }

    @Test
    public void initialItems_messagesRendered() {
        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals(3, messages.size());

        Assert.assertEquals("Hello, world!", messages.get(0).getText());
        Assert.assertEquals("Alice", messages.get(0).getUserName());

        Assert.assertEquals("How are you?", messages.get(1).getText());
        Assert.assertEquals("Bob", messages.get(1).getUserName());

        Assert.assertEquals("Fine, thanks!", messages.get(2).getText());
        Assert.assertEquals("Charlie", messages.get(2).getUserName());
    }

    @Test
    public void addMessage_messageAppended() {
        clickElementWithJs("addMessage");

        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals(4, messages.size());
        Assert.assertEquals("Message from User 4", messages.get(3).getText());
        Assert.assertEquals("User 4", messages.get(3).getUserName());
    }

    @Test
    public void removeLast_messageRemoved() {
        clickElementWithJs("removeLast");

        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals(2, messages.size());
        Assert.assertEquals("Hello, world!", messages.get(0).getText());
        Assert.assertEquals("How are you?", messages.get(1).getText());
    }

    @Test
    public void updateFirst_messageTextUpdated() {
        clickElementWithJs("updateFirst");

        List<MessageElement> messages = messageList.getMessageElements();
        Assert.assertEquals(3, messages.size());
        Assert.assertEquals("Hello, world! (edited)",
                messages.get(0).getText());
        Assert.assertEquals("Alice", messages.get(0).getUserName());
    }

    @Test
    public void addThenRemove_messagesUpdated() {
        clickElementWithJs("addMessage");
        Assert.assertEquals(4, messageList.getMessageElements().size());

        clickElementWithJs("removeLast");
        Assert.assertEquals(3, messageList.getMessageElements().size());
    }
}
