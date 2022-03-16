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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.flow.data.bean.HierarchicalTestBean;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class LazyHierarchicalDataProvider extends
        AbstractBackEndHierarchicalDataProvider<HierarchicalTestBean, Void> {

    private final int nodesPerLevel;
    private final int depth;

    private boolean cleared;

    public LazyHierarchicalDataProvider(int nodesPerLevel, int depth) {
        this.nodesPerLevel = nodesPerLevel;
        this.depth = depth;
    }

    @Override
    public int getChildCount(
            HierarchicalQuery<HierarchicalTestBean, Void> query) {
        if (cleared) {
            return 0;
        }

        Optional<Integer> count = query.getParentOptional()
                .flatMap(parent -> Optional.of(Integer.valueOf(
                        (internalHasChildren(parent) ? nodesPerLevel : 0))));

        return count.orElse(nodesPerLevel);
    }

    @Override
    public boolean hasChildren(HierarchicalTestBean item) {
        return internalHasChildren(item);
    }

    private boolean internalHasChildren(HierarchicalTestBean node) {
        return node.getDepth() < depth;
    }

    public void clear() {
        this.cleared = true;
    }

    @Override
    protected Stream<HierarchicalTestBean> fetchChildrenFromBackEnd(
            HierarchicalQuery<HierarchicalTestBean, Void> query) {
        final int depth = query.getParentOptional().isPresent()
                ? query.getParent().getDepth() + 1
                : 0;
        final Optional<String> parentKey = query.getParentOptional()
                .flatMap(parent -> Optional.of(parent.getId()));

        List<HierarchicalTestBean> list = new ArrayList<>();
        int limit = Math.min(query.getLimit(), nodesPerLevel);
        for (int i = 0; i < limit; i++) {
            list.add(new HierarchicalTestBean(parentKey.orElse(null), depth,
                    i + query.getOffset()));
        }

        query.getSortingComparator().ifPresent(sorting -> {
            list.sort(sorting);
        });
        return list.stream();
    }

    @Override
    public Object getId(HierarchicalTestBean item) {
        Objects.requireNonNull(item, "Cannot provide an id for a null item.");
        return item.getId();
    }
}
