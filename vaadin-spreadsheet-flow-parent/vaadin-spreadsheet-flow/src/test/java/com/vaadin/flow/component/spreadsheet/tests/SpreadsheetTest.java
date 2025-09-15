/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class SpreadsheetTest {
    @Test
    public void setId_getId_returnsId() {
        var spreadsheet = new Spreadsheet();
        spreadsheet.setId("foo");
        assertEquals("foo", spreadsheet.getId().orElse(null));
    }
}
