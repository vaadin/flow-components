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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.InputField;

import tools.jackson.databind.node.ArrayNode;

class MultiSelectComboBoxTest extends ComboBoxBaseTest {
    @Override
    protected <TItem> ComboBoxBase<?, TItem, ?> createComboBox(
            Class<TItem> itemClass) {
        return new MultiSelectComboBox<>();
    }

    @Test
    void initialValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        Assertions.assertEquals(Collections.emptySet(), comboBox.getValue());
    }

    @Test
    void getValue_returnsImmutableSet() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> comboBox.getValue().add("baz"));
    }

    @Test
    void setValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));

        Assertions.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
        // should refresh web components selectedItems property
        ArrayNode jsonArray = (ArrayNode) comboBox.getElement()
                .getPropertyRaw("selectedItems");
        Assertions.assertEquals(2, jsonArray.size());
    }

    @Test
    void setValueNull_setsEmptyValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));
        comboBox.setValue((Set<String>) null);

        // should hold an empty set, rather than null
        Assertions.assertEquals(Collections.emptySet(), comboBox.getValue());
        // should refresh web components selectedItems property
        ArrayNode jsonArray = (ArrayNode) comboBox.getElement()
                .getPropertyRaw("selectedItems");
        Assertions.assertEquals(0, jsonArray.size());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void setValue_setSameValue_doesNotTriggerChangeEvent() {
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
    void setValueWithVarArgs() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo", "bar");

        Assertions.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
    }

    @Test
    void setValueWithVarArgs_removesDuplicates() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo", "foo", "foo");

        Assertions.assertEquals(Set.of("foo"), comboBox.getValue());
    }

    @Test
    void setValueAsCollection() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(List.of("foo", "bar"));

        Assertions.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
    }

    @Test
    void setValueAsCollection_removesDuplicates() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(List.of("foo", "foo", "foo"));

        Assertions.assertEquals(Set.of("foo"), comboBox.getValue());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void setValueWithVarArgs_setSameValue_doesNotTriggerChangeEvent() {
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

    @Test
    void setValueWithEmptyVarArgs_emptySelection() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue("foo", "bar");
        comboBox.setValue();

        Assertions.assertEquals(Collections.emptySet(), comboBox.getValue());
    }

    @Test
    void setValue_updateDataProvider_valueIsReset() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));
        Assertions.assertEquals(Set.of("foo", "bar"), comboBox.getValue());

        comboBox.setItems(Arrays.asList("foo", "bar"));
        Assertions.assertEquals(Collections.emptySet(), comboBox.getValue());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void setValue_triggersValueChangeListener() {
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
        Assertions.assertEquals(Set.of("foo", "bar"),
                eventCaptor.getValue().getValue());
        Assertions.assertFalse(eventCaptor.getValue().isFromClient());
    }

    @Test
    void setValueWithoutItems_throw() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        Assertions.assertThrows(IllegalStateException.class,
                () -> comboBox.setValue(Set.of("foo", "bar")));
    }

    // https://github.com/vaadin/vaadin-flow-components/issues/391
    @Test
    void setValueWithLazyItems_doesntThrow() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(query -> Stream.of("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));

        Assertions.assertEquals(Set.of("foo", "bar"), comboBox.getValue());
    }

    @Test
    void setValue_disableComboBox_hasValue() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar"));
        comboBox.setValue(Set.of("bar"));
        comboBox.setEnabled(false);
        Assertions.assertEquals(Set.of("bar"), comboBox.getValue());
    }

    @Test
    void getSelectedItems_returnsImmutableSet() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(Arrays.asList("foo", "bar", "baz"));
        comboBox.setValue(Set.of("foo", "bar"));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> comboBox.getSelectedItems().add("baz"));
    }

    @Test
    void changeSelection_preservesOrder() {
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
        Assertions.assertEquals("Eight", valueAsList.get(0));
        Assertions.assertEquals("Two", valueAsList.get(1));
        Assertions.assertEquals("Four", valueAsList.get(2));
        comboBox.clear();

        Set<String> linkedHashSetValue = new LinkedHashSet<>(
                Arrays.asList("Eight", "Two", "Four").stream()
                        .collect(Collectors.toList()));
        comboBox.setValue(linkedHashSetValue);
        value = comboBox.getValue();
        valueAsList = value.stream().collect(Collectors.toList());
        Assertions.assertEquals("Eight", valueAsList.get(0));
        Assertions.assertEquals("Two", valueAsList.get(1));
        Assertions.assertEquals("Four", valueAsList.get(2));
        comboBox.clear();

        comboBox.select("Eight", "Two", "Four");
        value = comboBox.getValue();
        valueAsList = value.stream().collect(Collectors.toList());
        Assertions.assertEquals("Eight", valueAsList.get(0));
        Assertions.assertEquals("Two", valueAsList.get(1));
        Assertions.assertEquals("Four", valueAsList.get(2));
    }

    @Test
    void implementsInputField() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        Assertions.assertTrue(
                comboBox instanceof InputField<AbstractField.ComponentValueChangeEvent<MultiSelectComboBox<String>, Set<String>>, Set<String>>);
    }

    @Test
    void setAutoExpand_propertiesAreSet() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        // NONE
        Assertions.assertEquals(MultiSelectComboBox.AutoExpandMode.NONE,
                comboBox.getAutoExpand());
        Assertions.assertFalse(comboBox.getElement()
                .getProperty("autoExpandHorizontally", false));
        Assertions.assertFalse(comboBox.getElement()
                .getProperty("autoExpandVertically", false));

        // HORIZONTAL
        comboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.HORIZONTAL);

        Assertions.assertTrue(comboBox.getElement()
                .getProperty("autoExpandHorizontally", true));
        Assertions.assertFalse(comboBox.getElement()
                .getProperty("autoExpandVertically", false));

        // VERTICAL
        comboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.VERTICAL);

        Assertions.assertFalse(comboBox.getElement()
                .getProperty("autoExpandHorizontally", false));
        Assertions.assertTrue(comboBox.getElement()
                .getProperty("autoExpandVertically", true));

        // BOTH
        comboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);

        Assertions.assertTrue(comboBox.getElement()
                .getProperty("autoExpandHorizontally", true));
        Assertions.assertTrue(comboBox.getElement()
                .getProperty("autoExpandVertically", true));
    }

    @Test
    void setSelectedItemsOnTop() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        Assertions.assertFalse(comboBox.isSelectedItemsOnTop());
        Assertions.assertFalse(
                comboBox.getElement().getProperty("selectedItemsOnTop", false));

        comboBox.setSelectedItemsOnTop(true);

        Assertions.assertTrue(comboBox.isSelectedItemsOnTop());
        Assertions.assertTrue(
                comboBox.getElement().getProperty("selectedItemsOnTop", true));
    }

    @Test
    void setKeepFilter() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();

        Assertions.assertFalse(comboBox.isKeepFilter());
        Assertions.assertFalse(
                comboBox.getElement().getProperty("keepFilter", false));

        comboBox.setKeepFilter(true);

        Assertions.assertTrue(comboBox.isKeepFilter());
        Assertions.assertTrue(
                comboBox.getElement().getProperty("keepFilter", true));
    }

    @Test
    void setOverlayWidth() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setOverlayWidth(null);
        Assertions.assertNull(comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
        comboBox.setOverlayWidth("30em");
        Assertions.assertEquals("30em", comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
        comboBox.setOverlayWidth(-1, Unit.EM);
        Assertions.assertNull(comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
        comboBox.setOverlayWidth(100, Unit.PIXELS);
        Assertions.assertEquals("100.0px", comboBox.getStyle()
                .get("--vaadin-multi-select-combo-box-overlay-width"));
    }

    @Test
    void setFilterTimeout_getFilterTimeout() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        Assertions.assertEquals(500, comboBox.getFilterTimeout());
        Assertions.assertEquals(500,
                comboBox.getElement().getProperty("_filterTimeout", 0));

        comboBox.setFilterTimeout(750);
        Assertions.assertEquals(750, comboBox.getFilterTimeout());
        Assertions.assertEquals(750,
                comboBox.getElement().getProperty("_filterTimeout", 0));
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(HasThemeVariant.class
                .isAssignableFrom(MultiSelectComboBox.class));
    }
}
