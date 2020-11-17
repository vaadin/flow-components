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

package com.vaadin.flow.component.combobox.test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("combobox-lazy-data-view-page")
public class ComboBoxLazyDataViewPage extends Div {

    public static final String COMBO_BOX_ID = "combo-box-get-items-page";
    public static final String ITEMS_LIST_ID = "items-list";
    public static final String GET_ITEMS_BUTTON_ID = "get-items-button";
    public static final String GET_ITEM_BUTTON_ID = "get-item-button";
    public static final String SWITCH_BUTTON_ID = "switch-to-unknown-button";

    public ComboBoxLazyDataViewPage() {

        ComboBox<String> comboBox = new ComboBox<>();
        ComboBoxLazyDataView<String> dataView = comboBox.setItems(
                query -> IntStream.range(0, 1000)
                        .mapToObj(index -> "Item " + index)
                        .filter(item -> item
                                .contains(query.getFilter().orElse("")))
                        .skip(query.getOffset()).limit(query.getLimit()),
                query -> (int) IntStream.range(0, 1000)
                        .mapToObj(index -> "Item " + index)
                        .filter(item -> item
                                .contains(query.getFilter().orElse("")))
                        .count());

        comboBox.setId(COMBO_BOX_ID);

        Span itemsList = new Span("Items shown here");
        itemsList.setId(ITEMS_LIST_ID);

        NativeButton getItemsButton = new NativeButton("Get Items", click -> {
            itemsList.setText(
                    dataView.getItems().collect(Collectors.joining(",")));
        });
        getItemsButton.setId(GET_ITEMS_BUTTON_ID);

        NativeButton getItemButton = new NativeButton("Get Item",
                click -> itemsList.setText(dataView.getItem(0)));
        getItemButton.setId(GET_ITEM_BUTTON_ID);

        NativeButton switchToUnknown = new NativeButton(
                "Switch To Unknown Item Count",
                click -> dataView.setItemCountUnknown());
        switchToUnknown.setId(SWITCH_BUTTON_ID);

        add(comboBox, switchToUnknown, getItemButton, getItemsButton,
                itemsList);
    }
}
