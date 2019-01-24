package com.vaadin.flow.component.gridpro.test;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.component.gridpro.testbench.GridTHTDElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BasicIT extends AbstractParallelTest {

    private GridProElement grid;

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        grid = $(GridProElement.class).waitForFirst();
    }

    @Test
    public void editColumnsAdded() {
        List<TestBenchElement> columns = grid.$("vaadin-grid-pro-edit-column").all();
        Assert.assertEquals(columns.size(), 3);
    }

    @Test
    public void textEditorIsUsedForTextColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 1, "vaadin-grid-pro-edit-text-field");
    }

    @Test
    public void checkboxEditorIsUsedForCheckboxColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 2, "vaadin-grid-pro-edit-checkbox");
    }

    @Test
    public void selectEditorIsUsedForSelectColumn() {
        AssertCellEnterEditModeOnDoubleClick(0, 3, "vaadin-grid-pro-edit-select-wrapper");
    }

    @Test
    public void selectEditorOptionsAreSet() {
        GridTHTDElement cell = grid.getCell(0, 3);
        ArrayList optionsList = cell.getColumn().getOptionsList();
        Assert.assertTrue(optionsList.contains("Male"));
        Assert.assertTrue(optionsList.contains("Female"));
        Assert.assertTrue(optionsList.contains("Unknown"));
    }

    private void AssertCellEnterEditModeOnDoubleClick(Integer rowIndex, Integer colIndex, String editorTag) {
        GridTHTDElement cell = grid.getCell(rowIndex, colIndex);

        // Not in edit mode initially
        Assert.assertFalse(cell.innerHTMLContains(editorTag));

        // Entering edit mode with double click
        // Workaround(yuriy-fix): doubleClick is not working on IE11
        cell.click();
        cell.click();
        cell.innerHTMLContains(editorTag);
    }
}
