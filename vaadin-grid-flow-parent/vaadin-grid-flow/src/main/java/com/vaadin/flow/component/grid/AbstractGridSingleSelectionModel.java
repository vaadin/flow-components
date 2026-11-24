/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;

import tools.jackson.databind.node.ObjectNode;

/**
 * Abstract implementation of a GridSingleSelectionModel.
 *
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the grid type
 * @since 1.0
 */
public abstract class AbstractGridSingleSelectionModel<T> extends
        AbstractGridExtension<T> implements GridSingleSelectionModel<T> {

    private T selectedItem;
    private boolean deselectAllowed = true;
    private GridSelectionSignalHelper.BindResult<T> selectionBinding;

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
        boolean selectable = getGrid().isItemSelectable(item);
        if (isSelected(item) || !selectable) {
            return;
        }
        doSelect(item, true);
    }

    @Override
    public void select(T item) {
        if (isSelected(item)) {
            return;
        }
        if (doSelect(item, false)) {
            getGrid().doClientSideSelection(Collections.singleton(item));
        }
    }

    @Override
    public void deselectFromClient(T item) {
        boolean selectable = getGrid().isItemSelectable(item);
        if (isSelected(item) && selectable && isDeselectAllowed()) {
            doSelect(null, true);
        }
    }

    @Override
    public void deselect(T item) {
        if (isSelected(item)) {
            select(null);
        }
    }

    @Override
    public boolean isSelected(T item) {
        return Objects.equals(getItemId(item), getItemId(selectedItem));
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

            @Override
            public SignalBinding<T> bindValue(Signal<T> valueSignal,
                    SerializableConsumer<T> writeCallback) {
                if (selectionBinding != null) {
                    throw new BindingActiveException();
                }
                selectionBinding = GridSelectionSignalHelper
                        .bindValue(getGrid(), this, valueSignal, writeCallback);
                return selectionBinding.signalBinding();
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
    public void generateData(T item, ObjectNode jsonObject) {
        if (isSelected(item)) {
            jsonObject.put("selected", true);
        }
    }

    @Override
    protected void remove() {
        super.remove();
        if (selectionBinding != null) {
            selectionBinding.cleanup().remove();
            selectionBinding = null;
        }
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

    private boolean doSelect(T item, boolean userOriginated) {
        T oldValue = selectedItem;
        selectedItem = item;
        if (!writeSelectionToSignal(item)) {
            return false;
        }
        fireSelectionEvent(new SingleSelectionEvent<>(getGrid(),
                getGrid().asSingleSelect(), oldValue, userOriginated));
        return true;
    }

    /**
     * Passes the selection to the write callback of the bound signal. If the
     * signal doesn't accept it, reverts the selection to the signal value
     * without firing an event and syncs it to the client.
     *
     * @param item
     *            the item being selected
     * @return {@code false} if the selection was reverted
     */
    private boolean writeSelectionToSignal(T item) {
        if (selectionBinding == null) {
            return true;
        }
        T signalValue = selectionBinding.writer().apply(item);
        if (Objects.equals(getItemId(signalValue), getItemId(item))) {
            return true;
        }
        selectedItem = signalValue;
        getGrid().doClientSideSelection(Collections.singleton(signalValue));
        return false;
    }

    private Object getItemId(T item) {
        return item == null ? null : getGrid().getItemId(item);
    }
}
