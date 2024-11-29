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
package com.vaadin.flow.component.treegrid;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.GridArrayUpdater;
import com.vaadin.flow.data.provider.ArrayUpdater.Update;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.tests.dataprovider.MockUI;

import elemental.json.JsonObject;
import elemental.json.JsonValue;

public class TreeGridSetViewportRangeByIndexPathTest {
    private MockUI ui;

    private Update updateMock = mock(Update.class);

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<JsonValue>> updateItemsCaptor = ArgumentCaptor
            .forClass(List.class);

    private TreeGrid<String> treeGrid = new TreeGrid<>() {
        @Override
        protected GridArrayUpdater createDefaultArrayUpdater() {
            return new GridArrayUpdater() {
                @Override
                public void initialize() {
                }

                @Override
                public Update startUpdate(int sizeChange) {
                    return updateMock;
                }
            };
        }
    };

    @Before
    public void init() {
        TreeData<String> treeData = new TreeData<>();
        treeData.addItems(generateItems(null, 100), (parentItem) -> {
            int depth = parentItem.split("-").length - 1;
            if (depth < 3) {
                return generateItems(parentItem, 3);
            }
            return Collections.emptyList();
        });
        treeGrid.addDataGenerator(
                (item, jsonObject) -> jsonObject.put("name", item));
        treeGrid.setTreeData(treeData);
        treeGrid.setPageSize(5);

        ui = new MockUI();
        ui.add(treeGrid);

        fakeClientCommunication();
        reset(updateMock);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void setViewportRangeByIndexPath_assertFlatIndex() {
        treeGrid.expand("Item 50", "Item 50-0", "Item 50-1", "Item 50-2");

        int ITEM_50_SIZE = 3;
        int ITEM_50_0_SIZE = 3;
        int ITEM_50_1_SIZE = 3;
        int ITEM_50_2_SIZE = 3;

        Assert.assertEquals(0,
                treeGrid.setViewportRangeByIndexPath(new int[] { 0 }, 5));

        treeGrid.getDataCommunicator().reset();

        Assert.assertEquals(40,
                treeGrid.setViewportRangeByIndexPath(new int[] { 40 }, 5));

        treeGrid.getDataCommunicator().reset();

        Assert.assertEquals(53 + ITEM_50_0_SIZE, treeGrid
                .setViewportRangeByIndexPath(new int[] { 50, 1, 0 }, 5));

        treeGrid.getDataCommunicator().reset();

        Assert.assertEquals(54 + ITEM_50_0_SIZE + ITEM_50_1_SIZE, treeGrid
                .setViewportRangeByIndexPath(new int[] { 50, 2, 0 }, 5));

        treeGrid.getDataCommunicator().reset();

        Assert.assertEquals(59 + ITEM_50_SIZE + ITEM_50_2_SIZE,
                treeGrid.setViewportRangeByIndexPath(new int[] { 59 }, 5));

        treeGrid.getDataCommunicator().reset();

        Assert.assertEquals(99,
                treeGrid.setViewportRangeByIndexPath(new int[] { 99 }, 5));
    }

    @Test
    public void setViewportRangeByIndexPath_assertViewportRange() {
        treeGrid.expandRecursively(treeGrid.getTreeData().getRootItems(), 1);

        treeGrid.setViewportRangeByIndexPath(new int[] { 0 }, 5);
        fakeClientCommunication();
        assertViewportRange(10, "Item 0", "Item 0-2");

        treeGrid.getDataCommunicator().reset();

        treeGrid.setViewportRangeByIndexPath(new int[] { 50, 0, 0 }, 5);
        fakeClientCommunication();
        assertViewportRange(15, "Item 49-2", "Item 50-2-0");

        treeGrid.getDataCommunicator().reset();

        treeGrid.setViewportRangeByIndexPath(new int[] { -1, -1, -1 }, 5);
        fakeClientCommunication();
        assertViewportRange(7, "Item 99-1-0", "Item 99-2-2");
    }

    @Test
    public void setSmallPageSize_setViewportRangeByIndexPath_throwsWhenBufferExceedsLimit() {
        treeGrid.setPageSize(20);

        treeGrid.setViewportRangeByIndexPath(new int[] { 0 }, 500);

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            treeGrid.setViewportRangeByIndexPath(new int[] { 0 }, 1001);
        });
    }

    @Test
    public void setLargePageSize_setViewportRangeByIndexPath_throwsWhenBufferExceedsLimit() {
        treeGrid.setPageSize(80);

        treeGrid.setViewportRangeByIndexPath(new int[] { 0 }, 800);

        Assert.assertThrows(IllegalArgumentException.class, () -> {
            treeGrid.setViewportRangeByIndexPath(new int[] { 0 }, 800 * 2);
        });
    }

    private LinkedList<String> captureViewportRange() {
        verify(updateMock).set(Mockito.anyInt(), updateItemsCaptor.capture());

        return updateItemsCaptor.getValue().stream().map(
                (jsonObject) -> ((JsonObject) jsonObject).getString("name"))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void assertViewportRange(int size, String firstItem,
            String lastItem) {
        LinkedList<String> viewportRange = captureViewportRange();
        Assert.assertEquals(size, viewportRange.size());
        Assert.assertEquals(firstItem, viewportRange.getFirst());
        Assert.assertEquals(lastItem, viewportRange.getLast());

        reset(updateMock);
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private List<String> generateItems(String parentItem, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> parentItem != null
                        ? "%s-%d".formatted(parentItem, i)
                        : "Item %d".formatted(i))
                .toList();
    }
}
