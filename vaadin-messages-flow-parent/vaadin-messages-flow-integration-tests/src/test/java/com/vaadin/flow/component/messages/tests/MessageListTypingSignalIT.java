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

import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-messages/message-list-typing-signal-test")
public class MessageListTypingSignalIT extends AbstractComponentIT {

    private MessageListElement messageList;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).first();
    }

    @Test
    public void signalSet_typingIndicatorRendered() {
        clickElementWithJs("showTyping");

        Assert.assertEquals("Unexpected typing users", List.of("Alice"),
                messageList.getTypingUserNames());
    }

    @Test
    public void userSignalChanged_typingUserRenamed() {
        clickElementWithJs("showTyping");

        clickElementWithJs("renameTypingUser");

        Assert.assertEquals("Unexpected typing users", List.of("Alice Smith"),
                messageList.getTypingUserNames());
    }

    @Test
    public void signalCleared_typingIndicatorRemoved() {
        clickElementWithJs("showTyping");

        clickElementWithJs("hideTyping");

        Assert.assertTrue("Expected no typing indicator",
                messageList.getTypingUserNames().isEmpty());
    }
}
