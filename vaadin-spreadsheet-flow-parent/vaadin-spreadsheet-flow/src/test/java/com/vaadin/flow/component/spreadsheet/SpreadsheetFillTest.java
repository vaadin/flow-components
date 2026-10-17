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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Selections filled by dragging their corner, as Excel fills them: the cells of
 * the selection repeated or counted on, into cells and rows the sheet does not
 * have yet, and a cell left empty in the selection emptying the cell it fills,
 * its style too.
 */
class SpreadsheetFillTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final Sheet sheet = workbook.createSheet();
    private Spreadsheet spreadsheet;

    private void set(String name, Object value) {
        CellReference reference = new CellReference(name);
        Row row = sheet.getRow(reference.getRow());
        if (row == null) {
            row = sheet.createRow(reference.getRow());
        }
        Cell cell = row.createCell(reference.getCol());
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue((String) value);
        }
    }

    private CellStyle bold() {
        CellStyle bold = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        bold.setFont(font);
        return bold;
    }

    private String value(String name) {
        Cell cell = spreadsheet.getCell(name);
        return cell == null ? "<none>" : spreadsheet.getCellValue(cell);
    }

    private short style(String name) {
        return spreadsheet.getCell(name).getCellStyle().getIndex();
    }

    /** Shows the sheet, its rows 1-20 and columns A-J reported. */
    private void show() {
        spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setLocale(Locale.US);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, \"lastColumn\": 10}");
        sent();
    }

    /** Fills from the selected range to the larger one, 1-based. */
    private void fill(int r1, int c1, int r2, int c2, int toR1, int toC1,
            int toR2, int toC2) {
        TestHelper.fireClientEvent(spreadsheet, "cellRangeSelected",
                "{\"row1\": " + r1 + ", \"col1\": " + c1 + ", \"row2\": " + r2
                        + ", \"col2\": " + c2 + "}");
        TestHelper.fireClientEvent(spreadsheet, "selectionIncreasePainted",
                "{\"r1\": " + toR1 + ", \"c1\": " + toC1 + ", \"r2\": " + toR2
                        + ", \"c2\": " + toC2 + "}");
    }

    /** Takes back the selected range from the 1-based row and column. */
    private void takeBack(int row, int column) {
        TestHelper.fireClientEvent(spreadsheet, "selectionDecreasePainted",
                "{\"row\": " + row + ", \"col\": " + column + "}");
    }

    private void select(int row, int column) {
        TestHelper.fireClientEvent(spreadsheet, "cellSelected",
                "{\"row\": " + row + ", \"column\": " + column
                        + ", \"oldSelectionRangeDiscarded\": true}");
    }

    private void selectRange(int r1, int c1, int r2, int c2) {
        TestHelper.fireClientEvent(spreadsheet, "cellRangeSelected",
                "{\"row1\": " + r1 + ", \"col1\": " + c1 + ", \"row2\": " + r2
                        + ", \"col2\": " + c2 + "}");
    }

    private String selectedRange() {
        return spreadsheet.getCellSelectionManager().getSelectedCellRange()
                .formatAsString();
    }

    /** The cells the listeners are told changed, by every event, sorted. */
    private List<String> changedCellsHeard() {
        List<String> heard = new ArrayList<>();
        spreadsheet.addCellValueChangeListener(event -> event.getChangedCells()
                .forEach(cell -> heard.add(cell.formatAsString(false))));
        return heard;
    }

    /** The cells sent to the client since the last look, as "A1=1". */
    private List<String> sent() {
        ui.fakeClientCommunication();
        List<String> sent = new ArrayList<>();
        JsonMapper mapper = JsonMapper.builder().build();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            for (Object parameter : invocation.getInvocation()
                    .getParameters()) {
                if (parameter instanceof String text && text.startsWith("[{")
                        && text.contains("\"row\"")) {
                    for (JsonNode cell : mapper.readTree(text)) {
                        sent.add(new CellReference(cell.get("row").asInt() - 1,
                                cell.get("col").asInt() - 1).formatAsString()
                                + "=" + cell.path("value").asString(""));
                    }
                }
            }
        }
        return sent;
    }

    @Test
    void aFillRight_stopsAtTheLastColumnPainted() {
        set("A1", "x");
        set("D1", "kept");
        show();

        fill(1, 1, 1, 1, 1, 1, 1, 3);

        assertEquals(List.of("x", "x", "x", "kept"),
                List.of(value("A1"), value("B1"), value("C1"), value("D1")));
    }

    @Test
    void aFillDown_stopsAtTheLastRowPainted() {
        set("A1", "x");
        set("A4", "kept");
        show();

        fill(1, 1, 1, 1, 1, 1, 3, 1);

        assertEquals(List.of("x", "x", "x", "kept"),
                List.of(value("A1"), value("A2"), value("A3"), value("A4")));
    }

    @Test
    void numbersFilledDown_countOn_intoRowsTheSheetHasNot() {
        set("A1", 1);
        set("A2", 2);
        show();

        fill(1, 1, 2, 1, 1, 1, 4, 1);

        assertEquals(List.of("3", "4"), List.of(value("A3"), value("A4")));
    }

    @Test
    void numbersFilledUp_countBack_intoRowsTheSheetHasNot() {
        set("A5", 5);
        set("A6", 6);
        show();

        fill(5, 1, 6, 1, 3, 1, 6, 1);

        assertEquals(List.of("3", "4"), List.of(value("A3"), value("A4")));
    }

    @Test
    void numbersFilledRight_countOn_intoCellsTheRowHasNot() {
        set("A1", 1);
        set("B1", 2);
        show();

        fill(1, 1, 1, 2, 1, 1, 1, 4);

        assertEquals(List.of("3", "4"), List.of(value("C1"), value("D1")));
    }

    @Test
    void numbersFilledLeft_countBack_intoCellsTheRowHasNot() {
        set("E1", 5);
        set("F1", 6);
        show();

        fill(1, 5, 1, 6, 1, 3, 1, 6);

        assertEquals(List.of("3", "4"), List.of(value("C1"), value("D1")));
    }

    @Test
    void aFillOverTheSameCells_changesNothing() {
        // The corner dragged out and back again.
        set("A1", 1);
        set("A2", 2);
        show();

        fill(1, 1, 2, 1, 1, 1, 2, 1);

        assertEquals(List.of("1", "2", "<none>"),
                List.of(value("A1"), value("A2"), value("A3")));
    }

    @Test
    void aFill_selectsTheCellsFilled() {
        set("A1", 1);
        set("A2", 2);
        show();

        fill(1, 1, 2, 1, 1, 1, 4, 1);

        assertEquals("A1:A4", selectedRange());
    }

    @Test
    void aFill_tellsTheListenersOfTheCellsFilled() {
        set("A1", 1);
        set("A2", 2);
        show();
        List<String> heard = changedCellsHeard();

        fill(1, 1, 2, 1, 1, 1, 4, 1);

        heard.sort(null);
        assertEquals(List.of("A3", "A4"), heard);
    }

    @Test
    void aFill_isUndone() {
        set("A1", 1);
        set("A2", 2);
        show();
        fill(1, 1, 2, 1, 1, 1, 4, 1);

        spreadsheet.getSpreadsheetHistoryManager().undo();

        // As before the fill: no cells there at all.
        assertEquals(List.of("<none>", "<none>"),
                List.of(value("A3"), value("A4")));
    }

    @Test
    void aFillTakenBack_emptiesTheCells_andSelectsWhatIsLeft() {
        set("A1", 1);
        set("A2", 2);
        set("A3", 3);
        set("A4", 4);
        show();
        selectRange(1, 1, 4, 1);

        takeBack(3, 1);

        assertEquals(List.of("", ""), List.of(value("A3"), value("A4")));
        assertEquals("A1:A2", selectedRange());
    }

    @Test
    void aFillTakenBack_fromTheSelectedCell_selectsTheFirstCellLeft() {
        set("A1", 1);
        set("A2", 2);
        set("A3", 3);
        set("A4", 4);
        show();
        select(4, 1);
        selectRange(1, 1, 4, 1);

        takeBack(3, 1);

        assertEquals("A1",
                spreadsheet.getSelectedCellReference().formatAsString(false));
    }

    @Test
    void aFillTakenBack_tellsTheListeners_andIsUndone() {
        set("A1", 1);
        set("A2", 2);
        set("A3", 3);
        show();
        selectRange(1, 1, 3, 1);
        List<String> heard = changedCellsHeard();

        takeBack(3, 1);
        assertEquals(List.of("A3"), heard);
        spreadsheet.getSpreadsheetHistoryManager().undo();

        assertEquals("3", value("A3"));
    }

    @Test
    void shiftCellValue_copiesTheValueOfOneCellToAnother() {
        // A hook of the public class, which the fills themselves do not use.
        set("A1", "copied");
        set("B1", "replaced");
        show();

        new CellSelectionShifter(spreadsheet).shiftCellValue(
                spreadsheet.getCell("A1"), spreadsheet.getCell("B1"), false,
                null);

        assertEquals("copied", value("B1"));
    }

    @Test
    void twoRowsFilledDownTwice_repeat() {
        set("A1", "a");
        set("A2", "b");
        show();

        fill(1, 1, 2, 1, 1, 1, 6, 1);

        assertEquals(List.of("a", "b", "a", "b"),
                List.of(value("A3"), value("A4"), value("A5"), value("A6")));
    }

    @Test
    void twoColumnsFilledRightTwice_repeat() {
        set("A1", "a");
        set("B1", "b");
        show();

        fill(1, 1, 1, 2, 1, 1, 1, 6);

        assertEquals(List.of("a", "b", "a", "b"),
                List.of(value("C1"), value("D1"), value("E1"), value("F1")));
    }

    @Test
    void aRowWithoutCellsFilledDown_emptiesTheRowItFills() {
        // Row 2 is not in the sheet: it fills row 4.
        set("A1", "x");
        set("A4", "was here");
        show();

        fill(1, 1, 2, 1, 1, 1, 4, 1);

        assertEquals("x", value("A3"));
        assertEquals("", value("A4"));
    }

    @Test
    void aRowWithoutCellsFilledRight_isLeftWithout() {
        set("A2", "x");
        show();

        fill(1, 1, 2, 1, 1, 1, 2, 3);

        assertEquals(List.of("x", "x"), List.of(value("B2"), value("C2")));
        assertEquals(null, sheet.getRow(0));
    }

    @Test
    void anEmptyCellFilledDown_overNoCell_addsNone() {
        set("B1", "x");
        show();

        fill(1, 1, 1, 2, 1, 1, 3, 2);

        assertEquals(List.of("<none>", "<none>", "x", "x"),
                List.of(value("A2"), value("A3"), value("B2"), value("B3")));
    }

    @Test
    void anEmptyCellFilledUp_overNoCell_addsNone() {
        set("B5", "x");
        show();

        fill(5, 1, 5, 2, 3, 1, 5, 2);

        assertEquals(List.of("<none>", "<none>", "x", "x"),
                List.of(value("A3"), value("A4"), value("B3"), value("B4")));
    }

    @Test
    void anEmptyCellFilledRight_overNoCell_addsNone() {
        set("A2", "x");
        // A row with a cell further away: the row is there, its A1 not.
        set("J1", "other");
        show();

        fill(1, 1, 2, 1, 1, 1, 2, 3);

        assertEquals(List.of("<none>", "<none>", "x", "x"),
                List.of(value("B1"), value("C1"), value("B2"), value("C2")));
    }

    @Test
    void anEmptyCellFilledLeft_overNoCell_addsNone() {
        set("E2", "x");
        // A row with a cell further away: the row is there, its E1 not.
        set("J1", "other");
        show();

        fill(1, 5, 2, 5, 1, 3, 2, 5);

        assertEquals(List.of("<none>", "<none>", "x", "x"),
                List.of(value("C1"), value("D1"), value("C2"), value("D2")));
    }

    @Test
    void anEmptyCellFilledDown_emptiesTheCellBelow_styleAndAll() {
        set("A2", "was here");
        sheet.getRow(1).getCell(0).setCellStyle(bold());
        set("B1", "source");
        set("B2", "replaced");
        show();

        // A1 is empty in A1:B1, filled down onto row 2.
        fill(1, 1, 1, 2, 1, 1, 2, 2);

        assertEquals("", value("A2"));
        assertEquals(0, style("A2"));
        assertEquals("source", value("B2"));
        assertTrue(sent().contains("A2="));
    }

    @Test
    void anEmptyCellFilledRight_emptiesTheCellBeside_styleAndAll() {
        set("B1", "was here");
        sheet.getRow(0).getCell(1).setCellStyle(bold());
        set("A2", "source");
        set("B2", "replaced");
        show();

        // A1 is empty in A1:A2, filled right onto column B.
        fill(1, 1, 2, 1, 1, 1, 2, 2);

        assertEquals("", value("B1"));
        assertEquals(0, style("B1"));
        assertEquals("source", value("B2"));
        assertTrue(sent().contains("B1="));
    }
}
