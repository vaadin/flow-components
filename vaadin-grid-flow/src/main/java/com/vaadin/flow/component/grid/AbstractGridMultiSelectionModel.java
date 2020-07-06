/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid.AbstractGridExtension;
import com.vaadin.flow.data.provider.Query;
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

    private final Set<T> selected;
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
        selected = new LinkedHashSet<>();
        selectionColumn = new GridSelectionColumn(this::clientSelectAll,
                this::clientDeselectAll);
        selectAllCheckBoxVisibility = SelectAllCheckboxVisibility.DEFAULT;

        selectionColumn
                .setSelectAllCheckBoxVisibility(isSelectAllCheckboxVisible());
        grid.getElement().getNode().runWhenAttached(ui -> grid.getElement()
                .insertChild(0, selectionColumn.getElement()));
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
        Set<T> oldSelection = new LinkedHashSet<>(selected);
        boolean added = selected.add(item);
        if (added) {
            fireSelectionEvent(new MultiSelectionEvent<>(getGrid(),
                    getGrid().asMultiSelect(), oldSelection, true));
        }
    }

    @Override
    public void deselectFromClient(T item) {
        if (!isSelected(item)) {
            return;
        }
        Set<T> oldSelection = new LinkedHashSet<>(selected);
        boolean removed = selected.remove(item);
        if (removed) {
            fireSelectionEvent(new MultiSelectionEvent<>(getGrid(),
                    getGrid().asMultiSelect(), oldSelection, true));
        }
        selectionColumn.setSelectAllCheckboxState(false);
    }

    @Override
    public Set<T> getSelectedItems() {
        /*
         * A new LinkedHashSet is created to avoid
         * ConcurrentModificationExceptions when changing the selection during
         * an iteration
         */
        return Collections.unmodifiableSet(new LinkedHashSet<>(selected));
    }

    @Override
    public Optional<T> getFirstSelectedItem() {
        return selected.stream().findFirst();
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
    }

    @Override
    public void deselectAll() {
        updateSelection(Collections.emptySet(), getSelectedItems());
        selectionColumn.setSelectAllCheckboxState(false);
    }

    @Override
    public void updateSelection(Set<T> addedItems, Set<T> removedItems) {
        Objects.requireNonNull(addedItems, "added items cannot be null");
        Objects.requireNonNull(removedItems, "removed items cannot be null");
        doUpdateSelection(addedItems, removedItems, false);
    }

    @Override
    public boolean isSelected(T item) {
        return getSelectedItems().contains(item);
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
            return true;
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

    private void clientSelectAll() {
        if (!isSelectAllCheckboxVisible()) {
            // ignore event if the checkBox was meant to be hidden
            return;
        }
        doUpdateSelection(
                getGrid().getDataCommunicator().getDataProvider()
                        .fetch(new Query<>()).collect(Collectors.toSet()),
                Collections.emptySet(), true);
        selectionColumn.setSelectAllCheckboxState(true);
    }

    private void clientDeselectAll() {
        if (!isSelectAllCheckboxVisible()) {
            // ignore event if the checkBox was meant to be hidden
            return;
        }
        doUpdateSelection(Collections.emptySet(), getSelectedItems(), true);
        selectionColumn.setSelectAllCheckboxState(false);
    }

    private void doUpdateSelection(Set<T> addedItems, Set<T> removedItems,
            boolean userOriginated) {
        addedItems.removeIf(removedItems::remove);
        if (selected.containsAll(addedItems)
                && Collections.disjoint(selected, removedItems)) {
            return;
        }
        Set<T> oldSelection = new LinkedHashSet<>(selected);
        selected.removeAll(removedItems);
        selected.addAll(addedItems);

        sendAddedItems(addedItems);
        sendRemovedItems(removedItems);

        fireSelectionEvent(new MultiSelectionEvent<>(getGrid(),
                getGrid().asMultiSelect(), oldSelection, userOriginated));
        if (!removedItems.isEmpty()) {
            selectionColumn.setSelectAllCheckboxState(false);
        }
    }

    private void sendAddedItems(Set<T> addedItems) {
        if (addedItems.isEmpty()) {
            return;
        }

        addedItems.forEach(getGrid().getDataCommunicator()::refresh);
        getGrid().doClientSideSelection(addedItems);
    }

    private void sendRemovedItems(Set<T> removedItems) {
        if (removedItems.isEmpty()) {
            return;
        }

        removedItems.forEach(getGrid().getDataCommunicator()::refresh);
        getGrid().doClientSideDeselection(removedItems);
    }
}
