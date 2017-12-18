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
package com.vaadin.flow.component.radiobutton;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.renderer.ComponentRenderer;
import com.vaadin.flow.renderer.TextRenderer;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code vaadin-radio-group} element.
 *
 * @author Vaadin Ltd.
 */
public class RadioButtonGroup<T>
        extends GeneratedVaadinRadioGroup<RadioButtonGroup<T>>
        implements SingleSelect<RadioButtonGroup<T>, T>, HasDataProvider<T> {

    private static final String VALUE = "value";

    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();

    private SerializablePredicate<T> itemEnabledProvider = item -> true;

    private ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>();

    public RadioButtonGroup() {
        getElement().synchronizeProperty(VALUE, "value-changed");
    }

    @Override
    public void setValue(T value) {
        if (!keyMapper.has(value)) {
            return;
        }
        getElement().setProperty(VALUE, keyMapper.key(value));
    }

    @Override
    public T getValue() {
        return keyMapper.get(getElement().getProperty(VALUE));
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
        refresh();
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<RadioButtonGroup<T>, T> listener) {
        return get().getElement().addPropertyChangeListener(
                getClientValuePropertyName(), event -> listener
                        .onComponentEvent(createValueChangeEvent(event)));
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
     * Returns the item enabled predicate.
     *
     * @return the item enabled predicate
     * @see #setItemEnabledProvider
     */
    public SerializablePredicate<T> getItemEnabledProvider() {
        return itemEnabledProvider;
    }

    /**
     * Sets the item enabled predicate for this radio button group. The
     * predicate is applied to each item to determine whether the item should be
     * enabled ({@code true}) or disabled ({@code false}). Disabled items are
     * displayed as grayed out and the user cannot select them. The default
     * predicate always returns true (all the items are enabled).
     *
     * @param itemEnabledProvider
     *            the item enable predicate, not {@code null}
     */
    public void setItemEnabledProvider(
            SerializablePredicate<T> itemEnabledProvider) {
        this.itemEnabledProvider = Objects.requireNonNull(itemEnabledProvider);
        refreshButtons();
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
     * Sets the item renderer for this radio button group. The renderer is
     * applied to each item to create a component which represents the item.
     *
     * @param itemRenderer
     *            the item renderer, not {@code null}
     */
    public void setItemRenderer(
            ComponentRenderer<? extends Component, T> itemRenderer) {
        this.itemRenderer = Objects.requireNonNull(itemRenderer);
        refreshButtons();
    }

    private void refresh() {
        keyMapper.removeAll();
        removeAll();
        getDataProvider().fetch(new Query<>()).map(this::createRadioButton)
                .forEach(this::add);
    }

    private Component createRadioButton(T item) {
        RadioButton<T> button = new RadioButton<>(keyMapper.key(item), item);
        updateButton(button);
        return button;
    }

    @SuppressWarnings("unchecked")
    private void refreshButtons() {
        getChildren().filter(RadioButton.class::isInstance)
                .map(child -> (RadioButton<T>) child)
                .forEach(this::updateButton);
    }

    private void updateButton(RadioButton<T> button) {
        button.setDisabled(!getItemEnabledProvider().test(button.getItem()));
        button.removeAll();
        button.add(getItemRenderer().createComponent(button.getItem()));
    }

    private ValueChangeEvent<RadioButtonGroup<T>, T> createValueChangeEvent(
            PropertyChangeEvent event) {
        Serializable oldKey = event.getOldValue();
        T oldValue = keyMapper.get(oldKey == null ? null : oldKey.toString());
        return new ValueChangeEvent<>(this, this, oldValue,
                event.isUserOriginated());
    }

    /**
     * Add components to the end of the current items and components.
     * <p>
     * Note! Changing the item set by setting new items or dataprovider will
     * clear all components.
     *
     * @param components
     *            components to add
     */
    public void addComponents(Component... components) {
        add(components);
    }

    /**
     * Remove a component from the group.
     *
     * @param component
     *            component to remove
     */
    public void removeComponent(Component component) {
        remove(component);
    }

    /**
     * Adds the components after the given item. The item must have be added to
     * group via setItems/dataProvider
     * <p>
     * Note! Changing the item set by setting new items or dataprovider will
     * clear all components.
     *
     * @param afterItem
     *            item to add components after
     * @param components
     *            components to add after item
     */
    public void addComponents(T afterItem, Component... components) {
        Optional<RadioButton<T>> itemButton = getRadioButtonForItem(afterItem);

        if (itemButton.isPresent()) {
            int insertPosition = getItemPosition(itemButton);

            for (Component component : components) {
                insertPosition += 1;
                getElement().insertChild(insertPosition,
                        component.getElement());
            }
        }
    }

    /**
     * Adds the components before the given item. The item must have be added to
     * group via setItems/dataProvider
     * <p>
     * Note! Changing the item set by setting new items or dataprovider will
     * clear all components.
     *
     * @param beforeItem
     *            item to add components in front of
     * @param components
     *            components to add before item
     */
    public void prependComponents(T beforeItem, Component... components) {
        Optional<RadioButton<T>> itemButton = getRadioButtonForItem(beforeItem);

        if (itemButton.isPresent()) {
            int insertPosition = getItemPosition(itemButton);

            for (Component component : components) {
                getElement().insertChild(insertPosition,
                        component.getElement());
                insertPosition += 1;
            }
        }
    }

    private Optional<RadioButton<T>> getRadioButtonForItem(T item) {
        return getChildren().filter(RadioButton.class::isInstance)
                .map(child -> (RadioButton<T>) child)
                .filter(button -> button.getItem().equals(item)).findFirst();
    }

    private int getItemPosition(Optional<RadioButton<T>> itemButton) {
        Element buttonElement = itemButton.get().getElement();
        int itemPosition = IntStream.range(0, getElement().getChildCount())
                .filter(i -> getElement().getChild(i).equals(buttonElement))
                .findFirst().orElse(-1);

        if (itemPosition == -1) {
            throw new IllegalArgumentException(
                    "Could not locate item RadioButton element in group to insert components after.");
        }
        return itemPosition;
    }
}
