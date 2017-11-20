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

import com.vaadin.data.HasDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.KeyMapper;
import com.vaadin.data.provider.Query;
import com.vaadin.data.selection.SingleSelect;
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

    private void refresh() {
        keyMapper.removeAll();
        removeAll();
        getDataProvider().fetch(new Query<>()).map(this::createRadioButton)
                .forEach(this::add);
    }

    private Component createRadioButton(T item) {
        return new RadioButton<>(keyMapper.key(item), item);
    }

    private ValueChangeEvent<RadioButtonGroup<T>, T> createValueChangeEvent(
            PropertyChangeEvent event) {
        Serializable oldKey = event.getOldValue();
        T oldValue = keyMapper.get(oldKey == null ? null : oldKey.toString());
        return new ValueChangeEvent<>(this, this, oldValue,
                event.isUserOriginated());
    }

}
