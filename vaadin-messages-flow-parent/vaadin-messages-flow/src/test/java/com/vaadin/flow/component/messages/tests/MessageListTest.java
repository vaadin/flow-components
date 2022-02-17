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

import static org.hamcrest.CoreMatchers.startsWith;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.server.StreamResource;

import elemental.json.JsonType;
import elemental.json.JsonValue;

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

    @Test
    public void setImageAsStreamResource_overridesImageUrl() {
        UI.setCurrent(new UI());
        item1.setUserImage("foo/bar");
        item1.setUserImageResource(new StreamResource("message-list-img",
                () -> getClass().getResourceAsStream("baz/qux")));
        MatcherAssert.assertThat(item1.getUserImage(),
                startsWith("VAADIN/dynamic"));
    }

    @Test
    public void setImageAsUrl_streamResourceBecomesNull() {
        UI.setCurrent(new UI());
        item1.setUserImageResource(new StreamResource("message-list-img",
                () -> getClass().getResourceAsStream("baz/qux")));
        item1.setUserImage("foo/bar");
        Assert.assertNull(item1.getUserImageResource());
    }

    @Test
    public void addThemeNames_serialize_separatedBySpaces() {
        item1.addThemeNames("foo", "bar");
        item1.addThemeNames("baz");
        Assert.assertEquals("foo bar baz", getSerializedThemeProperty(item1));
    }

    @Test
    public void addThemeNames_serialize_noDuplicates() {
        item1.addThemeNames("foo", "foo");
        Assert.assertEquals("foo", getSerializedThemeProperty(item1));
    }

    @Test
    public void removeThemeNames_serialize_themeNamesRemoved() {
        item1.addThemeNames("foo", "bar", "baz");
        item1.removeThemeNames("foo", "bar", "qux");
        Assert.assertEquals("baz", getSerializedThemeProperty(item1));
    }

    @Test
    public void clearThemeNames_serialize_nullProperty() {
        item1.addThemeNames("foo");
        item1.removeThemeNames("foo");
        Assert.assertNull(getSerializedThemeProperty(item1));
    }

    @Test
    public void hasThemeName_falseForNonExistingThemeName() {
        Assert.assertFalse(item1.hasThemeName("foo"));
    }

    @Test
    public void hasThemeName_trueForExistingThemeName() {
        item1.addThemeNames("foo");
        Assert.assertTrue(item1.hasThemeName("foo"));
    }

    private String getSerializedThemeProperty(MessageListItem item) {
        JsonValue theme = JsonUtils.beanToJson(item).get("theme");
        if (theme.getType() == JsonType.NULL) {
            return null;
        } else {
            return theme.asString();
        }
    }
}
