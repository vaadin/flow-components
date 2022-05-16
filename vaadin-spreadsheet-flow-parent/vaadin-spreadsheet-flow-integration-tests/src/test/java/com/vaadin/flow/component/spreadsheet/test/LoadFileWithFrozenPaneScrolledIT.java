package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoadFileWithFrozenPaneScrolledIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void loadFileWithFrozenPaneScrolled_firstColumnIsA() {
        loadFile("frozen_pane_scrolled.xlsx");

        final var spreadsheet = getSpreadsheet();

        final String row1 = spreadsheet.getRowHeader(1).getText();
        final String column1 = spreadsheet.getColumnHeader(1).getText();

        Assert.assertEquals("A1", column1 + row1);
    }
}
