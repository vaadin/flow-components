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
package com.vaadin.flow.component.ai.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.testbench.MessageInputElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the typing indicator shown by AIOrchestrator.
 */
@TestPath("vaadin-ai/orchestrator-typing-indicator")
public class AIOrchestratorTypingIndicatorIT extends AbstractComponentIT {

    private MessageListElement messageList;
    private MessageInputElement messageInput;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).single();
        messageInput = $(MessageInputElement.class).single();
    }

    @Test
    public void submitMessage_typingIndicatorShownUntilResponseArrives() {
        messageInput.submit("Hello");

        waitUntil(driver -> !messageList.getTypingUserNames().isEmpty(), 2);
        Assert.assertEquals("Unexpected typing users", List.of("Assistant"),
                messageList.getTypingUserNames());
        Assert.assertEquals("Expected no assistant message yet", 1,
                messageList.getMessageElements().size());

        waitUntil(driver -> messageList.getTypingUserNames().isEmpty(), 5);
        Assert.assertEquals(2, messageList.getMessageElements().size());
        Assert.assertTrue(messageList.getMessageElements().get(1).getText()
                .contains("Echo: Hello"));
    }
}
