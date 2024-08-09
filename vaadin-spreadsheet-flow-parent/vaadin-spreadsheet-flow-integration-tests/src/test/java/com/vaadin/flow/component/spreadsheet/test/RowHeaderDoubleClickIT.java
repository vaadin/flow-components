/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class RowHeaderDoubleClickIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void loadFixture_doubleClickOnRowHeader_rowHeaderDoubleClickEventFired() {
        loadTestFixture(TestFixtures.RowHeaderDoubleClick);

        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class)
                .first();

        spreadsheet.getRowHeader(3).getResizeHandle().doubleClick();

        Assert.assertEquals("Double-click on row header",
                getCellAt(1, 3).getValue());
    }

}
