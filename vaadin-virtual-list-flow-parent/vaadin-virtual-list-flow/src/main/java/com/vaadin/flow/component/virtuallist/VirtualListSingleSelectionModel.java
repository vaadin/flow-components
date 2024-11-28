/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist;

import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.data.selection.SingleSelectionEvent;
import com.vaadin.flow.data.selection.SingleSelectionListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.shared.Registration;

/**
 * Implementation of a SelectionModel.Single.
 *
 * @param <T>
 *            the virtual list bean type
 * @author Vaadin Ltd.
 */
public class VirtualListSingleSelectionModel<T>
        implements SelectionModel.Single<VirtualList<T>, T> {

    private T selectedItem;
    private boolean deselectAllowed = true;
    private VirtualList<T> list;

    /**
     * Constructor for passing a reference of the virtual list to this
     * implementation.
     *
     * @param list
     *            reference to the virtual list for which this selection model
     *            is created
     */
    public VirtualListSingleSelectionModel(VirtualList<T> list) {
        this.list = list;
    }

    @Override
    public void select(T item) {
        if (isSelected(item)) {
            return;
        }
        doSelect(item, false);
    }

    @Override
    public void deselect(T item) {
        select(null);
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

    public SingleSelect<VirtualList<T>, T> asSingleSelect() {
        return new SingleSelect<VirtualList<T>, T>() {

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
                    ValueChangeListener<? super ComponentValueChangeEvent<VirtualList<T>, T>> listener) {
                Objects.requireNonNull(listener, "listener cannot be null");
                ComponentEventListener componentEventListener = event -> listener
                        .valueChanged(
                                (ComponentValueChangeEvent<VirtualList<T>, T>) event);

                return ComponentUtil.addListener(list,
                        SingleSelectionEvent.class, componentEventListener);
            }

            @Override
            public Element getElement() {
                return list.getElement();
            }
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Registration addSelectionListener(
            SelectionListener<VirtualList<T>, T> listener) {
        Objects.requireNonNull(listener, "listener cannot be null");

        return ComponentUtil.addListener(list, SingleSelectionEvent.class,
                (ComponentEventListener) (event -> listener
                        .selectionChange((SelectionEvent) event)));
    }

    private void doSelect(T item, boolean userOriginated) {
        T oldValue = selectedItem;
        selectedItem = item;
        ComponentUtil.fireEvent(list, new SingleSelectionEvent<>(list,
                asSingleSelect(), oldValue, true));
    }

    private Object getItemId(T item) {
        return item == null ? null
                : list.getDataCommunicator().getDataProvider().getId(item);
    }
}
