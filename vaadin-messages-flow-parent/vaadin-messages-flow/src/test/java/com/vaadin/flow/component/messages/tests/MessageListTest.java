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
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

import net.jcip.annotations.NotThreadSafe;
import tools.jackson.databind.JsonNode;

@NotThreadSafe
public class MessageListTest {

    private MessageList messageList;
    private MessageListItem item1;
    private MessageListItem item2;
    private UI ui;

    @Before
    public void setup() {
        messageList = new MessageList();
        item1 = new MessageListItem();
        ui = new UI();
        UI.setCurrent(ui);
        item2 = new MessageListItem();
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
        ui = null;
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

    @Test
    public void addClassNames_removeClassNames_hasClassName() {
        item1.addClassNames("foo", "bar");
        Assert.assertTrue(item1.hasClassName("foo"));
        Assert.assertTrue(item1.hasClassName("bar"));

        item1.removeClassNames("foo");
        Assert.assertFalse(item1.hasClassName("foo"));
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
        item1.setUserImage("foo/bar");
        item1.setUserImageResource(new StreamResource("message-list-img",
                () -> getClass().getResourceAsStream("baz/qux")));
        String userImage = item1.getUserImage();
        Assert.assertTrue("User image should start with 'VAADIN/dynamic'",
                userImage.startsWith("VAADIN/dynamic"));
    }

    @Test
    public void setImageAsUrl_streamResourceBecomesNull() {
        item1.setUserImageResource(new StreamResource("message-list-img",
                () -> getClass().getResourceAsStream("baz/qux")));
        item1.setUserImage("foo/bar");
        Assert.assertNull(item1.getUserImageResource());
    }

    @Test
    public void setImageHandler_overridesImageUrl() {
        item1.setUserImage("foo/bar");
        item1.setUserImageHandler(
                DownloadHandler.fromInputStream(data -> new DownloadResponse(
                        getClass().getResourceAsStream("baz/qux"),
                        "message-list-img", null, -1)));
        String userImage = item1.getUserImage();
        Assert.assertTrue("User image should start with 'VAADIN/dynamic'",
                userImage.startsWith("VAADIN/dynamic"));
    }

    @Test
    public void setImageHandler_streamResourceBecomesNull() {
        item1.setUserImageHandler(
                DownloadHandler.fromInputStream(data -> new DownloadResponse(
                        getClass().getResourceAsStream("baz/qux"),
                        "message-list-img", null, -1)));
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

    @Test
    public void unattachedItem_setText_doesNotThrow() {
        item1.setText("foo");
    }

    @Test
    public void setMarkdown_isMarkdown() {
        Assert.assertFalse(messageList.isMarkdown());
        messageList.setMarkdown(true);
        Assert.assertTrue(messageList.isMarkdown());
    }

    @Test
    public void setAnnounceMessages_isAnnounceMessages() {
        Assert.assertFalse(messageList.isAnnounceMessages());
        Assert.assertFalse(messageList.getElement()
                .getProperty("announceMessages", false));

        messageList.setAnnounceMessages(true);
        Assert.assertTrue(messageList.isAnnounceMessages());
        Assert.assertTrue(messageList.getElement()
                .getProperty("announceMessages", false));
    }

    @Test
    public void getAttachments_defaultIsEmpty() {
        Assert.assertTrue(item1.getAttachments().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getAttachments_returnsUnmodifiableList() {
        item1.getAttachments().add(
                new MessageListItem.Attachment("test", "url", "text/plain"));
    }

    @Test
    public void addAttachment_getAttachments() {
        MessageListItem.Attachment attachment = new MessageListItem.Attachment(
                "file.pdf", "http://example.com/file.pdf", "application/pdf");
        item1.addAttachment(attachment);

        Assert.assertEquals(1, item1.getAttachments().size());
        Assert.assertEquals(attachment, item1.getAttachments().get(0));
    }

    @Test
    public void addAttachment_withStrings_getAttachments() {
        item1.addAttachment("file.pdf", "http://example.com/file.pdf",
                "application/pdf");

        Assert.assertEquals(1, item1.getAttachments().size());
        MessageListItem.Attachment attachment = item1.getAttachments().get(0);
        Assert.assertEquals("file.pdf", attachment.name());
        Assert.assertEquals("http://example.com/file.pdf", attachment.url());
        Assert.assertEquals("application/pdf", attachment.mimeType());
    }

    @Test
    public void setAttachments_getAttachments() {
        MessageListItem.Attachment attachment1 = new MessageListItem.Attachment(
                "file1.pdf", "http://example.com/file1.pdf", "application/pdf");
        MessageListItem.Attachment attachment2 = new MessageListItem.Attachment(
                "file2.png", "http://example.com/file2.png", "image/png");

        item1.setAttachments(List.of(attachment1, attachment2));

        Assert.assertEquals(2, item1.getAttachments().size());
        Assert.assertEquals(attachment1, item1.getAttachments().get(0));
        Assert.assertEquals(attachment2, item1.getAttachments().get(1));
    }

    @Test
    public void setAttachments_emptyList_clearsAttachments() {
        item1.addAttachment("file.pdf", "http://example.com/file.pdf",
                "application/pdf");
        item1.setAttachments(List.of());

        Assert.assertTrue(item1.getAttachments().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void setAttachments_null_throws() {
        item1.setAttachments(null);
    }

    @Test(expected = NullPointerException.class)
    public void addAttachment_null_throws() {
        item1.addAttachment(null);
    }

    @Test
    public void attachmentSerialization_containsExpectedFields() {
        item1.addAttachment("proposal.pdf", "#proposal.pdf", "application/pdf");

        JsonNode json = JacksonUtils.beanToJson(item1);
        JsonNode attachments = json.get("attachments");

        Assert.assertNotNull(attachments);
        Assert.assertTrue(attachments.isArray());
        Assert.assertEquals(1, attachments.size());

        JsonNode attachment = attachments.get(0);
        Assert.assertEquals("proposal.pdf", attachment.get("name").asString());
        Assert.assertEquals("#proposal.pdf", attachment.get("url").asString());
        Assert.assertEquals("application/pdf",
                attachment.get("type").asString());
    }

    private String getSerializedThemeProperty(MessageListItem item) {
        JsonNode theme = JacksonUtils.beanToJson(item).get("theme");
        if (theme.isNull()) {
            return null;
        } else {
            return theme.asString();
        }
    }
}
