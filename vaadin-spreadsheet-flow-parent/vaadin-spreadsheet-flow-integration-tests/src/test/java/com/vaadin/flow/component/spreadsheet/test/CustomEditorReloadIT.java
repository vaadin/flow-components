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
 * Tests that reloading the visible cell contents does not re-fire
 * {@code onCustomEditorDisplayed} or wipe/duplicate custom editors
 * (vaadin/flow-components#9180). The factory returns a new editor instance on
 * every call, so the framework must preserve the editor already shown for a
 * cell instead of re-creating it.
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
    public void editorSelected_visibleContentsReloaded_callbackNotRefiredAndValuePreserved() {
        // Select the editor cell B2 via keyboard (select a plain cell, then
        // Tab) rather than clicking the combobox, which is an unreliable way
        // to change the selection.
        selectCell("A2");
        getSpreadsheet().sendKeys(Keys.TAB);
        getCommandExecutor().waitForVaadin();

        // B2 is column index 1, so the factory sets the editor value to
        // FRUITS[1] = "Banana".
        Assert.assertEquals("1", getCallbackCount());
        Assert.assertEquals("Banana", getComboBoxValue("B2"));
        int comboBoxes = countComboBoxes();

        // Reloading keeps the selection on B2, so the callback must not fire
        // again, the editor value must be preserved, and no orphan editors
        // should accumulate across repeated reloads.
        for (int i = 0; i < 3; i++) {
            clickReload();
            getCommandExecutor().waitForVaadin();
            Assert.assertEquals("1", getCallbackCount());
            Assert.assertEquals("Banana", getComboBoxValue("B2"));
            Assert.assertEquals(comboBoxes, countComboBoxes());
        }
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
