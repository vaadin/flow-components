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

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.Avatar.AvatarI18n;
import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupI18n;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-avatar/i18n")
public class AvatarI18nPage extends Div {

    public AvatarI18nPage() {
        Avatar avatar = new Avatar();
        avatar.setTooltipEnabled(true);

        AvatarGroup avatarGroup = new AvatarGroup();
        avatarGroup.setItems(new AvatarGroupItem("User 1"),
                new AvatarGroupItem("User 2"));

        NativeButton setI18n = new NativeButton("Set i18n", e -> {
            avatar.setI18n(new AvatarI18n().setAnonymous("Custom anonymous"));
            avatarGroup.setI18n(new AvatarGroupI18n()
                    .setOneActiveUser("Custom one active user")
                    .setManyActiveUsers("Custom {count} active users"));
        });
        setI18n.setId("set-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty i18n", e -> {
            avatar.setI18n(new AvatarI18n());
            avatarGroup.setI18n(new AvatarGroupI18n());
        });
        setEmptyI18n.setId("set-empty-i18n");

        add(setI18n, setEmptyI18n, avatar, avatarGroup);
    }
}
