/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-spreadsheet")
public class ScrollingIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void cellCustomStyle_sheetIsScrolledToRightAndLeft_cellStyleNotRemoved() {
        loadFile("freezepanels.xlsx");
        selectSheetAt(2);
        final var cellAddress = "E1";
        final var expectedCellColor = getCellColor(cellAddress);
        getSpreadsheet().scrollLeft(1000);
        getSpreadsheet().scrollLeft(0);
        final var actualCellColor = getCellColor(cellAddress);

        Assert.assertEquals(expectedCellColor, actualCellColor);
    }

    @Test
    public void customEditor_scrollAwayAndBack_editorStillWorks() {
        createNewSpreadsheet();
        loadTestFixture(TestFixtures.CustomEditor);
        var editorCell = "F2";
        var editorOptional = getCustomEditorFromCell(editorCell);
        Assert.assertTrue("Custom editor should be visible at " + editorCell
                + " before scrolling", editorOptional.isPresent());

        getSpreadsheet().scroll(5000);
        getCommandExecutor().waitForVaadin();

        getSpreadsheet().scroll(0);
        getCommandExecutor().waitForVaadin();

        editorOptional = getCustomEditorFromCell(editorCell);
        Assert.assertTrue(
                "Custom editor should still be visible at " + editorCell
                        + " after scrolling away and back",
                editorOptional.isPresent());

        clickCell(editorCell);
        var editor = editorOptional.get();
        waitUntil(driver -> editor.getPropertyBoolean("opened"), 2);
        try {
            waitUntil(driver -> !editor.getPropertyBoolean("loading"), 2);
        } catch (TimeoutException e) {
            Assert.fail("Editor should not hang in loading state");
        }
    }

    private Optional<TestBenchElement> getCustomEditorFromCell(
            String cellAddress) {
        var cell = getSpreadsheet().getCellAt(cellAddress);
        try {
            var slot = cell.findElement(By.tagName("slot"));
            var slotName = slot.getDomAttribute("name");
            var editor = getSpreadsheet()
                    .findElement(By.cssSelector("[slot='" + slotName + "']"));
            return Optional.of(editor);
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }
}
