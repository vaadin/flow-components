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

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * Tests custom editor handling on scroll versus explicit refresh
 * (vaadin/flow-components#9180). The factory returns a new editor instance on
 * every call. Scrolling must reuse the editor already shown for a cell, so its
 * value survives and {@code onCustomEditorDisplayed} is not re-fired for an
 * unchanged selection. An explicit refresh keeps the long-standing behavior of
 * recreating editors from the factory.
 */
@TestPath("vaadin-spreadsheet/custom-editor-reload")
public class CustomEditorReloadIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        setSpreadsheet($(SpreadsheetElement.class).first());
        // Wait until the always-visible editors (B2:E2) have rendered, so the
        // first selection reliably lands on the editor instead of an
        // not-yet-loaded cell.
        waitUntil(driver -> countComboBoxes() == 4);
    }

    @Test
    public void editorSelected_scrolledAwayAndBack_callbackNotRefiredAndValuePreserved() {
        selectEditorCellB2();

        // B2 is column index 1, so the factory sets the editor value to
        // FRUITS[1] = "Banana".
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
        int comboBoxes = countComboBoxes();

        // Scroll the editor row out of view and back. The selection stays on
        // B2, so its editor must be reused: the callback must not fire again,
        // the editor value must be preserved, and no orphan editors should
        // accumulate.
        getSpreadsheet().scroll(5000);
        getCommandExecutor().waitForVaadin();
        getSpreadsheet().scroll(0);
        getCommandExecutor().waitForVaadin();
        waitUntil(driver -> countComboBoxes() == comboBoxes);

        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
        Assert.assertEquals(comboBoxes, countComboBoxes());
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

    private int countComboBoxes() {
        return getSpreadsheet().findElements(By.tagName("vaadin-combo-box"))
                .size();
    }
}
