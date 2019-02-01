package com.vaadin.flow.component.gridpro;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GridProTest {

    GridPro<String> grid;
    GridPro.EditColumn<String> textColumn;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        grid = new GridPro<>();
        textColumn = grid.addEditColumn(str -> str, EditColumnConfigurator.text((item, newValue) -> {}));
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
