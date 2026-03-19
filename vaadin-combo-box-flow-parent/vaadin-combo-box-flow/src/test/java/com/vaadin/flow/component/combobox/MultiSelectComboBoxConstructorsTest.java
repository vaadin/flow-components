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
package com.vaadin.flow.component.combobox;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.provider.ListDataProvider;

class MultiSelectComboBoxConstructorsTest {

    private HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<MultiSelectComboBox<String>, Set<String>>> valueChangeListener;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        valueChangeListener = Mockito.mock(HasValue.ValueChangeListener.class);
    }

    @Test
    void initializeWithPageSize() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(42);
        Assertions.assertEquals(42, comboBox.getPageSize());
    }

    @Test
    void initializeWithLabel() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label");
        Assertions.assertEquals("Test label", comboBox.getLabel());
    }

    @Test
    void initializeWithLabelAndCollection() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", List.of("foo", "bar", "baz"));
        Assertions.assertEquals("Test label", comboBox.getLabel());
        assertDataProviderItems(comboBox, "foo", "bar", "baz");
    }

    @Test
    void initializeWithLabelAndItems() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", "foo", "bar", "baz");
        Assertions.assertEquals("Test label", comboBox.getLabel());
        assertDataProviderItems(comboBox, "foo", "bar", "baz");
    }

    @Test
    void initializeWithValueChangeListener() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                valueChangeListener);
        comboBox.setItems("foo", "bar", "baz");
        comboBox.setValue(Set.of("foo"));
        Mockito.verify(valueChangeListener, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @Test
    void initializeWithLabelAndValueChangeListener() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", valueChangeListener);
        Assertions.assertEquals("Test label", comboBox.getLabel());

        comboBox.setItems("foo", "bar", "baz");
        comboBox.setValue(Set.of("foo"));
        Mockito.verify(valueChangeListener, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @Test
    void initializeWithLabelAndValueChangeListenerAndItems() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", valueChangeListener, "foo", "bar", "baz");
        Assertions.assertEquals("Test label", comboBox.getLabel());
        assertDataProviderItems(comboBox, "foo", "bar", "baz");

        comboBox.setValue(Set.of("foo"));
        Mockito.verify(valueChangeListener, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @SuppressWarnings({ "unchecked", "SuspiciousMethodCalls" })
    @SafeVarargs
    private <TItem> void assertDataProviderItems(
            MultiSelectComboBox<TItem> comboBox, TItem... items) {
        ListDataProvider<String> dataProvider = (ListDataProvider<String>) comboBox
                .getDataProvider();
        Collection<String> dataProviderItems = dataProvider.getItems();
        Assertions.assertEquals(items.length, dataProviderItems.size());
        Assertions.assertTrue(
                dataProviderItems.containsAll(Arrays.asList(items)));
    }
}
