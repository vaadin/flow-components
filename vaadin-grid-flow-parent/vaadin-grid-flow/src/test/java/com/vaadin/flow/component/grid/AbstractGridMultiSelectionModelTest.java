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
package com.vaadin.flow.component.grid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

class AbstractGridMultiSelectionModelTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Set<String> selected;
    private Set<String> deselected;
    private Grid<String> grid;

    @BeforeEach
    void setup() {
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

        ui.add(grid);
    }

    @Test
    void select_singleItemSignature_sendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");
        Assertions.assertEquals(1, selected.size());
        Assertions.assertEquals("foo", selected.iterator().next());
    }

    @Test
    void select_singleItemSignature_selectFormClient_dontSendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.getSelectionModel().selectFromClient("foo");
        Assertions.assertEquals(0, selected.size());
    }

    @Test
    void deselect_singleItemSignature_sendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.deselect("foo");
        Assertions.assertEquals(1, deselected.size());
        Assertions.assertEquals("foo", deselected.iterator().next());
    }

    @Test
    void singleItemSignature_deselectFormClient_dontSendToClientSide() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        grid.select("foo");

        grid.getSelectionModel().deselectFromClient("foo");
        Assertions.assertEquals(0, deselected.size());
    }

    @Test
    void isSelectAllCheckboxVisible_withInMemoryDataProviderAndDefaultVisibilityMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndDefaultVisibilityMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndDefaultVisibilityMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    void isSelectAllCheckboxVisible_withInMemoryDataProviderAndVisibleMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndVisibleMode_visible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndVisibleMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    void isSelectAllCheckboxVisible_withInMemoryDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    void isSelectAllCheckboxVisible_withDefinedSizeLazyDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    void isSelectAllCheckboxVisible_withUnknownSizeLazyDataProviderAndHiddenMode_notVisible() {
        verifySelectAllCheckboxVisibilityInMultiSelectMode(false, true, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    void selectFromClient_inMemoryDataProviderWithDefaultVisibility_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, true, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);
    }

    @Test
    void selectFromClient_lazyDefinedSizeDataProviderWithDefaultVisibility_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);

    }

    @Test
    void selectFromClient_lazyUnknownSizeDataProviderWithDefaultVisibility_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.DEFAULT);

    }

    @Test
    void selectFromClient_inMemoryDataProviderWithVisible_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, true, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
    }

    @Test
    void selectFromClient_lazyDefinedSizeDataProviderWithVisible_updatesCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, true,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);

    }

    @Test
    void selectFromClient_lazyUnknownSizeDataProviderWithVisible_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);

    }

    @Test
    void selectFromClient_inMemoryDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                true, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    void selectFromClient_lazyDefinedSizeDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, false, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);

    }

    @Test
    void selectFromClient_lazyUnknownSizeDataProviderWithHidden_skipsUpdateCheckboxState() {
        verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
                false, true, false, false,
                GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
    }

    @Test
    void select_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // select first
        grid.getSelectionModel().select("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select second, which equals all selected
        grid.getSelectionModel().select("bar");
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void selectFromClient_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // select first
        grid.getSelectionModel().selectFromClient("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select second, which equals all selected
        grid.getSelectionModel().selectFromClient("bar");
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void deselect_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect first
        grid.getSelectionModel().deselect("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect second, which equals none selected
        grid.getSelectionModel().deselect("bar");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void deselectFromClient_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect first
        grid.getSelectionModel().deselectFromClient("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect second, which equals none selected
        grid.getSelectionModel().deselectFromClient("bar");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void selectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void clientSelectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientSelectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void deselectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        grid.getSelectionModel().deselectAll();
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void clientDeselectAll_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // start with all selected
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientDeselectAll();
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void updateSelection_updatesCheckboxStates() {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItems("foo", "bar");
        Element columnElement = getGridSelectionColumn(grid).getElement();

        // Select all
        grid.asMultiSelect().updateSelection(Set.of("foo", "bar"), Set.of());
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect single
        grid.asMultiSelect().updateSelection(Set.of(), Set.of("foo"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect all
        grid.asMultiSelect().updateSelection(Set.of(), Set.of("bar"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void setFilterUsingDataView_clientSelectAll_selectionEventContainsFilteredValues() {
        grid.setSelectionMode(SelectionMode.MULTI);
        List<String> items = List.of("foo", "bar");
        ListDataView<String, ?> dataView = grid.setItems(items);
        dataView.setFilter(items.get(0)::equals);

        grid.addSelectionListener(e -> {
            Assertions.assertEquals(dataView.getItems().count(),
                    e.getAllSelectedItems().size(),
                    "Selected item count does not match data view item count");
            Assertions.assertTrue(
                    e.getAllSelectedItems().contains(items.get(0)),
                    "Selected items do not contain filtered item");
        });

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .clientSelectAll();

        Assertions.assertEquals(dataView.getItems().count(),
                grid.getSelectedItems().size(),
                "Selected item count does not match data view item count");
        Assertions.assertTrue(grid.getSelectedItems().contains(items.get(0)),
                "Selected items do not contain filtered item");
    }

    @Test
    void dragSelect_updatesColumnAttribute() {
        grid.setSelectionMode(SelectionMode.MULTI);
        Element columnElement = getGridSelectionColumn(grid).getElement();

        Assertions.assertFalse(columnElement.getProperty("dragSelect", false));

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .setDragSelect(true);
        Assertions.assertTrue(columnElement.getProperty("dragSelect", false));

        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .setDragSelect(false);
        Assertions.assertFalse(columnElement.getProperty("dragSelect", false));
    }

    @Test
    void setFilterUsingDataView_serverSelectAll_selectionEventContainsFilteredValues() {
        grid.setSelectionMode(SelectionMode.MULTI);
        List<String> items = List.of("foo", "bar");
        ListDataView<String, ?> dataView = grid.setItems(items);
        dataView.setFilter(items.get(0)::equals);

        grid.addSelectionListener(e -> {
            Assertions.assertEquals(dataView.getItems().count(),
                    e.getAllSelectedItems().size(),
                    "Selected item count does not match data view item count");
            Assertions.assertTrue(
                    e.getAllSelectedItems().contains(items.get(0)),
                    "Selected items do not contain filtered item");
        });

        ((AbstractGridMultiSelectionModel<String>) grid.getSelectionModel())
                .selectAll();

        Assertions.assertEquals(dataView.getItems().count(),
                grid.getSelectedItems().size(),
                "Selected item count does not match data view item count");
        Assertions.assertTrue(grid.getSelectedItems().contains(items.get(0)),
                "Selected items do not contain filtered item");
    }

    @Test
    void selectFromClient_withItemSelectableProvider_preventsSelection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // prevents selection of non-selectable item
        selectionModel.selectFromClient("foo");
        Assertions.assertEquals(Set.of(), grid.getSelectedItems());

        // allows selection of selectable item
        selectionModel.selectFromClient("bar");
        Assertions.assertEquals(Set.of("bar"), grid.getSelectedItems());
    }

    @Test
    void deselectFromClient_withItemSelectableProvider_preventsDeselection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // prevents deselection of non-selectable item
        selectionModel.select("foo");
        selectionModel.deselectFromClient("foo");
        Assertions.assertEquals(Set.of("foo"), grid.getSelectedItems());

        // allows deselection of selectable item
        selectionModel.select("bar");
        selectionModel.deselectFromClient("bar");
        Assertions.assertEquals(Set.of("foo"), grid.getSelectedItems());
    }

    @SuppressWarnings("unchecked")
    @Test
    void selectFromClient_clientItemToggleEventIsFired() {
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
    void deselectFromClient_clientItemToggleEventIsFired() {
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
    void select_withItemSelectableProvider_allowsSelection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // allows selection using select
        selectionModel.select("foo");
        Assertions.assertEquals(Set.of("foo"), grid.getSelectedItems());

        // allows selection using selectItems
        selectionModel.deselectAll();
        selectionModel.selectItems("foo", "bar");
        Assertions.assertEquals(Set.of("foo", "bar"), grid.getSelectedItems());

        // allows selection using updateSelection
        selectionModel.deselectAll();
        selectionModel.updateSelection(Set.of("foo", "bar"), Set.of());
        Assertions.assertEquals(Set.of("foo", "bar"), grid.getSelectedItems());
    }

    @Test
    void deselect_withItemSelectableProvider_allowsDeselection() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> !item.equals("foo"));

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        // allows deselection using deselect
        selectionModel.select("foo");
        selectionModel.deselect("foo");
        Assertions.assertEquals(Set.of(), grid.getSelectedItems());

        // allows deselection using deselectItems
        selectionModel.selectItems("foo", "bar");
        selectionModel.deselectItems("foo", "bar");
        Assertions.assertEquals(Set.of(), grid.getSelectedItems());

        // allows deselection using updateSelection
        selectionModel.updateSelection(Set.of("foo", "bar"), Set.of());
        selectionModel.updateSelection(Set.of(), Set.of("foo", "bar"));
        Assertions.assertEquals(Set.of(), grid.getSelectedItems());
    }

    @Test
    void selectAll_withItemSelectableProvider_works() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        selectionModel.selectAll();

        Assertions.assertEquals(2, selectionModel.getSelectedItems().size());
    }

    @Test
    void deselectAll_withItemSelectableProvider_works() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        selectionModel.selectAll();
        selectionModel.deselectAll();

        Assertions.assertEquals(0, selectionModel.getSelectedItems().size());
    }

    @Test
    void clientSelectAll_withItemSelectableProvider_ignored() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();

        selectionModel.clientSelectAll();

        Assertions.assertEquals(0, selectionModel.getSelectedItems().size());
    }

    @Test
    void clientDeselectAll_withItemSelectableProvider_ignored() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setItemSelectableProvider(item -> true);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();
        selectionModel.selectAll();

        selectionModel.clientSelectAll();

        Assertions.assertEquals(2, selectionModel.getSelectedItems().size());
    }

    @Test
    void setItemSelectableProvider_updatesSelectAllVisibility() {
        grid.setSelectionMode(SelectionMode.MULTI);

        AbstractGridMultiSelectionModel<String> selectionModel = (AbstractGridMultiSelectionModel<String>) grid
                .getSelectionModel();
        GridSelectionColumn selectionColumn = selectionModel
                .getSelectionColumn();

        // Visible initially
        Assertions.assertFalse(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));

        // Set provider, should hide select all checkbox
        grid.setItemSelectableProvider(item -> false);
        Assertions.assertTrue(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));

        // Try to explicitly make the checkbox visible, should still be hidden
        selectionModel.setSelectAllCheckboxVisibility(
                GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
        Assertions.assertTrue(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));

        // Remove provider, should show select all checkbox
        grid.setItemSelectableProvider(null);
        Assertions.assertFalse(selectionColumn.getElement()
                .getProperty("_selectAllHidden", false));
    }

    @Test
    void setMultiSelect_removeGrid_setSingleSelect_addGrid_selectionColumnRemoved() {
        grid.setItems("foo", "bar");
        grid.setSelectionMode(SelectionMode.MULTI);
        ui.remove(grid);
        grid.setSelectionMode(SelectionMode.SINGLE);
        ui.add(grid);
        Assertions.assertThrows(IllegalStateException.class,
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

        Assertions.assertEquals(expectedVisibility, selectAllCheckboxVisible,
                "Unexpected select all checkbox visibility in multi-select mode");
    }

    private void verifyUpdateSelectAllCheckboxStateWhenSelectFromClientInMultiSelectMode(
            boolean inMemory, boolean unknownItemCount,
            boolean expectedSizeQuery, boolean expectedCheckboxStateUpdate,
            GridMultiSelectionModel.SelectAllCheckboxVisibility visibility) {
        DataProvider<String, ?> dataProvider = customiseMultiSelectGridAndDataProvider(
                inMemory, unknownItemCount, visibility, true);

        ui.fakeClientCommunication();

        Mockito.reset(dataProvider);

        Mockito.when(dataProvider.isInMemory()).thenReturn(inMemory);

        grid.getSelectionModel().selectFromClient("foo");

        Mockito.verify(dataProvider, Mockito.times(expectedSizeQuery ? 1 : 0))
                .size(Mockito.any(Query.class));

        grid.getSelectionModel().selectFromClient("bar");

        Mockito.verify(dataProvider, Mockito.times(expectedSizeQuery ? 2 : 0))
                .size(Mockito.any(Query.class));

        Assertions.assertEquals(expectedCheckboxStateUpdate ? "true" : "false",
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
                    Assertions.fail("Unexpected size query call");
                    return 0;
                });
    }

    private DataProvider<String, ?> getInMemoryDataProvider() {
        return DataProvider.ofItems("foo", "bar");
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
