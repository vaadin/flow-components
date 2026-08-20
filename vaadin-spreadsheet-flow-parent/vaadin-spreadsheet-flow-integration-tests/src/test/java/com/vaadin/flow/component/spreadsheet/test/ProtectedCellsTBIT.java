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
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ProtectedCellsTBIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        loadFile("protected_sheet_examples.xlsx");
    }

    @Test
    public void sheetHasUnprotectedRowsAndColumns_trySetCellValue_onlyUnlockedCellsEditable() {
        checkProtectionInCell("B2", true);
        checkProtectionInCell("C2", false);
        checkProtectionInCell("D2", true);
        checkProtectionInCell("B3", false);
        checkProtectionInCell("C3", false);
        checkProtectionInCell("D3", false);
        checkProtectionInCell("B4", true);
        checkProtectionInCell("C4", false);
        checkProtectionInCell("D4", true);
    }

    @Test
    public void sheetHasUnprotectedColumns_trySetCellValue_onlyUnlockedCellsEditable() {
        selectSheetAt(1);

        checkProtectionInCell("A5", false);
        checkProtectionInCell("B5", true);
        checkProtectionInCell("C5", false);
        checkProtectionInCell("D5", true);
        checkProtectionInCell("A4", false);
        checkProtectionInCell("B4", true);
        checkProtectionInCell("C4", false);
        checkProtectionInCell("D4", true);
        checkProtectionInCell("A3", false);
        checkProtectionInCell("B3", true);
        checkProtectionInCell("C3", false);
        checkProtectionInCell("D3", true);
        checkProtectionInCell("A2", false);
        checkProtectionInCell("B2", true);
        checkProtectionInCell("C2", false);
        checkProtectionInCell("D2", true);
    }

    @Test
    public void sheetHasUnprotectedRows_trySetCellValue_onlyUnlockedCellsEditable() {
        selectSheetAt(2);

        checkProtectionInCell("B1", false);
        checkProtectionInCell("C1", false);
        checkProtectionInCell("D1", false);
        checkProtectionInCell("B2", true);
        checkProtectionInCell("C2", true);
        checkProtectionInCell("D2", true);
        checkProtectionInCell("B3", false);
        checkProtectionInCell("C3", false);
        checkProtectionInCell("D3", false);
        checkProtectionInCell("B4", true);
        checkProtectionInCell("C4", true);
        checkProtectionInCell("D4", true);
    }

    @Ignore("Ignored until https://github.com/vaadin/flow-components/issues/3233 is fixed")
    @Test
    public void sheetHasUnprotectedRanges_trySetCellValue_onlyUnlockedCellsEditable() {
        selectSheetAt(3);

        checkProtectionInCell("D1", false);
        checkProtectionInCell("E1", true);
        checkProtectionInCell("F1", true);
        checkProtectionInCell("D2", true);
        checkProtectionInCell("E2", true);
        checkProtectionInCell("F2", true);
        checkProtectionInCell("D3", false);
        checkProtectionInCell("E3", true);
        checkProtectionInCell("F3", false);
        checkProtectionInCell("D4", true);
        checkProtectionInCell("E4", true);
        checkProtectionInCell("F4", true);
        checkProtectionInCell("D5", false);
        checkProtectionInCell("E5", false);
        checkProtectionInCell("F5", false);
        checkProtectionInCell("D6", true);
        checkProtectionInCell("E6", true);
        checkProtectionInCell("F6", true);
    }

    @Test
    public void sheetIsAllLocked_trySetCellValue_noCellEditable() {
        selectSheetAt(4);

        checkProtectionInCell("B2", true);
        checkProtectionInCell("C2", true);
        checkProtectionInCell("D2", true);
        checkProtectionInCell("B3", true);
        checkProtectionInCell("C3", true);
        checkProtectionInCell("D3", true);
        checkProtectionInCell("B4", true);
        checkProtectionInCell("C4", true);
        checkProtectionInCell("D4", true);
    }

    @Ignore("Test ignored since it always passes locally but never on CI")
    @Test
    public void sheetIsAllLocked_changeDefaultStyleAndTrySetCellValue_allCellsEditable() {
        selectSheetAt(4);
        loadTestFixture(TestFixtures.DefaultStyleUnlocked);

        checkProtectionInCell("B2", false);
        checkProtectionInCell("C2", false);
        checkProtectionInCell("D2", false);
        checkProtectionInCell("B3", false);
        checkProtectionInCell("C3", false);
        checkProtectionInCell("D3", false);
        checkProtectionInCell("B4", false);
        checkProtectionInCell("C4", false);
        checkProtectionInCell("D4", false);
    }

    @Test
    public void sheetIsAllUnlocked_trySetCellValue_allCellsEditable() {
        selectSheetAt(5);

        checkProtectionInCell("B2", false);
        checkProtectionInCell("C2", false);
        checkProtectionInCell("D2", false);
        checkProtectionInCell("B3", false);
        checkProtectionInCell("C3", false);
        checkProtectionInCell("D3", false);
        checkProtectionInCell("B4", false);
        checkProtectionInCell("C4", false);
        checkProtectionInCell("D4", false);
    }

    private void checkProtectionInCell(String address,
            boolean shouldBeProtected) {
        SheetCellElement cell = getSpreadsheet().getCellAt(address);
        boolean editable = isCellEditable(cell);
        Assert.assertEquals(
                address + (shouldBeProtected ? " should be protected"
                        : " should be editable"),
                !shouldBeProtected, editable);
    }

    /**
     * Checks if a cell is editable by double-clicking it and checking whether
     * the cell value input becomes visible.
     */
    private boolean isCellEditable(SheetCellElement cell) {
        if (!cell.isNormalCell()) {
            return false;
        }
        try {
            waitUntil(driver -> {
                cell.doubleClick();
                return getSpreadsheet().getCellValueInput().isDisplayed();
            }, 2);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
