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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.HasDataProvider;
import com.vaadin.flow.data.binder.HasItemsAndComponents;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.KeyMapper;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the {@code vaadin-radio-group} element.
 *
 * @author Vaadin Ltd.
 */
public class RadioButtonGroup<T>
        extends GeneratedVaadinRadioGroup<RadioButtonGroup<T>>
        implements HasItemsAndComponents<T>,
        SingleSelect<RadioButtonGroup<T>, T>, HasDataProvider<T> {

    private final KeyMapper<T> keyMapper = new KeyMapper<>();

    private DataProvider<T, ?> dataProvider = DataProvider.ofItems();

    private SerializablePredicate<T> itemEnabledProvider = item -> isEnabled();

    private ComponentRenderer<? extends Component, T> itemRenderer = new TextRenderer<>();

    private boolean isReadOnly;

    public RadioButtonGroup() {
        getElement().synchronizeProperty(getClientValuePropertyName(),
                getClientPropertyChangeEventName());
        getElement().addPropertyChangeListener(getClientValuePropertyName(),
                this::validateSelectionEnabledState);
    }

    private void validateSelectionEnabledState(PropertyChangeEvent event) {
        if (!itemEnabledProvider
                .test(keyMapper.get(event.getValue().toString()))) {

            Serializable oldKey = event.getOldValue();
            T oldValue = keyMapper
                    .get(oldKey == null ? null : oldKey.toString());
            setValue(oldValue);
        }
    }

    @Override
    public void setValue(T value) {
        if (!keyMapper.has(value)) {
            return;
        }
        getElement().setProperty(getClientValuePropertyName(),
                keyMapper.key(value));
    }

    @Override
    public T getValue() {
        return keyMapper
                .get(getElement().getProperty(getClientValuePropertyName()));
    }

    @Override
    public void setDataProvider(DataProvider<T, ?> dataProvider) {
        this.dataProvider = dataProvider;
        refresh();
    }

    @Override
    public Registration addValueChangeListener(
            ValueChangeListener<RadioButtonGroup<T>, T> listener) {
        return getElement()
                .addPropertyChangeListener(getClientValuePropertyName(),
                        event -> listener.onComponentEvent(
                                createValueChangeEvent(event)));
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
     *         the item enable predicate, not {@code null}
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
     * @see #setRenderer(ComponentRenderer)
     */
    public ComponentRenderer<? extends Component, T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * Sets the item renderer for this radio button group. The renderer is
     * applied to each item to create a component which represents the item.
     *
     * @param renderer
     *         the item renderer, not {@code null}
     */
    public void setRenderer(
            ComponentRenderer<? extends Component, T> renderer) {
        this.itemRenderer = Objects.requireNonNull(renderer);
        refreshButtons();
    }

    @Override
    public void onEnabledStateChanged(boolean enabled) {
        if (isReadOnly()) {
            setDisabled(true);
        } else {
            setDisabled(!enabled);
        }
        refreshButtons();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
        if (isEnabled()) {
            setDisabled(readOnly);
        }
    }

    @Override
    public boolean isReadOnly() {
        return isReadOnly;
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

    private HasValue.ValueChangeEvent<RadioButtonGroup<T>, T> createValueChangeEvent(
            PropertyChangeEvent event) {
        Serializable oldKey = event.getOldValue();
        T oldValue = keyMapper.get(oldKey == null ? null : oldKey.toString());
        return new HasValue.ValueChangeEvent<>(this, this, oldValue,
                event.isUserOriginated());
    }
}
