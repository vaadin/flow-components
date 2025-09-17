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
package com.vaadin.flow.component.grid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.dom.Element;

public class AbstractGridMultiSelectionModelTest {

    private Set<String> selected;
    private Set<String> deselected;
    private Grid<String> grid;
    private DataCommunicatorTest.MockUI ui;

    @Before
    public void setup() {
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

    @Test
    public void select_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // select first
        grid.getSelectionModel().select("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select second, which equals all selected
        grid.getSelectionModel().select("bar");
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void selectFromClient_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // select first
        grid.getSelectionModel().selectFromClient("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select second, which equals all selected
        grid.getSelectionModel().selectFromClient("bar");
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void deselect_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect first
        grid.getSelectionModel().deselect("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect second, which equals none selected
        grid.getSelectionModel().deselect("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void deselectFromClient_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect first
        grid.getSelectionModel().deselectFromClient("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect second, which equals none selected
        grid.getSelectionModel().deselectFromClient("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void selectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void clientSelectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientSelectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void deselectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        grid.getSelectionModel().deselectAll();
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void clientDeselectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientDeselectAll();
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void updateSelection_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // Select all
        grid.asMultiSelect().updateSelection(Set.of("foo", "bar"), Set.of());
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect single
        grid.asMultiSelect().updateSelection(Set.of(), Set.of("foo"));
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect all
        grid.asMultiSelect().updateSelection(Set.of(), Set.of("bar"));
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    public void setFilterUsingDataView_clientSelectAll_selectionEventContainsFilteredValues() {
        grid.setSelectionMode(SelectionMode.MULTI);
        List<String> items = List.of("foo", "bar");
        ListDataView<String, ?> dataView = grid.setItems(items);
        dataView.setFilter(items.get(0)::equals);

        grid.addSelectionListener(e -> {
            Assert.assertEquals(
                    "Selected item count does not match data view item count",
                    dataView.getItems().count(),
                    e.getAllSelectedItems().size());
            Assert.assertTrue("Selected items do not contain filtered item",
                    e.getAllSelectedItems().contains(items.get(0)));
        });

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientSelectAll();

        Assert.assertEquals(
                "Selected item count does not match data view item count",
                dataView.getItems().count(), grid.getSelectedItems().size());
        Assert.assertTrue("Selected items do not contain filtered item",
                grid.getSelectedItems().contains(items.get(0)));
    }

    @Test
    public void dragSelect_updatesColumnAttribute() {
        grid.setSelectionMode(SelectionMode.MULTI);
        Element columnElement = getGridSelectionColumn(grid).getElement();

        Assert.assertFalse(columnElement.getProperty("dragSelect", false));

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .setDragSelect(true);
        Assert.assertTrue(columnElement.getProperty("dragSelect", false));

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .setDragSelect(false);
        Assert.assertFalse(columnElement.getProperty("dragSelect", false));
    }

    @Test
    public void setFilterUsingDataView_serverSelectAll_selectionEventContainsFilteredValues() {
        grid.setSelectionMode(SelectionMode.MULTI);
        List<String> items = List.of("foo", "bar");
        ListDataView<String, ?> dataView = grid.setItems(items);
        dataView.setFilter(items.get(0)::equals);

        grid.addSelectionListener(e -> {
            Assert.assertEquals(
                    "Selected item count does not match data view item count",
                    dataView.getItems().count(),
                    e.getAllSelectedItems().size());
            Assert.assertTrue("Selected items do not contain filtered item",
                    e.getAllSelectedItems().contains(items.get(0)));
        });

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();

        Assert.assertEquals(
                "Selected item count does not match data view item count",
                dataView.getItems().count(), grid.getSelectedItems().size());
        Assert.assertTrue("Selected items do not contain filtered item",
                grid.getSelectedItems().contains(items.get(0)));
    }

    @Test
    public void selectFromClient_withItemSelectableProvider_preventsSelection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // prevents selection of non-selectable item
        selectionModel.selectFromClient("foo");
        Assert.assertEquals(Set.of(), grid.getSelectedItems());

        // allows selection of selectable item
        selectionModel.selectFromClient("bar");
        Assert.assertEquals(Set.of("bar"), grid.getSelectedItems());
    }

    @Test
    public void deselectFromClient_withItemSelectableProvider_preventsDeselection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // prevents deselection of non-selectable item
        selectionModel.select("foo");
        selectionModel.deselectFromClient("foo");
        Assert.assertEquals(Set.of("foo"), grid.getSelectedItems());

        // allows deselection of selectable item
        selectionModel.select("bar");
        selectionModel.deselectFromClient("bar");
        Assert.assertEquals(Set.of("foo"), grid.getSelectedItems());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void selectFromClient_clientItemToggleEventIsFired() {
        grid.setItems("Item 0", "Item 1");
        grid.setSelectionMode(SelectionMode.MULTI);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        SelectionListener<Grid<String>, String> selectionListenerSpy = Mockito
                .spy(SelectionListener.class);
        selectionModel.addSelectionListener(selectionListenerSpy);

        ComponentEventListener<ClientItemToggleEvent<String>> clientItemToggleListenerSpy = Mockito
                .spy(ComponentEventListener.class);
        selectionModel.addClientItemToggleListener(clientItemToggleListenerSpy);

        selectionModel.selectFromClient("Item 0");

        InOrder inOrder = Mockito.inOrder(selectionListenerSpy,
                clientItemToggleListenerSpy);
        inOrder.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        inOrder.verify(clientItemToggleListenerSpy, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deselectFromClient_clientItemToggleEventIsFired() {
        grid.setItems("Item 0", "Item 1");
        grid.setSelectionMode(SelectionMode.MULTI);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        selectionModel.selectFromClient("Item 0");

        SelectionListener<Grid<String>, String> selectionListenerSpy = Mockito
                .spy(SelectionListener.class);
        selectionModel.addSelectionListener(selectionListenerSpy);

        ComponentEventListener<ClientItemToggleEvent<String>> clientItemToggleListenerSpy = Mockito
                .spy(ComponentEventListener.class);
        selectionModel.addClientItemToggleListener(clientItemToggleListenerSpy);

        selectionModel.deselectFromClient("Item 0");

        InOrder inOrder = Mockito.inOrder(selectionListenerSpy,
                clientItemToggleListenerSpy);
        inOrder.verify(selectionListenerSpy, Mockito.times(1))
                .selectionChange(Mockito.any());
        inOrder.verify(clientItemToggleListenerSpy, Mockito.times(1))
                .onComponentEvent(Mockito.any());
    }

    @Test
    public void select_withItemSelectableProvider_allowsSelection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // allows selection using select
        selectionModel.select("foo");
        Assert.assertEquals(Set.of("foo"), grid.getSelectedItems());

        // allows selection using selectItems
        selectionModel.deselectAll();
        selectionModel.selectItems("foo", "bar");
        Assert.assertEquals(Set.of("foo", "bar"), grid.getSelectedItems());

        // allows selection using updateSelection
        selectionModel.deselectAll();
        selectionModel.updateSelection(Set.of("foo", "bar"), Set.of());
        Assert.assertEquals(Set.of("foo", "bar"), grid.getSelectedItems());
    }

    @Test
    public void deselect_withItemSelectableProvider_allowsDeselection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // allows deselection using deselect
        selectionModel.select("foo");
        selectionModel.deselect("foo");
        Assert.assertEquals(Set.of(), grid.getSelectedItems());

        // allows deselection using deselectItems
        selectionModel.selectItems("foo", "bar");
        selectionModel.deselectItems("foo", "bar");
        Assert.assertEquals(Set.of(), grid.getSelectedItems());

        // allows deselection using updateSelection
        selectionModel.updateSelection(Set.of("foo", "bar"), Set.of());
        selectionModel.updateSelection(Set.of(), Set.of("foo", "bar"));
        Assert.assertEquals(Set.of(), grid.getSelectedItems());
    }

    @Test
    public void selectAll_withItemSelectableProvider_works() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        selectionModel.selectAll();

        Assert.assertEquals(2, selectionModel.getSelectedItems().size());
    }

    @Test
    public void deselectAll_withItemSelectableProvider_works() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        selectionModel.selectAll();
        selectionModel.deselectAll();

        Assert.assertEquals(0, selectionModel.getSelectedItems().size());
    }

    @Test
    public void clientSelectAll_withItemSelectableProvider_ignored() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        selectionModel.clientSelectAll();

        Assert.assertEquals(0, selectionModel.getSelectedItems().size());
    }

    @Test
    public void clientDeselectAll_withItemSelectableProvider_ignored() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();
        selectionModel.selectAll();

        selectionModel.clientSelectAll();

        Assert.assertEquals(2, selectionModel.getSelectedItems().size());
    }

    @Test
    public void setItemSelectableProvider_updatesSelectAllVisibility() {
        grid.setSelectionMode(SelectionMode.MULTI);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();
        GridSelectionColumn selectionColumn = selectionModel
                .getSelectionColumn();

        // Visible initially
        Assert.assertFalse(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));

        // Set provider, should hide select all checkbox
        grid.setItemSelectableProvider(item -> false);
        Assert.assertTrue(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));

        // Try to explicitly make the checkbox visible, should still be hidden
        selectionModel.setSelectAllCheckboxVisibility(
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
        Assert.assertTrue(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));

        // Remove provider, should show select all checkbox
        grid.setItemSelectableProvider(null);
        Assert.assertFalse(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));
    }

    @Test
    public void setMultiSelect_removeGrid_setSingleSelect_addGrid_selectionColumnRemoved() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        ui.remove(grid);
        grid.setSelectionMode(SelectionMode.SINGLE);
        ui.add(grid);
        Assert.assertThrows(IllegalStateException.class,
                () -> getGridSelectionColumn(grid));
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
                getGridSelectionColumn(grid).getElement()
                        .getProperty("selectAll"));
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

        grid.setItems((DataProvider) dataProvider);
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

    private <T> GridSelectionColumn getGridSelectionColumn(Grid<T> grid) {
        Component child = grid.getChildren().findFirst().orElseThrow(
                () -> new IllegalStateException("Grid does not have a child"));
        if (!(child instanceof GridSelectionColumn)) {
            throw new IllegalStateException(
                    "First Grid child is not a GridSelectionColumn");
        }
        return (GridSelectionColumn) child;
    }
}
