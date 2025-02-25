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

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class CustomFormattingIT extends AbstractSpreadsheetIT {

    // These values are specific to the test sheet
    private static final int ROW_OFFSET = 6;
    private static final int COL_OFFSET = 1;
    private static final int ROW_COUNT = 8;
    private static final int COL_COUNT = 8;

    // The 8 basic named colors, in order in the document, as CSS strings
    private static final String[] NAMED_COLOR_CSS = { "rgba(0, 0, 0, 1)", // black
            "rgba(0, 0, 255, 1)", // blue
            "rgba(0, 255, 255, 1)", // cyan
            "rgba(0, 255, 0, 1)", // green
            "rgba(255, 0, 0, 1)", // red
            "rgba(255, 255, 255, 1)", // white
            "rgba(255, 255, 0, 1)", // yellow
            "rgba(255, 0, 255, 1)" // magenta
    };

    // The remaining 7 rows of indexed colors, as CSS strings
    private static final String[] INDEXED_COLOR_CSS = new String[56];
    static {
        // Color values in hex, taken from the modified CellFormatPart.java
        // in vaadin-spreadsheet-flow. These are the 56 indexed Excel colors
        final int[] rgb_hex = { 0x000000, 0xFFFFFF, 0xFF0000, 0x00FF00,
                0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF, 0x800000, 0x008000,
                0x000080, 0x808000, 0x800080, 0x008080, 0xC0C0C0, 0x808080,
                0x9999FF, 0x993366, 0xFFFFCC, 0xCCFFFF, 0x660066, 0xFF8080,
                0x0066CC, 0xCCCCFF, 0x000080, 0xFF00FF, 0xFFFF00, 0x00FFFF,
                0x800080, 0x800000, 0x008080, 0x0000FF, 0x00CCFF, 0xCCFFFF,
                0xCCFFCC, 0xFFFF99, 0x99CCFF, 0xFF99CC, 0xCC99FF, 0xFFCC99,
                0x3366FF, 0x33CCCC, 0x99CC00, 0xFFCC00, 0xFF9900, 0xFF6600,
                0x666699, 0x969696, 0x003366, 0x339966, 0x003300, 0x333300,
                0x993300, 0x993366, 0x333399, 0x333333 };

        // Convert table to CSS strings
        for (int i = 0; i < 56; ++i) {
            INDEXED_COLOR_CSS[i] = "rgba(" + ((rgb_hex[i] >>> 16) & 0xff) + ", "
                    + ((rgb_hex[i] >>> 8) & 0xff) + ", " + (rgb_hex[i] & 0xff)
                    + ", 1)";
        }
    }

    @Before
    public void init() {
        open();
        loadFile("custom_formatting_rainbow.xlsx");
    }

    private void validateAllColors() {
        validateNamedColors();
        validateIndexedColors();
    }

    private void validateNamedColors() {
        // Verify all basic colors
        for (int col = 0; col < COL_COUNT; ++col) {
            SheetCellElement cell = getSpreadsheet().getCellAt(ROW_OFFSET,
                    col + COL_OFFSET);
            Assert.assertEquals(NAMED_COLOR_CSS[col],
                    cell.getCssValue("color"));
        }
    }

    private void validateIndexedColors() {
        // Verify all indexed colors (from second row of table onwards)
        for (int row = 0; row < (ROW_COUNT - 1); ++row) {
            for (int col = 0; col < COL_COUNT; ++col) {
                SheetCellElement cell = getSpreadsheet()
                        .getCellAt(row + ROW_OFFSET + 1, col + COL_OFFSET);
                Assert.assertEquals(INDEXED_COLOR_CSS[row * COL_COUNT + col],
                        cell.getCssValue("color"));
            }
        }
    }

    @Test
    public void customFormatting_verifyBasicColorsPresent() {
        validateNamedColors();
    }

    @Test
    public void customFormatting_verifyIndexedColors() {
        validateIndexedColors();
    }

    @Test
    public void customFormatting_verifyAllFormulasValid() {
        // Test initial conditions, make sure no cell is marked as invalid
        for (int row = 0; row < ROW_COUNT; ++row) {
            for (int col = 0; col < ROW_COUNT; ++col) {
                SheetCellElement cell = getSpreadsheet()
                        .getCellAt(row + ROW_OFFSET, col + COL_OFFSET);
                Assert.assertFalse(cell.hasInvalidFormulaIndicator());
            }
        }
    }

    @Test
    public void customFormatting_verifyAllFormulasValidAfterValueChange_Positive() {
        // Control cell other cells copy their value from
        SheetCellElement testValueCell = getSpreadsheet().getCellAt(4, 2);

        // Test positive value
        testValueCell.setValue("98");
        for (int row = 0; row < ROW_COUNT; ++row) {
            for (int col = 0; col < COL_COUNT; ++col) {
                SheetCellElement cell = getSpreadsheet()
                        .getCellAt(row + ROW_OFFSET, col + COL_OFFSET);
                Assert.assertFalse(cell.hasInvalidFormulaIndicator());
                Assert.assertEquals(Double.parseDouble(cell.getValue()),
                        Double.parseDouble(testValueCell.getValue()), 0d);
            }
        }

        validateAllColors();
    }

    @Test
    public void customFormatting_verifyAllFormulasValidAfterValueChange_Negative() {
        // Control cell other cells copy their value from
        SheetCellElement testValueCell = getSpreadsheet().getCellAt(4, 2);

        // Test negative value
        testValueCell.setValue("-75");
        for (int row = 0; row < ROW_COUNT; ++row) {
            for (int col = 0; col < COL_COUNT; ++col) {
                SheetCellElement cell = getSpreadsheet()
                        .getCellAt(row + ROW_OFFSET, col + COL_OFFSET);
                Assert.assertFalse(cell.hasInvalidFormulaIndicator());
                Assert.assertEquals(
                        Double.parseDouble(cell.getValue().replace('(', ' ')
                                .replace(')', ' ')),
                        -Double.parseDouble(testValueCell.getValue()), 0d);
            }
        }

        validateAllColors();
    }

    @Test
    public void customFormatting_verifyAllFormulasValidAfterValueChange_Zero() {
        // Control cell other cells copy their value from
        SheetCellElement testValueCell = getSpreadsheet().getCellAt(4, 2);

        // Test zero value
        testValueCell.setValue("0");
        for (int row = 0; row < ROW_COUNT; ++row) {
            for (int col = 0; col < COL_COUNT; ++col) {
                SheetCellElement cell = getSpreadsheet()
                        .getCellAt(row + ROW_OFFSET, col + COL_OFFSET);
                Assert.assertFalse(cell.hasInvalidFormulaIndicator());
                Assert.assertEquals("===", cell.getValue());
            }
        }

        validateAllColors();
    }

    @Test
    public void customFormatting_verifyAllFormulasValidAfterValueChange_String() {
        // Control cell other cells copy their value from
        SheetCellElement testValueCell = getSpreadsheet().getCellAt(4, 2);

        // Test string value
        testValueCell.setValue("test");
        for (int row = 0; row < ROW_COUNT; ++row) {
            for (int col = 0; col < COL_COUNT; ++col) {
                SheetCellElement cell = getSpreadsheet()
                        .getCellAt(row + ROW_OFFSET, col + COL_OFFSET);
                Assert.assertFalse(cell.hasInvalidFormulaIndicator());
                Assert.assertEquals("\"test\"", cell.getValue());
            }
        }

        validateAllColors();
    }
}
