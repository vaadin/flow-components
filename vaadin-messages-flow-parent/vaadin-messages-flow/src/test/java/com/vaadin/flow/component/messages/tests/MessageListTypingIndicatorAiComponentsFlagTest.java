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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.experimental.Feature;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListUser;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class MessageListTypingIndicatorAiComponentsFlagTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    /**
     * The umbrella AI components flag enables the typing indicator as well, so
     * that AI chats do not need to enable two flags.
     */
    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            new Feature("AI Components", "aiComponents", null, false, null));

    @Test
    void aiComponentsFeatureEnabled_setTypingUsers_doesNotThrow() {
        var messageList = new MessageList();
        ui.add(messageList);

        Assertions.assertDoesNotThrow(
                () -> messageList.setTypingUsers(new MessageListUser("Alice")));
    }
}
