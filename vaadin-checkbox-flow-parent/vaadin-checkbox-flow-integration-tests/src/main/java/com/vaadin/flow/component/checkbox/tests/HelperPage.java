/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import java.util.Set;

@Route("vaadin-checkbox/helper")
public class HelperPage extends Div {

    public HelperPage() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>();
        Span span = new Span("Helper text");
        checkboxGroup.setHelperComponent(span);

        checkboxGroup.setItems("foo", "bar", "baz");
        Binder<Bean> binder = new Binder<>();
        binder.bind(checkboxGroup, bean -> bean.choices,
                (bean, value) -> bean.choices = value);
        binder.setBean(new Bean());

        Checkbox helperTextCheckbox = new Checkbox("Using helper text");
        helperTextCheckbox.setHelperText("Helper text");
        helperTextCheckbox.setId("checkbox-helper-text");

        NativeButton emptyHelperText = new NativeButton("Clear helper text",
                e -> helperTextCheckbox.setHelperText(""));
        emptyHelperText.setId("empty-helper-text");

        Checkbox helperComponentCheckbox = new Checkbox(
                "Using helper component");
        helperComponentCheckbox.setId("checkbox-helper-component");

        Span helper = new Span("Helper component");
        helper.setId("helper-component");
        helperComponentCheckbox.setHelperComponent(helper);

        NativeButton emptyHelperComponent = new NativeButton(
                "Clear helper component",
                e -> helperComponentCheckbox.setHelperComponent(null));
        emptyHelperComponent.setId("empty-helper-component");

        add(checkboxGroup, helperTextCheckbox, helperComponentCheckbox,
                emptyHelperText, emptyHelperComponent);
    }

    public static class Bean {
        private Set<String> choices;
    }
}
