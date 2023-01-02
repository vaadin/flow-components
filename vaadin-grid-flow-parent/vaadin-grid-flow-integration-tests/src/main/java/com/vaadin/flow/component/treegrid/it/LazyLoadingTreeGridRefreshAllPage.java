/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@Route("vaadin-grid/lazy-loading-treegrid-refreshall")
public class LazyLoadingTreeGridRefreshAllPage extends Div {

    private TreeGrid<String> treeGrid = new TreeGrid<>();

    private IntegerField childCount = new IntegerField("Child count");

    private Button refreshAll = new Button("Refresh all",
            e -> treeGrid.getDataProvider().refreshAll());

    public LazyLoadingTreeGridRefreshAllPage() {
        childCount.setId("child-count");
        childCount.setValue(500);
        refreshAll.setId("refresh-all");
        add(childCount, refreshAll);

        treeGrid.setDataProvider(new LazyLoadingProvider());
        treeGrid.addHierarchyColumn(ValueProvider.identity()).setHeader("Name");
        add(treeGrid);
    }

    private class LazyLoadingProvider
            extends AbstractHierarchicalDataProvider<String, Void> {

        @Override
        public int getChildCount(HierarchicalQuery<String, Void> query) {
            return childCount.getValue();
        }

        @Override
        public Stream<String> fetchChildren(
                HierarchicalQuery<String, Void> query) {
            int limit = query.getLimit();
            int offset = query.getOffset();
            return IntStream.range(offset, offset + limit)
                    .mapToObj(index -> "Item " + index);
        }

        @Override
        public boolean hasChildren(String item) {
            return false;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }
    }
}
