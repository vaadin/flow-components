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

import com.vaadin.flow.testutil.TestPath;

/**
 * Test for #9294: hiding the last column while a freeze pane is active must
 * actually hide it, not leave its frozen-row cell leaking past the grid edge.
 */
@TestPath("spreadsheet-last-column")
public class SpreadsheetLastColumnIT extends AbstractSpreadsheetIT {

    // Last column (index 3 -> 1-based class col4) in the frozen top row.
    private static final By LAST_FROZEN_CELL = By
            .cssSelector(".top-right-pane .col4.row1");

    @Before
    public void init() {
        open();
    }

    @Test
    public void freezePaneActive_hideLastColumn_columnNotDisplayed() {
        // Sanity check: the last column's frozen-row cell is initially shown.
        Assert.assertNotEquals("none", findElementInShadowRoot(LAST_FROZEN_CELL)
                .getCssValue("display"));

        findElement(By.id("hide-last-column")).click();

        waitUntil(driver -> "none"
                .equals(findElementInShadowRoot(LAST_FROZEN_CELL)
                        .getCssValue("display")));
    }
}
