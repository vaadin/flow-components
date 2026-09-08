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
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListTypingIndicatorExperimentalFeatureException;
import com.vaadin.flow.component.messages.MessageListTypingIndicatorFeatureFlagProvider;
import com.vaadin.flow.component.messages.MessageListUser;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;
import com.vaadin.tests.EnableFeatureFlagExtension;

class MessageListTypingUsersSignalTest extends AbstractSignalsTest {

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            MessageListTypingIndicatorFeatureFlagProvider.TYPING_INDICATOR);

    private MessageList messageList;

    @BeforeEach
    void setup() {
        messageList = new MessageList();
    }

    @Test
    void bindTypingUsers_setsUsersFromSignal() {
        var aliceSignal = new ValueSignal<>(new MessageListUser("Alice"));
        var bobSignal = new ValueSignal<>(new MessageListUser("Bob"));
        var listSignal = new ValueSignal<>(List.of(aliceSignal, bobSignal));

        messageList.bindTypingUsers(listSignal);
        ui.add(messageList);

        assertTypingUserNames("Alice", "Bob");
    }

    @Test
    void bindTypingUsers_updatesWhenListSignalChanges() {
        var aliceSignal = new ValueSignal<>(new MessageListUser("Alice"));
        var listSignal = new ValueSignal<>(List.of(aliceSignal));

        messageList.bindTypingUsers(listSignal);
        ui.add(messageList);
        assertTypingUserNames("Alice");

        listSignal.set(List.of());

        assertTypingUserNames();
    }

    @Test
    void bindTypingUsers_updatesWhenUserSignalChanges() {
        var aliceSignal = new ValueSignal<>(new MessageListUser("Alice"));
        var listSignal = new ValueSignal<>(List.of(aliceSignal));

        messageList.bindTypingUsers(listSignal);
        ui.add(messageList);
        assertTypingUserNames("Alice");

        aliceSignal.set(new MessageListUser("Alice Smith"));

        assertTypingUserNames("Alice Smith");
    }

    @Test
    void setTypingUsersWhileBound_throws() {
        var aliceSignal = new ValueSignal<>(new MessageListUser("Alice"));
        var listSignal = new ValueSignal<>(List.of(aliceSignal));

        messageList.bindTypingUsers(listSignal);
        ui.add(messageList);

        Assertions.assertThrows(BindingActiveException.class,
                () -> messageList.setTypingUsers(new MessageListUser("Bob")));
    }

    @Test
    void bindTypingUsers_calledTwice_throws() {
        var aliceSignal = new ValueSignal<>(new MessageListUser("Alice"));
        var listSignal = new ValueSignal<>(List.of(aliceSignal));

        messageList.bindTypingUsers(listSignal);

        Assertions.assertThrows(BindingActiveException.class,
                () -> messageList.bindTypingUsers(listSignal));
    }

    @Test
    void bindTypingUsers_nullSignal_throws() {
        var exception = Assertions.assertThrows(NullPointerException.class,
                () -> messageList.bindTypingUsers(null));

        Assertions.assertTrue(
                exception.getMessage().contains("Signal cannot be null"));
    }

    @Test
    void featureDisabled_bindTypingUsers_throws() {
        featureFlagExtension.disableFeature();
        var aliceSignal = new ValueSignal<>(new MessageListUser("Alice"));
        var listSignal = new ValueSignal<>(List.of(aliceSignal));

        messageList.bindTypingUsers(listSignal);

        Assertions.assertThrows(
                MessageListTypingIndicatorExperimentalFeatureException.class,
                () -> ui.add(messageList));
    }

    private void assertTypingUserNames(String... expectedNames) {
        Assertions.assertEquals(List.of(expectedNames),
                messageList.getTypingUsers().stream()
                        .map(MessageListUser::getName).toList());

        var expectedJson = JacksonUtils.createArrayNode();
        messageList.getTypingUsers().stream().map(JacksonUtils::beanToJson)
                .forEach(expectedJson::add);
        Assertions.assertEquals(expectedJson,
                messageList.getElement().getPropertyRaw("_usersTyping"));
    }
}
