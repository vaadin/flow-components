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
package com.vaadin.flow.component.treegrid.it;

import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-changing-hierarchy")
public class TreeGridChangingHierarchyPage extends Div {

    private static class TestDataProvider extends TreeDataProvider<String> {

        private TreeData<String> treeData;

        public TestDataProvider(TreeData<String> treeData) {
            super(treeData);
            this.treeData = treeData;
        }

        @Override
        public boolean hasChildren(String item) {
            if (!treeData.contains(item)) {
                return false;
            }
            return super.hasChildren(item);
        }

        @Override
        public Stream<String> fetchChildren(
                HierarchicalQuery<String, SerializablePredicate<String>> query) {
            if (!treeData.contains(query.getParent())) {
                return Stream.empty();
            }
            return super.fetchChildren(query);
        }
    }

    public TreeGridChangingHierarchyPage() {
        TreeData<String> data = new TreeData<>();
        data.addItems(null, "a", "b", "c").addItem("b", "b/a");

        TreeGrid<String> grid = new TreeGrid<>();
        grid.setDataProvider(new TestDataProvider(data));
        grid.addHierarchyColumn(ValueProvider.identity());

        NativeButton btn = new NativeButton("add items to a and refresh");
        btn.addClickListener(event -> {
            data.addItems("a", "a/a", "a/b");
            grid.getDataProvider().refreshItem("a");
        });
        NativeButton btn2 = new NativeButton("add items to a/a and refresh");
        btn2.addClickListener(event -> {
            data.addItems("a/a", "a/a/a", "a/a/c").addItem("a/a/a", "a/a/a/a");
            grid.getDataProvider().refreshItem("a/a");
        });
        NativeButton btn3 = new NativeButton("remove a/a");
        btn3.addClickListener(event -> {
            data.removeItem("a/a");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn4 = new NativeButton("remove children of a/a");
        btn4.addClickListener(event -> {
            data.removeItem("a/a/a");
            data.removeItem("a/a/c");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn5 = new NativeButton("remove a");
        btn5.addClickListener(event -> {
            data.removeItem("a");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn6 = new NativeButton("remove children of a");
        btn6.addClickListener(event -> {
            data.removeItem("a/a");
            data.removeItem("a/b");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });
        NativeButton btn7 = new NativeButton("remove children of a/a/a");
        btn7.addClickListener(event -> {
            data.removeItem("a/a/a/a");
            // All the children of an item are removed so it should be collapsed
            // (otherwise TreeGrid will get stuck in loading state)
            grid.collapse("a/a/a");
            // Inform item removal to DataProvider
            grid.getDataProvider().refreshAll();
        });

        add(grid, btn, btn2, btn3, btn4, btn5, btn6, btn7);
    }
}
