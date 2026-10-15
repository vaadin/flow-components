/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
                removeClassNames);
    }
}
