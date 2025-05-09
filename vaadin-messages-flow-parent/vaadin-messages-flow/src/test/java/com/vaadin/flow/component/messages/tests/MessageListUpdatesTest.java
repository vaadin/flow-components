/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.server.VaadinSession;

public class MessageListUpdatesTest {

    private UI ui;
    private MessageList messageList;
    private MessageListItem item1;
    private MessageListItem item2;

    @Before
    public void setup() {
        messageList = new MessageList();
        item1 = new MessageListItem();
        item2 = new MessageListItem();

        ui = new UI();
        var mockSession = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(mockSession);
        ui.add(messageList);
    }

    @Test
    public void setSameTextAfterSetItems_noJSInvocations() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        item1.setText("foo");
        Assert.assertEquals(0, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void setText_setNullText_doesNotThrow() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        item1.setText(null);
        fakeClientCommunication();
    }

    private List<PendingJavaScriptInvocation> getPendingJavaScriptInvocations() {
        fakeClientCommunication();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

}
