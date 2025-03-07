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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.PersonWithLevel;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.FlatHierarchyDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/tree-grid-basic")
public class TreeGridBasicPage extends Div {
    private List<Item> items = buildItemsHierarchy();

    private class DataProvider extends AbstractBackEndHierarchicalDataProvider<Item, String> {
        @Override
        public int getChildCount(HierarchicalQuery<Item, String> query) {
            return query.getParent() == null ? items.size() : query.getParent().getChildItems().size();
        }

        @Override
        public boolean hasChildren(Item item) {
            return item.getChildItems() != null && !item.getChildItems().isEmpty();
        }

        @Override
        public Stream<Item> fetchChildrenFromBackEnd(HierarchicalQuery<Item, String> query) {
            return query.getParent() == null ? items.stream() : query.getParent().getChildItems().stream();
        }
    }

    private class Item {
        private String name;

        private List<Item> childItems;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Item> getChildItems() {
            return childItems;
        }

        public void setChildItems(List<Item> childItems) {
            this.childItems = childItems;
        }
    }

    public TreeGridBasicPage() {
        TreeGrid<Item> grid = new TreeGrid<>();
        grid.addHierarchyColumn(person -> person.getFirstName())
                .setHeader("First name");
        grid.setDataProvider(new DataProvider());

        // NativeButton expandAll = new NativeButton("Expand all",
        //         e -> grid.expandRecursively(data.getRootItems(), 3));
        // expandAll.setId("expand-all");

        // NativeButton refreshItem = new NativeButton("Refresh item", e -> {
        //     PersonWithLevel person = data.getChildren(people.get(0)).get(1);
        //     person.setFirstName("Updated");
        //     grid.getDataProvider().refreshItem(person);
        // });

        add(grid);
    }

    private List<Item> getFlatItems() {
        return items.stream()
            .flatMap(item -> expandedItems.contains(item)
                ? Stream.concat(Stream.of(item), item.childItems().stream())
                : Stream.of(item))
            .toList();
    }

    private List<Item> buildItemsHierarchy() {
        return IntStream.range(0, 100)
            .mapToObj(i -> new Item("Item " + i,
                IntStream.range(0, 50)
                    .mapToObj(j -> new Item("Child Item " + i + "-" + j, null))
                    .toList()))
            .toList();
    }

}
