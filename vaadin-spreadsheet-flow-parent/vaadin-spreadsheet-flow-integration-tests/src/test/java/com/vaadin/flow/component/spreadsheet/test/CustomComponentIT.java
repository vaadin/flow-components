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
import org.openqa.selenium.By;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class CustomComponentIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
        loadTestFixture(TestFixtures.CustomComponent);
    }

    @Test
    public void clickButtonInsideCell_notificationShown() {
        clickCell("B2");
        Assert.assertTrue(
                !findElements(By.tagName("vaadin-notification")).isEmpty());
    }
}
