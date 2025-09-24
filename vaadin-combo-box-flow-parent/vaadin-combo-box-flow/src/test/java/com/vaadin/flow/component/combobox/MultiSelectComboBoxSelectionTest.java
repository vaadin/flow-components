/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.data.selection.MultiSelectionListener;

public class MultiSelectComboBoxSelectionTest {

    MultiSelectComboBox<String> comboBox;
    private MultiSelectionListener<MultiSelectComboBox<String>, String> selectionListenerSpy;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        comboBox = new MultiSelectComboBox<>();
        comboBox.setItems("1", "2", "3", "4", "5");
        selectionListenerSpy = Mockito.mock(MultiSelectionListener.class);
        comboBox.addSelectionListener(selectionListenerSpy);
    }

    @Test
    public void isSelected() {
        comboBox.select("2", "3");

        Assert.assertTrue(comboBox.isSelected("2"));
        Assert.assertTrue(comboBox.isSelected("3"));

        Assert.assertFalse(comboBox.isSelected("1"));
        Assert.assertFalse(comboBox.isSelected("4"));
        Assert.assertFalse(comboBox.isSelected("5"));
        Assert.assertFalse(comboBox.isSelected("99"));
    }

    @Test(expected = NullPointerException.class)
    public void isNullSelected_throws() {
        comboBox.isSelected(null);
    }

    @Test
    public void getSelectedItems() {
        comboBox.select("2", "3");

        Assert.assertEquals(Set.of("2", "3"), comboBox.getSelectedItems());
    }

    @Test
    public void setValue_updatesSelectionAndTriggersSelectionListener() {
        comboBox.setValue(Set.of("2", "3"));

        Assert.assertEquals(Set.of("2", "3"), comboBox.getValue());
        Assert.assertEquals(Set.of("2", "3"), comboBox.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setExistingValue_noChanges() {
        comboBox.setValue(Set.of("2", "3"));
        Mockito.reset(selectionListenerSpy);
        comboBox.setValue(Set.of("2", "3"));

        Assert.assertEquals(Set.of("2", "3"), comboBox.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setDifferentValue_selectionChanged() {
        comboBox.setValue(Set.of("2", "3"));
        Mockito.reset(selectionListenerSpy);
        comboBox.setValue(Set.of("1", "2"));

        Assert.assertEquals(Set.of("1", "2"), comboBox.getValue());
        Assert.assertEquals(Set.of("1", "2"), comboBox.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void changeSelection_updatesSelectionAndValueAndTriggersSelectionListener() {
        comboBox.select("1", "2", "3");
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        comboBox.deselect("2", "3");
        Assert.assertEquals(Set.of("1"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        comboBox.deselectAll();
        Assert.assertEquals(Set.of(), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of(), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateSelection_updatesSelectionAndValueAndTriggersSelectionListener() {
        comboBox.updateSelection(Set.of("1", "2", "3"), Collections.emptySet());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        comboBox.updateSelection(Collections.emptySet(), Set.of("2", "3"));
        Assert.assertEquals(Set.of("1"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void selectExistingItems_noChanges() {
        comboBox.select("1", "2", "3");
        Mockito.reset(selectionListenerSpy);

        comboBox.select();
        comboBox.select("1");
        comboBox.select("1", "2");
        comboBox.select("1", "2", "3");
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deselectUnselectedItems_noChanges() {
        comboBox.select("1", "2", "3");
        Mockito.reset(selectionListenerSpy);

        comboBox.deselect();
        comboBox.deselect("4", "5");
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), comboBox.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @Test
    public void emptySelection_deselectAll_noChanges() {
        comboBox.deselectAll();
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }
}
