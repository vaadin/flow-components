package com.vaadin.flow.component.spreadsheet.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class NamedRangeIT extends AbstractSpreadsheetIT {

    // named ranges defined in the xlsx
    private Map<String, String> sheet1ranges = new HashMap<String, String>();
    {
        sheet1ranges.put("john", "G7:M16");
        sheet1ranges.put("local", "G2:H3");
        sheet1ranges.put("numbers", "C3:C9");
    }

    private List<String> selectableRangesOnSheet1 = Arrays.asList("", "john",
            "local", "numbers", "sheet2");

    @Before
    public void init() {
        open();
        loadFile("named_ranges.xlsx");
    }

    @Test
    public void testNamedRanges() throws Exception {
        assertNamedRangeSelectValues(selectableRangesOnSheet1);
    }

    @Test
    public void testCreatingNewRangeAfterSelectingRange() {
        final String newCellRangeName = "numbers2";

        setAddressFieldValue("A2");

        selectAndAssertNameRange("numbers", sheet1ranges.get("numbers"));

        setAddressFieldValue(newCellRangeName);

        Assert.assertTrue(getNamedRanges().contains(newCellRangeName));

        setAddressFieldValue("A3");

        selectAndAssertNameRange(newCellRangeName, sheet1ranges.get("numbers"));
    }

    @Test
    public void testCreatingNewRange() {
        final String newCellRange = "E3:F4";
        final String newCellRangeName = "new_range";

        setAddressFieldValue(newCellRange);
        setAddressFieldValue(newCellRangeName);

        Assert.assertTrue(getNamedRanges().contains(newCellRangeName));

        selectAndAssertNameRange("john", sheet1ranges.get("john"));

        selectAndAssertNameRange(newCellRangeName, newCellRange);
    }

    @Test
    public void selectAndAssertJohnRange() {
        typeAndAssertNameRange("john", sheet1ranges.get("john"));

        Assert.assertEquals("Sheet1", getSelectedSheetName());
    }

    @Test
    public void testEnteringRangeAndAssertNameIsSelected() {
        for (String name : sheet1ranges.keySet()) {
            typeCellRangeAndAssertNameRange(sheet1ranges.get(name), name);
        }
    }

    @Test
    public void testSheetSwitch_selectSheet2Range_assertSheet2AndCorrectSelection() {
        typeAndAssertNameRange("sheet2", "B3:D9");

        Assert.assertEquals("Sheet2", getSelectedSheetName());
    }

    @Test
    public void testTypingExistingSheet1NamedRanges() {
        for (String name : sheet1ranges.keySet()) {
            typeAndAssertNameRange(name, sheet1ranges.get(name));
        }
    }

    @Test
    public void testSelectingExistingSheet1Ranges() {
        for (String name : sheet1ranges.keySet()) {
            selectAndAssertNameRange(name, sheet1ranges.get(name));
        }
    }

    private void assertNamedRangeSelectValues(
            List<String> expectedNamedRanges) {
        final List<String> actualNamedRanges = getNamedRanges();

        Assert.assertEquals(expectedNamedRanges, actualNamedRanges);
    }

    private void typeAndAssertNameRange(String name, String expected) {
        setAddressFieldValue(name);

        assertSelectedRange(name, expected);
    }

    private void typeCellRangeAndAssertNameRange(String cellRange,
            String name) {
        setAddressFieldValue(cellRange);

        assertSelectedRange(name, cellRange);
    }

    private void selectAndAssertNameRange(String name, String expected) {
        selectNamedRange(name);

        assertSelectedRange(name, expected);
    }

    private void assertSelectedRange(String name, String expected) {
        String selection = getSelectionFormula();

        Assert.assertEquals("Wrong selection for range " + name, expected,
                selection);

        Assert.assertEquals("Wrong address field for name", name,
                getAddressFieldValue());
    }

}
