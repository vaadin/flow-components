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
import com.vaadin.flow.component.combobox.MultiSelectComboBoxI18n;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/i18n")
public class MultiSelectComboBoxI18nPage extends Div {
    public MultiSelectComboBoxI18nPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        NativeButton toggleAttached = new NativeButton("Toggle attached", e -> {
            if (comboBox.getParent().isPresent()) {
                remove(comboBox);
            } else {
                add(comboBox);
            }
        });
        toggleAttached.setId("toggle-attached");

        NativeButton setI18n = new NativeButton("Set I18N", e -> {
            MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n()
                    .setCleared("Custom cleared").setFocused("Custom focused")
                    .setSelected("Custom selected")
                    .setDeselected("Custom deselected")
                    .setTotal("{count} Custom total");
            comboBox.setI18n(i18n);
        });
        setI18n.setId("set-i18n");

        NativeButton setEmptyI18n = new NativeButton("Set empty I18N", e -> {
            MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n();
            comboBox.setI18n(i18n);
        });
        setEmptyI18n.setId("set-empty-i18n");

        add(comboBox);
        add(new Div(toggleAttached, setI18n, setEmptyI18n));
    }
}
