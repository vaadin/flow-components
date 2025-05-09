/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/item-class-name")
public class MultiSelectComboBoxItemClassNamePage extends Div {

    public MultiSelectComboBoxItemClassNamePage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems("foo", "bar", "baz");

        // Make component wider, so that we can fit multiple chips
        comboBox.setWidth("300px");

        NativeButton setClassNameGenerator = new NativeButton(
                "Set class name generator", e -> {
                    comboBox.setClassNameGenerator(item -> "item-" + item);
                });
        setClassNameGenerator.setId("set-generator");

        NativeButton resetClassNameGenerator = new NativeButton(
                "Reset class name generator", e -> {
                    comboBox.setClassNameGenerator(item -> null);
                });
        resetClassNameGenerator.setId("reset-generator");

        NativeButton setValue = new NativeButton("Set value", e -> {
            comboBox.select("foo", "bar");
        });
        setValue.setId("set-value");

        add(comboBox, setClassNameGenerator, resetClassNameGenerator, setValue);
    }
}
