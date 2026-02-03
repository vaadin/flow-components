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

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet/custom-number-format-colors")
public class CustomNumberFormatColorsIT extends AbstractSpreadsheetIT {

    private static final String COLOR_BLUE = "rgba(0, 0, 255, 1)";
    private static final String COLOR_RED = "rgba(255, 0, 0, 1)";
    private static final String COLOR_GREEN = "rgba(0, 255, 0, 1)";
    private static final String COLOR_MAGENTA = "rgba(255, 0, 255, 1)";
    private static final String COLOR_CYAN = "rgba(0, 255, 255, 1)";
    private static final String COLOR_YELLOW = "rgba(255, 255, 0, 1)";
    private static final String COLOR_BLACK = "rgba(0, 0, 0, 1)";
    private static final String COLOR_INDEXED_10 = "rgba(0, 128, 0, 1)";

    private SheetCellElement positiveCell;
    private SheetCellElement negativeCell;
    private SheetCellElement zeroCell;
    private SheetCellElement textCell;

    @Before
    public void init() {
        open();
        setSpreadsheet($(SpreadsheetElement.class).first());

        positiveCell = getSpreadsheet().getCellAt(1, 1);
        negativeCell = getSpreadsheet().getCellAt(2, 1);
        zeroCell = getSpreadsheet().getCellAt(3, 1);
        textCell = getSpreadsheet().getCellAt(4, 1);
    }

    @Test
    public void fourPartFormat_eachValueType_hasCorrectColor() {
        setFormatAndApply("[Blue]#,##0;[Red](#,##0);[Green]0;[Magenta]@");

        Assert.assertEquals(COLOR_BLUE, positiveCell.getCssValue("color"));
        Assert.assertEquals(COLOR_RED, negativeCell.getCssValue("color"));
        Assert.assertEquals(COLOR_GREEN, zeroCell.getCssValue("color"));
        Assert.assertEquals(COLOR_MAGENTA, textCell.getCssValue("color"));

        assertNoInvalidFormulaIndicators();
    }

    @Test
    public void twoPartFormat_positiveAndNegative_haveCorrectColors() {
        setFormatAndApply("[Green]#,##0;[Red](#,##0)");

        Assert.assertEquals(COLOR_GREEN, positiveCell.getCssValue("color"));
        Assert.assertEquals(COLOR_RED, negativeCell.getCssValue("color"));

        assertNoInvalidFormulaIndicators();
    }

    @Test
    public void indexedColor10_hasCorrectDarkGreenColor() {
        setFormatAndApply("[Color 10]#,##0");

        Assert.assertEquals(COLOR_INDEXED_10,
                positiveCell.getCssValue("color"));

        assertNoInvalidFormulaIndicators();
    }

    @Test
    public void singleColorFormat_allValues_haveSameColor() {
        setFormatAndApply("[Cyan]0");

        Assert.assertEquals(COLOR_CYAN, positiveCell.getCssValue("color"));
        Assert.assertEquals(COLOR_CYAN, negativeCell.getCssValue("color"));
        Assert.assertEquals(COLOR_CYAN, zeroCell.getCssValue("color"));

        assertNoInvalidFormulaIndicators();
    }

    @Test
    public void customZeroFormat_zeroValue_displaysCustomTextWithColor() {
        setFormatAndApply("[Blue]#,##0;[Red](#,##0);[Yellow]\"---\";@");

        Assert.assertEquals("---", zeroCell.getValue());
        Assert.assertEquals(COLOR_YELLOW, zeroCell.getCssValue("color"));

        assertNoInvalidFormulaIndicators();
    }

    @Test
    public void customValueFormatting_formatApplied() {
        setFormatAndApply("[Blue]#,##0;[Red](#,##0);[Green]---;[Magenta]@");

        Assert.assertEquals("Negative should have parentheses", "42",
                positiveCell.getValue());
        Assert.assertEquals("Negative should have parentheses", "(75)",
                negativeCell.getValue());
        Assert.assertEquals("Negative should have parentheses", "---",
                zeroCell.getValue());
        Assert.assertEquals("Negative should have parentheses", "text",
                textCell.getValue());

        assertNoInvalidFormulaIndicators();
    }

    @Test
    public void changeFormat_allCellColorsChange() {
        setFormatAndApply("[Blue]#,##0;[Red](#,##0);[Green]0;[Magenta]@");

        Assert.assertEquals(COLOR_BLUE, positiveCell.getCssValue("color"));
        Assert.assertEquals(COLOR_RED, negativeCell.getCssValue("color"));
        Assert.assertEquals(COLOR_GREEN, zeroCell.getCssValue("color"));
        Assert.assertEquals(COLOR_MAGENTA, textCell.getCssValue("color"));

        setFormatAndApply("[Yellow]#,##0;[Cyan](#,##0);[Black]0;[Red]@");

        Assert.assertEquals(COLOR_YELLOW, positiveCell.getCssValue("color"));
        Assert.assertEquals(COLOR_CYAN, negativeCell.getCssValue("color"));
        Assert.assertEquals(COLOR_BLACK, zeroCell.getCssValue("color"));
        Assert.assertEquals(COLOR_RED, textCell.getCssValue("color"));

        assertNoInvalidFormulaIndicators();
    }

    @Test
    public void changeValue_colorChangesBasedOnValueType() {
        setFormatAndApply("[Blue]#,##0;[Red](#,##0);[Green]0;[Magenta]@");

        Assert.assertEquals(COLOR_BLUE, positiveCell.getCssValue("color"));

        setCellValue("A1", "-50");
        Assert.assertEquals(COLOR_RED, positiveCell.getCssValue("color"));

        setCellValue("A1", "0");
        Assert.assertEquals(COLOR_GREEN, positiveCell.getCssValue("color"));

        setCellValue("A1", "hello");
        Assert.assertEquals(COLOR_MAGENTA, positiveCell.getCssValue("color"));

        assertNoInvalidFormulaIndicators();
    }

    private void setFormatAndApply(String format) {
        $(TextFieldElement.class).id("format-field").setValue(format);
        $("vaadin-button").id("apply-format-btn").click();
    }

    private void assertNoInvalidFormulaIndicators() {
        Assert.assertFalse(positiveCell.hasInvalidFormulaIndicator());
        Assert.assertFalse(negativeCell.hasInvalidFormulaIndicator());
        Assert.assertFalse(zeroCell.hasInvalidFormulaIndicator());
        Assert.assertFalse(textCell.hasInvalidFormulaIndicator());
    }
}
