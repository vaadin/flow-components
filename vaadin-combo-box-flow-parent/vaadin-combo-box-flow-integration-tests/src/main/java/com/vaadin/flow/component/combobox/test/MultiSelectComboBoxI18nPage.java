/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
