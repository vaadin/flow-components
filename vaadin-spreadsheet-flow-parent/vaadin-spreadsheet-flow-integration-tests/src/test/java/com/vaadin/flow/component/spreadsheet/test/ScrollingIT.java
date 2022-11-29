package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScrollingIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        loadFile("freezepanels.xlsx");
        selectSheetAt(2);
    }

    @Test
    public void cellCustomStyle_sheetIsScrolledToRightAndLeft_cellStyleNotRemoved() {
        final var cellAddress = "E1";
        final var expectedCellColor = getCellColor(cellAddress);
        getSpreadsheet().scrollLeft(1000);
        getSpreadsheet().scrollLeft(0);
        final var actualCellColor = getCellColor(cellAddress);

        Assert.assertEquals(expectedCellColor, actualCellColor);
    }
}
