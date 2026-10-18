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

import com.vaadin.flow.component.spreadsheet.tests.fixtures.ServerErrorFixture;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

/**
 * The check the "no exception" tests rely on sees an error the server reports,
 * which Flow otherwise only logs: it used to look for an element nothing
 * renders, and could not fail.
 */
@TestPath("vaadin-spreadsheet")
public class ServerErrorIndicatorIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void noServerError_passesTheCheck() {
        assertNoErrorIndicatorDetected();
    }

    @Test
    public void aServerError_failsTheCheck_withItsMessage() {
        loadTestFixture(TestFixtures.ServerError);

        AssertionError reported = Assert.assertThrows(AssertionError.class,
                this::assertNoErrorIndicatorDetected);
        Assert.assertTrue(reported.getMessage(),
                reported.getMessage().contains(ServerErrorFixture.MESSAGE));
    }
}
