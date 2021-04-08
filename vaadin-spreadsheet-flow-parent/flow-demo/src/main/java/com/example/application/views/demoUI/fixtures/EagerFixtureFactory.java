package com.example.application.views.demoUI.fixtures;

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
