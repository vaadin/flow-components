/*
 * Copyright 2000-2022 Vaadin Ltd.
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
 *
 */
package com.vaadin.flow.component.messages.tests;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;

public class MessageInputTest {

    private MessageInput messageInput;
    private MessageInputI18n i18n;

    @Before
    public void setup() {
        messageInput = new MessageInput();
        i18n = new MessageInputI18n();
    }

    @Test
    public void getI18n_returnsNull() {
        Assert.assertNull(messageInput.getI18n());
    }

    @Test
    public void setI18n_getI18n() {
        messageInput.setI18n(i18n);
        Assert.assertSame(i18n, messageInput.getI18n());
    }

    @Test(expected = NullPointerException.class)
    public void setI18n_null_throws() {
        messageInput.setI18n(null);
    }

    @Test
    public void i18nPropertySetters_returnI18n() {
        Assert.assertSame(i18n, i18n.setMessage("foo"));
        Assert.assertSame(i18n, i18n.setSend("bar"));
    }

    @Test
    public void constructWithSubmitListener_fireEvent_listenerCalled() {
        AtomicReference<MessageInput.SubmitEvent> eventRef = new AtomicReference<>();
        MessageInput messageInput = new MessageInput(eventRef::set);
        MessageInput.SubmitEvent event = new MessageInput.SubmitEvent(
                messageInput, false, "foo");
        ComponentUtil.fireEvent(messageInput, event);
        Assert.assertSame(event, eventRef.get());
    }
}
