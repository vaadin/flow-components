package com.vaadin.flow.component.gridpro;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid;

public class GridProTest {

    GridPro<String> grid;
    Grid.Column<String> textColumn;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        grid = new GridPro<>();
        textColumn = grid.addEditColumn(str -> str).text((item, newValue) -> {});
    }

    @Test
    public void setEnterNextRow_getEnterNextRow() {
        grid.setEnterNextRow(true);
        Assert.assertEquals(grid.getEnterNextRow(), true);
    }

    @Test
    public void setKeepEditorOpen_getKeepEditorOpen() {
        grid.setKeepEditorOpen(true);
        Assert.assertEquals(grid.getKeepEditorOpen(), true);
    }
}
