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
        // The server answers the right-click with the actions it offers,
        // so the menu is a round trip away.
        waitUntil(webDriver -> Boolean.TRUE.equals(executeScript("""
                const menu = arguments[0].shadowRoot.querySelector(
                    'vaadin-spreadsheet-internal-context-menu');
                return !!(menu && menu.shadowRoot
                    && menu.shadowRoot.querySelector('.item'));
                """, spreadsheet)));
        // Each spreadsheet draws its own context menu inside its own
        // shadow root — which is the point of the test, so the item is
        // looked up through that instance and not at document level.
        WebElement item = spreadsheet.getContextMenu().getItem(actionLabel);
        Assert.assertNotNull(
                "No '" + actionLabel + "' item in the menu of '" + id + "'",
                item);
        item.click();
    }

    private void assertLastAction(String id, String expected) {
        WebElement output = findElement(By.id(id + "-last-action"));
        Assert.assertEquals(
                "Spreadsheet '" + id + "' should record only its own action",
                expected, output.getText());
    }
}
