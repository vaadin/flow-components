/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-grid/column-resize-event")
public class ColumnResizeIT extends AbstractComponentIT {

    private GridElement grid;
    private final double RESIZE_AMOUNT_PX = -100;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id(ColumnResizeEventPage.GRID_ID));
        grid = $(GridElement.class).id(ColumnResizeEventPage.GRID_ID);
    }

    @Test
    public void columnWidthsAreSetCorrectly() {
        resizeSecondColumnBy(RESIZE_AMOUNT_PX);

        WebElement resizedColIdMessage = findElement(
                By.id(ColumnResizeEventPage.RESIZED_COLUMN_ID_MESSAGE));
        Assert.assertEquals("ID of resized column did not match expected one.",
                ColumnResizeEventPage.RESIZED_COLUMN_ID,
                resizedColIdMessage.getText());

        WebElement colFlexGrowsMessage = findElement(
                By.id(ColumnResizeEventPage.FLEX_GROWS_COLUMN_VALUES_MESSAGE));
        Assert.assertEquals(
                "Column flex-grow values did not match expected ones.", "0|0|1",
                colFlexGrowsMessage.getText());

        WebElement colWidthsMessage = findElement(
                By.id(ColumnResizeEventPage.WIDTHS_COLUMN_VALUES_MESSAGE));
        String[] colWidths = colWidthsMessage.getText().split("\\|");

        Assert.assertEquals("Expected 3 column widths from the event.",
                colWidths.length, 3);

        for (String colWidth : colWidths) {
            Assert.assertTrue(
                    "Expected column width value to end with 'px'. Actual value was: "
                            + colWidth,
                    colWidth.endsWith("px"));
        }
    }

    private void resizeSecondColumnBy(double pixels) {
        TestBenchElement resizeHandle = grid.getHeaderCell(1)
                .findElement(By.cssSelector("div[part='resize-handle']"));

        Actions actions = new Actions(driver);
        actions.clickAndHold(resizeHandle);
        actions.moveByOffset((int) pixels, 0);
        actions.release(resizeHandle);
        actions.perform();
    }
}
