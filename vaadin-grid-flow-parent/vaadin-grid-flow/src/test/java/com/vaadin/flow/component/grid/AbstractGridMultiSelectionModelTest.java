/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;

public class AbstractGridMultiSelectionModelTest {

    private Set<String> selected;
    private Set<String> deselected;
    private Grid<String> grid;
    private DataCommunicatorTest.MockUI ui;

    @Before
    public void init() {
        selected = new HashSet<>();
        deselected = new HashSet<>();
        grid = new Grid<String>() {
            @Override
            void doClientSideSelection(Set items) {
                selected.addAll(items);
            }

            @Override
            void doClientSideDeselection(Set<String> items) {
                deselected.addAll(items);
            }

            @Override
            boolean isInActiveRange(String item) {
                // Updates are sent only for items loaded by client
                return true;
            }
        };
        ui = new DataCommunicatorTest.MockUI();
        ui.add(grid);
    }

    @Test
    public void select_singleItemSignature_sendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");
        Assert.assertEquals(1, selected.size());
        Assert.assertEquals("foo", selected.iterator().next());
    }

    @Test
    public void select_singleItemSignature_selectFormClient_dontSendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.getSelectionModel().selectFromClient("foo");
        Assert.assertEquals(0, selected.size());
    }

    @Test
    public void deselect_singleItemSignature_sendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.deselect("foo");
        Assert.assertEquals(1, deselected.size());
        Assert.assertEquals("foo", deselected.iterator().next());
    }

    @Test
    public void singleItemSignature_deselectFormClient_dontSendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.getSelectionModel().deselectFromClient("foo");
        Assert.assertEquals(0, deselected.size());
    }

    @Test
    public void treegrid_select_singleItemSignature_selectFromClient() {
        TreeGrid<String> grid = new TreeGrid<>();

        grid.setSelectionMode(SelectionMode.MULTI);
        List<String> roots = Arrays.asList("foo", "bar");
        grid.setItems(roots,
                root -> roots.contains(root) ? Arrays.asList(root + 1, root + 2)
                        : Collections.emptyList());
        // Asserting that selectFromClient does not throw any exception
        grid.getSelectionModel().selectFromClient("foo");

        Assert.assertEquals(1, grid.getSelectedItems().size());
    }

    @Test
    public void isSelectAllCheckboxVisible_withInMemoryDataProviderAndDefaultVisibilityMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndDefaultVisibilityMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndDefaultVisibilityMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void isSelectAllCheckboxVisible_withInMemoryDataProviderAndVisibleMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndVisibleMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndVisibleMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void isSelectAllCheckboxVisible_withInMemoryDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void selectFromClient_inMemoryDataProviderWithDefaultVisibility_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, true, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    public void selectFromClient_lazyDefinedSizeDataProviderWithDefaultVisibility_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);

    }

    @Test
    public void selectFromClient_lazyUnknownSizeDataProviderWithDefaultVisibility_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);

    }

    @Test
    public void selectFromClient_inMemoryDataProviderWithVisible_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, true, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    public void selectFromClient_lazyDefinedSizeDataProviderWithVisible_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);

    }

    @Test
    public void selectFromClient_lazyUnknownSizeDataProviderWithVisible_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);

    }

    @Test
    public void selectFromClient_inMemoryDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    public void selectFromClient_lazyDefinedSizeDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);

    }

    @Test
    public void selectFromClient_lazyUnknownSizeDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);

    }

    private void verifySelectAllCheckboxVisibilityInMultiSelectMode(
            boolean inMemory, boolean unknownItemCount,
            boolean expectedVisibility,
            GridMultiSelectionModel.SelectAllCheckboxVisibility visibility) {
        customiseMultiSelectGridAndDataProvider(inMemory, unknownItemCount,
                visibility, false);

        boolean selectAllCheckboxVisible = ((GridMultiSelectionModel<String>) grid
                .getSelectionModel()).isSelectAllCheckboxVisible();

        Assert.assertEquals(
                "Unexpected select all checkbox visibility in multi-select mode",
                expectedVisibility, selectAllCheckboxVisible);
    }

    private void verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
            boolean inMemory, boolean unknownItemCount,
            boolean expectedSizeQuery, boolean expectedCheckboxStateUpdate,
            GridMultiSelectionModel.SelectAllCheckboxVisibility visibility) {
        DataProvider<String, ?> dataProvider = customiseMultiSelectGridAndDataProvider(
                inMemory, unknownItemCount, visibility, true);

        fakeClientCommunication();

        Mockito.reset(dataProvider);

        Mockito.when(dataProvider.isInMemory()).thenReturn(inMemory);

        grid.getSelectionModel().selectFromClient("foo");

        Mockito.verify(dataProvider, Mockito.times(expectedSizeQuery ? 1 : 0))
                .size(Mockito.any(Query.class));

        grid.getSelectionModel().selectFromClient("bar");

        Mockito.verify(dataProvider, Mockito.times(expectedSizeQuery ? 2 : 0))
                .size(Mockito.any(Query.class));

        Assert.assertEquals(expectedCheckboxStateUpdate ? "true" : "false",
                grid.getElement().getChild(0).getProperty("selectAll"));
    }

    private DataProvider<String, ?> customiseMultiSelectGridAndDataProvider(
            boolean inMemory, boolean unknownItemCount,
            GridMultiSelectionModel.SelectAllCheckboxVisibility visibility,
            boolean mockDataProvider) {
        grid.setSelectionMode(SelectionMode.MULTI);

        GridMultiSelectionModel<String> selectionModel = (GridMultiSelectionModel<String>) grid
                .getSelectionModel();
        selectionModel.setSelectAllCheckboxVisibility(visibility);

        DataProvider<String, ?> dataProvider;

        if (inMemory) {
            dataProvider = getInMemoryDataProvider();
        } else if (unknownItemCount) {
            dataProvider = getUnknownItemCountLazyDataProvider();
        } else {
            dataProvider = getDefinedSizeLazyDataProvider();
        }

        if (mockDataProvider) {
            dataProvider = Mockito.spy(dataProvider);
        }

        grid.setDataProvider(dataProvider);
        grid.getDataCommunicator().setDefinedSize(!unknownItemCount);
        return dataProvider;
    }

    private DataProvider<String, Void> getDefinedSizeLazyDataProvider() {
        return DataProvider.fromCallbacks(
                query -> Stream.of("foo", "bar").skip(query.getOffset())
                        .limit(query.getLimit()),
                query -> (int) Stream.of("foo", "bar").skip(query.getOffset())
                        .limit(query.getLimit()).count());
    }

    private DataProvider<String, Void> getUnknownItemCountLazyDataProvider() {
        return DataProvider.fromCallbacks(query -> Stream.of("foo", "bar")
                .skip(query.getOffset()).limit(query.getLimit()), query -> {
                    Assert.fail("Unexpected size query call");
                    return 0;
                });
    }

    private DataProvider<String, ?> getInMemoryDataProvider() {
        return DataProvider.ofItems("foo", "bar");
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
