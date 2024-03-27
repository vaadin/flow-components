/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.treegrid.it;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/lazy-loading-treegrid")
public class LazyLoadingTreeGridView extends Div {

    private static class LazyLoadingProvider
            extends AbstractHierarchicalDataProvider<String, Void> {

        @Override
        public int getChildCount(HierarchicalQuery<String, Void> query) {
            /*
             * TODO : SHOULD this method be removed or implemented somehow in
             * the super class by default
             */
            // Do we support indefinite children size ?
            return Integer.MAX_VALUE;
        }

        @Override
        public Stream<String> fetchChildren(
                HierarchicalQuery<String, Void> query) {
            String parent = query.getParent();
            int limit = query.getLimit();
            int offset = query.getOffset();
            return IntStream.range(offset, offset + limit)
                    .mapToObj(index -> "Child of " + parent + " " + index);
        }

        @Override
        public boolean hasChildren(String item) {
            // indefinite (infinitive) hierarchy: every item has a child
            // Do we support this already now ?
            return true;
        }

        @Override
        public boolean isInMemory() {
            return true;
        }

    }

    public LazyLoadingTreeGridView() {
        TreeGrid<String> treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(new LazyLoadingProvider());

        treeGrid.addHierarchyColumn(ValueProvider.identity()).setHeader("Name");

    }
}
