package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpreadsheetTest {
    @Test
    public void setId_getId_returnsId() {
        var spreadsheet = new Spreadsheet();
        spreadsheet.setId("foo");
        assertEquals("foo", spreadsheet.getId().orElse(null));
    }
}
