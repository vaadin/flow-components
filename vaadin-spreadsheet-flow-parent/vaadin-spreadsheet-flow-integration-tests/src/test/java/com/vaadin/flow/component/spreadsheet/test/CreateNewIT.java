package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CreateNewIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());

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
