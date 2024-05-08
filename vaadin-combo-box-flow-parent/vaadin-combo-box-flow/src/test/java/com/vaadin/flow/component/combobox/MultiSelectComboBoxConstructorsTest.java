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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MultiSelectComboBoxConstructorsTest {

    private HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<MultiSelectComboBox<String>, Set<String>>> valueChangeListener;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        valueChangeListener = Mockito.mock(HasValue.ValueChangeListener.class);
    }

    @Test
    public void initializeWithPageSize() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(42);
        Assert.assertEquals(42, comboBox.getPageSize());
    }

    @Test
    public void initializeWithLabel() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label");
        Assert.assertEquals("Test label", comboBox.getLabel());
    }

    @Test
    public void initializeWithLabelAndCollection() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", List.of("foo", "bar", "baz"));
        Assert.assertEquals("Test label", comboBox.getLabel());
        assertDataProviderItems(comboBox, "foo", "bar", "baz");
    }

    @Test
    public void initializeWithLabelAndItems() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", "foo", "bar", "baz");
        Assert.assertEquals("Test label", comboBox.getLabel());
        assertDataProviderItems(comboBox, "foo", "bar", "baz");
    }

    @Test
    public void initializeWithValueChangeListener() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                valueChangeListener);
        comboBox.setItems("foo", "bar", "baz");
        comboBox.setValue(Set.of("foo"));
        Mockito.verify(valueChangeListener, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @Test
    public void initializeWithLabelAndValueChangeListener() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", valueChangeListener);
        Assert.assertEquals("Test label", comboBox.getLabel());

        comboBox.setItems("foo", "bar", "baz");
        comboBox.setValue(Set.of("foo"));
        Mockito.verify(valueChangeListener, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @Test
    public void initializeWithLabelAndValueChangeListenerAndItems() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>(
                "Test label", valueChangeListener, "foo", "bar", "baz");
        Assert.assertEquals("Test label", comboBox.getLabel());
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
        Assert.assertEquals(items.length, dataProviderItems.size());
        Assert.assertTrue(dataProviderItems.containsAll(Arrays.asList(items)));
    }
}
