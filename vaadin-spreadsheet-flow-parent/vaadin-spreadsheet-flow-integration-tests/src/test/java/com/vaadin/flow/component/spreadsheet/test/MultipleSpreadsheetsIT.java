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
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("multiple-spreadsheets")
public class MultipleSpreadsheetsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void contextMenuActionFires_onCorrectSpreadsheetInstance() {
        triggerContextMenuActionOn("first", "Alpha");
        assertLastAction("first", "Alpha");
        assertLastAction("second", "");

        triggerContextMenuActionOn("second", "Beta");
        assertLastAction("first", "Alpha");
        assertLastAction("second", "Beta");
    }

    private void triggerContextMenuActionOn(String id, String actionLabel) {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).id(id);
        new Actions(getDriver()).contextClick(spreadsheet.getCellAt(2, 2))
                .perform();
        getDriver().findElement(By.xpath(
                "//div[@class='popupContent']//*[normalize-space(text())='"
                        + actionLabel + "']"))
                .click();
    }

    private void assertLastAction(String id, String expected) {
        WebElement output = findElement(By.id(id + "-last-action"));
        Assert.assertEquals(
                "Spreadsheet '" + id + "' should record only its own action",
                expected, output.getText());
    }
}
