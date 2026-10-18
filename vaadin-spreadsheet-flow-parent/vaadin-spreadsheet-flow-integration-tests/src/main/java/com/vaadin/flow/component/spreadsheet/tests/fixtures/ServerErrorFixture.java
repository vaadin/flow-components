/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Fails on the server, as the code under test can: the test page shows the
 * error, where a test looks for it.
 */
public class ServerErrorFixture implements SpreadsheetFixture {

    /** The message of the error this fixture throws. */
    public static final String MESSAGE = "The fixture failed on the server";

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        throw new IllegalStateException(MESSAGE);
    }
}
