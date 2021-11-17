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
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.grid.Grid.AbstractGridExtension;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.selection.SingleSelectionEvent;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

/**
 * Abstract implementation of a GridSingleSelectionModel.
 *
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the grid type
 */
public abstract class AbstractGridSingleSelectionModel<T> extends
        AbstractGridExtension<T> implements GridSingleSelectionModel<T> {

    private T selectedItem;
    private boolean deselectAllowed = true;

    /**
     * Constructor for passing a reference of the grid to this implementation.
     *
     * @param grid
     *            reference to the grid for which this selection model is
     *            created
     */
    public AbstractGridSingleSelectionModel(Grid<T> grid) {
        super(grid);
    }

    @Override
    public void selectFromClient(T item) {
        if (Objects.equals(getItemId(item), getItemId(selectedItem))) {
            return;
        }
        doSelect(item, true);
    }

    @Override
    public void select(T item) {
        if (Objects.equals(getItemId(item), getItemId(selectedItem))) {
            return;
        }
        T oldItem = selectedItem;
        doSelect(item, false);

        getGrid().doClientSideSelection(Collections.singleton(item));
        if (oldItem != null) {
            getGrid().getDataCommunicator().refresh(oldItem);
        }
        if (item != null) {
            getGrid().getDataCommunicator().refresh(item);
        }
    }

    @Override
    public void deselectFromClient(T item) {
        if (isSelected(item) && isDeselectAllowed()) {
            selectFromClient(null);
        }
    }

    @Override
    public void deselect(T item) {
        if (isSelected(item)) {
            select(null);
        }
    }

    @Override
    public Optional<T> getSelectedItem() {
        return Optional.ofNullable(selectedItem);
    }

    @Override
    public void setDeselectAllowed(boolean deselectAllowed) {
        this.deselectAllowed = deselectAllowed;
    }

    @Override
    public boolean isDeselectAllowed() {
        return deselectAllowed;
    }

    @Override
    public SingleSelect<Grid<T>, T> asSingleSelect() {
        return new SingleSelect<Grid<T>, T>() {

            @Override
            public void setValue(T value) {
                setSelectedItem(value);
            }

            @Override
            public T getValue() {
                return getSelectedItem().orElse(getEmptyValue());
            }

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Registration addValueChangeListener(
                    ValueChangeListener<? super ComponentValueChangeEvent<Grid<T>, T>> listener) {
                Objects.requireNonNull(listener, "listener cannot be null");
                ComponentEventListener componentEventListener = event -> listener
                        .valueChanged(
                                (ComponentValueChangeEvent<Grid<T>, T>) event);

                return ComponentUtil.addListener(getGrid(),
                        SingleSelectionEvent.class, componentEventListener);
            }

            @Override
            public Element getElement() {
                return getGrid().getElement();
            }
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Registration addSelectionListener(
            SelectionListener<Grid<T>, T> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return ComponentUtil.addListener(getGrid(), SingleSelectionEvent.class,
                (ComponentEventListener) (event -> listener
                        .selectionChange((SelectionEvent) event)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Registration addSingleSelectionListener(
            SingleSelectionListener<Grid<T>, T> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return ComponentUtil.addListener(getGrid(), SingleSelectionEvent.class,
                (ComponentEventListener) (event -> listener
                        .selectionChange((SingleSelectionEvent) event)));
    }

    @Override
    public void generateData(T item, JsonObject jsonObject) {
        if (isSelected(item)) {
            jsonObject.put("selected", true);
        }
    }

    @Override
    protected void remove() {
        super.remove();
        deselectAll();
    }

    /**
     * Method for handling the firing of selection events.
     *
     * @param event
     *            the selection event to fire
     */
    protected abstract void fireSelectionEvent(
            SelectionEvent<Grid<T>, T> event);

    private void doSelect(T item, boolean userOriginated) {
        T oldValue = selectedItem;
        selectedItem = item;
        fireSelectionEvent(new SingleSelectionEvent<>(getGrid(),
                getGrid().asSingleSelect(), oldValue, userOriginated));
    }

    private Object getItemId(T item) {
        return item == null ? null
                : getGrid().getDataCommunicator().getDataProvider().getId(item);
    }
}
