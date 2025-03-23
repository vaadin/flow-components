/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.FlatHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/tree-grid-basic")
public class TreeGridBasicPage extends Div {
    private class DataProvider
            extends AbstractHierarchicalDataProvider<Item, String>
            implements FlatHierarchicalDataProvider<Item, String> {
        private List<Item> items = buildItemsHierarchy();

        @Override
        public int getChildCount(HierarchicalQuery<Item, String> query) {
            Set<Object> expandedItemIds = query.getExpandedItemIds();
            return getFlatItems(expandedItemIds).size();
        }

        @Override
        public Stream<Item> fetchChildren(
                HierarchicalQuery<Item, String> query) {
            int offset = query.getOffset();
            int limit = query.getLimit();
            Set<Object> expandedItemIds = query.getExpandedItemIds();
            return getFlatItems(expandedItemIds).subList(offset, offset + limit)
                    .stream();
        }

        @Override
        public boolean hasChildren(Item item) {
            return item.getChildItems() != null
                    && !item.getChildItems().isEmpty();
        }

        @Override
        public Item getParentItem(Item item) {
            return item.getParentItem();
        }

        @Override
        public int getDepth(Item item) {
            return item.getDepth();
        }

        @Override
        public Object getId(Item item) {
            return item.getName();
        }

        @Override
        public boolean isInMemory() {
            return false;
        }

        private List<Item> getFlatItems(Set<Object> expandedItemIds) {
            return items.stream()
                    .flatMap(item -> expandedItemIds.contains(getId(item))
                            ? Stream.concat(Stream.of(item),
                                    item.getChildItems().stream())
                            : Stream.of(item))
                    .toList();
        }

        private List<Item> buildItemsHierarchy() {
            return IntStream.range(0, 100).mapToObj(i -> {
                Item item = new Item("Item " + i);
                item.setChildItems(IntStream.range(0, 50).mapToObj(
                        j -> new Item(item, "Child Item " + i + "-" + j))
                        .toList());
                return item;
            }).toList();
        }
    }

    private class Item {
        private String name;
        private Item parentItem;
        private List<Item> childItems;

        public Item(String name) {
            this.name = name;
        }

        public Item(Item parentItem, String name) {
            this.name = name;
            this.parentItem = parentItem;
        }

        public String getName() {
            return name;
        }

        public int getDepth() {
            return parentItem == null ? 0 : parentItem.getDepth() + 1;
        }

        public Item getParentItem() {
            return parentItem;
        }

        public void setChildItems(List<Item> childItems) {
            this.childItems = childItems;
        }

        public List<Item> getChildItems() {
            return childItems;
        }
    }

    public TreeGridBasicPage() {
        TreeGrid<Item> grid = new TreeGrid<>();
        grid.addHierarchyColumn(item -> item.getName()).setHeader("Name");
        grid.setDataProvider(new DataProvider());

        add(grid);
    }

}
