/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;

public class GridDelegatesToSelectionModelTest {

    private GridSelectionModel<String> selectionModelMock;

    private CustomGrid grid;

    private class CustomGrid extends Grid<String> {
        CustomGrid() {
            setSelectionModel(selectionModelMock, SelectionMode.SINGLE);
        }
    }

    @Before
    public void init() {
        selectionModelMock = Mockito.mock(GridSelectionModel.class);
        grid = new CustomGrid();
    }

    @Test
    public void grid_getSelectedItems_delegated_to_SelectionModel() {
        grid.getSelectedItems();
        verify(selectionModelMock).getSelectedItems();
    }

    @Test
    public void grid_select_delegated_to_SelectionModel() {
        grid.select("");
        verify(selectionModelMock).select("");
    }

    @Test
    public void grid_deselect_delegated_to_SelectionModel() {
        grid.deselect("");
        verify(selectionModelMock).deselect("");
    }

    @Test
    public void grid_deselectAll_delegated_to_SelectionModel() {
        grid.deselectAll();
        verify(selectionModelMock).deselectAll();
    }
}
