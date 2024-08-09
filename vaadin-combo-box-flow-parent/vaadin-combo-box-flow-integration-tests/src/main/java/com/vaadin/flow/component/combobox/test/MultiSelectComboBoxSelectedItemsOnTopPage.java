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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-multi-select-combo-box/selected-items-on-top")
public class MultiSelectComboBoxSelectedItemsOnTopPage extends Div {
    public MultiSelectComboBoxSelectedItemsOnTopPage() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Items");
        List<String> items = IntStream.range(0, 100)
                .mapToObj(i -> "Item " + (i + 1)).collect(Collectors.toList());
        comboBox.setItems(items);

        NativeButton setSelectedOnTop = new NativeButton("Set selected on top",
                e -> {
                    comboBox.setSelectedItemsOnTop(true);
                });
        setSelectedOnTop.setId("set-selected-on-top");

        NativeButton unsetSelectedOnTop = new NativeButton(
                "Un-set selected on top", e -> {
                    comboBox.setSelectedItemsOnTop(false);
                });
        unsetSelectedOnTop.setId("unset-selected-on-top");

        NativeButton setComponentRenderer = new NativeButton(
                "Set component renderer", e -> comboBox.setRenderer(
                        new ComponentRenderer<>(i -> new Span(i))));
        setComponentRenderer.setId("set-component-renderer");

        NativeButton useCustomValueSetListener = new NativeButton(
                "Use custom value set listener", click -> {
                    comboBox.setItems(query -> comboBox.getValue().stream()
                            .skip(query.getOffset()).limit(query.getLimit()));
                    comboBox.addCustomValueSetListener(event -> {
                        String value = event.getDetail();
                        if (value != null) {
                            List<String> accounts = new ArrayList<>(
                                    comboBox.getValue());
                            accounts.add(value);
                            comboBox.setValue(accounts);
                        }
                    });
                });
        useCustomValueSetListener.setId("use-custom-value-set-listener");

        add(comboBox);
        add(new Div(setSelectedOnTop, unsetSelectedOnTop, setComponentRenderer,
                useCustomValueSetListener));
    }
}
