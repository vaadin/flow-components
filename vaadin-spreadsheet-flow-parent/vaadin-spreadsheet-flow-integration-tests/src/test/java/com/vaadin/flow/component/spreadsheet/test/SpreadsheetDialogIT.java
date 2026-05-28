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

import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("spreadsheet-dialog")
public class SpreadsheetDialogIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openDialog_noConsoleErrors_initialSelectionApplied() {
        $(TestBenchElement.class).id("open-dialog").click();

        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class)
                .waitForFirst();
        Assert.assertEquals("H3",
                spreadsheet.getAddressField().getPropertyString("value"));

        checkLogsForErrors();
    }

}
