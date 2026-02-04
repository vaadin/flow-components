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

import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for AiOrchestrator.
 */
@TestPath("vaadin-ai/orchestrator")
public class AiOrchestratorIT extends AbstractComponentIT {

    private MessageListElement messageList;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).waitForFirst();
    }

    @Test
    public void promptButton_sendsMessage_responseIsDisplayed() {
        clickElementWithJs("prompt-button");
        waitUntil(driver -> getMessageCount() >= 2, 5);
        Assert.assertTrue("Should have at least 2 messages (user + assistant)",
                getMessageCount() >= 2);
    }

    private int getMessageCount() {
        return messageList.getMessageElements().size();
    }
}
