/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * Tests custom editor handling on the paths that re-render the visible cells
 * (vaadin/flow-components#9180). The factory returns a new editor instance on
 * every call. Scrolling, changing the selection (column, row, or range) and
 * resizing rows or columns all keep the same cells visible, so the editor
 * already shown for a cell must be reused: its value survives and
 * {@code onCustomEditorDisplayed} is not re-fired for an unchanged selection.
 * An explicit refresh keeps the long-standing behavior of recreating editors
 * from the factory.
 */
@TestPath("vaadin-spreadsheet/custom-editor-reload")
public class CustomEditorReloadIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        setSpreadsheet($(SpreadsheetElement.class).first());
        // Wait until the always-visible editor (B2) has rendered, so the first
        // selection reliably lands on the editor instead of a not-yet-loaded
        // cell.
        waitUntil(driver -> !getSpreadsheet()
                .findElements(By.tagName("vaadin-combo-box")).isEmpty());
    }

    @Test
    public void editorSelected_scrolledAwayAndBack_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();

        // B2 is column index 1, so the factory sets the editor value to
        // FRUITS[1] = "Banana".
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

        // Scroll the editor row out of view and back. The selection stays on
        // B2, so its editor must be reused: the callback must not fire again
        // and the editor value must be preserved.
        getSpreadsheet().scroll(5000);
        getCommandExecutor().waitForVaadin();
        getSpreadsheet().scroll(0);
        getCommandExecutor().waitForVaadin();
        waitUntil(driver -> !getSpreadsheet()
                .findElements(By.tagName("vaadin-combo-box")).isEmpty());

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
    }

    @Test
    public void editorSelected_columnSelected_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

        // Selecting a column re-renders the visible cells. B2's editor stays
        // visible, so it must be reused and its value preserved. The new
        // selected cell (B1) has no editor, so the callback does not fire
        // again.
        selectColumn("B");
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
    }

    @Test
    public void editorSelected_rangeSelected_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

        // Extending the selection to a range (B2:B5) keeps B2 as the selected
        // cell and re-renders the visible cells. The editor must be reused: the
        // callback must not fire again for the unchanged selection and the
        // editor value must be preserved.
        selectCell("B5", false, true);
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
    }

    @Test
    public void editorSelected_rowResized_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

        // Resizing the editor row re-renders the visible cells while the
        // selection stays on B2, so the editor must be reused: the callback
        // must not fire again and the editor value must be preserved.
        resizeRow(2);

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
    }

    @Test
    public void editorSelected_columnResized_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

        // Resizing the editor column re-renders the visible cells while the
        // selection stays on B2, so the editor must be reused: the callback
        // must not fire again and the editor value must be preserved.
        resizeColumn("B");

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
    }

    @Test
    public void editorSelected_visibleContentsReloaded_editorRecreated() {
        selectEditorCellB2();
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

        // An explicit refresh recreates editors from the factory (preserving
        // the long-standing behavior of reloadVisibleCellContents). The factory
        // returns a blank editor and a refresh does not invoke the callback, so
        // the previously shown value is gone.
        clickReload();
        getCommandExecutor().waitForVaadin();
        Assert.assertEquals("", getComboBoxValue("B2"));
    }

    private void selectEditorCellB2() {
        // Select the editor cell B2 via keyboard (select a plain cell, then
        // Tab) rather than clicking the combobox, which is an unreliable way to
        // change the selection.
        selectCell("A2");
        getSpreadsheet().sendKeys(Keys.TAB);
        getCommandExecutor().waitForVaadin();
    }

    private void resizeColumn(String column) {
        int index = column.charAt(0) - 'A' + 1;
        var handle = getSpreadsheet().getColumnHeader(index).getResizeHandle();
        var target = getSpreadsheet().getColumnHeader(index + 1);
        new Actions(getDriver()).dragAndDrop(handle, target).perform();
        getCommandExecutor().waitForVaadin();
    }

    private void resizeRow(int row) {
        var handle = getSpreadsheet().getRowHeader(row).getResizeHandle();
        var target = getSpreadsheet().getRowHeader(row + 1);
        new Actions(getDriver()).dragAndDrop(handle, target).perform();
        getCommandExecutor().waitForVaadin();
    }

    private String getCallbackCount() {
        return $(TestBenchElement.class).id("callbackCount").getText();
    }

    private void clickReload() {
        $("vaadin-button").id("reloadBtn").click();
    }

    private String getComboBoxValue(String cellAddress) {
        var cell = getSpreadsheet().getCellAt(cellAddress);
        try {
            var slotName = cell.findElement(By.tagName("slot"))
                    .getDomAttribute("name");
            return getSpreadsheet()
                    .findElement(By.cssSelector("[slot='" + slotName + "']"))
                    .findElement(By.cssSelector("input"))
                    .getDomProperty("value");
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
