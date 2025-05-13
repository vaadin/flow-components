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
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.VaadinSession;

import elemental.json.impl.JreJsonArray;

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

    @Test
    public void setItems_appendText() {
        messageList.setItems(Arrays.asList(item1, item2));
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        item1.appendText("foo");
        Assert.assertEquals("foo", item1.getText());
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void setItems_setText_appendText() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        item1.appendText("bar");
        Assert.assertEquals("foobar", item1.getText());
    }

    @Test
    public void setItems_setText_appendTextNull() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        item1.appendText(null);
        Assert.assertEquals(0, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void setItems_setText_appendTextEmpty() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        item1.appendText("");
        Assert.assertEquals(0, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void addItem() {
        messageList.addItem(item1);
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());
        Assert.assertEquals(item1, messageList.getItems().get(0));
        Assert.assertEquals(1, messageList.getItems().size());
    }

    @Test(expected = NullPointerException.class)
    public void addItemNull_throws() {
        messageList.addItem(null);
    }

    @Test
    public void addItem_updateText() {
        messageList.addItem(item1);
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        item1.setText("foo");
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void setItems_addItem() {
        messageList.setItems(Arrays.asList(item1));
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        messageList.addItem(item2);
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void setTextAfteraAddItem_noExtraJSInvocations() {
        messageList.setItems(Arrays.asList(item1));
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        messageList.addItem(item2);
        item2.setText("bar");

        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());
    }

    @Test
    public void addItem_setItems_expectFullUpdate() {
        messageList.addItem(item1);
        messageList.setItems(
                Arrays.asList(new MessageListItem("Foo", null, "User")));
        assertFullUpdate();
    }

    @Test
    public void setItems_addItem_expectFullUpdate() {
        messageList.setItems(
                Arrays.asList(new MessageListItem("Foo", null, "User")));
        messageList.addItem(item1);
        assertFullUpdate();
    }

    @Test
    public void setText_setItems_expectFullUpdate() {
        messageList.setItems(Arrays.asList(item1));
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        item1.setText("foobar");
        messageList.setItems(item1, item2);
        assertFullUpdate();
    }

    @Test
    public void setItems_setText_expectFullUpdate() {
        messageList.setItems(Arrays.asList(item1));
        Assert.assertEquals(1, getPendingJavaScriptInvocations().size());

        messageList.setItems(item1, item2);
        item1.setText("foobar");
        assertFullUpdate();
    }

    /**
     * Asserts that the only pending JavaScript invocation is a full update
     * (setItems) and that the parameters of the invocation match the items in
     * the message list.
     */
    private void assertFullUpdate() {
        var pendingInvocations = getPendingJavaScriptInvocations();
        // Expect only one pending invocation
        Assert.assertEquals(1, pendingInvocations.size());

        var invocation = pendingInvocations.get(0);
        // Expect the only invocation to be setItems
        Assert.assertTrue(invocation.getInvocation().getExpression()
                .contains("setItems"));

        // Expect the parameters to equal the items in the message list
        var parameterItems = (JreJsonArray) invocation.getInvocation()
                .getParameters().get(0);
        var expectedItems = JsonUtils.listToJson(messageList.getItems());
        Assert.assertTrue(JsonUtils.jsonEquals(expectedItems, parameterItems));
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
