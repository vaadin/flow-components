package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.vaadin.flow.component.spreadsheet.test.util.PopupHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Tests for hyperlinks.
 *
 */
public class HyperlinkIT extends AbstractSpreadsheetIT {

    private PopupHelper popup;

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        loadFile("spreadsheet_hyperlinks.xlsx");
        popup = new PopupHelper(driver);
    }

    /**
     * Test for #18564 where hyperlink to same sheet caused no selection if the
     * sheet was defined in the hyperlink.
     */
    @Test
    public void hyperlink_sheetWithLinkToSameSheet_selectionIsMoved() {
        testInternal("A6", "B6");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_internalFromFormulaMovesToCorrectCell() {
        testInternal("A4", "B4");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_internalFromLinkMovesToCorrectCell() {
        testInternal("A5", "B5");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_externalFromLinkOpensPopupToCorrectPage() {
        testExternal("A3", "google");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_externalFromFormulaOpensPopupToCorrectPage() {
        testExternal("A2", "google");
    }

    @Test
    public void hyperlinkWithSpace_sheetWithHyperLinks_externalFromFormulaOpensPopupToCorrectPage() {
        testExternal("A7", "google");
    }

    @Test
    public void hyperlinkWithBracketAndSpace1_sheetWithHyperLinks_externalFromFormulaOpensPopupToCorrectPage() {
        testExternal("A8", "google");
    }

    @Test
    public void hyperlinkWithBracketAndSpace2_sheetWithHyperLinks_externalFromFormulaOpensPopupToCorrectPage() {
        testExternal("A10", "google");
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3184 is fixed")
    @Test
    public void hyperlinkInFormula_sheetWithHyperLinks_externalFromFormulaOpensPopupToCorrectPage() {
        testExternal("I1", "google");
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3184 is fixed")
    @Test
    public void hyperlinkInSharedFormula_sheetWithHyperLinks_externalFromFormulaOpensPopupToCorrectPage() {
        testExternal("I2", "mail");
    }

    @Test
    public void hyperlink_sheetWithHyperLinks_externalFromMergedCellOpensPopupToCorrectPage() {
        testExternal("C7", "google");
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3184 is fixed")
    @Test
    public void hyperlink_sheetWithHyperLinks_internalFromFileNameFormulaMovesToCorrectSheetAndCell() {
        loadFile("hyper_links.xlsx");
        // ensure hyperlink switches to correct cell
        testInternal("B30", "B10");
        // ensure correct sheet
        testInternal("A3", "A3");
        assertEquals("Unexpected formula for cell A3, ", "=5000",
                getFormulaFieldValue());
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3184 is fixed")
    @Test
    public void hyperlink_sheetWithNumericSheetName_internalFromFileNameFormulaMovesToCorrectSheetAndCell() {
        loadFile("hyper_links.xlsx");
        // ensure hyperlink switches to correct cell
        testInternal("B9", "B3");
        // ensure correct sheet
        testInternal("B3", "B3");
        assertEquals("Unexpected formula for cell B3, ", "300",
                getFormulaFieldValue());
    }

    @Ignore("Ignore until https://github.com/vaadin/flow-components/issues/3184 is fixed")
    @Test
    public void hyperlink_sheetWithSpacesInSheetName_internalFromFileNameFormulaMovesToCorrectSheetAndCell() {
        loadFile("hyper_links.xlsx");
        // ensure hyperlink switches to correct cell
        testInternal("C9", "C3");
        // ensure correct sheet
        testInternal("C3", "C3");
        assertEquals("Unexpected formula for cell C3, ", "125",
                getFormulaFieldValue());
    }

    /**
     * SHEET-86. Test that hyperlinks are immediately updated.
     */
    @Ignore("Investigate why the second cell.click() does not select the cell")
    @Test
    public void hyperlinkState_hyperlinkModified_hyperlinkUpdated()
            throws IOException {
        var sheet = getSpreadsheet();
        var cell = sheet.getCellAt("A9");

        // Enter hyperlink and check that it is followed
        cell.setValue("=HYPERLINK(\"#Sheet1!B9\")");
        cell.click();
        waitUntilSelected("B9");

        // Clear hyperlink and check that no link is followed
        action(Keys.LEFT);
        action(Keys.DELETE);
        action(Keys.DOWN);
        cell.click();
        waitUntilSelected("A9");
    }

    private void testInternal(String initial, String expected) {
        clickCell(initial);
        waitUntilSelected(expected);
    }

    private void testExternal(String cell, String urlSubstring) {
        clickCell(cell);
        popup.switchToPopup();
        waitUntilUrlContains(urlSubstring);
        popup.backToMainWindow();
    }

    public String getSelectedCell() {
        String elemClass = findElement(By.cssSelector(".sheet-selection"))
                .getAttribute("class");

        int rowStart = elemClass.indexOf("row");
        if (rowStart == -1) {
            return "A1";
        }

        int k = rowStart + "row".length();
        String rowNumber = "";
        while (k < elemClass.length()) {
            char digit = elemClass.charAt(k);
            if (digit == ' ') {
                break;
            }
            rowNumber += elemClass.charAt(k);
            k++;
        }

        int colStart = elemClass.indexOf("col");
        k = colStart + "col".length();
        String colNumberStr = "";
        while (k < elemClass.length()) {
            char digit = elemClass.charAt(k);
            if (digit == ' ') {
                break;
            }
            colNumberStr += elemClass.charAt(k);
            k++;
        }
        int colNumber = Integer.parseInt(colNumberStr);
        int dividend = colNumber;
        String columnName = "";
        int modulo;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            columnName = ((char) (65 + modulo)) + columnName;
            dividend = (dividend - modulo) / 26;
        }

        return columnName + rowNumber;
    }

    private void waitUntilSelected(final String expected) {
        waitUntil(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver arg0) {
                return expected.equals(getSelectedCell());
            }

            @Override
            public String toString() {
                // ...waiting for ...
                return expected + " to get selected";
            }
        });
    }

    private void waitUntilUrlContains(final String expected) {
        waitUntil(new ExpectedCondition<Boolean>() {

            @Override
            public Boolean apply(WebDriver arg0) {
                return getDriver().getCurrentUrl().contains(expected);
            }

            @Override
            public String toString() {
                // ...waiting for ...
                return String.format("current url to contain '%s'", expected);
            }
        });
    }

}
