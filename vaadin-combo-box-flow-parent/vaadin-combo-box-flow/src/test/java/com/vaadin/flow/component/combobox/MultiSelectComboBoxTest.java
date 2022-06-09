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
package com.vaadin.flow.component.combobox;

import com.vaadin.flow.component.HasValue;
import elemental.json.JsonArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public class MultiSelectComboBoxTest extends ComboBoxBaseTest {
    @Override
    protected <TItem> ComboBoxBase<?, TItem, ?> createComboBox(
            Class<TItem> itemClass) {
        return new MultiSelectComboBox<>();
    }

    @Test
    public void initialValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        Assert.assertEquals(Collections.emptySet(), comboBox.getValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getValue_returnsImmutableSet() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));

        comboBox.getValue().add("baz");
    }

    @Test
    public void setValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));

        Assert.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
        // should refresh web components selectedItems property
        JsonArray jsonArray = (JsonArray) comboBox.getElement()
                .getPropertyRaw("selectedItems");
        Assert.assertEquals(2, jsonArray.length());
    }

    @Test
    public void setValueNull_setsEmptyValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));
        comboBox.setValue(null);

        // should hold an empty set, rather than null
        Assert.assertEquals(Collections.emptySet(), comboBox.getValue());
        // should refresh web components selectedItems property
        JsonArray jsonArray = (JsonArray) comboBox.getElement()
                .getPropertyRaw("selectedItems");
        Assert.assertEquals(0, jsonArray.length());
    }

    @Test
    public void setValue_updateDataProvider_valueIsReset() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));
        Assert.assertEquals(Set.of("foo", "bar"), comboBox.getValue());

        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assert.assertEquals(Collections.emptySet(), comboBox.getValue());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void setValue_triggersValueChangeListener() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        HasValue.ValueChangeListener listener = Mockito
                .mock(HasValue.ValueChangeListener.class);
        ArgumentCaptor<HasValue.ValueChangeEvent> eventCaptor = ArgumentCaptor
                .forClass(HasValue.ValueChangeEvent.class);

        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.addValueChangeListener(listener);
        comboBox.setValue(Set.of("foo", "bar"));

        Mockito.verify(listener, Mockito.times(1))
                .valueChanged(eventCaptor.capture());
        Assert.assertEquals(Set.of("foo", "bar"),
                eventCaptor.getValue().getValue());
        Assert.assertFalse(eventCaptor.getValue().isFromClient());
    }

    @Test(expected = IllegalStateException.class)
    public void setValueWithoutItems_throw() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setValue(Set.of("foo", "bar"));
    }

    // https://github.com/vaadin/vaadin-flow-components/issues/391
    @Test
    public void setValueWithLazyItems_doesntThrow() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(query -> Stream.of("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));

        Assert.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
    }

    @Test
    public void setValue_disableComboBox_hasValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.setValue(Set.of("bar"));
        comboBox.setEnabled(false);
        Assert.assertEquals(Set.of("bar"), comboBox.getValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSelectedItems_returnsImmutableSet() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));

        comboBox.getSelectedItems().add("baz");
    }
}
