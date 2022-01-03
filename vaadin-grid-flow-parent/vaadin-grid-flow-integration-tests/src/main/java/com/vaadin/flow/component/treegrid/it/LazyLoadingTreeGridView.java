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
