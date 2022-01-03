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
 */
package com.vaadin.flow.component.avatar.tests;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarGroup;

import com.vaadin.flow.component.avatar.Avatar.AvatarI18n;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupI18n;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("vaadin-avatar/i18n-test")
public class I18nPage extends Div {

    public I18nPage() {
        Avatar avatar = new Avatar();
        AvatarGroup avatarGroup = new AvatarGroup();

        List<AvatarGroup.AvatarGroupItem> items = new ArrayList<>();

        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev"));

        AvatarGroupItem avatarWithAbbr = new AvatarGroupItem();
        avatarWithAbbr.setAbbreviation("SK");
        items.add(avatarWithAbbr);
        items.add(new AvatarGroupItem("Jens Jansson"));
        avatarGroup.setItems(items);

        NativeButton addI18n = new NativeButton("Add i18n", e -> {
            avatar.setI18n(new AvatarI18n().setAnonymous("анонимный"));
            avatarGroup.setI18n(new AvatarGroupI18n().setAnonymous("анонимный")
                    .setOneActiveUser("Один активный пользователь")
                    .setManyActiveUsers("{count} активных пользователей"));
        });
        addI18n.setId("set-i18n");

        Div dataTitle = new Div();
        dataTitle.setId("data-title-i18n");

        Div dataAriaLabel = new Div();
        dataAriaLabel.setId("data-aria-label-i18n");

        NativeButton getI18n = new NativeButton("Get i18n", e -> {
            dataTitle.setText(avatar.getI18n().getAnonymous());
            dataAriaLabel.setText(avatarGroup.getI18n().getManyActiveUsers()
                    .replaceFirst("\\{count\\}",
                            ((Integer) avatarGroup.getItems().size())
                                    .toString()));
        });
        getI18n.setId("get-i18n");

        add(avatar, avatarGroup, addI18n, dataTitle, dataAriaLabel, getI18n);
    }
}
