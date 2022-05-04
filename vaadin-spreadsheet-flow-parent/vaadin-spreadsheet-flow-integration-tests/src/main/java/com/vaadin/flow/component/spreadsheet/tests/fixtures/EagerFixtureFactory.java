package com.vaadin.flow.component.spreadsheet.tests.fixtures;

public class EagerFixtureFactory implements SpreadsheetFixtureFactory {

    private SpreadsheetFixture fixture;

    public EagerFixtureFactory(SpreadsheetFixture fixture) {
        this.fixture = fixture;
    }

    @Override
    public SpreadsheetFixture create() {
        return fixture;
    }
}
