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
import com.vaadin.tests.AbstractComponentIT;

/**
 * Tests that the LangChain4j features configured through the provider builder
 * take part in the turns of a chat.
 */
@TestPath("vaadin-ai/pipeline/langchain4j")
public class LangChain4JPipelineIT extends AbstractComponentIT {

    private MessageListElement messageList;
    private MessageInputElement messageInput;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).single();
        messageInput = $(MessageInputElement.class).single();
    }

    @Test
    public void contentRetriever_retrievedFactReachesTheModel() {
        var answer = ask("What is the codename?");

        Assert.assertTrue(answer, answer.contains("What is the codename?"));
        Assert.assertTrue(answer, answer.contains("Bluefin"));
    }

    @Test
    public void toolProvider_toolIsCalled() {
        Assert.assertEquals("Tool said: Sunny", ask("weather"));
    }

    @Test
    public void controller_toolIsCalledNextToTheProviderTools() {
        Assert.assertEquals("Tool said: Shipped", ask("order status"));
    }

    @Test
    public void inputGuardrail_blockedWordEndsTheTurnWithAnError() {
        var answer = ask("What is my password?");

        Assert.assertTrue(answer, answer.contains("An error occurred"));
    }

    private String ask(String question) {
        messageInput.submit(question);
        waitUntil(driver -> messageList.getMessageElements().size() >= 2, 5);
        return messageList.getMessageElements().get(1).getText();
    }
}
