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
package com.vaadin.flow.component.virtuallist.tests;

import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.component.virtuallist.VirtualList.SelectionMode;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.MultiSelectionListener;

/**
 * Tests using selection via VirtualList's MultiSelect API.
 */
public class VirtualListMultiSelectionTest {

    private VirtualList<String> list;
    private MultiSelect<VirtualList<String>, String> multiSelect;
    private MultiSelectionListener<VirtualList<String>, String> selectionListenerSpy;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        list = new VirtualList<>();
        list.setItems("1", "2", "3", "4", "5");
        list.setSelectionMode(SelectionMode.MULTI);
        multiSelect = list.asMultiSelect();
        selectionListenerSpy = Mockito.mock(MultiSelectionListener.class);
        multiSelect.addSelectionListener(selectionListenerSpy);
    }

    @Test
    public void isSelected() {
        multiSelect.select("2", "3");

        Assert.assertTrue(multiSelect.isSelected("2"));
        Assert.assertTrue(multiSelect.isSelected("3"));

        Assert.assertFalse(multiSelect.isSelected("1"));
        Assert.assertFalse(multiSelect.isSelected("4"));
        Assert.assertFalse(multiSelect.isSelected("5"));
        Assert.assertFalse(multiSelect.isSelected("99"));
    }

    @Test
    public void getSelectedItems() {
        multiSelect.select("2", "3");

        Assert.assertEquals(Set.of("2", "3"), multiSelect.getSelectedItems());
    }

    @Test
    public void setValue_updatesSelectionAndTriggersSelectionListener() {
        multiSelect.setValue(Set.of("2", "3"));

        Assert.assertEquals(Set.of("2", "3"), multiSelect.getValue());
        Assert.assertEquals(Set.of("2", "3"), multiSelect.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setExistingValue_noChanges() {
        multiSelect.setValue(Set.of("2", "3"));
        Mockito.reset(selectionListenerSpy);
        multiSelect.setValue(Set.of("2", "3"));

        Assert.assertEquals(Set.of("2", "3"), multiSelect.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setDifferentValue_selectionChanged() {
        multiSelect.setValue(Set.of("2", "3"));
        Mockito.reset(selectionListenerSpy);
        multiSelect.setValue(Set.of("1", "2"));

        Assert.assertEquals(Set.of("1", "2"), multiSelect.getValue());
        Assert.assertEquals(Set.of("1", "2"), multiSelect.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void changeSelection_updatesSelectionAndValueAndTriggersSelectionListener() {
        multiSelect.select("1", "2", "3");
        Assert.assertEquals(Set.of("1", "2", "3"),
                multiSelect.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), multiSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        multiSelect.deselect("2", "3");
        Assert.assertEquals(Set.of("1"), multiSelect.getSelectedItems());
        Assert.assertEquals(Set.of("1"), multiSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        multiSelect.deselectAll();
        Assert.assertEquals(Set.of(), multiSelect.getSelectedItems());
        Assert.assertEquals(Set.of(), multiSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateSelection_updatesSelectionAndValueAndTriggersSelectionListener() {
        multiSelect.updateSelection(Set.of("1", "2", "3"),
                Collections.emptySet());
        Assert.assertEquals(Set.of("1", "2", "3"),
                multiSelect.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), multiSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);

        multiSelect.updateSelection(Collections.emptySet(), Set.of("2", "3"));
        Assert.assertEquals(Set.of("1"), multiSelect.getSelectedItems());
        Assert.assertEquals(Set.of("1"), multiSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        Mockito.reset(selectionListenerSpy);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void selectExistingItems_noChanges() {
        multiSelect.select("1", "2", "3");
        Mockito.reset(selectionListenerSpy);

        multiSelect.select();
        multiSelect.select("1");
        multiSelect.select("1", "2");
        multiSelect.select("1", "2", "3");
        Assert.assertEquals(Set.of("1", "2", "3"),
                multiSelect.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), multiSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deselectUnselectedItems_noChanges() {
        multiSelect.select("1", "2", "3");
        Mockito.reset(selectionListenerSpy);

        multiSelect.deselect();
        multiSelect.deselect("4", "5");
        Assert.assertEquals(Set.of("1", "2", "3"),
                multiSelect.getSelectedItems());
        Assert.assertEquals(Set.of("1", "2", "3"), multiSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @Test
    public void emptySelection_deselectAll_noChanges() {
        multiSelect.deselectAll();
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }
}
