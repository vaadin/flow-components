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
package com.vaadin.flow.component.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;

/**
 * Implements the logic necessary for proper column reordering:
 * {@link Grid#setColumnOrder(List)}.
 *
 * @author Vaadin Ltd
 */
class GridColumnOrderHelper<T> {
    private final Grid<T> grid;

    GridColumnOrderHelper(Grid<T> grid) {
        this.grid = Objects.requireNonNull(grid);
    }

    /**
     * See {@link Grid#setColumnOrder(List)}.
     *
     * @param columns
     *            the new column order, not {@code null}.
     */
    void setColumnOrder(List<Grid.Column<T>> columns) {
        // first, a couple of sanity checks whether the input list is complete
        // (contains all Grid columns), doesn't repeat any columns, is not null
        // etc.
        Objects.requireNonNull(columns, "columns");
        final Set<Grid.Column<T>> newColumns = new HashSet<>(columns);
        if (newColumns.size() < columns.size()) {
            throw new IllegalArgumentException(
                    "A column is present multiple times in the list of columns: "
                            + columns.stream().map(Grid.Column::getKey)
                                    .collect(Collectors.joining(", ")));
        }
        final List<Grid.Column<T>> currentColumns = grid.getColumns();
        if (newColumns.size() < currentColumns.size()) {
            final String missingColumnKeys = currentColumns.stream()
                    .filter(col -> !newColumns.contains(col))
                    .map(Grid.Column::getKey).collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "The 'columns' list is missing the following columns: "
                            + missingColumnKeys);
        }
        for (Grid.Column<T> column : newColumns) {
            grid.ensureOwner(column);
        }

        // sanity test passed. Reorder the columns.
        final List<String> newOrderIDs = columns.stream()
                .map(Grid.Column::getInternalId).collect(Collectors.toList());
        final GraphNodeLeafCache nodeLeafCache = new GraphNodeLeafCache();
        // first run a dry run, to check whether the column ordering is possible
        // without actually performing the DOM reorder.
        // This allows us to either fail and leave DOM intact, or succeed
        // and modify the DOM, which guarantees atomicity.
        reorderColumnsAndConsumeIDs(grid, new IdQueue(newOrderIDs),
                nodeLeafCache, true);
        // no exception thrown, the reordering is possible. Run the algorithm
        // again,
        // but this time also reorder the DOM.
        reorderColumnsAndConsumeIDs(grid, new IdQueue(newOrderIDs),
                nodeLeafCache, false);

        // update the new column ordering in the column layers as well,
        // otherwise
        // any future header/footer cell joining would use old ordering.
        final List<ColumnBase<?>> columnsPreOrder = getColumnsPreOrder();
        for (ColumnLayer columnLayer : grid.getColumnLayers()) {
            columnLayer.updateColumnOrder(columnsPreOrder);
        }

