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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button/radio-button-group-required-binder")
public class RequiredValidationPage extends Div {

    public RequiredValidationPage() {
        final RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("male", "female", "unknown");
        group.setLabel("Gender");

        Entity entity = new Entity();
        Binder<Entity> binder = new Binder<>(Entity.class);
        Binding<Entity, String> nonRequiredBinding = binder.forField(group)
                .bind("gender");

        group.setId("gender");

        binder.setBean(entity);

        add(group);

        NativeButton off = new NativeButton("Make required and validate",
                event -> {
                    nonRequiredBinding.unbind();
                    binder.forField(group).asRequired("required")
                            .bind("gender");
                    binder.validate();
                });
        off.setId("hide");
        add(off);

        RadioButtonGroup<String> radioGroupWithInvalidOption = new RadioButtonGroup<>();
        radioGroupWithInvalidOption.setId("radio-button-with-invalid-option");
        radioGroupWithInvalidOption.setItems("valid 1", "valid 2", "invalid");
        Binder<Entity> binderForInvalidOption = new Binder<>(Entity.class);
        binderForInvalidOption.forField(radioGroupWithInvalidOption)
                .withValidator(value -> !"invalid".equals(value),
                        "Value is invalid")
                .bind("gender");
        add(radioGroupWithInvalidOption);

        RadioButtonGroup<String> radioGroupInvalidOnAttach = new RadioButtonGroup<>();
        radioGroupInvalidOnAttach.setId("radio-button-invalid-on-attach");
        radioGroupInvalidOnAttach.setItems("valid 1", "valid 2", "invalid");
        Binder<Entity> binderForInvalidOnAttach = new Binder<>(Entity.class);
        binderForInvalidOnAttach.forField(radioGroupInvalidOnAttach)
                .withValidator(value -> !"invalid".equals(value),
                        "Value is invalid")
                .bind("gender");
        Entity invalidBean = new Entity();
        invalidBean.setGender("invalid");
        binderForInvalidOnAttach.setBean(invalidBean);
        binderForInvalidOnAttach.validate();
        add(radioGroupInvalidOnAttach);
    }
}
