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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.MockUIRule;

import tools.jackson.databind.node.ArrayNode;

public class MessageListUpdatesTest {
    @Rule
    public MockUIRule ui = new MockUIRule();

    private MessageList messageList;
    private MessageListItem item1;
    private MessageListItem item2;

    @Before
    public void setup() {
        messageList = new MessageList();
        item1 = new MessageListItem();
        item2 = new MessageListItem();
        ui.add(messageList);
        ui.dumpPendingJavaScriptInvocations();
    }

    @Test
    public void setSameTextAfterSetItems_noJSInvocations() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        assertFullUpdate();

        item1.setText("foo");
        assertNoUpdate();
    }

    @Test
    public void setText_setNullText_doesNotThrow() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        assertFullUpdate();

        item1.setText(null);
        ui.fakeClientCommunication();
    }

    @Test
    public void setItems_appendText() {
        messageList.setItems(Arrays.asList(item1, item2));
        assertFullUpdate();

        item1.appendText("foo");
        Assert.assertEquals("foo", item1.getText());
        assertSetItemTextUpdate(item1, "foo");
    }

    @Test
    public void setItems_setText_appendText() {
        messageList.setItems(Arrays.asList(item1, item2));
        assertFullUpdate();

        item1.setText("foo");
        assertSetItemTextUpdate(item1, "foo");

        item1.appendText("bar");
        assertAppendItemTextUpdate(item1, "bar");

        Assert.assertEquals("foobar", item1.getText());
    }

    @Test
    public void setItems_setText_appendTextNull() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        assertFullUpdate();

        item1.appendText(null);
        assertNoUpdate();
    }

    @Test
    public void setItems_setText_appendTextEmpty() {
        messageList.setItems(Arrays.asList(item1, item2));
        item1.setText("foo");
        assertFullUpdate();

        item1.appendText("");
        assertNoUpdate();
    }

    @Test
    public void addItem() {
        messageList.addItem(item1);
        assertAddItemUpdate(item1);
        Assert.assertEquals(item1, messageList.getItems().getFirst());
        Assert.assertEquals(1, messageList.getItems().size());
    }

    @Test(expected = NullPointerException.class)
    public void addItemNull_throws() {
        messageList.addItem(null);
    }

    @Test
    public void addItem_updateText() {
        messageList.addItem(item1);
        assertAddItemUpdate(item1);

        item1.setText("foo");
        assertSetItemTextUpdate(item1, "foo");
    }

    @Test
    public void setItems_addItem() {
        messageList.setItems(Collections.singletonList(item1));
        assertFullUpdate();

        messageList.addItem(item2);
        assertAddItemUpdate(item2);
    }

    @Test
    public void setTextAfterAddItem_expectAddItemUpdate() {
        messageList.setItems(Collections.singletonList(item1));
        assertFullUpdate();

        messageList.addItem(item2);
        item2.setText("bar");

        assertAddItemUpdate(item2);
    }

    @Test
    public void addItem_setItems_expectFullUpdate() {
        messageList.addItem(item1);
        messageList.setItems(List.of(new MessageListItem("Foo", null, "User")));
        assertFullUpdate();
    }

    @Test
    public void addItem_addItem_expectAddItemUpdate() {
        messageList.addItem(item1);
        messageList.addItem(item2);
        assertAddItemUpdate(item1, item2);
    }

    @Test
    public void setItems_addItem_expectFullUpdate() {
        messageList.setItems(List.of(new MessageListItem("Foo", null, "User")));
        messageList.addItem(item1);
        assertFullUpdate();
    }

    @Test
    public void setText_setItems_expectFullUpdate() {
        messageList.setItems(Collections.singletonList(item1));
        assertFullUpdate();

        item1.setText("foobar");
        messageList.setItems(item1, item2);
        assertFullUpdate();
    }

    @Test
    public void setItems_setText_expectFullUpdate() {
        messageList.setItems(Collections.singletonList(item1));
        assertFullUpdate();

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
        var pendingInvocations = ui.dumpPendingJavaScriptInvocations();
        // Expect only one pending invocation
        Assert.assertEquals(1, pendingInvocations.size());

        var invocation = pendingInvocations.getFirst();
        // Expect the only invocation to be setItems
        Assert.assertTrue(invocation.getInvocation().getExpression()
                .contains("setItems"));

        // Expect the parameters to equal the items in the message list
        var parameterItems = (ArrayNode) invocation.getInvocation()
                .getParameters().getFirst();
        var expectedItems = JacksonUtils.listToJson(messageList.getItems());
        Assert.assertTrue(
                JacksonUtils.jsonEquals(expectedItems, parameterItems));
    }

    private void assertAddItemUpdate(MessageListItem... items) {
        var pendingInvocations = ui.dumpPendingJavaScriptInvocations();
        // Expect only one pending invocation
        Assert.assertEquals(1, pendingInvocations.size());

        var invocation = pendingInvocations.getFirst();
        // Expect the only invocation to be addItems
        Assert.assertTrue(invocation.getInvocation().getExpression()
                .contains("addItems"));

        // Expect the parameters to equal the provided items as JSON
        var parameterItems = (ArrayNode) invocation.getInvocation()
                .getParameters().getFirst();
        var expectedItems = JacksonUtils.listToJson(Arrays.asList(items));
        Assert.assertTrue(
                JacksonUtils.jsonEquals(expectedItems, parameterItems));
    }

    private void assertSetItemTextUpdate(MessageListItem item, String newText) {
        var pendingInvocations = ui.dumpPendingJavaScriptInvocations();
        // Expect only one pending invocation
        Assert.assertEquals(1, pendingInvocations.size());

        var invocation = pendingInvocations.getFirst();
        // Expect the only invocation to be setItemText
        Assert.assertTrue(invocation.getInvocation().getExpression()
                .contains("setItemText"));

        // Expect the parameters to match the text and item index
        var parameters = invocation.getInvocation().getParameters();
        Assert.assertEquals(newText, parameters.get(0));
        var expectedIndex = messageList.getItems().indexOf(item);
        Assert.assertEquals(expectedIndex,
                ((Number) parameters.get(1)).intValue());
    }

    private void assertAppendItemTextUpdate(MessageListItem item,
            String appendedText) {
        var pendingInvocations = ui.dumpPendingJavaScriptInvocations();
        // Expect only one pending invocation
        Assert.assertEquals(1, pendingInvocations.size());

        var invocation = pendingInvocations.getFirst();
        // Expect the only invocation to be appendItemText
        Assert.assertTrue(invocation.getInvocation().getExpression()
                .contains("appendItemText"));

        // Expect the parameters to match the text and item index
        var parameters = invocation.getInvocation().getParameters();
        Assert.assertEquals(appendedText, parameters.get(0));
        var expectedIndex = messageList.getItems().indexOf(item);
        Assert.assertEquals(expectedIndex,
                ((Number) parameters.get(1)).intValue());
    }

    private void assertNoUpdate() {
        var pendingInvocations = ui.dumpPendingJavaScriptInvocations();
        Assert.assertEquals(0, pendingInvocations.size());
    }
}