        // This will reset all column orders so that the visual column order
        // will also reflect that in the DOM.
        grid.getElement()
                .executeJs("this._updateOrders(this._columnTree, null)");
    }

    /**
     * Computes a total order of all columns and column groups, in pre-order
     * order.
     *
     * @return a list of all columns and column groups, ordered with preorder,
     *         never {@code null}.
     */
    private List<ColumnBase<?>> getColumnsPreOrder() {
        return getColumnsPreOrder(grid);
    }

    private List<ColumnBase<?>> getColumnsPreOrder(Component parent) {
        final List<ColumnBase<?>> list = new ArrayList<>();
        if (parent instanceof AbstractColumn) {
            list.add((ColumnBase<?>) parent);
        }
        parent.getChildren().filter(col -> col instanceof AbstractColumn)
                .forEach(col -> list.addAll(getColumnsPreOrder(col)));
        return list;
    }

    /**
     * The function walks the expected column ordering and tries to rearrange
     * its child columns/column-groups so that the DOM order corresponds to the
     * expected column ordering.
     * <p>
     * The function recursively invokes itself upon its children, to rearrange
     * the tree properly.
     * <p>
     * The tree "dances" until everything is neatly rearranged.
     * <p>
     * The function will never move DOM elements into another parent.
     *
     * @param column
     *            rearrange children of this column. This can only be a
     *            {@link Grid} or {@link AbstractColumn} instance. Not
     *            {@code null}.
     * @param unconsumedIDs
     *            expected column ordering. The IDs are consumed as we visit the
     *            column element tree and successfully reorder DOM nodes. Not
     *            {@code null}.
     * @param nodeLeafCache
     *            used to quickly find a child column/column-group that contains
     *            given leaf column ID as we consume column IDs.
     * @param dryRun
     *            if true then the DOM is not modified.
     * @throws IllegalArgumentException
     *             if the tree can not be rearranged according to the expected
     *             column ordering (e.g. we would have to split a group of
     *             columns apart).
     */
    private void reorderColumnsAndConsumeIDs(Component column,
            IdQueue unconsumedIDs, GraphNodeLeafCache nodeLeafCache,
            boolean dryRun) {
        Objects.requireNonNull(column);
        if (column instanceof Grid.Column) {
            // special case: we're at the leaf of the column hierarchy.
            // no children to reorder here.
            // We've successfully reordered children in the column tree.
            // Mark this fact by consuming the column ID and bail out.
            unconsumedIDs.consumeIdFor((Grid.Column<T>) column);
            return;
        }

        // attempt to reorder direct children of this column group based on
        // the next ID from the unconsumed ID set. If that succeeds,
        // recursively reorder children of children etc.

        // holds the current immediate child columns.
        final Set<AbstractColumn<?>> childColumns = new HashSet<>();
        column.getChildren().filter(c -> !(c instanceof GridSelectionColumn))
                .forEach(it -> childColumns.add((AbstractColumn) it));
        // the new order of the children is computed here.
        final List<AbstractColumn<?>> newOrder = new ArrayList<>();

        while (!childColumns.isEmpty()) {
            // There are still columns left. Peek on the next ID in the desired
            // order of columns, and try to find a column/column-group for it.
            final String id = unconsumedIDs.element();
            final AbstractColumn<?> child = nodeLeafCache
                    .findFirstContaining(id, childColumns);
            if (child == null) {
                throw new IllegalArgumentException(dumpColumnHierarchyFromDOM()
                        + ": Cannot reorder columns, at ID: " + unconsumedIDs);
            }

            // found the column. Make sure that its contents are ordered as
            // well.
            reorderColumnsAndConsumeIDs(child, unconsumedIDs, nodeLeafCache,
                    dryRun);
            // success - add it to the result list.
            childColumns.remove(child);
            newOrder.add(child);
        }

        // The new node order has been computed successfully. Reorder the
        // elements in DOM.
        if (!dryRun) {
            newOrder.forEach(it -> it.getElement().removeFromParent());
            newOrder.forEach(
                    it -> column.getElement().appendChild(it.getElement()));
        }
    }

    /**
     * Offers methods to remove IDs from the head of given ID list and provide
     * informative error messages if it fails.
     */
    private class IdQueue {
        /**
         * Only for error-reporting purposes.
         */
        private final String originalIDs;
        private final Queue<String> unconsumedIDs;

        public IdQueue(Collection<String> internalIDs) {
            this.unconsumedIDs = new LinkedList<>(internalIDs);
            originalIDs = String.join(", ", internalIDs);
        }

        @Override
        public String toString() {
            return unconsumedIDs.toString();
        }

        /**
         * Peeks at the first ID in the internal queue but doesn't remove it.
         *
         * @return the head ID, never {@code null}.
         * @throws IllegalArgumentException
         *             if the queue is empty.
         */
        public String element() {
            if (unconsumedIDs.isEmpty()) {
                throw new IllegalArgumentException(dumpColumnHierarchyFromDOM()
                        + ": all IDs have been consumed but there are still columns left. Original set of IDs: "
                        + originalIDs);
            }
            return unconsumedIDs.element();
        }

        /**
         * Makes sure the {@link Grid.Column#getInternalId()} matches the
         * {@link #element() head} of the ID queue. If it does, removes the head
         * of the queue.
         *
         * @param column
         *            the column to match, not {@code null}
         * @throws IllegalArgumentException
         *             if the column ID doesn't match the head of the queue.
         */
        public void consumeIdFor(Grid.Column<T> column) {
            if (!element().equals(column.getInternalId())) {
                throw new IllegalArgumentException(dumpColumnHierarchyFromDOM()
                        + ": Cannot reorder columns at ID: " + unconsumedIDs);
            }
            unconsumedIDs.remove();
        }
    }

    /**
     * Computes and caches a set of leafs attached transitively under a graph
     * node. The root node is the {@link Grid}, child nodes are instances of
     * {@link AbstractColumn}s, leaves are {@link Grid.Column}s.
     */
    private static class GraphNodeLeafCache {
        /**
         * Maps {@link Grid} or {@link AbstractColumn} to a set of
         * {@link Grid.Column#getInternalId()}s of leaf {@link Grid.Column}s
         * nested under this node. The sets are unmodifiable.
         */
        private final Map<Component, Set<String>> nodeLeafsCache = new HashMap<>();

        /**
         * Returns a set of {@link Grid.Column#getInternalId()} of columns
         * hooked under this {@link Grid} or {@link AbstractColumn}.
         *
         * @param component
         *            the component to check, not null, must be {@link Grid} or
         *            {@link AbstractColumn}
         * @return set of {@link Grid.Column#getInternalId()}, never null.
         *         Returns a singleton set for {@link Grid.Column}.
         */
        private Set<String> getColumnIDs(Component component) {
            Objects.requireNonNull(component);
            if (nodeLeafsCache.get(component) == null) {
                Set<String> computeNodeLeafs = computeNodeLeafs(component);
                if (computeNodeLeafs != null) {
                    nodeLeafsCache.put(component, computeNodeLeafs);
                } else {
                    return new HashSet<>();
                }
            }
            return nodeLeafsCache.get(component);
        }

        /**
         * Finds first column from given list of columns which contains a leaf
         * with given ID.
         *
         * @param columnID
         *            the desired {@link Grid.Column#getInternalId()}, not null.
         * @param columns
         *            the list of columns to search in, not null.
         * @return the first column from the {@code columns} parameter
         *         containing leaf with given ID, or null if no such column
         *         exists in the list.
         */
        public AbstractColumn<?> findFirstContaining(String columnID,
                Collection<AbstractColumn<?>> columns) {
            Objects.requireNonNull(columnID);
            for (AbstractColumn<?> column : columns) {
                if (getColumnIDs(column).contains(columnID)) {
                    return column;
                }
            }
            return null;
        }

        private Set<String> computeNodeLeafs(Component component) {
            if (component instanceof Grid.Column) {
                return Collections
                        .singleton(((Grid.Column) component).getInternalId());
            }
            if (component instanceof Grid
                    || component instanceof AbstractColumn) {
                return component.getChildren()
                        .filter(col -> col instanceof AbstractColumn)
                        .flatMap(col -> getColumnIDs(col).stream())
                        .collect(Collectors.toSet());
            }
            throw new IllegalArgumentException(
                    "Parameter component: invalid value " + component
                            + ": must be Grid or AbstractColumn");
        }
    }

    private String dumpColumnHierarchyFromDOM() {
        return dumpColumnHierarchyFromDOM(grid);
    }

    private String dumpColumnHierarchyFromDOM(Component component) {
        return component.getChildren().map(child -> {
            if (child instanceof Grid.Column) {
                return ((Grid.Column) child).getInternalId() + "/"
                        + ((Grid.Column) child).getKey();
            } else {
                return "(" + dumpColumnHierarchyFromDOM(child) + ")";
            }
        }).collect(Collectors.joining(", "));
    }
}
