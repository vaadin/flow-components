/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.Assert.assertNotNull;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class InvalidParametersForApiTest {

    @Test
    public void createCell_withNullValue_noException() {
        Spreadsheet spreadsheet = new Spreadsheet();
        Cell cell = spreadsheet.createCell(0, 0, null);
        assertNotNull(cell);
    }
}
