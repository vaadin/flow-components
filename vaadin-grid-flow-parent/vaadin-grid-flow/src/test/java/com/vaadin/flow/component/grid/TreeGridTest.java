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

package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

public class TreeGridTest {

    private DataCommunicatorTest.MockUI ui;
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

        ui = new DataCommunicatorTest.MockUI();
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
