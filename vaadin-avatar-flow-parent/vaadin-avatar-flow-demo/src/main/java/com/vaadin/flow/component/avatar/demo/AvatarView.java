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
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.component.checkbox.CheckboxGroup;

import java.util.Collections;

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
        createAvatarWithCombinedProperties();
    }

    private void createBasicAvatar() {
        // begin-source-example
        // source-example-heading: Basic usage
        Avatar anonymousAvatar = new Avatar();

        Avatar avatarWithAbbr = new Avatar();
        avatarWithAbbr.setAbbreviation("YY");

        Avatar avatarWithName = new Avatar();
        avatarWithName.setName("Yuriy Yevstihnyeyev");

        Avatar avatarWithImg = new Avatar();
        avatarWithImg.setImage("https://vaadin.com/static/content/view/company/team/photos/Yuriy-Yevstihnyeyev.JPG");

        add(anonymousAvatar, avatarWithAbbr, avatarWithName, avatarWithImg);
        // end-source-example
        Div container = new Div(anonymousAvatar, avatarWithAbbr, avatarWithName, avatarWithImg);

        addCard("Basic usage", container);
    }

    private void createAvatarWithCombinedProperties() {
        // begin-source-example
        // source-example-heading: Combined properties
        Avatar avatar = new Avatar();
        avatar.setImage("https://vaadin.com/static/content/view/company/team/photos/Yuriy-Yevstihnyeyev.JPG");

        add(avatar);
        // end-source-example

        CheckboxGroup checkboxGroup = new CheckboxGroup();
        checkboxGroup.setLabel("Set avatar's properties");
        checkboxGroup.setItems("setImage(\"photos/Yuriy-Yevstihnyeyev.JPG\")", "setName(\"Serhii Kulykov\")", "setAbbreviation(\"YY\")");
        checkboxGroup.setValue(Collections.singleton("setImage(\"photos/Yuriy-Yevstihnyeyev.JPG\")"));
        checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        checkboxGroup.addValueChangeListener(e -> {
            String valueString = e.getValue().toString();

            if (valueString.contains("setImage")) {
                avatar.setImage("https://vaadin.com/static/content/view/company/team/photos/Yuriy-Yevstihnyeyev.JPG");
            } else {
                avatar.setImage(null);
            }

            if (valueString.contains("setName")) {
                avatar.setName("Serhii Kulykov");
            } else {
                avatar.setName(null);
            }

            if (valueString.contains("setAbbreviation")) {
                avatar.setAbbreviation("YY");
            } else {
                avatar.setAbbreviation(null);
            }
        });

        addCard("Combined properties", avatar, checkboxGroup);
    }
}
