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
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("spreadsheet-dialog")
public class SpreadsheetDialogIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void rightClickContextMenuAction_firesInModalDialog() {
        openDialog();

        triggerContextMenuActionAndAssert("Test");
    }

    @Test
    public void rightClickContextMenuAction_firesInModelessDialog() {
        findElement(By.id("toggle-modality")).click();
        openDialog();

        triggerContextMenuActionAndAssert("Other");
    }

    private void openDialog() {
        findElement(By.id("open-dialog")).click();
        setSpreadsheet($(SpreadsheetElement.class).first());
    }

    private void triggerContextMenuActionAndAssert(String action) {
        SheetCellElement cell = getSpreadsheet().getCellAt(3, 3);
        cell.contextClick();
        clickItem(action);

        WebElement lastAction = findElement(By.id("last-action"));
        Assert.assertEquals(
                "Context menu action should fire and update the page", action,
                lastAction.getText());
    }
}
