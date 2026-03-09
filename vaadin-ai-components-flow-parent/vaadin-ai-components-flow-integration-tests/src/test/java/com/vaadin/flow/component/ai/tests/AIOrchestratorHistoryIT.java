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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.testbench.MessageInputElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for AIOrchestrator chat history.
 */
@TestPath("vaadin-ai/orchestrator-history")
public class AIOrchestratorHistoryIT extends AbstractComponentIT {

    private MessageListElement messageList;
    private MessageInputElement messageInput;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).single();
        messageInput = $(MessageInputElement.class).single();
    }

    @Test
    public void sendMessage_getHistory_containsUserAndAssistantMessages() {
        var message = "Hello";
        submitMessage(message);

        clickElementWithJs("get-history");
        var historyInfo = getHistoryInfo();
        Assert.assertEquals(2, getHistorySize(historyInfo));
        Assert.assertTrue(containsUserMessage(historyInfo, message));
        Assert.assertTrue(containsAssistantMessage(historyInfo, message));
    }

    @Test
    public void restoreHistory_messagesDisplayed() {
        restoreHistory();

        var messages = messageList.getMessageElements();
        Assert.assertEquals(2, messages.size());
        Assert.assertTrue(
                messages.get(0).getText().contains("Previous question"));
        Assert.assertTrue(
                messages.get(1).getText().contains("Previous answer"));
    }

    @Test
    public void restoreHistory_sendMessage_historyContainsAll() {
        restoreHistory();

        var newMessage = "Follow-up";
        submitMessage(newMessage);

        clickElementWithJs("get-history");
        var historyInfo = getHistoryInfo();
        Assert.assertEquals(4, getHistorySize(historyInfo));
        Assert.assertTrue(
                containsUserMessage(historyInfo, "Previous question"));
        Assert.assertTrue(containsUserMessage(historyInfo, newMessage));
    }

    private void restoreHistory() {
        clickElementWithJs("restore-history");
        // Re-query components since the page creates new instances
        messageList = $(MessageListElement.class).single();
        messageInput = $(MessageInputElement.class).single();
        waitUntilMessagesDisplayed(2);
    }

    private void waitUntilMessagesDisplayed(int expectedMessageCount) {
        waitUntil(driver -> getMessageCount() == expectedMessageCount, 2);
    }

    private boolean containsAssistantMessage(TestBenchElement historyInfo,
            String assistantMessage) {
        return historyInfo.getText()
                .contains("ASSISTANT:Echo: " + assistantMessage);
    }

    private boolean containsUserMessage(TestBenchElement historyInfo,
            String userMessage) {
        return historyInfo.getText().contains("USER:" + userMessage);
    }

    private void submitMessage(String message) {
        var initialMessageCount = getMessageCount();
        messageInput.submit(message);
        waitUntilMessagesDisplayed(initialMessageCount + 2);
    }

    private int getHistorySize(TestBenchElement historyInfo) {
        return Integer.parseInt(
                historyInfo.getText().split("\\|")[0].replace("size=", ""));
    }

    private TestBenchElement getHistoryInfo() {
        var historyInfo = $("span").id("history-info");
        waitUntil(driver -> historyInfo.getText().startsWith("size="), 2);
        return historyInfo;
    }

    private int getMessageCount() {
        return messageList.getMessageElements().size();
    }
}
