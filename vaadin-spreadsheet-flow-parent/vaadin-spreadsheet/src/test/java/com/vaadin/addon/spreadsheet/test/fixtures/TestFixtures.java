package com.vaadin.addon.spreadsheet.test.fixtures;

/**
 * Test fixtures for server-side Spreadsheet manipulation
 *
 */
public enum TestFixtures {
    FirstColumnWidth(new ClassFixtureFactory(FirstColumnWidthFixture.class));

    public final SpreadsheetFixtureFactory factory;

    TestFixtures(SpreadsheetFixtureFactory factory) {
        this.factory = factory;
    }
}
