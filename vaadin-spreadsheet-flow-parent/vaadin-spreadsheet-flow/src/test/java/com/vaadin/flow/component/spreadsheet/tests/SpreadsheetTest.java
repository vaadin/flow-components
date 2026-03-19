/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

class SpreadsheetTest {
    @Test
    void setId_getId_returnsId() {
        var spreadsheet = new Spreadsheet();
        spreadsheet.setId("foo");
        assertEquals("foo", spreadsheet.getId().orElse(null));
    }
}
