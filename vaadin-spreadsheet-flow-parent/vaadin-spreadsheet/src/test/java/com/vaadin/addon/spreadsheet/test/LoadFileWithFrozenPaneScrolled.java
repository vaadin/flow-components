package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class LoadFileWithFrozenPaneScrolled
    extends AbstractSpreadsheetTestCase {

    @Test
    public void loadFileWithFrozenPaneScrolled_firstColumnIsA() {
        headerPage.loadFile("frozen_pane_scrolled.xlsx", this);

        final SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();

        final String row1 = spreadsheet.getRowHeader(1)
            .getText();
        final String column1 = spreadsheet.getColumnHeader(1)
            .getText();

        Assert.assertEquals("A1", column1 + row1);
    }
}
