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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.tests.MockUIExtension;

import net.jcip.annotations.NotThreadSafe;
import tools.jackson.databind.JsonNode;

@NotThreadSafe
class MessageListTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private MessageList messageList;
    private MessageListItem item1;
    private MessageListItem item2;

    @BeforeEach
    void setup() {
        messageList = new MessageList();
        item1 = new MessageListItem();
        item2 = new MessageListItem();
    }

    @Test
    void getItems_returnsUnmodifiableList() {
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> messageList.getItems().add(new MessageListItem()));
    }

    @Test
    void setItemsCollection_getItems() {
        messageList.setItems(Arrays.asList(item1, item2));
        Assertions.assertEquals(Arrays.asList(item1, item2),
                messageList.getItems());
    }

    @Test
    void setItemsVarArgs_getItems() {
        messageList.setItems(item1, item2);
        Assertions.assertEquals(Arrays.asList(item1, item2),
                messageList.getItems());
    }

    @Test
    void addClassNames_removeClassNames_hasClassName() {
        item1.addClassNames("foo", "bar");
        Assertions.assertTrue(item1.hasClassName("foo"));
        Assertions.assertTrue(item1.hasClassName("bar"));

        item1.removeClassNames("foo");
        Assertions.assertFalse(item1.hasClassName("foo"));
    }

    @Test
    void setItems_nullCollection_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageList.setItems((Collection<MessageListItem>) null));
    }

    @Test
    void setItems_containsNullItem_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> messageList.setItems(item1, null, item2));
    }

    @Test
    void setImageAsStreamResource_overridesImageUrl() {
        item1.setUserImage("foo/bar");
        item1.setUserImageResource(new StreamResource("message-list-img",
                () -> getClass().getResourceAsStream("baz/qux")));
        String userImage = item1.getUserImage();
        Assertions.assertTrue(userImage.startsWith("VAADIN/dynamic"),
                "User image should start with 'VAADIN/dynamic'");
    }

    @Test
    void setImageAsUrl_streamResourceBecomesNull() {
        item1.setUserImageResource(new StreamResource("message-list-img",
                () -> getClass().getResourceAsStream("baz/qux")));
        item1.setUserImage("foo/bar");
        Assertions.assertNull(item1.getUserImageResource());
    }

    @Test
    void setImageHandler_overridesImageUrl() {
        item1.setUserImage("foo/bar");
        item1.setUserImageHandler(
                DownloadHandler.fromInputStream(data -> new DownloadResponse(
                        getClass().getResourceAsStream("baz/qux"),
                        "message-list-img", null, -1)));
        String userImage = item1.getUserImage();
        Assertions.assertTrue(userImage.startsWith("VAADIN/dynamic"),
                "User image should start with 'VAADIN/dynamic'");
    }

    @Test
    void setImageHandler_streamResourceBecomesNull() {
        item1.setUserImageHandler(
                DownloadHandler.fromInputStream(data -> new DownloadResponse(
                        getClass().getResourceAsStream("baz/qux"),
                        "message-list-img", null, -1)));
        item1.setUserImage("foo/bar");
        Assertions.assertNull(item1.getUserImageResource());
    }

    @Test
    void addThemeNames_serialize_separatedBySpaces() {
        item1.addThemeNames("foo", "bar");
        item1.addThemeNames("baz");
        Assertions.assertEquals("foo bar baz",
                getSerializedThemeProperty(item1));
    }

    @Test
    void addThemeNames_serialize_noDuplicates() {
        item1.addThemeNames("foo", "foo");
        Assertions.assertEquals("foo", getSerializedThemeProperty(item1));
    }

    @Test
    void removeThemeNames_serialize_themeNamesRemoved() {
        item1.addThemeNames("foo", "bar", "baz");
        item1.removeThemeNames("foo", "bar", "qux");
        Assertions.assertEquals("baz", getSerializedThemeProperty(item1));
    }

    @Test
    void clearThemeNames_serialize_nullProperty() {
        item1.addThemeNames("foo");
        item1.removeThemeNames("foo");
        Assertions.assertNull(getSerializedThemeProperty(item1));
    }

    @Test
    void hasThemeName_falseForNonExistingThemeName() {
        Assertions.assertFalse(item1.hasThemeName("foo"));
    }

    @Test
    void hasThemeName_trueForExistingThemeName() {
        item1.addThemeNames("foo");
        Assertions.assertTrue(item1.hasThemeName("foo"));
    }

    @Test
    void unattachedItem_setText_doesNotThrow() {
        item1.setText("foo");
    }

    @Test
    void setMarkdown_isMarkdown() {
        Assertions.assertFalse(messageList.isMarkdown());
        messageList.setMarkdown(true);
        Assertions.assertTrue(messageList.isMarkdown());
    }

    @Test
    void setAnnounceMessages_isAnnounceMessages() {
        Assertions.assertFalse(messageList.isAnnounceMessages());
        Assertions.assertFalse(messageList.getElement()
                .getProperty("announceMessages", false));

        messageList.setAnnounceMessages(true);
        Assertions.assertTrue(messageList.isAnnounceMessages());
        Assertions.assertTrue(messageList.getElement()
                .getProperty("announceMessages", false));
    }

    @Test
    void getAttachments_defaultIsEmpty() {
        Assertions.assertTrue(item1.getAttachments().isEmpty());
    }

    @Test
    void getAttachments_returnsUnmodifiableList() {
        Assertions
                .assertThrows(UnsupportedOperationException.class,
                        () -> item1.getAttachments()
                                .add(new MessageListItem.Attachment("test",
                                        "url", "text/plain")));
    }

    @Test
    void addAttachment_getAttachments() {
        var attachment = new MessageListItem.Attachment("file.pdf",
                "http://example.com/file.pdf", "application/pdf");
        item1.addAttachment(attachment);

        Assertions.assertEquals(1, item1.getAttachments().size());
        Assertions.assertEquals(attachment, item1.getAttachments().get(0));
    }

    @Test
    void setAttachments_getAttachments() {
        var attachment1 = new MessageListItem.Attachment("file1.pdf",
                "http://example.com/file1.pdf", "application/pdf");
        var attachment2 = new MessageListItem.Attachment("file2.png",
                "http://example.com/file2.png", "image/png");

        item1.setAttachments(List.of(attachment1, attachment2));

        Assertions.assertEquals(2, item1.getAttachments().size());
        Assertions.assertEquals(attachment1, item1.getAttachments().get(0));
        Assertions.assertEquals(attachment2, item1.getAttachments().get(1));
    }

    @Test
    void setAttachments_emptyList_clearsAttachments() {
        item1.addAttachment(new MessageListItem.Attachment("file.pdf",
                "http://example.com/file.pdf", "application/pdf"));
        item1.setAttachments(List.of());

        Assertions.assertTrue(item1.getAttachments().isEmpty());
    }

    @Test
    void setAttachments_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> item1.setAttachments(null));
    }

    @Test
    void addAttachment_null_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> item1.addAttachment(null));
    }

    @Test
    void attachmentSerialization_containsExpectedFields() {
        item1.addAttachment(new MessageListItem.Attachment("proposal.pdf",
                "#proposal.pdf", "application/pdf"));

        var json = JacksonUtils.beanToJson(item1);
        var attachments = json.get("attachments");

        Assertions.assertNotNull(attachments);
        Assertions.assertTrue(attachments.isArray());
        Assertions.assertEquals(1, attachments.size());

        var attachment = attachments.get(0);
        Assertions.assertEquals("proposal.pdf",
                attachment.get("name").asString());
        Assertions.assertEquals("#proposal.pdf",
                attachment.get("url").asString());
        Assertions.assertEquals("application/pdf",
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
