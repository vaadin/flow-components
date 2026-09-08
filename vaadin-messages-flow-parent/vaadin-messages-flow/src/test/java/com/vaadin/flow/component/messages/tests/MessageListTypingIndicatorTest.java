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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListI18n;
import com.vaadin.flow.component.messages.MessageListTypingIndicatorExperimentalFeatureException;
import com.vaadin.flow.component.messages.MessageListTypingIndicatorFeatureFlagProvider;
import com.vaadin.flow.component.messages.MessageListTypingIndicatorType;
import com.vaadin.flow.component.messages.MessageListUser;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

class MessageListTypingIndicatorTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            MessageListTypingIndicatorFeatureFlagProvider.TYPING_INDICATOR);

    private MessageList messageList;

    @BeforeEach
    void setup() {
        messageList = new MessageList();
        ui.add(messageList);
    }

    @Test
    void setTypingUsers_getTypingUsers() {
        var alice = new MessageListUser("Alice");
        var bob = new MessageListUser("Bob");

        messageList.setTypingUsers(List.of(alice, bob));

        Assertions.assertEquals(List.of(alice, bob),
                messageList.getTypingUsers());
    }

    @Test
    void setTypingUsersVarArgs_getTypingUsers() {
        var alice = new MessageListUser("Alice");

        messageList.setTypingUsers(alice);

        Assertions.assertEquals(List.of(alice), messageList.getTypingUsers());
    }

    @Test
    void getTypingUsers_defaultIsEmpty() {
        Assertions.assertTrue(messageList.getTypingUsers().isEmpty());
    }

    @Test
    void getTypingUsers_returnsUnmodifiableList() {
        messageList.setTypingUsers(new MessageListUser("Alice"));

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> messageList.getTypingUsers()
                        .add(new MessageListUser("Bob")));
    }

    @Test
    void setTypingUsers_doesNotReflectLaterCollectionChanges() {
        var users = new ArrayList<MessageListUser>();
        users.add(new MessageListUser("Alice"));

        messageList.setTypingUsers(users);
        users.add(new MessageListUser("Bob"));

        Assertions.assertEquals(1, messageList.getTypingUsers().size());
    }

    @Test
    void setTypingUsers_nullCollection_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageList.setTypingUsers((List<MessageListUser>) null));
    }

    @Test
    void setTypingUsers_containsNullUser_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageList.setTypingUsers(
                        Arrays.asList(new MessageListUser("Alice"), null)));
    }

    @Test
    void setTypingUsers_serializesTypingUsersProperty() {
        var alice = new MessageListUser("Alice");
        alice.setAbbreviation("AL");
        alice.setColorIndex(2);

        messageList.setTypingUsers(alice);

        var expected = JacksonUtils.createArrayNode();
        expected.add(JacksonUtils.beanToJson(alice));
        Assertions.assertEquals(expected,
                messageList.getElement().getPropertyRaw("_usersTyping"));
    }

    @Test
    void setTypingUsers_emptyCollection_serializesEmptyArray() {
        messageList.setTypingUsers(new MessageListUser("Alice"));

        messageList.setTypingUsers();

        Assertions.assertEquals(JacksonUtils.createArrayNode(),
                messageList.getElement().getPropertyRaw("_usersTyping"));
    }

    @Test
    void getTypingIndicatorType_defaultIsDefault() {
        Assertions.assertEquals(MessageListTypingIndicatorType.DEFAULT,
                messageList.getTypingIndicatorType());
        Assertions.assertFalse(
                messageList.getElement().hasProperty("_typingIndicatorType"));
    }

    @Test
    void setTypingIndicatorType_getTypingIndicatorType() {
        messageList.setTypingIndicatorType(
                MessageListTypingIndicatorType.ELLIPSIS);

        Assertions.assertEquals(MessageListTypingIndicatorType.ELLIPSIS,
                messageList.getTypingIndicatorType());
    }

    @Test
    void setTypingIndicatorType_setsProperty() {
        messageList.setTypingIndicatorType(
                MessageListTypingIndicatorType.ELLIPSIS);
        Assertions.assertEquals("ellipsis",
                messageList.getElement().getProperty("_typingIndicatorType"));

        messageList
                .setTypingIndicatorType(MessageListTypingIndicatorType.MINIMAL);
        Assertions.assertEquals("minimal",
                messageList.getElement().getProperty("_typingIndicatorType"));
    }

    @Test
    void setTypingIndicatorType_default_removesProperty() {
        messageList
                .setTypingIndicatorType(MessageListTypingIndicatorType.MINIMAL);

        messageList
                .setTypingIndicatorType(MessageListTypingIndicatorType.DEFAULT);

        Assertions.assertFalse(
                messageList.getElement().hasProperty("_typingIndicatorType"));
        Assertions.assertEquals(MessageListTypingIndicatorType.DEFAULT,
                messageList.getTypingIndicatorType());
    }

    @Test
    void setTypingIndicatorType_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageList.setTypingIndicatorType(null));
    }

    @Test
    void setTypingIndicatorType_whileUsersTyping_usersUnchanged() {
        messageList.setTypingUsers(new MessageListUser("Alice"));

        messageList
                .setTypingIndicatorType(MessageListTypingIndicatorType.MINIMAL);

        Assertions.assertEquals(1, messageList.getTypingUsers().size());
    }

    @Test
    void getI18n_defaultIsNull() {
        Assertions.assertNull(messageList.getI18n());
    }

    @Test
    void setI18n_getI18n() {
        var i18n = new MessageListI18n();

        messageList.setI18n(i18n);

        Assertions.assertSame(i18n, messageList.getI18n());
    }

    @Test
    void setI18n_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageList.setI18n(null));
    }

    @Test
    void i18nSetters_returnI18n() {
        var i18n = new MessageListI18n();

        Assertions.assertSame(i18n, i18n.setTypingIndicatorText("foo"));
    }

    @Test
    void setI18n_typingIndicatorText_setsProperty() {
        messageList.setI18n(
                new MessageListI18n().setTypingIndicatorText("is thinking…"));

        Assertions.assertEquals("is thinking…",
                messageList.getElement().getProperty("_typingIndicatorText"));
    }

    @Test
    void setI18n_withoutTypingIndicatorText_removesProperty() {
        messageList.setI18n(
                new MessageListI18n().setTypingIndicatorText("is thinking…"));

        messageList.setI18n(new MessageListI18n());

        Assertions.assertFalse(
                messageList.getElement().hasProperty("_typingIndicatorText"));
    }

    @Test
    void setI18n_whileUsersTyping_propertyUpdated() {
        messageList.setTypingUsers(new MessageListUser("Alice"));

        messageList.setI18n(
                new MessageListI18n().setTypingIndicatorText("is thinking…"));

        Assertions.assertEquals("is thinking…",
                messageList.getElement().getProperty("_typingIndicatorText"));
        Assertions.assertEquals(1, messageList.getTypingUsers().size());
    }

    @Test
    void featureDisabled_setTypingUsers_throws() {
        featureFlagExtension.disableFeature();

        Assertions.assertThrows(
                MessageListTypingIndicatorExperimentalFeatureException.class,
                () -> messageList.setTypingUsers(new MessageListUser("Alice")));
    }

    @Test
    void featureDisabled_setTypingIndicatorType_throws() {
        featureFlagExtension.disableFeature();

        Assertions.assertThrows(
                MessageListTypingIndicatorExperimentalFeatureException.class,
                () -> messageList.setTypingIndicatorType(
                        MessageListTypingIndicatorType.ELLIPSIS));
    }

    @Test
    void featureDisabled_detachedList_throwsOnAttach() {
        featureFlagExtension.disableFeature();
        var detachedList = new MessageList();
        detachedList.setTypingUsers(new MessageListUser("Alice"));

        Assertions.assertThrows(
                MessageListTypingIndicatorExperimentalFeatureException.class,
                () -> ui.add(detachedList));
    }

}
