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

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Avatar} demo.
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-avatar")
public class AvatarView extends DemoView {

    @Override
    public void initView() {
        createBasicAvatar();
    }

    private void createBasicAvatar() {
        // begin-source-example
        // source-example-heading: Basic avatar
        Avatar avatar = new Avatar();

        add(avatar);
        // end-source-example

        avatar.setId("basic-tabs");
        addCard("Horizontal tabs", avatar);
    }

}
