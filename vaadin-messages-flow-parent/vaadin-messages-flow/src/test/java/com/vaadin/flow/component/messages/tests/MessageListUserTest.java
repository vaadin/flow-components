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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListTypingIndicatorFeatureFlagProvider;
import com.vaadin.flow.component.messages.MessageListUser;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ArrayNode;

class MessageListUserTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @RegisterExtension
    EnableFeatureFlagExtension featureFlagExtension = new EnableFeatureFlagExtension(
            MessageListTypingIndicatorFeatureFlagProvider.TYPING_INDICATOR);

    private MessageList messageList;
    private MessageListUser alice;
    private DownloadHandler imageHandler;

    @BeforeEach
    void setup() {
        messageList = new MessageList();
        alice = new MessageListUser("Alice");
        imageHandler = DownloadHandler
                .fromInputStream(event -> new DownloadResponse(
                        new ByteArrayInputStream(
                                "image".getBytes(StandardCharsets.UTF_8)),
                        "alice.png", "image/png", 5));
        ui.add(messageList);
    }

    @Test
    void setName_getName() {
        alice.setName("Alice Adams");

        Assertions.assertEquals("Alice Adams", alice.getName());
    }

    @Test
    void setImage_getImage() {
        alice.setImage("https://example.com/alice.png");

        Assertions.assertEquals("https://example.com/alice.png",
                alice.getImage());
    }

    @Test
    void setImageHandler_notInMessageList_imageUrlIsNull() {
        alice.setImageHandler(imageHandler);

        Assertions.assertSame(imageHandler, alice.getImageHandler());
        Assertions.assertNull(alice.getImage());
    }

    @Test
    void setImageHandler_setTypingUsers_imageServedByMessageList() {
        alice.setImageHandler(imageHandler);

        messageList.setTypingUsers(alice);

        assertImageIsServed(alice.getImage());
    }

    @Test
    void setTypingUsers_setImageHandler_imageServedByMessageList() {
        messageList.setTypingUsers(alice);

        alice.setImageHandler(imageHandler);

        assertImageIsServed(alice.getImage());
    }

    @Test
    void setImageHandler_setTypingUsers_imageUrlSentToClient() {
        alice.setImageHandler(imageHandler);

        messageList.setTypingUsers(alice);

        var serialized = JacksonUtils.beanToJson(alice);
        Assertions.assertEquals(alice.getImage(),
                serialized.get("img").asString());
    }

    @Test
    void setImageHandler_detachedMessageList_imageServedAfterAttach() {
        var detachedList = new MessageList();
        alice.setImageHandler(imageHandler);

        detachedList.setTypingUsers(alice);
        Assertions.assertNull(alice.getImage());

        ui.add(detachedList);

        assertImageIsServed(alice.getImage());
    }

    @Test
    void setImageHandler_messageListDetached_imageNoLongerServed() {
        alice.setImageHandler(imageHandler);
        messageList.setTypingUsers(alice);
        var imageUrl = alice.getImage();

        ui.remove(messageList);

        Assertions.assertNull(alice.getImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty(),
                "Expected the image resource to be unregistered");
    }

    @Test
    void setImageHandler_messageListReattached_imageServedAgain() {
        alice.setImageHandler(imageHandler);
        messageList.setTypingUsers(alice);
        ui.remove(messageList);

        ui.add(messageList);

        assertImageIsServed(alice.getImage());
    }

    @Test
    void setImageHandler_setImage_handlerRemovedAndNoLongerServed() {
        alice.setImageHandler(imageHandler);
        messageList.setTypingUsers(alice);
        var imageUrl = alice.getImage();

        alice.setImage("https://example.com/alice.png");

        Assertions.assertNull(alice.getImageHandler());
        Assertions.assertEquals("https://example.com/alice.png",
                alice.getImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty(),
                "Expected the image resource to be unregistered");
    }

    @Test
    void setImageHandler_null_imageRemoved() {
        alice.setImageHandler(imageHandler);
        messageList.setTypingUsers(alice);

        alice.setImageHandler(null);

        Assertions.assertNull(alice.getImageHandler());
        Assertions.assertNull(alice.getImage());
    }

    @Test
    void setImage_setImageHandler_imageUrlReplaced() {
        alice.setImage("https://example.com/alice.png");
        messageList.setTypingUsers(alice);

        alice.setImageHandler(imageHandler);

        assertImageIsServed(alice.getImage());
        Assertions.assertNotEquals("https://example.com/alice.png",
                alice.getImage());
    }

    @Test
    void setImageHandler_afterSetTypingUsers_imageUrlSentToClient() {
        messageList.setTypingUsers(alice);

        alice.setImageHandler(imageHandler);

        var typingUsers = messageList.getElement()
                .getPropertyRaw("_usersTyping");
        Assertions.assertEquals(alice.getImage(),
                ((ArrayNode) typingUsers).get(0).get("img").asString());
    }

    @Test
    void addClassNames_getClassName() {
        alice.addClassNames("typing", "highlight");

        Assertions.assertEquals("typing highlight", alice.getClassName());

        alice.removeClassNames("typing");

        Assertions.assertEquals("highlight", alice.getClassName());
    }

    @Test
    void getClassName_noClassNames_isNull() {
        Assertions.assertNull(alice.getClassName());
    }

    @Test
    void serializedUser_containsOnlyClientProperties() {
        alice.setAbbreviation("AL");
        alice.setColorIndex(2);
        alice.setImage("https://example.com/alice.png");
        alice.addClassNames("typing");

        var serialized = JacksonUtils.beanToJson(alice);

        Assertions.assertEquals("Alice", serialized.get("name").asString());
        Assertions.assertEquals("AL", serialized.get("abbr").asString());
        Assertions.assertEquals("https://example.com/alice.png",
                serialized.get("img").asString());
        Assertions.assertEquals(2, serialized.get("colorIndex").asInt());
        Assertions.assertEquals("typing",
                serialized.get("className").asString());
        Assertions.assertEquals(5, serialized.size());
    }

    private void assertImageIsServed(String imageUrl) {
        Assertions.assertNotNull(imageUrl, "Expected an image URL");
        Assertions.assertTrue(resolveImage(imageUrl).isPresent(),
                "Expected the image resource to be served from " + imageUrl);
    }

    private Optional<AbstractStreamResource> resolveImage(String imageUrl) {
        return ui.getSession().getResourceRegistry()
                .getResource(URI.create(imageUrl));
    }
}
