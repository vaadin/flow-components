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
 *
 */
package com.vaadin.flow.component.messages.tests;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;

public class MessageListTest {

    private MessageList messageList;
    private MessageListItem item1;
    private MessageListItem item2;

    @Before
    public void setup() {
        messageList = new MessageList();
        item1 = new MessageListItem();
        item2 = new MessageListItem();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getItems_returnsUnmodifiableList() {
        messageList.getItems().add(new MessageListItem());
    }

    @Test
    public void setItemsCollection_getItems() {
        messageList.setItems(Arrays.asList(item1, item2));
        Assert.assertEquals(Arrays.asList(item1, item2),
                messageList.getItems());
    }

    @Test
    public void setItemsVarArgs_getItems() {
        messageList.setItems(item1, item2);
        Assert.assertEquals(Arrays.asList(item1, item2),
                messageList.getItems());
    }

    @Test(expected = NullPointerException.class)
    public void setItems_nullCollection_throws() {
        messageList.setItems((Collection<MessageListItem>) null);
    }

    @Test(expected = NullPointerException.class)
    public void setItems_containsNullItem_throws() {
        messageList.setItems(item1, null, item2);
    }
}
