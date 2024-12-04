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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.component.virtuallist.VirtualList.SelectionMode;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link VirtualList}
 *
 * @author Vaadin Ltd.
 */
@Route("vaadin-virtual-list/selection")
public class VirtualListSelectionPage extends Div {

    public VirtualListSelectionPage() {
        var list = new VirtualList<Item>();
        list.setHeight("200px");

        var items = createItems();
        list.setDataProvider(DataProvider.ofCollection(items));

        list.setRenderer(LitRenderer.<Item> of("<div>${item.name}</div>")
                .withProperty("name", item -> item.name));

        list.setItemAccessibleNameGenerator(item -> "Accessible " + item.name);

        add(list);

        var selectedIndexes = new Div();
        selectedIndexes.setHeight("30px");
        selectedIndexes.setId("selected-indexes");
        SelectionListener<VirtualList<Item>, Item> selectionListener = event -> {
            selectedIndexes.setText(event.getAllSelectedItems().stream()
                    .map(item -> String.valueOf(items.indexOf(item)))
                    .collect(Collectors.joining(", ")));
        };

        add(new Div(new H2("Selected item indexes"), selectedIndexes));

        var selectFirstButton = new NativeButton("Select first item", e -> {
            list.select(items.get(0));
        });
        selectFirstButton.setId("select-first");

        var deselectAllButton = new NativeButton("Deselect all", e -> {
            list.deselectAll();
        });
        deselectAllButton.setId("deselect-all");

        add(new Div(new H2("Actions"), selectFirstButton, deselectAllButton));

        var noneSelectionModeButton = new NativeButton("None", e -> {
            list.setSelectionMode(SelectionMode.NONE);
        });
        noneSelectionModeButton.setId("none-selection-mode");

        var singleSelectionModeButton = new NativeButton("Single", e -> {
            list.setSelectionMode(SelectionMode.SINGLE);
            list.addSelectionListener(selectionListener);
        });
        singleSelectionModeButton.setId("single-selection-mode");

        var singleSelectionModeDeselectionDisallowedButton = new NativeButton(
                "Single (deselection disallowed)", e -> {
                    var model = list.setSelectionMode(SelectionMode.SINGLE);
                    ((SelectionModel.Single<VirtualList<Item>, Item>) model)
                            .setDeselectAllowed(false);
                    list.addSelectionListener(selectionListener);
                });
        singleSelectionModeDeselectionDisallowedButton
                .setId("single-selection-mode-deselection-disallowed");

        var multiSelectionModeButton = new NativeButton("Multi", e -> {
            list.setSelectionMode(SelectionMode.MULTI);
            list.addSelectionListener(selectionListener);
        });
        multiSelectionModeButton.setId("multi-selection-mode");

        add(new Div(new H2("Selection mode"), noneSelectionModeButton,
                singleSelectionModeButton,
                singleSelectionModeDeselectionDisallowedButton,
                multiSelectionModeButton));

    }

    private List<Item> createItems() {
        return IntStream.range(0, 1000).mapToObj(i -> new Item("Item " + i, i))
                .collect(Collectors.toList());
    }

    public static record Item(String name, int value) {
    };

}
