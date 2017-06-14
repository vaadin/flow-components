package com.vaadin.addon.spreadsheet.test.junit;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.Spreadsheet;

/**
 * Unit test for fix for issue #550.
 */
public class CellValueChangeEventOnFormulaChangeTest {

    private Spreadsheet spreadsheet;

    @Before
    public void setup() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1);
        row.createCell(1).setCellValue(0);
        row.createCell(2).setCellFormula("A1+B1");

        spreadsheet = new Spreadsheet();
        spreadsheet.setWorkbook(workbook);
    }

    /**
     * Verify that a CellValueChangeEvent is fired when a cell's formula
     * changes, but the new formula still produces the same result as the
     * previous formula.
     */
    @Test
    public void formulaChangeResultingInSameValue() {
        List<CellReference> changedCells = new LinkedList<>();

        spreadsheet.addCellValueChangeListener(
                event -> changedCells.addAll(event.getChangedCells()));

        spreadsheet.setSelection("C1");
        // B1 is 0, so the result doesn't change
        spreadsheet.getCellValueManager().onCellValueChange(3, 1, "=A1+2*B1");

        assertEquals("There should be 1 changed cell", 1, changedCells.size());
        assertEquals("The changed cell should be C1", new CellReference("C1"),
                changedCells.get(0));
    }

}
