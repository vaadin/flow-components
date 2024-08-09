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
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("helper-text")
public class HelperTextPage extends Div {

    public HelperTextPage() {
        ArrayList<String> items = new ArrayList<>();
        items.add("foo");

        ComboBox<String> helperTextComboBox = new ComboBox<>();
        helperTextComboBox.setItems(items);
        helperTextComboBox.setHelperText("Helper text");
        helperTextComboBox.setId("combobox-helper-text");

        NativeButton emptyHelperText = new NativeButton("Clear helper text",
                e -> helperTextComboBox.setHelperText(""));
        emptyHelperText.setId("empty-helper-text");

        ComboBox<String> helperComponentCombobox = new ComboBox<>();
        helperComponentCombobox.setItems(items);
        helperComponentCombobox.setId("combobox-helper-component");

        Span span = new Span("Helper Component");
        span.setId("helper-component");
        helperComponentCombobox.setHelperComponent(span);

        NativeButton emptyHelperComponent = new NativeButton(
                "Clear helper component",
                e -> helperComponentCombobox.setHelperComponent(null));
        emptyHelperComponent.setId("empty-helper-component");

        add(helperTextComboBox, helperComponentCombobox, emptyHelperText,
                emptyHelperComponent);
    }
}
