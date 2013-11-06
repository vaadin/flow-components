package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.test.demoapps.TestexcelsheetUI;

public abstract class UIFixture implements SpreadsheetFixture {

    protected TestexcelsheetUI ui;

    public UIFixture(TestexcelsheetUI ui) {
        this.ui = ui;
    }
}