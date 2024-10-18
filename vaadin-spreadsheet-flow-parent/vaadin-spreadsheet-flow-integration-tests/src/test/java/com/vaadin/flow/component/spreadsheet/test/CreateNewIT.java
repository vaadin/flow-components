/**
 * Copyright 2000-2024 Vaadin Ltd.
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
public class CreateNewIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        createNewSpreadsheet();
    }

    @Test
    public void testCreateNewSpreadsheet() throws Exception {
        Assert.assertTrue(getSpreadsheet().isDisplayed());
    }

    @Test
    public void testNewSpreadsheetHasA1Focused() throws Exception {
        Assert.assertEquals("A1", getAddressFieldValue());
        Assert.assertTrue(isCellSelected(1, 1));
    }
}
