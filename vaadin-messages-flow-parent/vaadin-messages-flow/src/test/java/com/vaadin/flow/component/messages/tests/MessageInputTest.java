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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;

class MessageInputTest {

    private MessageInput messageInput;
    private MessageInputI18n i18n;

    @BeforeEach
    void setup() {
        messageInput = new MessageInput();
        i18n = new MessageInputI18n();
    }

    @Test
    void getI18n_returnsNull() {
        Assertions.assertNull(messageInput.getI18n());
    }

    @Test
    void setI18n_getI18n() {
        messageInput.setI18n(i18n);
        Assertions.assertSame(i18n, messageInput.getI18n());
    }

    @Test
    void setI18n_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageInput.setI18n(null));
    }

    @Test
    void i18nPropertySetters_returnI18n() {
        Assertions.assertSame(i18n, i18n.setMessage("foo"));
        Assertions.assertSame(i18n, i18n.setSend("bar"));
    }

    @Test
    void constructWithSubmitListener_fireEvent_listenerCalled() {
        AtomicReference<MessageInput.SubmitEvent> eventRef = new AtomicReference<>();
        MessageInput messageInput = new MessageInput(eventRef::set);
        MessageInput.SubmitEvent event = new MessageInput.SubmitEvent(
                messageInput, false, "foo");
        ComponentUtil.fireEvent(messageInput, event);
        Assertions.assertSame(event, eventRef.get());
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(messageInput instanceof HasTooltip);
    }

    @Test
    void implementsFocusable() {
        Assertions.assertTrue(
                Focusable.class.isAssignableFrom(messageInput.getClass()),
                "MessageInput should be focusable");
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(MessageInput.class));
    }
}
