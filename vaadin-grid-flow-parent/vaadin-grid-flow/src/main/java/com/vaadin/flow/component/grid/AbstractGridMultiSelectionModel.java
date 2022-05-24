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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid.AbstractGridExtension;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

/**
 * Abstract implementation of a GridMultiSelectionModel.
 *
 * @param <T>
 *            the grid type
 * @author Vaadin Ltd.
 */
public abstract class AbstractGridMultiSelectionModel<T>
        extends AbstractGridExtension<T> implements GridMultiSelectionModel<T> {

    private final Map<Object, T> selected;
    private final GridSelectionColumn selectionColumn;
    private SelectAllCheckboxVisibility selectAllCheckBoxVisibility;

    /**
     * Constructor for passing a reference of the grid to this implementation.
     *
     * @param grid
     *            reference to the grid for which this selection model is
     *            created
     */
    public AbstractGridMultiSelectionModel(Grid<T> grid) {
        super(grid);
        selected = new LinkedHashMap<>();
        selectionColumn = new GridSelectionColumn(this::clientSelectAll,
                this::clientDeselectAll);
        selectAllCheckBoxVisibility = SelectAllCheckboxVisibility.DEFAULT;

        selectionColumn
                .setSelectAllCheckBoxVisibility(isSelectAllCheckboxVisible());

        if (grid.getElement().getNode().isAttached()) {
            this.insertSelectionColumn(grid, selectionColumn);
        } else {
            grid.getElement().getNode().runWhenAttached(ui -> {
                if (grid.getSelectionModel() == this) {
                    this.insertSelectionColumn(grid, selectionColumn);
                }
            });
        }
    }

    private void insertSelectionColumn(Grid<T> grid,
            GridSelectionColumn selectionColumn) {
        grid.getElement().insertChild(0, selectionColumn.getElement());
    }

    @Override
    protected void remove() {
        super.remove();
        deselectAll();
        if (selectionColumn.getElement().getNode().isAttached()) {
            getGrid().getElement().removeChild(selectionColumn.getElement());
        }
    }

    @Override
    public void selectFromClient(T item) {
        if (isSelected(item)) {
            return;
        }

        Set<T> oldSelection = getSelectedItems();
        selected.put(getItemId(item), item);

        fireSelectionEvent(new MultiSelectionEvent<>(getGrid(),
                getGrid().asMultiSelect(), oldSelection, true));

        if (!isSelectAllCheckboxVisible()) {
            // Skip changing the state of Select All checkbox if it was
            // meant to be hidden
            return;
        }

        long size = getDataProviderSize();
        selectionColumn.setSelectAllCheckboxState(
                !isHierarchicalDataProvider() && size == selected.size());
        selectionColumn.setSelectAllCheckboxIndeterminateState(
                isHierarchicalDataProvider() ? selected.size() > 0
                        : selected.size() > 0 && selected.size() < size);
    }

    @Override
    public void deselectFromClient(T item) {
        if (!isSelected(item)) {
            return;
        }

        Set<T> oldSelection = getSelectedItems();
        selected.remove(getItemId(item));

        fireSelectionEvent(new MultiSelectionEvent<>(getGrid(),
                getGrid().asMultiSelect(), oldSelection, true));

        long size = getDataProviderSize();
        selectionColumn.setSelectAllCheckboxState(false);
        selectionColumn.setSelectAllCheckboxIndeterminateState(
                isHierarchicalDataProvider() ? selected.size() > 0
                        : selected.size() > 0 && selected.size() < size);
    }

    @Override
    public Set<T> getSelectedItems() {
        /*
         * A new LinkedHashSet is created to avoid
         * ConcurrentModificationExceptions when changing the selection during
         * an iteration
         */
        return Collections
                .unmodifiableSet(new LinkedHashSet<>(selected.values()));
    }

    @Override
    public Optional<T> getFirstSelectedItem() {
        return selected.values().stream().findFirst();
    }

    @Override
    public void select(T item) {
        if (isSelected(item)) {
            return;
        }
        Set<T> selected = new HashSet<>();
        if (item != null) {
            selected.add(item);
        }
        doUpdateSelection(selected, Collections.emptySet(), false);
    }

    @Override
    public void deselect(T item) {
        if (!isSelected(item)) {
            return;
        }
        Set<T> deselected = new HashSet<>();
        if (item != null) {
            deselected.add(item);
        }
        doUpdateSelection(Collections.emptySet(), deselected, false);
        selectionColumn.setSelectAllCheckboxState(false);
    }

    @Override
    public void selectAll() {
        updateSelection(
                getGrid().getDataCommunicator().getDataProvider()
                        .fetch(new Query<>()).collect(Collectors.toSet()),
                Collections.emptySet());
        selectionColumn.setSelectAllCheckboxState(true);
        selectionColumn.setSelectAllCheckboxIndeterminateState(false);
    }

    @Override
    public void deselectAll() {
        updateSelection(Collections.emptySet(), getSelectedItems());
        selectionColumn.setSelectAllCheckboxState(false);
        selectionColumn.setSelectAllCheckboxIndeterminateState(false);
    }

    @Override
    public void updateSelection(Set<T> addedItems, Set<T> removedItems) {
        Objects.requireNonNull(addedItems, "added items cannot be null");
        Objects.requireNonNull(removedItems, "removed items cannot be null");
        doUpdateSelection(addedItems, removedItems, false);
    }

    @Override
    public boolean isSelected(T item) {
        return selected.containsKey(getItemId(item));
    }

    @Override
    public MultiSelect<Grid<T>, T> asMultiSelect() {
        return new MultiSelect<Grid<T>, T>() {

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Registration addValueChangeListener(
                    ValueChangeListener<? super ComponentValueChangeEvent<Grid<T>, Set<T>>> listener) {
                Objects.requireNonNull(listener, "listener cannot be null");

                ComponentEventListener componentEventListener = event -> listener
                        .valueChanged(
                                (ComponentValueChangeEvent<Grid<T>, Set<T>>) event);

                return ComponentUtil.addListener(getGrid(),
                        MultiSelectionEvent.class, componentEventListener);
            }

            @Override
            public Registration addSelectionListener(
                    MultiSelectionListener<Grid<T>, T> listener) {
                return addMultiSelectionListener(listener);
            }

            @Override
            public void deselectAll() {
                AbstractGridMultiSelectionModel.this.deselectAll();
            }

            @Override
            public void updateSelection(Set<T> addedItems,
                    Set<T> removedItems) {
                AbstractGridMultiSelectionModel.this.updateSelection(addedItems,
                        removedItems);
            }

            @Override
            public Element getElement() {
                return getGrid().getElement();
            }

            @Override
            public Set<T> getSelectedItems() {
                return AbstractGridMultiSelectionModel.this.getSelectedItems();
            }
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Registration addSelectionListener(
            SelectionListener<Grid<T>, T> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return ComponentUtil.addListener(getGrid(), MultiSelectionEvent.class,
                (ComponentEventListener) (event -> listener
                        .selectionChange((SelectionEvent) event)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Registration addMultiSelectionListener(
            MultiSelectionListener<Grid<T>, T> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return ComponentUtil.addListener(getGrid(), MultiSelectionEvent.class,
                (ComponentEventListener) (event -> listener
                        .selectionChange((MultiSelectionEvent) event)));
    }

    @Override
    public void setSelectAllCheckboxVisibility(
            SelectAllCheckboxVisibility selectAllCheckBoxVisibility) {
        this.selectAllCheckBoxVisibility = selectAllCheckBoxVisibility;
        selectionColumn
                .setSelectAllCheckBoxVisibility(isSelectAllCheckboxVisible());
    }

    @Override
    public SelectAllCheckboxVisibility getSelectAllCheckboxVisibility() {
        return selectAllCheckBoxVisibility;
    }

    @Override
    public boolean isSelectAllCheckboxVisible() {
        switch (selectAllCheckBoxVisibility) {
        case DEFAULT:
            return getGrid().getDataCommunicator().getDataProvider()
                    .isInMemory();
        case HIDDEN:
            return false;
        case VISIBLE:
            // Don't show the Select All Checkbox for undefined size, even if
            // the visible property is chosen. Select All Checkbox's state
            // changing requires a size query, which is not expected for
            // undefined size
            return getGrid().getDataCommunicator().isDefinedSize();
        default:
            throw new IllegalStateException(String.format(
                    "Select all checkbox visibility is set to an unsupported value: %s",
                    selectAllCheckBoxVisibility));
        }
    }

    @Override
    public void generateData(T item, JsonObject jsonObject) {
        if (isSelected(item)) {
            jsonObject.put("selected", true);
        }
    }

    @Override
    public void setSelectionColumnFrozen(boolean frozen) {
        selectionColumn.setFrozen(frozen);
    }

    @Override
    public boolean isSelectionColumnFrozen() {
        return selectionColumn.isFrozen();
    }

    /**
     * Method for handling the firing of selection events.
     *
     * @param event
     *            the selection event to fire
     */
    protected abstract void fireSelectionEvent(
            SelectionEvent<Grid<T>, T> event);

    void clientSelectAll() {
        if (!isSelectAllCheckboxVisible()) {
            // ignore event if the checkBox was meant to be hidden
            return;
        }
        Stream<T> allItemsStream;
        DataProvider<T, ?> dataProvider = getGrid().getDataCommunicator()
                .getDataProvider();
        if (dataProvider instanceof HierarchicalDataProvider) {
            allItemsStream = fetchAllHierarchical(
                    (HierarchicalDataProvider<T, ?>) dataProvider);
        } else {
            allItemsStream = dataProvider.fetch(new Query<>());
        }
        doUpdateSelection(allItemsStream.collect(Collectors.toSet()),
                Collections.emptySet(), true);
        selectionColumn.setSelectAllCheckboxState(true);
        selectionColumn.setSelectAllCheckboxIndeterminateState(false);
    }

    /**
     * Fetch all items from the given hierarchical data provider.
     *
     * @param dataProvider
     *            the data provider to fetch from
     * @return all items in the data provider
     */
    private Stream<T> fetchAllHierarchical(
            HierarchicalDataProvider<T, ?> dataProvider) {
        return fetchAllDescendants(null, dataProvider);
    }

    /**
     * Fetch all the descendants of the given parent item from the given data
     * provider.
     *
     * @param parent
     *            the parent item to fetch descendants for
     * @param dataProvider
     *            the data provider to fetch from
     * @return the stream of all descendant items
     */
    private Stream<T> fetchAllDescendants(T parent,
            HierarchicalDataProvider<T, ?> dataProvider) {
        if (parent != null && !dataProvider.hasChildren(parent)) {
            return Stream.empty();
        }
        List<T> children = dataProvider
                .fetchChildren(new HierarchicalQuery<>(null, parent))
                .collect(Collectors.toList());
        if (children.isEmpty()) {
            return Stream.empty();
        }
        return children.stream()
                .flatMap(child -> Stream.concat(Stream.of(child),
                        fetchAllDescendants(child, dataProvider)));
    }

    void clientDeselectAll() {
        if (!isSelectAllCheckboxVisible()) {
            // ignore event if the checkBox was meant to be hidden
            return;
        }
        doUpdateSelection(Collections.emptySet(), getSelectedItems(), true);
        selectionColumn.setSelectAllCheckboxState(false);
        selectionColumn.setSelectAllCheckboxIndeterminateState(false);
    }

    private void doUpdateSelection(Set<T> addedItems, Set<T> removedItems,
            boolean userOriginated) {
        Map<Object, T> addedItemsMap = mapItemsById(addedItems);
        Map<Object, T> removedItemsMap = mapItemsById(removedItems);
        addedItemsMap.keySet().stream().filter(removedItemsMap::containsKey)
                .collect(Collectors.toList()).forEach(key -> {
                    addedItemsMap.remove(key);
                    removedItemsMap.remove(key);
                });
        doUpdateSelection(addedItemsMap, removedItemsMap, userOriginated);
    }

    private void doUpdateSelection(Map<Object, T> addedItems,
            Map<Object, T> removedItems, boolean userOriginated) {

        if (selected.keySet().containsAll(addedItems.keySet()) && Collections
                .disjoint(selected.keySet(), removedItems.keySet())) {
            return;
        }
        Set<T> oldSelection = getSelectedItems();
        removedItems.keySet().forEach(selected::remove);
        selected.putAll(addedItems);

        sendSelectionUpdate(new LinkedHashSet<>(addedItems.values()),
                getGrid()::doClientSideSelection);
        sendSelectionUpdate(new LinkedHashSet<>(removedItems.values()),
                getGrid()::doClientSideDeselection);

        fireSelectionEvent(new MultiSelectionEvent<>(getGrid(),
                getGrid().asMultiSelect(), oldSelection, userOriginated));

        long size = getDataProviderSize();
        selectionColumn.setSelectAllCheckboxState(
                !isHierarchicalDataProvider() && size == selected.size());
        selectionColumn.setSelectAllCheckboxIndeterminateState(
                isHierarchicalDataProvider() ? selected.size() > 0
                        : selected.size() > 0 && selected.size() < size);
    }

    private Map<Object, T> mapItemsById(Set<T> items) {
        return items.stream().collect(LinkedHashMap::new,
                (map, item) -> map.put(this.getItemId(item), item),
                Map::putAll);
    }

    private void sendSelectionUpdate(Set<T> updatedItems,
            Consumer<Set<T>> clientSideUpdater) {
        // Avoid sending updates for the items that the client doesn't have.
        // This is important for the performance of e.g. selectAll.
        Set<T> activeItems = updatedItems.stream()
                .filter(getGrid()::isInActiveRange).collect(Collectors.toSet());
        if (activeItems.isEmpty()) {
            return;
        }

        activeItems.forEach(getGrid().getDataCommunicator()::refresh);
        clientSideUpdater.accept(activeItems);
    }

    private Object getItemId(T item) {
        return getGrid().getDataCommunicator().getDataProvider().getId(item);
    }

    private long getDataProviderSize() {
        long size = 0;
        final DataCommunicator<T> dataCommunicator = getGrid()
                .getDataCommunicator();

        final DataProvider<T, ?> dataProvider = dataCommunicator
                .getDataProvider();

        // Avoid throwing an IllegalArgumentException in case of
        // HierarchicalDataProvider
        if (!(dataProvider instanceof HierarchicalDataProvider)) {
            if (dataProvider.isInMemory()) {
                size = dataProvider.size(new Query<>());
            } else if (dataCommunicator.isDefinedSize()) {
                // Use a cached value of items count for defined size
                size = dataCommunicator.getItemCount();
            }
        }
        return size;
    }

    private boolean isHierarchicalDataProvider() {
        return getGrid().getDataCommunicator()
                .getDataProvider() instanceof HierarchicalDataProvider;
    }
}
