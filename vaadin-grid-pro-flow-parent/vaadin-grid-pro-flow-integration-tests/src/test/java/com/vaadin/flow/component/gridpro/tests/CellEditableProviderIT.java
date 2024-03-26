package com.vaadin.flow.component.gridpro.tests;

import com.vaadin.flow.component.gridpro.testbench.GridProElement;
import com.vaadin.flow.component.gridpro.testbench.GridTHTDElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

@TestPath("vaadin-grid-pro/cell-editable-provider")
public class CellEditableProviderIT extends AbstractComponentIT {
    private GridProElement grid;

    @Before
    public void init() {
        open();
        grid = $(GridProElement.class).waitForFirst();
    }

    @Test
    public void noProvider_allCellsEditable() {
        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", true);

        assertCellEditable(1, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 1, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 2, "vaadin-grid-pro-edit-checkbox", true);
    }

    @Test
    public void setProvider_individualCellsEditable() {
        $("button").id("set-provider").click();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", false);

        assertCellEditable(1, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 1, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 2, "vaadin-grid-pro-edit-checkbox", true);
    }

    @Test
    public void setProvider_updateData_editableCellsUpdated() {
        $("button").id("set-provider").click();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", false);

        // Reverse the approved state of all transactions
        $("button").id("update-data").click();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", true);

        assertCellEditable(1, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(1, 2, "vaadin-grid-pro-edit-checkbox", false);
    }

    @Test
    public void setProvider_clearProvider_allCellsEditable() {
        $("button").id("set-provider").click();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", false);

        $("button").id("clear-provider").click();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", true);

        assertCellEditable(1, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 1, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 2, "vaadin-grid-pro-edit-checkbox", true);
    }

    @Test
    public void setProvider_detach_attach_individualCellsEditable() {
        $("button").id("set-provider").click();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", false);

        $("button").id("detach").click();
        $("button").id("attach").click();
        grid = $(GridProElement.class).waitForFirst();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", false);
    }

    @Test
    public void detach_setProvider_attach_editableCellsUpdated() {
        $("button").id("detach").click();
        $("button").id("set-provider").click();
        $("button").id("attach").click();
        grid = $(GridProElement.class).waitForFirst();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", false);
    }

    @Test
    public void setProvider_detach_updateData_attach_editableCellsUpdated() {
        $("button").id("set-provider").click();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", false);

        $("button").id("detach").click();
        $("button").id("update-data").click();
        $("button").id("attach").click();
        grid = $(GridProElement.class).waitForFirst();

        assertCellEditable(0, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 1, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(0, 2, "vaadin-grid-pro-edit-checkbox", true);

        assertCellEditable(1, 0, "vaadin-grid-pro-edit-text-field", true);
        assertCellEditable(1, 1, "vaadin-grid-pro-edit-text-field", false);
        assertCellEditable(1, 2, "vaadin-grid-pro-edit-checkbox", false);
    }

    private void assertCellEditable(Integer rowIndex, Integer colIndex,
            String editorTag, boolean editable) {
        GridTHTDElement cell = grid.getCell(rowIndex, colIndex);

        // Not in edit mode initially
        Assert.assertFalse(cell.innerHTMLContains(editorTag));

        // Entering edit mode with enter
        cell.sendKeys(Keys.ENTER);
        Assert.assertEquals(editable, cell.innerHTMLContains(editorTag));

        // Exiting edit mode with escape
        cell.sendKeys(Keys.ESCAPE);
    }
}
