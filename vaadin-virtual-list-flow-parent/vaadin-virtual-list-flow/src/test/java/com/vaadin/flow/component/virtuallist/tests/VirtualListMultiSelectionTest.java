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

import static com.vaadin.flow.component.virtuallist.tests.VirtualListTestHelpers.*;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.component.virtuallist.VirtualList.SelectionMode;
import com.vaadin.flow.data.provider.CompositeDataGenerator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.selection.SelectionListener;

/**
 * Tests multi-selectable VirtualList
 */
public class VirtualListMultiSelectionTest {

    private VirtualList<String> list;
    private SelectionListener<VirtualList<String>, String> selectionListenerSpy;
    private CompositeDataGenerator<String> dataGenerator;
    private DataGenerator<String> dataGeneratorSpy;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        list = new VirtualList<>();
        list.setItems("1", "2", "3", "4", "5");
        list.setSelectionMode(SelectionMode.MULTI);

        selectionListenerSpy = Mockito.mock(SelectionListener.class);
        list.addSelectionListener(selectionListenerSpy);

        dataGenerator = getDataGenerator(list);
        dataGeneratorSpy = Mockito.mock(DataGenerator.class);
        dataGenerator.addDataGenerator(dataGeneratorSpy);
    }

    @Test
    public void setsWebComponentSelectionMode() {
        Assert.assertEquals("multi",
                list.getElement().getProperty("selectionMode"));
    }

    @Test
    public void getSelectionMode_returnsMode() {
        Assert.assertEquals(SelectionMode.MULTI, list.getSelectionMode());
    }

    @Test
    public void setSelectionMode_returnsModel() {
        var model = list.setSelectionMode(SelectionMode.MULTI);
        Assert.assertEquals(list.getSelectionModel(), model);
    }

    @Test(expected = IllegalStateException.class)
    public void asSingleSelect_throwsIfSelectionModeIsMulti() {
        list.asSingleSelect();
    }

    @Test
    public void getSelectedItems() {
        list.select("2");
        list.select("3");

        Assert.assertEquals(Set.of("2", "3"), list.getSelectedItems());
    }

    @Test
    public void select_triggersSelectionListener() {
        list.select("1");

        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void select_selectExistingValue_noChanges() {
        list.select("1");
        Mockito.reset(selectionListenerSpy);
        list.select("1");

        Assert.assertEquals(Set.of("1"), list.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void select_selectDifferentValue_selectionChanged() {
        list.select("1");
        Mockito.reset(selectionListenerSpy);
        list.select("2");

        Assert.assertEquals(Set.of("1", "2"), list.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void select_deselect_selectionChanged() {
        list.select("1");
        Mockito.reset(selectionListenerSpy);
        list.deselect("1");

        Assert.assertEquals(Set.of(), list.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void selecti_deselectAll_selectionChanged() {
        list.select("1");
        Mockito.reset(selectionListenerSpy);
        list.deselectAll();
        Assert.assertEquals(Set.of(), list.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @Test
    public void emptySelection_deselectAll_noChanges() {
        list.deselectAll();
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .selectionChange(Mockito.any());
    }

    @Test
    public void select_generateItemSelected() {
        list.select("1");
        Assert.assertTrue(generatesSelected(dataGenerator, "1"));
        Assert.assertFalse(generatesSelected(dataGenerator, "2"));
    }

    @Test
    public void deselect_generateItemSelected() {
        list.select("1");
        list.deselect("1");
        Assert.assertFalse(generatesSelected(dataGenerator, "1"));
    }

    @Test
    public void select_generateItemData() {
        list.select("1");
        Mockito.verify(dataGeneratorSpy, Mockito.times(1))
                .refreshData(Mockito.eq("1"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deselect_generateItemData() {
        list.select("1");
        Mockito.reset(dataGeneratorSpy);
        list.deselect("1");
        Mockito.verify(dataGeneratorSpy, Mockito.times(1))
                .refreshData(Mockito.eq("1"));
    }

    @Test
    public void updateSelectionFromClient_itemsSelected() {
        updateSelectionFromClient(list, Set.of("1", "2"), Set.of());
        Assert.assertEquals(Set.of("1", "2"), list.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateSelectionFromClient_itemsChanged() {
        list.select("1");
        list.select("2");
        Mockito.reset(selectionListenerSpy);
        updateSelectionFromClient(list, Set.of("3"), Set.of("1"));
        Assert.assertEquals(Set.of("2", "3"), list.getSelectedItems());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
    }

}
