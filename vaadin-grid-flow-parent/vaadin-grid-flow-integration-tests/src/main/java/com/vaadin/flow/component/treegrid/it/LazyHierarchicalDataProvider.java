/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
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
        return list.stream();
    }

    @Override
    public Object getId(HierarchicalTestBean item) {
        Objects.requireNonNull(item, "Cannot provide an id for a null item.");
        return item.getId();
    }
}
