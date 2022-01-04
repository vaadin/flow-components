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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.dataview.ComboBoxLazyDataView;
import com.vaadin.flow.component.combobox.dataview.ComboBoxListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clientside-filter")
public class ClientSideFilterPage extends Div {

    public static final String CLIENT_FILTER_COMBO_BOX_ITEM_COUNT_SPAN_ID = "options-combo-box-item-count-span";
    public static final String IN_MEMORY_COMBO_BOX_ITEM_COUNT_SPAN_ID = "in-memory-combo-box-item-count-span";
    public static final String BACKEND_COMBO_BOX_ITEM_COUNT_SPAN_ID = "backend-combo-box-item-count-span";

    public static final String OPTIONS_COMBO_BOX = "options-combo-box";
    public static final String CLIENT_FILTER_COMBO_BOX = "client-filter-combo-box";
    public static final String IN_MEMORY_COMBO_BOX = "in-memory-combo-box";
    public static final String BACKEND_COMBO_BOX = "backend-combo-box";

    public ClientSideFilterPage() {
        ComboBox<String> cb = new ComboBox<>("Choose option", "Option 2",
                "Option 3", "Option 4", "Option 5");
        cb.setId(OPTIONS_COMBO_BOX);
        this.add(cb);
        cb.focus();

        this.add(new Hr());

        createClientFilterComboBox();
        createInMemoryComboBox();
        createBackEndComboBox();
    }

    private void createBackEndComboBox() {
        ComboBox<String> backendComboBox = new ComboBox<>("Backend");
        backendComboBox.setId(BACKEND_COMBO_BOX);
        ComboBoxLazyDataView<String> lazyDataView = backendComboBox.setItems(
                query -> IntStream.range(0, 30)
                        .mapToObj(index -> "Item " + index)
                        .filter(item -> item
                                .contains(query.getFilter().orElse("")))
                        .skip(query.getOffset()).limit(query.getLimit()),
                query -> (int) IntStream.range(0, 30)
                        .mapToObj(index -> "Item " + index)
                        .filter(item -> item
                                .contains(query.getFilter().orElse("")))
                        .count());
        Span itemCountSpan = new Span("0");
        itemCountSpan.setId(BACKEND_COMBO_BOX_ITEM_COUNT_SPAN_ID);
        this.add(itemCountSpan);

        addListener(itemCountSpan, lazyDataView);
        this.add(backendComboBox);
    }

    private void createInMemoryComboBox() {
        ComboBox<String> inMemoryComboBox = new ComboBox<>("InMemory");
        inMemoryComboBox.setId(IN_MEMORY_COMBO_BOX);
        ComboBoxListDataView<String> listDataView = inMemoryComboBox
                .setItems(IntStream.range(0, inMemoryComboBox.getPageSize() * 2)
                        .mapToObj(i -> "Item " + i)
                        .collect(Collectors.toList()));
        Span itemCountSpan = new Span("0");
        itemCountSpan.setId(IN_MEMORY_COMBO_BOX_ITEM_COUNT_SPAN_ID);
        this.add(itemCountSpan);

        addListener(itemCountSpan, listDataView);
        this.add(inMemoryComboBox);
    }

    private void createClientFilterComboBox() {
        ComboBox<String> clientFilterComboBox = new ComboBox<>("Browsers");
        clientFilterComboBox.setId(CLIENT_FILTER_COMBO_BOX);
        ComboBoxListDataView<String> clientFilterDataView = clientFilterComboBox
                .setItems("Google Chrome", "Mozilla Firefox", "Opera",
                        "Apple Safari", "Microsoft Edge");

        Span itemCountSpan = new Span("0");
        itemCountSpan.setId(CLIENT_FILTER_COMBO_BOX_ITEM_COUNT_SPAN_ID);
        this.add(itemCountSpan);

        addListener(itemCountSpan, clientFilterDataView);
        this.add(clientFilterComboBox);
    }

    private void addListener(Span itemCountSpan, DataView<String> dataView) {
        dataView.addItemCountChangeListener(event -> itemCountSpan
                .setText(String.valueOf(event.getItemCount())));
    }
}
