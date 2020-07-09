/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.avatar.demo;

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * View for {@link AvatarGroup} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-avatar-group")
public class AvatarGroupView extends DemoView {

    @Override
    public void initView() {
        createBasicAvatarGroup();
        createMaxAvatarGroup();
    }

    private void createBasicAvatarGroup() {
        // begin-source-example
        // source-example-heading: Avatar Group
        AvatarGroup avatarGroup = new AvatarGroup();

        List<AvatarGroupItem> items = new ArrayList<>();

        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev"));

        AvatarGroupItem avatarWithAbbr = new AvatarGroupItem();
        avatarWithAbbr.setAbbreviation("SK");
        items.add(avatarWithAbbr);

        items.add(new AvatarGroupItem("Jens Jansson"));
        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev", "https://vaadin.com/static/content/view/company/team/photos/Yuriy-Yevstihnyeyev.JPG"));

        avatarGroup.setItems(items);

        add(avatarGroup);
        // end-source-example

        addCard("Avatar Group", avatarGroup);
    }

    private void createMaxAvatarGroup() {
        // begin-source-example
        // source-example-heading: Setting Max
        AvatarGroup avatarGroup = new AvatarGroup();

        avatarGroup.setMax(3);

        List<AvatarGroupItem> items = new ArrayList<>();

        items.add(new AvatarGroupItem("Yuriy Yevstihnyeyev"));

        AvatarGroupItem avatarWithAbbr = new AvatarGroupItem();
        avatarWithAbbr.setAbbreviation("SK");
        items.add(avatarWithAbbr);

        items.add(new AvatarGroupItem("Leif Åstrand"));
        items.add(new AvatarGroupItem("Jens Jansson"));
        items.add(new AvatarGroupItem("Pekka Maanpää"));

        avatarGroup.setItems(items);

        add(avatarGroup);
        // end-source-example

        addCard("Setting Max", avatarGroup);
    }
}
