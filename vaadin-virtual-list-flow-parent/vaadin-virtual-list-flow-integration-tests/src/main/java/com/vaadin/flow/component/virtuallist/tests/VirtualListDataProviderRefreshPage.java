package com.vaadin.flow.component.virtuallist.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

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
