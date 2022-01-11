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
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clear-items")
public class ClearItemsPage extends Div {

    public ClearItemsPage() {
        ArrayList<String> items = new ArrayList<>();
        items.add("foo");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(items);

        NativeButton setEmptyDataProvider = new NativeButton(
                "Set empty data provider", e -> comboBox.setItems());
        setEmptyDataProvider.setId("set-empty-data-provider");

        NativeButton clearAndRefreshDataProvider = new NativeButton(
                "Clear and refresh data provider", e -> {
                    items.clear();
                    comboBox.getDataProvider().refreshAll();
                });
        clearAndRefreshDataProvider.setId("clear-and-refresh-data-provider");

        add(comboBox, setEmptyDataProvider, clearAndRefreshDataProvider);
    }
}
