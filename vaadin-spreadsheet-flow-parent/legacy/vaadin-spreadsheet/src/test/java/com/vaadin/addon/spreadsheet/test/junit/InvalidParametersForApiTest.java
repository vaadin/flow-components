package com.vaadin.addon.spreadsheet.test.junit;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class InvalidParametersForApiTest {

    @Test
    public void createCell_withNullValue_noException() {
        Spreadsheet spreadsheet = new Spreadsheet();
        Cell cell = spreadsheet.createCell(0, 0, null);
        assertNotNull(cell);
    }
}
