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
package com.vaadin.flow.component.avatar.tests;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.streams.DownloadHandler;

@Route("vaadin-avatar/avatar-group-test")
public class AvatarGroupPage extends Div {

    public AvatarGroupPage() {
        AvatarGroup avatarGroup = new AvatarGroup();

        List<AvatarGroup.AvatarGroupItem> items = new ArrayList<>();

        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev"));

        AvatarGroupItem avatarWithAbbr = new AvatarGroupItem();
        avatarWithAbbr.setAbbreviation("SK");
        items.add(avatarWithAbbr);
        items.add(new AvatarGroupItem("Jens Jansson"));
        avatarGroup.setItems(items);

        NativeButton updateItems = new NativeButton("Update Item", e -> {
            items.get(0).setAbbreviation("FF");
            items.get(1).setAbbreviation("FF");
        });
        updateItems.setId("update-items");

        NativeButton setItemsWithResource = new NativeButton(
                "Set new item with StreamResource image", e -> {
                    StreamResource resource = new StreamResource(
                            "avatar-group-img",
                            () -> getClass().getResourceAsStream(
                                    "/META-INF/resources/frontend/images/user.png"));
                    AvatarGroupItem newItem = new AvatarGroupItem();
                    newItem.setImageResource(resource);

                    avatarGroup.setItems(newItem);
                });
        setItemsWithResource.setId("set-items-with-resource");

        NativeButton setItemsWithDownloadHandler = new NativeButton(
                "Set new item with download resource image", e -> {
                    DownloadHandler download = DownloadHandler.forClassResource(
                            getClass(),
                            "/META-INF/resources/frontend/images/user.png",
                            "avatar-group-img");
                    AvatarGroupItem newItem = new AvatarGroupItem();
                    newItem.setImageHandler(download);

                    avatarGroup.setItems(newItem);
                });
        setItemsWithDownloadHandler.setId("set-items-with-download-resource");

        NativeButton addClassNames = new NativeButton("Add class name", e -> {
            items.get(0).addClassNames("red");
        });
        addClassNames.setId("add-class-names");

        NativeButton removeClassNames = new NativeButton("Remove class name",
                e -> {
                    items.get(0).removeClassNames("red");
                });
        removeClassNames.setId("remove-class-names");

        add(avatarGroup, updateItems, setItemsWithResource, addClassNames,
                removeClassNames, setItemsWithDownloadHandler);
    }
}
