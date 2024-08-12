/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

/**
 * The issue we are trying to test has a race condition: when you scroll left
 * and then back right, if a cell element is attached before its clientWidth is
 * measured, the cell shows fine, otherwise it will display ### for needsMeasure
 * cell, because clientWidth is 0 and Cell.java#updateInnerText thinks the
 * content doesn't fit.
 *
 * Because it's a race condition, it cannot be tested reliably, we are doing the
 * best here in hope that we catch an issue if there is one.
 */
@TestPath("vaadin-spreadsheet")
public class WrongHashesOnScrollIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openSpreadsheet_scrollLeftAndRight_thereAreNoHashes()
            throws Exception {

        loadFile("wrong_hashes.xlsx");

        getSpreadsheet().scroll(250);

        getSpreadsheet().scrollLeft(1600);

        Thread.sleep(500);

        getSpreadsheet().scrollLeft(0);

        Thread.sleep(500);

        final List<WebElement> elements = findElementsInShadowRoot(
                By.cssSelector(".cell"));

        for (WebElement cell : elements) {
            assertNotEquals(
                    "Cell with class " + cell.getAttribute("class") + " fails",
                    "###", cell.getText());
        }
    }

}
