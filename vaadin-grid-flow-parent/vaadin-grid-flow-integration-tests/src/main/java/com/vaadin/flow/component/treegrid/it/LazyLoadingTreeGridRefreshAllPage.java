/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
