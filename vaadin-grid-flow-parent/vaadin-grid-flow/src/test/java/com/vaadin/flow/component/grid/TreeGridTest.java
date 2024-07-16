/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class TreeGridTest {

    private MockUI ui;
    private TreeGrid<Item> treeGrid;

    @Before
    public void init() {
        Item item1 = new Item("key 1");
        Item item2 = new Item("key 2");
        treeGrid = new TreeGrid<>();
        TreeData<Item> treeData = new TreeData<>();
        treeData.addItem(null, item1);
        treeData.addItem(null, item2);
        HierarchicalDataProvider<Item, ?> treeDataProvider = new TreeDataProvider<>(
                treeData);
        treeGrid.setDataProvider(treeDataProvider);

        ui = new MockUI();
        ui.add(treeGrid);
    }

    @Test
    public void uniqueKeyProviderNotSet_usesKeyMapper() {
        fakeClientCommunication();

        Assert.assertNotNull(
                treeGrid.getDataCommunicator().getKeyMapper().get("1"));
        Assert.assertNotNull(
                treeGrid.getDataCommunicator().getKeyMapper().get("2"));
        Assert.assertNull(
                treeGrid.getDataCommunicator().getKeyMapper().get("3"));
    }

    @Test
    public void uniqueKeyProviderSet_usesUniqueKeyProvider() {
        treeGrid.setUniqueKeyProvider(Item::toString);
        fakeClientCommunication();

        Assert.assertNull(
                treeGrid.getDataCommunicator().getKeyMapper().get("1"));
        Assert.assertNull(
                treeGrid.getDataCommunicator().getKeyMapper().get("2"));
        Assert.assertNotNull(
                treeGrid.getDataCommunicator().getKeyMapper().get("key 1"));
        Assert.assertNotNull(
                treeGrid.getDataCommunicator().getKeyMapper().get("key 2"));
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private static class Item {
        private final String key;

        public Item(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

}
