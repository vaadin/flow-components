package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

import static org.junit.Assert.assertNotNull;

public class InvalidParametersForApiTest {

    @Test
    public void createCell_withNullValue_noException() {
        Spreadsheet spreadsheet = new Spreadsheet();
        Cell cell = spreadsheet.createCell(0, 0, null);
        assertNotNull(cell);
    }
}
