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
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.shared.InputField;
import elemental.json.JsonArray;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
        comboBox.setValue((Set<String>) null);

        // should hold an empty set, rather than null
        Assert.assertEquals(Collections.emptySet(), comboBox.getValue());
        // should refresh web components selectedItems property
        JsonArray jsonArray = (JsonArray) comboBox.getElement()
                .getPropertyRaw("selectedItems");
        Assert.assertEquals(0, jsonArray.length());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void setValue_setSameValue_doesNotTriggerChangeEvent() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));

        HasValue.ValueChangeListener valueChangeListenerMock = Mockito
                .mock(HasValue.ValueChangeListener.class);
        comboBox.addValueChangeListener(valueChangeListenerMock);
        comboBox.setValue(Set.of("foo", "bar"));

        Mockito.verify(valueChangeListenerMock, Mockito.times(0))
                .valueChanged(Mockito.any());
    }

    @Test
    public void setValueWithVarArgs() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo", "bar");

        Assert.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
    }

    @Test
    public void setValueWithVarArgs_removesDuplicates() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo", "foo", "foo");

        Assert.assertEquals(Set.of("foo"), comboBox.getValue());
    }

    @Test
    public void setValueAsCollection() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(List.of("foo", "bar"));

        Assert.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
    }

    @Test
    public void setValueAsCollection_removesDuplicates() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(List.of("foo", "foo", "foo"));

        Assert.assertEquals(Set.of("foo"), comboBox.getValue());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void setValueWithVarArgs_setSameValue_doesNotTriggerChangeEvent() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo", "bar");

        HasValue.ValueChangeListener valueChangeListenerMock = Mockito
                .mock(HasValue.ValueChangeListener.class);
        comboBox.addValueChangeListener(valueChangeListenerMock);
        comboBox.setValue("foo", "bar");

        Mockito.verify(valueChangeListenerMock, Mockito.times(0))
                .valueChanged(Mockito.any());
    }

    @Test()
    public void setValueWithEmptyVarArgs_emptySelection() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo", "bar");
        comboBox.setValue();

        Assert.assertEquals(Collections.emptySet(), comboBox.getValue());
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

    @Test
    public void changeSelection_preservesOrder() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems("One", "Two", "Three", "Four", "Five", "Six", "Seven",
                "Eight", "Nine", "Ten");

        Set<String> value = null;
        List<String> valueAsList = null;

        comboBox.select("Eight");
        comboBox.select("Two");
        comboBox.select("Four");
        value = comboBox.getValue();
        valueAsList = value.stream().collect(Collectors.toList());
        Assert.assertEquals("Eight", valueAsList.get(0));
        Assert.assertEquals("Two", valueAsList.get(1));
        Assert.assertEquals("Four", valueAsList.get(2));
        comboBox.clear();

        Set<String> linkedHashSetValue = new LinkedHashSet<>(
                Arrays.asList("Eight", "Two", "Four").stream()
                        .collect(Collectors.toList()));
        comboBox.setValue(linkedHashSetValue);
        value = comboBox.getValue();
        valueAsList = value.stream().collect(Collectors.toList());
        Assert.assertEquals("Eight", valueAsList.get(0));
        Assert.assertEquals("Two", valueAsList.get(1));
        Assert.assertEquals("Four", valueAsList.get(2));
        comboBox.clear();

        comboBox.select("Eight", "Two", "Four");
        value = comboBox.getValue();
        valueAsList = value.stream().collect(Collectors.toList());
        Assert.assertEquals("Eight", valueAsList.get(0));
        Assert.assertEquals("Two", valueAsList.get(1));
        Assert.assertEquals("Four", valueAsList.get(2));
    }

    @Test
    public void implementsInputField() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        Assert.assertTrue(
                comboBox instanceof InputField<AbstractField.ComponentValueChangeEvent<MultiSelectComboBox<String>, Set<String>>, Set<String>>);
    }

    @Test
    public void setAutoExpand_propertiesAreSet() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        // NONE
        Assert.assertEquals(MultiSelectComboBox.AutoExpandMode.NONE,
                comboBox.getAutoExpand());
        Assert.assertFalse(comboBox.getElement()
                .getProperty("autoExpandHorizontally", false));
        Assert.assertFalse(comboBox.getElement()
                .getProperty("autoExpandVertically", false));

        // HORIZONTAL
        comboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.HORIZONTAL);

        Assert.assertTrue(comboBox.getElement()
                .getProperty("autoExpandHorizontally", true));
        Assert.assertFalse(comboBox.getElement()
                .getProperty("autoExpandVertically", false));

        // VERTICAL
        comboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.VERTICAL);

        Assert.assertFalse(comboBox.getElement()
                .getProperty("autoExpandHorizontally", false));
        Assert.assertTrue(comboBox.getElement()
                .getProperty("autoExpandVertically", true));

        // BOTH
        comboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);

        Assert.assertTrue(comboBox.getElement()
                .getProperty("autoExpandHorizontally", true));
        Assert.assertTrue(comboBox.getElement()
                .getProperty("autoExpandVertically", true));
    }

    @Test
    public void setSelectedItemsOnTop() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        Assert.assertFalse(comboBox.isSelectedItemsOnTop());
        Assert.assertFalse(
                comboBox.getElement().getProperty("selectedItemsOnTop", false));

        comboBox.setSelectedItemsOnTop(true);

        Assert.assertTrue(comboBox.isSelectedItemsOnTop());
        Assert.assertTrue(
                comboBox.getElement().getProperty("selectedItemsOnTop", true));
    }

    @Test
    public void setKeepFilter() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        Assert.assertFalse(comboBox.isKeepFilter());
        Assert.assertFalse(
                comboBox.getElement().getProperty("keepFilter", false));

        comboBox.setKeepFilter(true);

        Assert.assertTrue(comboBox.isKeepFilter());
        Assert.assertTrue(
                comboBox.getElement().getProperty("keepFilter", true));
    }

    @Test
    public void setOverlayWidth_smokeTests() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setOverlayWidth(null);
        Assert.assertNull(comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
        comboBox.setOverlayWidth("30em");
        Assert.assertEquals("30em", comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
        comboBox.setOverlayWidth(-1, Unit.EM);
        Assert.assertNull(comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
        comboBox.setOverlayWidth(100, Unit.PIXELS);
        Assert.assertEquals("100.0px", comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
    }
}
