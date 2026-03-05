/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;

@Route("vaadin-virtual-list/virtual-list-bind-items")
public class VirtualListBindItemsPage extends Div {

    public static final String VIRTUAL_LIST_ID = "test-virtual-list";
    public static final String ADD_ITEM_BUTTON = "add-item-button";
    public static final String REMOVE_ITEM_BUTTON = "remove-item-button";
    public static final String UPDATE_ITEM_BUTTON = "update-item-button";
    public static final String ITEM_COUNT_SPAN = "item-count";

    private final ListSignal<String> itemsSignal;

    public VirtualListBindItemsPage() {
        // Create list signal and add initial items
        itemsSignal = new ListSignal<>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        itemsSignal.insertLast("Item 3");

        // Create and configure virtual list
        VirtualList<String> virtualList = new VirtualList<>();
        virtualList.setId(VIRTUAL_LIST_ID);
        virtualList.setHeight("200px");
        virtualList.setWidth("300px");
        virtualList.bindItems(itemsSignal);
//        virtualList.setItems("one", "two", "three");



        // Item count display
        Span itemCount = new Span(String.valueOf(itemsSignal.peek().size()));
        itemCount.setId(ITEM_COUNT_SPAN);

        // Button to add a new item
        NativeButton addItemButton = new NativeButton("Add Item", event -> {
            int currentSize = itemsSignal.peek().size();
            itemsSignal.insertLast("Item " + (currentSize + 1));

            // Update display
            itemCount.setText(String.valueOf(itemsSignal.peek().size()));
        });
        addItemButton.setId(ADD_ITEM_BUTTON);

        // Button to remove the last item
        NativeButton removeItemButton = new NativeButton("Remove Last Item",
                event -> {
                    var currentItems = itemsSignal.peek();
                    if (!currentItems.isEmpty()) {
                        itemsSignal.remove(currentItems.getLast());

                        // Update display
                        itemCount.setText(
                                String.valueOf(itemsSignal.peek().size()));
                    }
                });
        removeItemButton.setId(REMOVE_ITEM_BUTTON);

        // Button to update the first item
        NativeButton updateItemButton = new NativeButton("Update First Item",
                event -> {
                    var currentItems = itemsSignal.peek();
                    if (!currentItems.isEmpty()) {
                        ValueSignal<String> firstItem = currentItems.getFirst();
                        firstItem.update(value -> value + " Updated");
                    }
                });
        updateItemButton.setId(UPDATE_ITEM_BUTTON);

        add(virtualList, new Div(new Span("Item Count: "), itemCount),
                addItemButton, removeItemButton, updateItemButton);
    }
}
