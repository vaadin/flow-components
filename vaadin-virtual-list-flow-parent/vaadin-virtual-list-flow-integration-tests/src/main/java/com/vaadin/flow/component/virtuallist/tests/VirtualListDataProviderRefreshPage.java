/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.virtuallist.tests;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-virtual-list/data-provider-refresh")
public class VirtualListDataProviderRefreshPage extends Div {

    public VirtualListDataProviderRefreshPage() {
        VirtualList<Item> virtualList = new VirtualList<>();
        List<Item> items = createItems();
        ListDataProvider<Item> dataProvider = new ListDataProvider<>(items);
        virtualList.setDataProvider(dataProvider);
        virtualList.setRenderer(new TextRenderer<>(Item::getName));

        NativeButton updateItemFive = new NativeButton("Update 5th item", e -> {
            Item item = items.get(4);
            item.setName(item.getName() + " updated");
            dataProvider.refreshItem(item);
        });
        updateItemFive.setId("update-item-five");

        NativeButton updateAllItems = new NativeButton("Update all items",
                e -> {
                    items.forEach(item -> {
                        item.setName(item.getName() + " updated");
                    });
                    dataProvider.refreshAll();
                });
        updateAllItems.setId("update-all-items");

        add(virtualList);
        add(new Div(updateItemFive, updateAllItems));
    }

    private List<Item> createItems() {
        final int numberToGenerate = 200;
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < numberToGenerate; i++) {
            items.add(new Item("Item" + (i + 1)));
        }

        return items;
    }

    private static class Item {
        private String name;

        public Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
