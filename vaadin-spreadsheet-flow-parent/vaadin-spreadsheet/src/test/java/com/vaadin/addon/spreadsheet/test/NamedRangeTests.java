package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class NamedRangeTests extends AbstractSpreadsheetTestCase {

    private SpreadsheetPage spreadsheetPage;
    
    // named ranges defined in the xlsx
    private Map<String, String> sheet1ranges = new HashMap<String, String>();
    {
        sheet1ranges.put("john", "G7:M16");
        sheet1ranges.put("local", "G2:H3");
        sheet1ranges.put("numbers", "C3:C9");
    }

    private List<String> selectableRangesOnSheet1 = Arrays
        .asList("", "john", "local", "numbers", "sheet2");

    private List<String> selectableRangesOnSheet2 = Arrays
        .asList("", "john", "numbers", "sheet2");

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        spreadsheetPage = headerPage.loadFile("named_ranges.xlsx", this);
    }

    /**
     * I put all testing in one method because it runs much faster instead 
     * of running a new browser windows for each minor test.
     * --Michael
     */
    @Test
    public void testNamedRanges() throws Exception {
        assertNamedRangeSelectValues(selectableRangesOnSheet1);

        testTypingExistingSheet1NamedRanges();

        testSelectingExistingSheet1Ranges();

        testEnteringRangeAndAssertNameIsSelected();

        testSheetSwitch_selectSheet2Range_assertSheet2AndCorrectSelection();

        assertNamedRangeSelectValues(selectableRangesOnSheet2);

        selectAndAssertJohnRange();

        testCreatingNewRange();
        
        testCreatingNewRangeAfterSelectingRange();
    }

    private void testCreatingNewRangeAfterSelectingRange() {
        final String newCellRangeName = "numbers2";

        spreadsheetPage.setAddressFieldValue("A2");
        
        selectAndAssertNameRange("numbers", sheet1ranges.get("numbers"));

        spreadsheetPage.setAddressFieldValue(newCellRangeName);

        assertTrue(spreadsheetPage.getNamedRanges().contains(newCellRangeName));

        spreadsheetPage.setAddressFieldValue("A3");

        selectAndAssertNameRange(newCellRangeName, sheet1ranges.get("numbers"));
    }

    private void testCreatingNewRange() {
        final String newCellRange = "E3:F4";
        final String newCellRangeName = "new_range";
        
        spreadsheetPage.setAddressFieldValue(newCellRange);
        spreadsheetPage.setAddressFieldValue(newCellRangeName);
        
        assertTrue(spreadsheetPage.getNamedRanges().contains(newCellRangeName));

        selectAndAssertNameRange("john", sheet1ranges.get("john"));

        selectAndAssertNameRange(newCellRangeName, newCellRange);
    }

    private void selectAndAssertJohnRange() {
        typeAndAssertNameRange("john", sheet1ranges.get("john"));

        assertEquals("Sheet1", spreadsheetPage.getSelectedSheetName());
    }

    private void testEnteringRangeAndAssertNameIsSelected() {
        for (String name : sheet1ranges.keySet()) {
            typeCellRangeAndAssertNameRange(sheet1ranges.get(name), name);
        }
    }

    private void assertNamedRangeSelectValues(List<String> expectedNamedRanges) {
        final List<String> actualNamedRanges = spreadsheetPage.getNamedRanges();

        assertEquals(expectedNamedRanges, actualNamedRanges);
    }

    private void testSheetSwitch_selectSheet2Range_assertSheet2AndCorrectSelection() {
        typeAndAssertNameRange("sheet2", "B3:D9");
        
        assertEquals("Sheet2", spreadsheetPage.getSelectedSheetName());
    }

    private void testTypingExistingSheet1NamedRanges() {
        for (String name : sheet1ranges.keySet()) {
            typeAndAssertNameRange(name, sheet1ranges.get(name));
        }
    }

    private void testSelectingExistingSheet1Ranges() {
        for (String name : sheet1ranges.keySet()) {
            selectAndAssertNameRange(name, sheet1ranges.get(name));
        }
    }

    private void typeAndAssertNameRange(String name, String expected) {
        
        spreadsheetPage.setAddressFieldValue(name);

        assertSelectedRange(name, expected);
    }

    private void typeCellRangeAndAssertNameRange(String cellRange, String name) {

        spreadsheetPage.setAddressFieldValue(cellRange);

        assertSelectedRange(name, cellRange);
    }

    private void selectAndAssertNameRange(String name, String expected) {

        spreadsheetPage.selectNamedRange(name);

        assertSelectedRange(name, expected);
    }

    private void assertSelectedRange(String name, String expected) {
        
        String selection = spreadsheetPage.getSelectionFormula();

        assertEquals("Wrong selection for range " + name, expected, selection);

        assertEquals("Wrong address field for name", name,
            spreadsheetPage.getAddressFieldValue());
    }

}
