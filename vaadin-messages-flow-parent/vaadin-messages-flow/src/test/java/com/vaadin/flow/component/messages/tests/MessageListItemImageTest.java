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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

class MessageListItemImageTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private MessageList messageList;
    private MessageListItem item;

    @BeforeEach
    void setup() {
        messageList = new MessageList();
        item = new MessageListItem("Hello", null, "Alice");
        messageList.setItems(item);
        ui.add(messageList);
    }

    @Test
    void setUserImageHandler_imageServed() {
        item.setUserImageHandler(createImageHandler());

        assertImageIsServed(item.getUserImage());
    }

    @Test
    void setUserImageHandler_detachedList_imageServedAfterAttach() {
        var detachedList = new MessageList();
        var detachedItem = new MessageListItem("Hello", null, "Bob");
        detachedList.setItems(detachedItem);
        detachedItem.setUserImageHandler(createImageHandler());
        var imageUrl = detachedItem.getUserImage();
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());

        ui.add(detachedList);

        assertImageIsServed(imageUrl);
    }

    @Test
    void setUserImageResource_itemAddedToListLater_imageServed() {
        var newItem = new MessageListItem("Hello", null, "Bob");
        newItem.setUserImageResource(createImageResource());

        messageList.addItem(newItem);

        assertImageIsServed(newItem.getUserImage());
    }

    @Test
    void setUserImageHandler_listDetached_imageNoLongerServed() {
        item.setUserImageHandler(createImageHandler());
        var imageUrl = item.getUserImage();

        ui.remove(messageList);

        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setUserImageHandler_listReattached_imageServedAgain() {
        item.setUserImageHandler(createImageHandler());
        var imageUrl = item.getUserImage();

        ui.remove(messageList);
        ui.add(messageList);

        Assertions.assertEquals(imageUrl, item.getUserImage());
        assertImageIsServed(imageUrl);
    }

    @Test
    void setUserImageHandler_listDetachedAndReattached_getUserImageResourceUnchanged() {
        item.setUserImageHandler(createImageHandler());
        var resource = item.getUserImageResource();
        Assertions.assertNotNull(resource);

        ui.remove(messageList);
        Assertions.assertSame(resource, item.getUserImageResource());

        ui.add(messageList);
        Assertions.assertSame(resource, item.getUserImageResource());
    }

    @Test
    void setUserImageResource_listDetachedAndReattached_getUserImageResourceUnchanged() {
        var resource = createImageResource();
        item.setUserImageResource(resource);

        ui.remove(messageList);
        Assertions.assertSame(resource, item.getUserImageResource());

        ui.add(messageList);
        Assertions.assertSame(resource, item.getUserImageResource());
    }

    @Test
    void setUserImage_imageResourceNoLongerServed() {
        item.setUserImageHandler(createImageHandler());
        var imageUrl = item.getUserImage();

        item.setUserImage("https://example.com/alice.png");

        Assertions.assertNull(item.getUserImageResource());
        Assertions.assertEquals("https://example.com/alice.png",
                item.getUserImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setUserImage_listReattached_imageResourceNotServedAgain() {
        item.setUserImageHandler(createImageHandler());
        var imageUrl = item.getUserImage();
        item.setUserImage("https://example.com/alice.png");

        ui.remove(messageList);
        ui.add(messageList);

        Assertions.assertEquals("https://example.com/alice.png",
                item.getUserImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setUserImageHandlerNull_imageRemoved() {
        item.setUserImageHandler(createImageHandler());
        var imageUrl = item.getUserImage();

        item.setUserImageHandler(null);

        Assertions.assertNull(item.getUserImageResource());
        Assertions.assertNull(item.getUserImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setUserImageHandler_replaceHandler_previousImageNoLongerServed() {
        item.setUserImageHandler(createImageHandler());
        var previousImageUrl = item.getUserImage();

        item.setUserImageHandler(createImageHandler());

        assertImageIsServed(item.getUserImage());
        Assertions.assertTrue(resolveImage(previousImageUrl).isEmpty());
    }

    @Test
    void setUserImageHandler_itemRemoved_imageNoLongerServed() {
        item.setUserImageHandler(createImageHandler());
        var imageUrl = item.getUserImage();

        messageList.setItems();

        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
        Assertions.assertNotNull(item.getUserImageResource());
    }

    @Test
    void setUserImageResource_itemMovedToOtherList_previousListDetached_imageStillServed() {
        var otherList = new MessageList();
        ui.add(otherList);
        item.setUserImageResource(createImageResource());
        messageList.setItems();
        otherList.addItem(item);

        ui.remove(messageList);

        assertImageIsServed(item.getUserImage());
    }

    @Test
    void setUserImageResource_detachedList_itemRemoved_listAttached_imageNotServed() {
        var detachedList = new MessageList();
        var detachedItem = new MessageListItem("Hello", null, "Bob");
        detachedList.setItems(detachedItem);
        detachedItem.setUserImageResource(createImageResource());
        detachedList.setItems();

        Assertions.assertDoesNotThrow(() -> ui.add(detachedList));

        Assertions.assertTrue(
                resolveImage(detachedItem.getUserImage()).isEmpty());
    }

    @Test
    void setItems_sameItem_imageStillServed() {
        item.setUserImageHandler(createImageHandler());

        messageList.setItems(item);

        assertImageIsServed(item.getUserImage());
    }

    @Test
    void setItems_sameItem_imageNotRegisteredAgain() {
        // The registry reads the ID of a resource each time it is registered
        var idReads = new AtomicInteger();
        item.setUserImageResource(
                new StreamResource("alice.png", () -> new ByteArrayInputStream(
                        "image".getBytes(StandardCharsets.UTF_8))) {
                    @Override
                    public String getId() {
                        idReads.incrementAndGet();
                        return super.getId();
                    }
                });
        idReads.set(0);

        messageList.setItems(item);

        Assertions.assertEquals(0, idReads.get());
    }

    @Test
    void setItems_itemMovedToOtherList_imageStillServed() {
        var otherList = new MessageList();
        ui.add(otherList);
        item.setUserImageResource(createImageResource());
        otherList.addItem(item);

        messageList.setItems();

        assertImageIsServed(item.getUserImage());
    }

    @Test
    void setUserImageHandler_serializeAttachedList_roundTripSucceeds()
            throws IOException, ClassNotFoundException {
        item.setUserImageHandler(createImageHandler());

        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(messageList);
        }
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            var deserialized = (MessageList) in.readObject();
            Assertions.assertNotNull(
                    deserialized.getItems().get(0).getUserImageResource());
        }
    }

    @Test
    void setUserImageHandlerNull_clientItemsUpdated() {
        item.setUserImageHandler(createImageHandler());
        Assertions.assertTrue(clientItem().hasNonNull("userImg"));

        item.setUserImageHandler(null);

        Assertions.assertFalse(clientItem().hasNonNull("userImg"));
    }

    @Test
    void setUserImageHandler_itemMovedToOtherList_previousListDetached_imageOwnedByOtherList() {
        var otherList = new MessageList();
        ui.add(otherList);
        item.setUserImageHandler(createImageHandler());
        messageList.setItems();
        otherList.addItem(item);

        ui.remove(messageList);

        assertImageIsServed(item.getUserImage());
        Assertions.assertSame(otherList.getElement(),
                getOwner(item.getUserImageResource()));
    }

    @Test
    void setUserImageHandler_itemAddedToListLater_imageOwnedByList() {
        var newItem = new MessageListItem("Hello", null, "Bob");
        newItem.setUserImageHandler(createImageHandler());

        messageList.addItem(newItem);

        assertImageIsServed(newItem.getUserImage());
        Assertions.assertSame(messageList.getElement(),
                getOwner(newItem.getUserImageResource()));
    }

    private DownloadHandler createImageHandler() {
        return DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(
                        "image".getBytes(StandardCharsets.UTF_8)),
                "alice.png", "image/png", 5));
    }

    private StreamResource createImageResource() {
        return new StreamResource("alice.png", () -> new ByteArrayInputStream(
                "image".getBytes(StandardCharsets.UTF_8)));
    }

    private JsonNode clientItem() {
        var invocations = ui.dumpPendingJavaScriptInvocations();
        Assertions.assertEquals(1, invocations.size());
        var items = (ArrayNode) invocations.getFirst().getInvocation()
                .getParameters().getFirst();
        return items.get(0);
    }

    private Element getOwner(AbstractStreamResource resource) {
        return ((ElementStreamResource) resource).getOwner();
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
