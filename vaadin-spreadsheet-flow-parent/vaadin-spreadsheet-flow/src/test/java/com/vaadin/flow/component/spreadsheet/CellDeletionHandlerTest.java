/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellDeletionHandler;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * The deletion handler an application sets decides whether Delete clears the
 * cells: it is asked for a single cell, and for a range.
 */
class CellDeletionHandlerTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;
    private final List<String> asked = new ArrayList<>();
    private boolean allowed;

    /** A1:D4, each cell holding its own name, and the handler set. */
    @BeforeEach
    void show() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int r = 0; r < 4; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < 4; c++) {
                row.createCell(c)
                        .setCellValue(new CellReference(r, c).formatAsString());
            }
        }
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        spreadsheet.setCellDeletionHandler(new CellDeletionHandler() {
            @Override
            public boolean cellDeleted(Cell cell, Sheet sheet, int colIndex,
                    int rowIndex, FormulaEvaluator formulaEvaluator,
                    DataFormatter formatter,
                    ConditionalFormattingEvaluator conditionalFormattingEvaluator) {
                asked.add("cell " + new CellReference(rowIndex, colIndex)
                        .formatAsString());
                return allowed;
            }

            @Override
            public boolean individualSelectedCellsDeleted(
                    List<CellReference> individualSelectedCells, Sheet sheet,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                    ConditionalFormattingEvaluator conditionalFormattingEvaluator) {
                asked.add("cells " + individualSelectedCells.size());
                return allowed;
            }

            @Override
            public boolean cellRangeDeleted(
                    List<CellRangeAddress> cellRangeAddresses, Sheet sheet,
                    FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                    ConditionalFormattingEvaluator conditionalFormattingEvaluator) {
                asked.add("range " + cellRangeAddresses.stream()
                        .map(CellRangeAddress::formatAsString).toList());
                return allowed;
            }
        });
    }

    /** Clicks the cell, which the client numbers from 1. */
    private void click(String address) {
        CellReference cell = new CellReference(address);
        TestHelper.fireClientEvent(spreadsheet, "cellSelected",
                "{\"row\": " + (cell.getRow() + 1) + ", \"column\": "
                        + (cell.getCol() + 1)
                        + ", \"oldSelectionRangeDiscarded\": true}");
    }

    private void pressDelete() {
        TestHelper.fireClientEvent(spreadsheet, "deleteSelectedCells", "{}");
    }

    private String valueOf(String address) {
        Cell cell = spreadsheet.getCell(address);
        return cell == null ? "" : cell.getStringCellValue();
    }

    @Test
    void aSingleCell_isKeptWhenTheHandlerSaysNo() {
        click("B2");

        pressDelete();

        assertEquals(List.of("cell B2"), asked);
        assertEquals("B2", valueOf("B2"));
    }

    @Test
    void aSingleCell_isClearedWhenTheHandlerSaysYes() {
        allowed = true;
        click("B2");

        pressDelete();

        assertEquals(List.of("cell B2"), asked);
        assertEquals("", valueOf("B2"));
    }

    @Test
    void aRange_isKeptWhenTheHandlerSaysNo() {
        // Pressed on A1 and dragged to B2.
        click("A1");
        TestHelper.fireClientEvent(spreadsheet, "cellRangeSelected",
                "{\"row1\": 1, \"col1\": 1, \"row2\": 2, \"col2\": 2}");

        pressDelete();

        assertEquals(List.of("range [A1:B2]"), asked);
        assertEquals("A1", valueOf("A1"));
        assertEquals("B2", valueOf("B2"));
    }
}
