package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-spreadsheet")
public class CustomComponentIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
        loadTestFixture(TestFixtures.CustomComponent);
    }

    @Test
    public void testButton() {
        // TODO revisar - implement
    }
}
