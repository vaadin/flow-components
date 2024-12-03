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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.component.virtuallist.VirtualListSingleSelectionModel;
import com.vaadin.flow.component.virtuallist.VirtualList.SelectionMode;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link VirtualList}
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-virtual-list/virtual-list-selection")
public class VirtualListSelectionPage extends Div {

    public VirtualListSelectionPage() {
        var list = new VirtualList<Item>();
        list.setHeight("200px");

        var items = createItems();
        list.setDataProvider(DataProvider.ofCollection(items));

        list.setRenderer(LitRenderer.<Item> of("<div>${item.name}</div>")
                .withProperty("name", item -> item.name));

        var model = (VirtualListSingleSelectionModel)list.setSelectionMode(SelectionMode.SINGLE);
        model.setDeselectAllowed(false);

        list.select(items.get(0));

        list.addSelectionListener(e -> {
            System.out.println("Selected items: " + e.getAllSelectedItems());
        });

        list.setItemAccessibleNameGenerator(item -> "Accessible " + item.name);

        add(list);

        var button = new NativeButton("Select second item", e -> {
            list.select(items.get(1));
        });
        add(button);

        var deselectAll = new NativeButton("Deselect all", e -> {
            list.deselectAll();
        });
        add(deselectAll);

        var printSelectionButton = new NativeButton("Print selection", e -> {
            System.out.println("Selected item: "
                    + list.getSelectionModel().getSelectedItems());
        });
        add(printSelectionButton);

    }

    private List<Item> createItems() {
        return IntStream.range(0, 1000).mapToObj(i -> new Item("Item " + i, i))
                .collect(Collectors.toList());
    }

    public static record Item(String name, int value) {
    };

}
