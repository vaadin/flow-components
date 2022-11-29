package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.TestPath;

import java.util.List;

import static org.junit.Assert.assertFalse;

@TestPath("vaadin-spreadsheet")
public class UngroupingIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    /**
     * Ticket 599#
     */
    @Test
    public void grouping_expandColumnGroup_groupingElementsHaveCorrectValues()
            throws Exception {
        loadFile("ungrouping_cellUpdating.xlsx");
        List<WebElement> groupings = getGroupings();
        groupings.get(1).click();
        String cellValue = getSpreadsheet().getCellAt(3, 1).getValue();
        assertFalse(cellValue.contains("#"));
    }

}
