/*
 * Copyright 2000-2021 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;

public class MessageInputTest {

    @Test
    public void constructWithSubmitListener_fireEvent_listenerCalled() {
        AtomicReference<SubmitEvent> eventRef = new AtomicReference<>();
        MessageInput messageInput = new MessageInput(eventRef::set);
        SubmitEvent event = new SubmitEvent(messageInput, false, "foo");
        ComponentUtil.fireEvent(messageInput, event);
        Assert.assertSame(event, eventRef.get());
    }
}
