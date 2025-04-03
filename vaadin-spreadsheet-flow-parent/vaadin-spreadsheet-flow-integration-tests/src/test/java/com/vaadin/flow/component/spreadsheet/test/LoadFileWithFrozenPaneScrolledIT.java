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

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class LoadFileWithFrozenPaneScrolledIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
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
