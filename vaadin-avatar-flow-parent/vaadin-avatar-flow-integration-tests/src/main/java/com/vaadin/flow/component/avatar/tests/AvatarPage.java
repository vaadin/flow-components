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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@Route("vaadin-avatar/avatar-test")
public class AvatarPage extends Div {

    public AvatarPage() {
        Avatar avatar = new Avatar();

        NativeButton toggleImage = new NativeButton("Toggle img", e -> {
            if (avatar.getImage() == null || avatar.getImage().isEmpty()) {
                avatar.setImage("https://vaadin.com/");
            } else {
                avatar.setImage(null);
            }
        });
        toggleImage.setId("toggle-img");

        NativeButton toggleAbbr = new NativeButton("Toggle abbr", e -> {
            if (avatar.getAbbreviation() == null
                    || avatar.getAbbreviation().isEmpty()) {
                avatar.setAbbreviation("BB");
            } else {
                avatar.setAbbreviation(null);
            }
        });
        toggleAbbr.setId("toggle-abbr");

        NativeButton toggleName = new NativeButton("Toggle name", e -> {
            if (avatar.getName() == null || avatar.getName().isEmpty()) {
                avatar.setName("Foo Bar");
            } else {
                avatar.setName(null);
            }
        });
        toggleName.setId("toggle-name");

        NativeButton toggleImgResource = new NativeButton(
                "Toggle image resource", e -> {
                    if (avatar.getImageResource() == null) {
                        StreamResource avatarResource = new StreamResource(
                                "user+.png",
                                () -> getClass().getResourceAsStream(
                                        "/META-INF/resources/frontend/images/user.png"));
                        avatar.setImageResource(avatarResource);
                    } else {
                        avatar.setImageResource(null);
                    }
                });
        toggleImgResource.setId("toggle-res");

        Div dataImg = new Div();
        dataImg.setId("data-block-img");

        Div dataAbbr = new Div();
        dataAbbr.setId("data-block-abbr");

        Div dataName = new Div();
        dataName.setId("data-block-name");

        Div dataResource = new Div();
        dataResource.setId("data-block-resource");

        NativeButton getPropertyValues = new NativeButton("Get properties",
                e -> {
                    dataImg.setText(avatar.getElement().getAttribute("img"));
                    dataAbbr.setText(avatar.getElement().getProperty("abbr"));
                    dataName.setText(avatar.getElement().getProperty("name"));
                    dataResource
                            .setText(avatar.getElement().getAttribute("img"));
                });
        getPropertyValues.setId("get-props");

        add(avatar, toggleImage, toggleAbbr, toggleName, toggleImgResource,
                dataImg, dataAbbr, dataName, dataResource, getPropertyValues);
    }
}
