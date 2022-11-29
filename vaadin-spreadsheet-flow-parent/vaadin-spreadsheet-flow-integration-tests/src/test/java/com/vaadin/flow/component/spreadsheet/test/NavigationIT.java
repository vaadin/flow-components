package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.AddressUtil;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-spreadsheet")
public class NavigationIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        // set window size to large to avoid cells overlapping with the
        // dev tool popup
        getDriver().manage().window().setSize(WINDOW_SIZE_LARGE);
        open();
        createNewSpreadsheet();
    }

    @Test
    public void testClickingOnCellsUpdatesAddressFieldAndUpdatesSelection()
            throws Exception {
        clickCell("A2");
        assertAddressFieldValue("A2", getAddressFieldValue());
        assertSelectedCell("A2", isCellSelected("A2"));

        clickCell("B1");
        assertAddressFieldValue("B1", getAddressFieldValue());
        assertSelectedCell("B1", isCellSelected("B1"));
        assertNotSelectedCell("A2", isCellSelected("A2"));

        clickCell("D7");
        assertAddressFieldValue("D7", getAddressFieldValue());
        assertSelectedCell("D7", isCellSelected("D7"));
        assertNotSelectedCell("B1", isCellSelected("B1"));

        clickCell("Q15");
        assertAddressFieldValue("Q15", getAddressFieldValue());
        assertSelectedCell("Q15", isCellSelected("Q15"));
    }

    @Test
    public void testUpdatingAddressFieldMovesSelection() throws Exception {
        setAddressFieldValue("A5");
        assertSelectedCell("A5", isCellSelected("A5"));

        setAddressFieldValue("G10");
        assertSelectedCell("G10", isCellSelected("G10"));

        setAddressFieldValue("D20");
        assertSelectedCell("D20", isCellSelected("D20"));

        setAddressFieldValue("AC2");
        assertSelectedCell("AC2", isCellSelected("AC2"));
    }

    @Test
    public void testDragSelection() throws Exception {
        dragFromCellToCell("C5", "F11");
        assertSelectionRange("C5:F11", true);
    }

    @Test
    public void testKeyboardSelection() throws Exception {
        clickCell("H10");
        assertSelectedCell("H10", isCellSelected("H10"));
        WebElement cell = getCellAt(8, 10);
        new Actions(getDriver()).moveToElement(cell).keyDown(Keys.SHIFT)
                .sendKeys(Keys.RIGHT).sendKeys(Keys.RIGHT).sendKeys(Keys.DOWN)
                .sendKeys(Keys.DOWN).keyUp(Keys.SHIFT).build().perform();
        assertSelectionRange("H10:J12", true);
        assertActiveCellInsideSelection("H10");
    }

    @Test
    public void testAddressFieldUpdatesWhenDragging() throws Exception {
        new Actions(getDriver()).clickAndHold(getCellAt(8, 10))
                .moveToElement(getCellAt(9, 10)).build().perform();
        assertAddressFieldValue("1R x 2C", getAddressFieldValue());
        new Actions(getDriver()).moveToElement(getCellAt(10, 10)).build()
                .perform();
        assertAddressFieldValue("1R x 3C", getAddressFieldValue());
        new Actions(getDriver()).moveToElement(getCellAt(10, 11)).build()
                .perform();
        assertAddressFieldValue("2R x 3C", getAddressFieldValue());
        new Actions(getDriver()).moveToElement(getCellAt(10, 12)).build()
                .perform();
        assertAddressFieldValue("3R x 3C", getAddressFieldValue());
        new Actions(getDriver()).release().build().perform();
        assertAddressFieldValue("H10", getAddressFieldValue());
    }

    @Test
    @Ignore("Known bug in Spreadsheet component implementation (notimetofix) - Shift selection does not update address field")
    public void testAddressFieldUpdatesWhenShiftSelecting() throws Exception {
        clickCell("H10");
        WebElement cell = getCellAt(8, 10);
        new Actions(getDriver()).moveToElement(cell).keyDown(Keys.SHIFT)
                .sendKeys(Keys.RIGHT).build().perform();
        assertAddressFieldValue("1R x 2C", getAddressFieldValue());
        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertAddressFieldValue("1R x 3C", getAddressFieldValue());
        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertAddressFieldValue("2R x 3C", getAddressFieldValue());
        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertAddressFieldValue("3R x 3C", getAddressFieldValue());
        new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        assertAddressFieldValue("H10", getAddressFieldValue());
    }

    @Test
    public void testGrowShrinkSelectionWithShiftArrowsHorizontal()
            throws Exception {
        assertSelectionRange("G10:J10", false);
        clickCell("H10");
        assertNotSelectedCell("G10", isCellSelected("G10"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertNotSelectedCell("I10", isCellSelected("I10"));
        assertNotSelectedCell("J10", isCellSelected("J10"));
        new Actions(getDriver()).moveToElement(getCellAt(8, 10))
                .keyDown(Keys.SHIFT).sendKeys(Keys.RIGHT).build().perform();
        assertNotSelectedCell("G10", isCellSelected("G10"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertSelectedCell("I10", isCellSelected("I10"));
        assertNotSelectedCell("J10", isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertNotSelectedCell("G10", isCellSelected("G10"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertSelectedCell("I10", isCellSelected("I10"));
        assertSelectedCell("J10", isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertNotSelectedCell("G10", isCellSelected("G10"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertSelectedCell("I10", isCellSelected("I10"));
        assertNotSelectedCell("J10", isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertNotSelectedCell("G10", isCellSelected("G10"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertNotSelectedCell("I10", isCellSelected("I10"));
        assertNotSelectedCell("J10", isCellSelected("J10"));
        new Actions(getDriver()).sendKeys(Keys.LEFT).keyUp(Keys.SHIFT).build()
                .perform();
        assertSelectedCell("G10", isCellSelected("G10"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertNotSelectedCell("I10", isCellSelected("I10"));
        assertNotSelectedCell("J10", isCellSelected("J10"));
    }

    @Test
    public void testGrowShrinkSelectionWithShiftArrowsVertical()
            throws Exception {
        assertSelectionRange("H9:H12", false);
        clickCell("H10");
        assertNotSelectedCell("H9", isCellSelected("H9"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertNotSelectedCell("H11", isCellSelected("H11"));
        assertNotSelectedCell("H12", isCellSelected("H12"));
        new Actions(getDriver()).moveToElement(getCellAt(8, 10))
                .keyDown(Keys.SHIFT).sendKeys(Keys.DOWN).build().perform();
        assertNotSelectedCell("H9", isCellSelected("H9"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertSelectedCell("H11", isCellSelected("H11"));
        assertNotSelectedCell("H12", isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertNotSelectedCell("H9", isCellSelected("H9"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertSelectedCell("H11", isCellSelected("H11"));
        assertSelectedCell("H12", isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertNotSelectedCell("H9", isCellSelected("H9"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertSelectedCell("H11", isCellSelected("H11"));
        assertNotSelectedCell("H12", isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertNotSelectedCell("H9", isCellSelected("H9"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertNotSelectedCell("H11", isCellSelected("H11"));
        assertNotSelectedCell("H12", isCellSelected("H12"));
        new Actions(getDriver()).sendKeys(Keys.UP).keyUp(Keys.SHIFT).build()
                .perform();
        assertSelectedCell("H9", isCellSelected("H9"));
        assertSelectedCell("H10", isCellSelected("H10"));
        assertNotSelectedCell("H11", isCellSelected("H11"));
        assertNotSelectedCell("H12", isCellSelected("H12"));
    }

    @Test
    public void testEnterSelectionRangeInAddress() throws Exception {
        setAddressFieldValue("A1:C7");
        assertSelectionRange("A1:C7", true);
    }

    @Test
    public void testEnterSelectionRangeInAddress_outsideOfViewport()
            throws Exception {
        setAddressFieldValue("AT1:AV7");
        assertSelectionRange("AT1:AV7", true);
    }

    @Test
    public void testKeyboardNavigation() throws Exception {
        clickCell("J10");
        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertSelectedCell("K10", isCellSelected("K10"));

        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertSelectedCell("K11", isCellSelected("K11"));

        new Actions(getDriver()).sendKeys(Keys.ENTER, Keys.ENTER).build()
                .perform();
        assertSelectedCell("K12", isCellSelected("K12"));

        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertSelectedCell("J12", isCellSelected("J12"));

        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertSelectedCell("J11", isCellSelected("J11"));

        new Actions(getDriver()).keyDown(Keys.SHIFT)
                .sendKeys(Keys.ENTER, Keys.ENTER).keyUp(Keys.SHIFT).build()
                .perform();
        assertSelectedCell("J10", isCellSelected("J10"));

        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertSelectedCell("K10", isCellSelected("K10"));

        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.TAB)
                .keyUp(Keys.SHIFT).build().perform();
        assertSelectedCell("J10", isCellSelected("J10"));
    }

    @Test
    public void testNavigationInSelectionWithEnterAndTab() throws Exception {
        setAddressFieldValue("A1:C2");
        // Assert that everything is selected
        assertSelectionRange("A1:C2", true);

        // Press enter/return 2 times to end up in cell B1
        assertActiveCellInsideSelection("A1");
        new Actions(getDriver()).sendKeys(Keys.ENTER).build().perform();
        assertActiveCellInsideSelection("A2");
        new Actions(getDriver()).sendKeys(Keys.ENTER).build().perform();
        assertActiveCellInsideSelection("B1");

        // Continue from B1 by pressing TAB twice, getting to A2 and shift tab
        // from there to get back to C1
        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertActiveCellInsideSelection("C1");
        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertActiveCellInsideSelection("A2");
        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.TAB)
                .keyUp(Keys.SHIFT).build().perform();
        assertActiveCellInsideSelection("C1");

        // Everything should still be selected
        assertSelectionRange("A1:C2", true);
    }

    @Test
    public void testRightKeyDiscardsSelection() throws Exception {
        setAddressFieldValue("A1:B2");
        // Assert that everything is selected
        assertSelectionRange("A1:B2", true);

        new Actions(getDriver()).sendKeys(Keys.RIGHT).build().perform();
        assertNotSelectedCell("A1", isCellSelected("A1"));
        assertNotSelectedCell("A2", isCellSelected("A2"));
        assertSelectedCell("B1", isCellSelected("B1"));
        assertNotSelectedCell("B2", isCellSelected("B2"));
    }

    @Test
    public void testDownKeyDiscardsSelection() throws Exception {
        setAddressFieldValue("A1:B2");
        // Assert that everything is selected
        assertSelectionRange("A1:B2", true);

        new Actions(getDriver()).sendKeys(Keys.DOWN).build().perform();
        assertNotSelectedCell("A1", isCellSelected("A1"));
        assertSelectedCell("A2", isCellSelected("A2"));
        assertNotSelectedCell("B1", isCellSelected("B1"));
        assertNotSelectedCell("B2", isCellSelected("B2"));
    }

    @Test
    public void testLeftKeyDiscardsSelection() throws Exception {
        setAddressFieldValue("B1:C2");
        // Assert that everything is selected
        assertSelectionRange("B1:C2", true);

        new Actions(getDriver()).sendKeys(Keys.LEFT).build().perform();
        assertSelectedCell("A1", isCellSelected("A1"));
        assertSelectionRange("B1:C2", false);
    }

    @Test
    public void testUpKeyDiscardsSelection() throws Exception {
        setAddressFieldValue("A2:B3");
        // Assert that everything is selected
        assertSelectionRange("A2:B3", true);

        new Actions(getDriver()).sendKeys(Keys.UP).build().perform();
        assertSelectedCell("A1", isCellSelected("A1"));
        assertSelectionRange("A2:B3", false);
    }

    @Test
    public void testShiftArrowsShrinksSelectionWhenActiveOnEdgeOfSelection()
            throws Exception {
        // TODO well this test is a bit incomplete
        setAddressFieldValue("C4:E6");
        assertSelectionRange("C4:E6", true);
    }

    @Test
    public void testSheetScrollsWhenPushingAgainstRightEdge() throws Exception {
        setAddressFieldValue("Z1");

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.RIGHT).sendKeys(Keys.RIGHT)
                .build().perform();
        assertSelectedCell("AB1", isCellSelected("AB1"));

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.RIGHT).sendKeys(Keys.RIGHT)
                .build().perform();
        assertSelectedCell("AD1", isCellSelected("AD1"));
    }

    @Test
    public void testSheetScrollsWhenPushingAgainstBottomEdge()
            throws Exception {
        setAddressFieldValue("A40");

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.DOWN).sendKeys(Keys.DOWN).build()
                .perform();
        assertSelectedCell("A42", isCellSelected("A42"));

        // We need to press the key two times to make it scroll.
        new Actions(getDriver()).sendKeys(Keys.DOWN).sendKeys(Keys.DOWN).build()
                .perform();
        assertSelectedCell("A44", isCellSelected("A44"));
    }

    @Test
    public void testEnterAndTabWhenFormulaFieldIsFocused() throws Exception {
        clickCell("J10");
        clickOnFormulaField();
        setFormulaFieldValue("2");

        clickCell("J10");
        new Actions(getDriver()).sendKeys(Keys.ENTER).sendKeys(Keys.ENTER)
                .build().perform();
        assertSelectedCell("J11", isCellSelected("J11"));
        assertCellValue("2", "J10");

        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.ENTER)
                .sendKeys(Keys.ENTER).keyUp(Keys.SHIFT).build().perform();
        assertSelectedCell("J10", isCellSelected("J10"));

        new Actions(getDriver()).sendKeys(Keys.TAB).build().perform();
        assertSelectedCell("K10", isCellSelected("K10"));
        assertCellValue("2", "J10");

        new Actions(getDriver()).keyDown(Keys.SHIFT).sendKeys(Keys.TAB)
                .keyUp(Keys.SHIFT).build().perform();
        assertSelectedCell("J10", isCellSelected("J10"));
    }

    @Test
    public void testClickOnColumnHeaderSelectsColumn() throws Exception {
        clickOnColumnHeader("B");
        // We can't assert all the way down to B200, because of lazy loading.
        assertSelectionRange("B1:B20", true);
        assertSelectionRange("A1:A20", false);
        assertSelectionRange("C1:C20", false);
    }

    @Test
    public void testClickOnRowHeaderSelectsRow() throws Exception {
        clickOnRowHeader(10);
        // We can't assert all the way to AZ because of lazy loading
        assertSelectionRange("A10:L10", true);
        assertSelectionRange("A9:L9", false);
        assertSelectionRange("A11:L11", false);
        assertActiveCellInsideSelection("A10");
    }

    @Test
    public void testShiftClickShouldSelect() throws Exception {
        clickCell("B2");
        assertSelectedCell("B2", isCellSelected("B2"));
        // new Actions(getDriver()).keyDown(Keys.SHIFT).build().perform();
        // clickCell("D7");
        // new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        clickCell("D7", Keys.SHIFT);
        assertSelectionRange("B2:D7", true);
        assertActiveCellInsideSelection("B2");
    }

    @Test
    public void testShiftClickOnColumnHeader() throws Exception {
        clickCell("B2");
        assertSelectedCell("B2", isCellSelected("B2"));
        // new Actions(getDriver()).keyDown(Keys.SHIFT).build().perform();
        // clickOnColumnHeader("D");
        // new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        clickOnColumnHeader("D", Keys.SHIFT);
        // We can't assert the entire range because of lazy loading
        assertSelectionRange("B1:D20", true);
        assertActiveCellInsideSelection("B2");
    }

    @Test
    public void testShiftClickOnRowHeader() throws Exception {

        clickCell("B10");
        assertSelectedCell("B10", isCellSelected("B10"));
        // new Actions(getDriver()).keyDown(Keys.SHIFT).build().perform();
        // clickOnRowHeader(15);
        // new Actions(getDriver()).keyUp(Keys.SHIFT).build().perform();
        clickOnRowHeader(15, Keys.SHIFT);
        // We can't assert the entire range because of lazy loading
        assertSelectionRange("A10:L15", true);
        assertActiveCellInsideSelection("B10");
    }

    @Test
    public void testSelectCellsByCtrlClick() throws Exception {

        // ("only works on windows due to
        // https://code.google.com/p/selenium/issues/detail?id=4843 (patch
        // pending)")
        clickCell("A2");
        clickCell("A1");
        assertSelectedCell("A1", isCellSelected("A1"));
        new Actions(getDriver()).keyDown(Keys.CONTROL).build().perform();
        clickCell("B2");
        assertSelectedCell("A1", isCellSelected("A1"));
        assertSelectedCell("B2", isCellSelected("B2"));
        clickCell("F12");
        new Actions(getDriver()).keyUp(Keys.CONTROL).build().perform();
        assertSelectedCell("A1", isCellSelected("A1"));
        assertSelectedCell("B2", isCellSelected("B2"));
        assertSelectedCell("F12", isCellSelected("F12"));
    }

    private void assertActiveCellInsideSelection(String cellAddress) {
        Assert.assertTrue(
                cellAddress + " should be the active cell in selection",
                isCellActiveWithinSelection(cellAddress));
    }

    private void assertCellValue(String expected, String cellAddress) {
        String actual = getCellValue(cellAddress);
        Assert.assertEquals(cellAddress + " value expected " + expected
                + " but actual " + actual, expected, actual);
    }

    /**
     * Asserts that a range like A1:B3 is selected.
     *
     * @param range
     *            the range to assert
     * @param selected
     *            whether to assert that it is selected or unselected.
     */
    private void assertSelectionRange(String range, boolean selected) {
        for (Point coordinate : AddressUtil.addressRangeToPoints(range)) {
            boolean cellSelected = isCellSelected(coordinate.getX(),
                    coordinate.getY());
            if (selected) {
                Assert.assertTrue("Expected cell at " + coordinate.toString()
                        + " to be selected because part of range " + range,
                        cellSelected);
            } else {
                Assert.assertFalse(
                        "Expected cell to NOT be selected because NOT part of range "
                                + range,
                        cellSelected);
            }
        }
    }
}
