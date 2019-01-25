/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("radio-button-group-required-binder")
public class RequiredValidationPage extends Div {

    public RequiredValidationPage() {
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("male", "female", "unknown");
        group.setLabel("Gender");

        Entity entity = new Entity();
        Binder<Entity> binder = new Binder<>(Entity.class);
        binder.forField(group).bind("gender");

        group.setId("gender");

        binder.setBean(entity);

        add(group);

        NativeButton off = new NativeButton(
                "Make required indicator invisible and set requied", event -> {
                    group.setRequiredIndicatorVisible(false);
                    group.setRequired(true);
                });
        off.setId("hide");
        add(off);
    }
}
