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
        textColumn = grid.addEditColumn(str -> str, EditColumnConfigurator.text((modifiedItem, columnPath) -> {}));
    }

    @Test
    public void setAllowEnterRowChange_getAllowEnterRowChange() {
        grid.setAllowEnterRowChange(true);
        Assert.assertEquals(grid.getAllowEnterRowChange(), true);
    }

    @Test
    public void setPreserveEditMode_getPreserveEditMode() {
        grid.setPreserveEditMode(true);
        Assert.assertEquals(grid.getPreserveEditMode(), true);
    }
}
