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
package com.vaadin.ui.radiobutton;

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.KeyMapper;
import com.vaadin.data.provider.Query;
import com.vaadin.data.selection.SingleSelect;
import com.vaadin.function.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.ui.Component;
import com.vaadin.ui.event.PropertyChangeEvent;

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
        Objects.requireNonNull(itemEnabledProvider);
        this.itemEnabledProvider = itemEnabledProvider;
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
    }

    private ValueChangeEvent<RadioButtonGroup<T>, T> createValueChangeEvent(
            PropertyChangeEvent event) {
        Serializable oldKey = event.getOldValue();
        T oldValue = keyMapper.get(oldKey == null ? null : oldKey.toString());
        return new ValueChangeEvent<>(this, this, oldValue,
                event.isUserOriginated());
    }

}
