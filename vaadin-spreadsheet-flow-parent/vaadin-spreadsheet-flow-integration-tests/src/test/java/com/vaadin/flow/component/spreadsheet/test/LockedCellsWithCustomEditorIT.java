/**
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet/locked-cell-with-custom-editor")
public class LockedCellsWithCustomEditorIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        setSpreadsheet($(SpreadsheetElement.class).first());
    }

    @Test
    public void clickRegularCell_navigateVerticallyToCellWithCustomEditor_customEditorDisplayed() {
        clickCell("C4");
        getSpreadsheet().sendKeys(Keys.UP);
        getSpreadsheet().sendKeys(Keys.ENTER);
        assertCustomEditorDisplayed();
    }

    @Test
    public void clickRegularCell_navigateHorizontallyToCellWithCustomEditor_customEditorDisplayed() {
        clickCell("C4");
        getSpreadsheet().sendKeys(Keys.RIGHT);
        getSpreadsheet().sendKeys(Keys.ENTER);
        assertCustomEditorDisplayed();
    }

    @Test
    public void clickLockedCell_navigateVerticallyToCellWithCustomEditor_customEditorDisplayed() {
        clickCell("C2");
        getSpreadsheet().sendKeys(Keys.DOWN);
        getSpreadsheet().sendKeys(Keys.ENTER);
        assertCustomEditorDisplayed();
    }

    @Test
    public void clickLockedCell_navigateHorizontallyToCellWithCustomEditor_customEditorDisplayed() {
        clickCell("B3");
        getSpreadsheet().sendKeys(Keys.RIGHT);
        getSpreadsheet().sendKeys(Keys.ENTER);
        assertCustomEditorDisplayed();
    }

    private void assertCustomEditorDisplayed() {
        var editor = getDriver().switchTo().activeElement()
                .findElement(By.xpath(".."));
        Assert.assertEquals("vaadin-text-field", editor.getTagName());
    }
}
