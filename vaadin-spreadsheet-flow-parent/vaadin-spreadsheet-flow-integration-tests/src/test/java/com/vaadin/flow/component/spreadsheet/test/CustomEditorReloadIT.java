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
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.CustomEditorReloadPage;
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
        assertEditorIntact();

        getSpreadsheet().scroll(5000);
        getCommandExecutor().waitForVaadin();
        getSpreadsheet().scroll(0);
        getCommandExecutor().waitForVaadin();
        waitUntil(driver -> getEditorElementCount() > 0);

        assertEditorIntact();
    }

    @Test
    public void editorSelected_columnSelected_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        assertEditorIntact();

        selectColumn("B");
        getCommandExecutor().waitForVaadin();

        assertEditorIntact();
    }

    @Test
    public void editorSelected_rangeSelected_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        assertEditorIntact();

        selectCell("B5", false, true);
        getCommandExecutor().waitForVaadin();

        assertEditorIntact();
    }

    @Test
    public void editorSelected_rowResized_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        assertEditorIntact();

        resizeRow(2);

        assertEditorIntact();
    }

    @Test
    public void editorSelected_columnResized_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        assertEditorIntact();

        resizeColumn("B");

        assertEditorIntact();
    }

    @Test
    public void editorSelected_otherCellValueCommitted_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        assertEditorIntact();
        int initialEditorCount = getEditorElementCount();

        // Committing a plain cell runs the internal updateMarkedCells refresh,
        // which must reuse the cached editors instead of wiping them.
        setCellValue("A4", "committed");
        getCommandExecutor().waitForVaadin();

        assertEditorIntact();
        Assert.assertEquals(initialEditorCount, getEditorElementCount());
    }

    @Test
    public void editorValueSet_applicationRefreshedEditedCell_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();
        assertEditorIntact();

        // Picking a value makes the page call refreshCells, as in issue #9180.
        // That refresh must not recreate the editor, or the picked value is
        // replaced by whatever onCustomEditorDisplayed sets.
        getEditor("B2").selectByText("Cherry");
        getCommandExecutor().waitForVaadin();

        assertEditorIntact("Cherry");
    }

    @Test
    public void plainCellEditorOpened_editorCellSelected_editorStillShown() {
        // Open the built-in inline editor on a plain cell, then select a cell
        // that has a custom editor. That editor must still be rendered.
        getInlineEditor("B5");
        getCommandExecutor().waitForVaadin();

        clickCell("B2");
        getCommandExecutor().waitForVaadin();

        Assert.assertNotNull("Custom editor disappeared from B2",
                getEditor("B2"));
        // Not enough that it exists: the reported symptom was wiped text.
        assertEditorIntact();
    }

    @Test
    public void editorSelected_visibleContentsReloaded_editorRecreated() {
        selectEditorCellB2();
        assertEditorIntact();

        clickReload();
        getCommandExecutor().waitForVaadin();
        // The explicit reload recreates the editors from the factory without
        // re-firing onCustomEditorDisplayed, so the fresh editor is empty.
        assertEditorIntact("");
    }

    /**
     * Asserts B2 still holds the value onCustomEditorDisplayed wrote, i.e. the
     * editor was reused rather than replaced.
     */
    private void assertEditorIntact() {
        assertEditorIntact(CustomEditorReloadPage.CALLBACK_VALUE);
    }

    /**
     * Asserts that B2 shows the given editor value and that
     * onCustomEditorDisplayed has fired exactly once, i.e. only for the initial
     * selection of B2.
     */
    private void assertEditorIntact(String expectedValue) {
        Assert.assertEquals("Unexpected editor value in B2", expectedValue,
                getComboBoxValue("B2"));
        Assert.assertEquals("Unexpected onCustomEditorDisplayed call count",
                "1", getCallbackCount());
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

    private int getEditorElementCount() {
        return getSpreadsheet().$(ComboBoxElement.class).all().size();
    }

    private void clickReload() {
        $("vaadin-button").id("reloadBtn").click();
    }

    private ComboBoxElement getEditor(String cellAddress) {
        return getCellEditor(cellAddress, ComboBoxElement.class);
    }

    private String getComboBoxValue(String cellAddress) {
        var editor = getEditor(cellAddress);
        return editor == null ? null : editor.getInputElementValue();
    }
}
