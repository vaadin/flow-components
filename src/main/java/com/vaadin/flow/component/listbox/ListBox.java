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
package com.vaadin.flow.component.listbox;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.HasItemsAndComponents;
import com.vaadin.flow.data.provider.DataChangeEvent.DataRefreshEvent;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code vaadin-list-box} element.
 *
 * @author Vaadin Ltd
 *
 * @param <T>
 *            the type of the items contained by this component
 */
public class ListBox<T> extends GeneratedVaadinListBox<ListBox<T>>
        implements HasItemsAndComponents<T>, SingleSelect<ListBox<T>, T>,
        HasDataProvider<T>, HasComponents {

    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();
    private ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>();
    private SerializablePredicate<T> itemEnabledProvider = item -> true;

    public ListBox() {
        getElement().synchronizeProperty(getClientValuePropertyName(),
                getClientPropertyChangeEventName());
    }

    @Override
    public String getClientValuePropertyName() {
        return "selected";
    }

    /**
     * Gets the selected item.
     *
     * @return the selected item, or {@code null} if none is selected
     */
    @Override
    public T getValue() {
        int selectedIndex = getElement()
                .getProperty(getClientValuePropertyName(), -1);
        if (selectedIndex == -1) {
            return null;
        }
        return getItemComponents().get(selectedIndex).getItem();
    }

    /**
     * Selects the given item.
     *
     * @param value
     *            the item to select, {@code null} to undo selection
     * @throws IllegalArgumentException
     *             if this component doesn't contain the item
     */
    @Override
    public void setValue(T value) {
        if (value == null) {
            getElement().setProperty(getClientValuePropertyName(), -1);
            return;
        }
        List<VaadinItem<T>> itemComponents = getItemComponents();
        int newSelected = IntStream.range(0, itemComponents.size())
                .filter(i -> value.equals(itemComponents.get(i).getItem()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Could not find given value from the item set"));
        getElement().setProperty(getClientValuePropertyName(), newSelected);
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider = Objects.requireNonNull(dataProvider);
        dataProvider.addDataProviderListener(event -> {
            if (event instanceof DataRefreshEvent) {
                refresh(((DataRefreshEvent<T>) event).getItem());
            } else {
                rebuild();
            }
        });
        rebuild();
    }

    /**
     * Gets the data provider.
     *
     * @return the data provider, not {@code null}
     */
    public DataProvider<T, ?> getDataProvider() {
        return dataProvider;
    }

    /**
     * Returns the item component renderer.
     *
     * @return the item renderer
     * @see #setItemRenderer
     */
    public ComponentRenderer<? extends Component, T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer for this ListBox. The renderer is applied to each
     * item to create a component which represents the item.
     *
     * @param itemRenderer
     *            the item renderer, not {@code null}
     */
    public void setRenderer(
            ComponentRenderer<? extends Component, T> itemRenderer) {
        this.itemRenderer = Objects.requireNonNull(itemRenderer);
        getItemComponents().forEach(this::refreshContent);
    }

    /**
     * Sets the item enabled predicate for this ListBox. The predicate is
     * applied to each item to determine whether the item should be enabled
     * ({@code true}) or disabled ({@code false}). Disabled items are displayed
     * as grayed out and the user cannot select them. The default predicate
     * always returns true (all the items are enabled).
     *
     * @param itemEnabledProvider
     *            the item enable predicate, not {@code null}
     */
    public void setItemEnabledProvider(
            SerializablePredicate<T> itemEnabledProvider) {
        this.itemEnabledProvider = Objects.requireNonNull(itemEnabledProvider);
        getItemComponents().forEach(this::refreshEnabled);
    }

    /**
     * Returns the item enabled predicate.
     *
     * @return the item enabled predicate
     * @see #setItemEnabledProvider
     */
    public SerializablePredicate<T> getItemEnabledProvider() {
        return itemEnabledProvider;
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<ListBox<T>, T> listener) {
        return getElement().addPropertyChangeListener(
                getClientValuePropertyName(), event -> listener
                        .onComponentEvent(createValueChangeEvent(event)));
    }

    private ValueChangeEvent<ListBox<T>, T> createValueChangeEvent(
            PropertyChangeEvent event) {
        T oldValue = null;
        if (event.getOldValue() != null) {
            Double oldSelectedIndex = (Double) event.getOldValue();
            if (oldSelectedIndex >= 0) {
                oldValue = getItemComponents().get(oldSelectedIndex.intValue())
                        .getItem();
            }
        }
        return new ValueChangeEvent<>(this, this, oldValue,
                event.isUserOriginated());
    }

    /**
     * <b>Not supported!</b>
     * <p>
     * Not supported by the client-side web-component, see
     * <a href= "https://github.com/vaadin/vaadin-list-box/issues/19">issue in
     * GitHub</a>.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        throw new UnsupportedOperationException(
                "Not supported by the client-side web-component: "
                        + "https://github.com/vaadin/vaadin-list-box/issues/19");
    }

    private void rebuild() {
        removeAll();
        setValue(null);
        getDataProvider().fetch(new Query<>()).map(this::createItemComponent)
                .forEach(this::add);
    }

    private VaadinItem<T> createItemComponent(T item) {
        VaadinItem<T> itemComponent = new VaadinItem<>(item);
        refresh(itemComponent);
        return itemComponent;
    }

    private void refresh(T item) {
        VaadinItem<T> itemComponent = getItemComponents().stream()
                .filter(component -> component.getItem().equals(item))
                .findFirst().get();
        refresh(itemComponent);
    }

    private void refresh(VaadinItem<T> itemComponent) {
        refreshContent(itemComponent);
        refreshEnabled(itemComponent);
    }

    private void refreshContent(VaadinItem<T> itemComponent) {
        itemComponent.removeAll();
        itemComponent
                .add(itemRenderer.createComponent(itemComponent.getItem()));
    }

    private void refreshEnabled(VaadinItem<T> itemComponent) {
        itemComponent.setDisabled(
                !itemEnabledProvider.test(itemComponent.getItem()));
    }

    private List<VaadinItem<T>> getItemComponents() {
        return getChildren().filter(VaadinItem.class::isInstance)
                .map(component -> (VaadinItem<T>) component)
                .collect(Collectors.toList());
    }

    @Override
    public Registration addItemsChangeListener(
            ComponentEventListener<ItemsChangeEvent<ListBox<T>>> listener) {
        return super.addItemsChangeListener(listener);
    }
}
