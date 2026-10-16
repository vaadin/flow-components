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
package com.vaadin.flow.component.avatar.tests;

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

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry.ElementStreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

class AvatarGroupItemImageTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private AvatarGroup avatarGroup;
    private AvatarGroupItem item;

    @BeforeEach
    void setup() {
        avatarGroup = new AvatarGroup();
        item = new AvatarGroupItem("Alice");
        avatarGroup.setItems(item);
        ui.add(avatarGroup);
    }

    @Test
    void setImageHandler_imageServed() {
        item.setImageHandler(createImageHandler());

        assertImageIsServed(item.getImage());
    }

    @Test
    void setImageHandler_detachedGroup_imageServedAfterAttach() {
        var detachedGroup = new AvatarGroup();
        var detachedItem = new AvatarGroupItem("Bob");
        detachedGroup.setItems(detachedItem);
        detachedItem.setImageHandler(createImageHandler());
        var imageUrl = detachedItem.getImage();
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());

        ui.add(detachedGroup);

        assertImageIsServed(imageUrl);
    }

    @Test
    void setImageResource_itemAddedToGroupLater_imageServed() {
        var newItem = new AvatarGroupItem("Bob");
        newItem.setImageResource(createImageResource());

        avatarGroup.add(newItem);

        assertImageIsServed(newItem.getImage());
    }

    @Test
    void setImageHandler_groupDetached_imageNoLongerServed() {
        item.setImageHandler(createImageHandler());
        var imageUrl = item.getImage();

        ui.remove(avatarGroup);

        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setImageHandler_groupReattached_imageServedAgain() {
        item.setImageHandler(createImageHandler());
        var imageUrl = item.getImage();

        ui.remove(avatarGroup);
        ui.add(avatarGroup);

        Assertions.assertEquals(imageUrl, item.getImage());
        assertImageIsServed(imageUrl);
    }

    @Test
    void setImageHandler_groupDetachedAndReattached_getImageResourceUnchanged() {
        item.setImageHandler(createImageHandler());
        var resource = item.getImageResource();
        Assertions.assertNotNull(resource);

        ui.remove(avatarGroup);
        Assertions.assertSame(resource, item.getImageResource());

        ui.add(avatarGroup);
        Assertions.assertSame(resource, item.getImageResource());
    }

    @Test
    void setImageResource_groupDetachedAndReattached_getImageResourceUnchanged() {
        var resource = createImageResource();
        item.setImageResource(resource);

        ui.remove(avatarGroup);
        Assertions.assertSame(resource, item.getImageResource());

        ui.add(avatarGroup);
        Assertions.assertSame(resource, item.getImageResource());
    }

    @Test
    void setImage_imageResourceNoLongerServed() {
        item.setImageHandler(createImageHandler());
        var imageUrl = item.getImage();

        item.setImage("https://example.com/alice.png");

        Assertions.assertNull(item.getImageResource());
        Assertions.assertEquals("https://example.com/alice.png",
                item.getImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setImage_groupReattached_imageResourceNotServedAgain() {
        item.setImageHandler(createImageHandler());
        var imageUrl = item.getImage();
        item.setImage("https://example.com/alice.png");

        ui.remove(avatarGroup);
        ui.add(avatarGroup);

        Assertions.assertEquals("https://example.com/alice.png",
                item.getImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setImageHandlerNull_imageRemoved() {
        item.setImageHandler(createImageHandler());
        var imageUrl = item.getImage();

        item.setImageHandler(null);

        Assertions.assertNull(item.getImageResource());
        Assertions.assertNull(item.getImage());
        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
    }

    @Test
    void setImageHandler_replaceHandler_previousImageNoLongerServed() {
        item.setImageHandler(createImageHandler());
        var previousImageUrl = item.getImage();

        item.setImageHandler(createImageHandler());

        assertImageIsServed(item.getImage());
        Assertions.assertTrue(resolveImage(previousImageUrl).isEmpty());
    }

    @Test
    void setImageHandler_itemRemoved_imageNoLongerServed() {
        item.setImageHandler(createImageHandler());
        var imageUrl = item.getImage();

        avatarGroup.remove(item);

        Assertions.assertTrue(resolveImage(imageUrl).isEmpty());
        Assertions.assertNotNull(item.getImageResource());
    }

    @Test
    void setImageResource_itemMovedToOtherGroup_previousGroupDetached_imageStillServed() {
        var otherGroup = new AvatarGroup();
        ui.add(otherGroup);
        item.setImageResource(createImageResource());
        avatarGroup.remove(item);
        otherGroup.add(item);

        ui.remove(avatarGroup);

        assertImageIsServed(item.getImage());
    }

    @Test
    void setImageResource_detachedGroup_itemRemoved_groupAttached_imageNotServed() {
        var detachedGroup = new AvatarGroup();
        var detachedItem = new AvatarGroupItem("Bob");
        detachedGroup.setItems(detachedItem);
        detachedItem.setImageResource(createImageResource());
        detachedGroup.remove(detachedItem);

        Assertions.assertDoesNotThrow(() -> ui.add(detachedGroup));

        Assertions.assertTrue(resolveImage(detachedItem.getImage()).isEmpty());
    }

    @Test
    void setItems_sameItem_imageStillServed() {
        item.setImageHandler(createImageHandler());

        avatarGroup.setItems(item);

        assertImageIsServed(item.getImage());
    }

    @Test
    void setItems_sameItem_imageNotRegisteredAgain() {
        // The registry reads the ID of a resource each time it is registered
        var idReads = new AtomicInteger();
        item.setImageResource(
                new StreamResource("alice.png", () -> new ByteArrayInputStream(
                        "image".getBytes(StandardCharsets.UTF_8))) {
                    @Override
                    public String getId() {
                        idReads.incrementAndGet();
                        return super.getId();
                    }
                });
        idReads.set(0);

        avatarGroup.setItems(item);

        Assertions.assertEquals(0, idReads.get());
    }

    @Test
    void setItems_itemMovedToOtherGroup_imageStillServed() {
        var otherGroup = new AvatarGroup();
        ui.add(otherGroup);
        item.setImageResource(createImageResource());
        otherGroup.add(item);

        avatarGroup.setItems();

        assertImageIsServed(item.getImage());
    }

    @Test
    void setImageHandler_serializeAttachedGroup_roundTripSucceeds()
            throws IOException, ClassNotFoundException {
        item.setImageHandler(createImageHandler());

        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(avatarGroup);
        }
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            var deserialized = (AvatarGroup) in.readObject();
            Assertions.assertNotNull(
                    deserialized.getItems().get(0).getImageResource());
        }
    }

    @Test
    void setImageHandlerNull_clientItemsUpdated() {
        item.setImageHandler(createImageHandler());
        ui.fakeClientCommunication();
        Assertions.assertTrue(clientItem().hasNonNull("img"));

        item.setImageHandler(null);
        ui.fakeClientCommunication();

        Assertions.assertFalse(clientItem().hasNonNull("img"));
    }

    @Test
    void setImageHandler_itemMovedToOtherGroup_previousGroupDetached_imageOwnedByOtherGroup() {
        var otherGroup = new AvatarGroup();
        ui.add(otherGroup);
        item.setImageHandler(createImageHandler());
        avatarGroup.remove(item);
        otherGroup.add(item);

        ui.remove(avatarGroup);

        assertImageIsServed(item.getImage());
        Assertions.assertSame(otherGroup.getElement(),
                getOwner(item.getImageResource()));
    }

    @Test
    void setImageHandler_itemAddedToGroupLater_imageOwnedByGroup() {
        var newItem = new AvatarGroupItem("Bob");
        newItem.setImageHandler(createImageHandler());

        avatarGroup.add(newItem);

        assertImageIsServed(newItem.getImage());
        Assertions.assertSame(avatarGroup.getElement(),
                getOwner(newItem.getImageResource()));
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
        return ((ArrayNode) avatarGroup.getElement().getPropertyRaw("items"))
                .get(0);
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
