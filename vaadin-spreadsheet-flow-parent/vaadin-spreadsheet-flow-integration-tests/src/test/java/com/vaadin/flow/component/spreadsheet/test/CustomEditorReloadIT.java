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

@TestPath("vaadin-spreadsheet/custom-editor-reload")
public class CustomEditorReloadIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        setSpreadsheet($(SpreadsheetElement.class).single());
        $("vaadin-combo-box").waitForFirst();
    }

    @Test
    public void editorSelected_scrolledAwayAndBack_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

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

        resizeRow(2);

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
    }

    @Test
    public void editorSelected_columnResized_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

        resizeColumn("B");

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
    }

    @Test
    public void editorSelected_visibleContentsReloaded_editorRecreated() {
        selectEditorCellB2();
        Assert.assertEquals("Banana", getComboBoxValue("B2"));

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
