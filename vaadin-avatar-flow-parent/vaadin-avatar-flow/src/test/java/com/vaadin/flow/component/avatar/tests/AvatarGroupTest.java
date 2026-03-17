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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.component.shared.HasThemeVariant;

class AvatarGroupTest {

    private AvatarGroup avatarGroup;
    private AvatarGroupItem avatarGroupItem1;
    private AvatarGroupItem avatarGroupItem2;

    @BeforeEach
    void setup() {
        avatarGroup = new AvatarGroup();
        avatarGroupItem1 = new AvatarGroupItem();
        avatarGroupItem2 = new AvatarGroupItem("Foo Bar");
    }

    @Test
    void setName_getName() {
        avatarGroupItem1.setName("foo bar");
        Assertions.assertEquals("foo bar", avatarGroupItem1.getName());
    }

    @Test
    void setAbbr_getAbbr() {
        avatarGroupItem1.setAbbreviation("fb");
        Assertions.assertEquals("fb", avatarGroupItem1.getAbbreviation());
    }

    @Test
    void setImgUrl_getImgUrl() {
        avatarGroupItem1.setImage("https://vaadin.com/");
        Assertions.assertEquals("https://vaadin.com/",
                avatarGroupItem1.getImage());
    }

    @Test
    void setColorIndex_getColorIndex() {
        avatarGroupItem1.setColorIndex(3);
        Assertions.assertEquals((Integer) 3, avatarGroupItem1.getColorIndex());
    }

    @Test
    void addClassNames_removeClassNames_getClassNames() {
        avatarGroupItem1.addClassNames("foo", "bar");
        Assertions.assertEquals("foo bar", avatarGroupItem1.getClassName());

        avatarGroupItem1.removeClassNames("foo");
        Assertions.assertEquals("bar", avatarGroupItem1.getClassName());
    }

    @Test
    void setCreatedItems_getCreatedItems() {
        List<AvatarGroupItem> items = List.of(avatarGroupItem1,
                avatarGroupItem2);
        avatarGroup.setItems(items);

        Assertions.assertEquals(items, avatarGroup.getItems());
    }

    @Test
    void getEmptyItems_doesNotThrow() {
        avatarGroup.getItems();
    }

    @Test
    void createWithItems_getCreatedItems() {
        List<AvatarGroupItem> items = List.of(avatarGroupItem1,
                avatarGroupItem2);
        AvatarGroup createdAvatarGroup = new AvatarGroup(items);

        Assertions.assertEquals(items, createdAvatarGroup.getItems());
    }

    @Test
    void createWithVarargsItems_getCreatedItems() {
        AvatarGroup createdAvatarGroup = new AvatarGroup(avatarGroupItem1,
                avatarGroupItem2);

        Assertions.assertEquals(List.of(avatarGroupItem1, avatarGroupItem2),
                createdAvatarGroup.getItems());
    }

    @Test
    void addItems_getItems() {
        avatarGroup.setItems(List.of(avatarGroupItem1, avatarGroupItem2));
        AvatarGroupItem addedItem = new AvatarGroupItem("Bar Baz");
        avatarGroup.add(addedItem);

        Assertions.assertEquals(
                List.of(avatarGroupItem1, avatarGroupItem2, addedItem),
                avatarGroup.getItems());
    }

    @Test
    void removeItems_getItems() {
        avatarGroup.setItems(List.of(avatarGroupItem1, avatarGroupItem2));
        avatarGroup.remove(avatarGroupItem2);

        Assertions.assertEquals(List.of(avatarGroupItem1),
                avatarGroup.getItems());
    }

    @Test
    void setMaxItemsVisible_getMaxItemsVisible() {
        avatarGroup.setMaxItemsVisible(3);

        Assertions.assertEquals((Integer) 3, avatarGroup.getMaxItemsVisible());
    }

    @Test
    void getMaxItemsVisible_returnsNull() {
        Assertions.assertNull(avatarGroup.getMaxItemsVisible());
    }

    @Test
    void setI18n() {
        AvatarGroup.AvatarGroupI18n i18n = new AvatarGroup.AvatarGroupI18n()
                .setAnonymous("anonyymi")
                .setOneActiveUser("Yksi käyttäjä aktiivinen")
                .setManyActiveUsers("{count} aktiivista käyttäjää");

        avatarGroup.setI18n(i18n);
        Assertions.assertEquals(i18n, avatarGroup.getI18n());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(AvatarGroup.class));
    }
}
