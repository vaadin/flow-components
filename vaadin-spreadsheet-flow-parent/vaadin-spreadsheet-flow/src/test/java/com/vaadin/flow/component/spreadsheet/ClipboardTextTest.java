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

import java.util.List;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;

/**
 * Pasted text is read the way spreadsheets put cells on the clipboard: tabs
 * between cells, line breaks between rows, and a cell that holds either - or a
 * quote - in double quotes with its own quotes doubled.
 */
class ClipboardTextTest {

    @Test
    void readsCellsAndRows() {
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")),
                ClipboardText.parse("a\tb\nc\td"));
    }

    @Test
    void readsAQuotedCellWithALineBreakAsOneCell() {
        // Split on line breaks alone, this was two rows.
        assertEquals(List.of(List.of("line 1\nline 2", "b")),
                ClipboardText.parse("\"line 1\nline 2\"\tb"));
    }

    @Test
    void undoublesQuotesInAQuotedCell() {
        assertEquals(List.of(List.of("say \"hi\"")),
                ClipboardText.parse("\"say \"\"hi\"\"\""));
    }

    @Test
    void readsAQuotedCellWithATab() {
        assertEquals(List.of(List.of("a\tb", "c")),
                ClipboardText.parse("\"a\tb\"\tc"));
    }

    @Test
    void takesEveryKindOfLineBreak() {
        assertEquals(List.of(List.of("a"), List.of("b"), List.of("c")),
                ClipboardText.parse("a\r\nb\rc"));
    }

    @Test
    void ignoresALineBreakAtTheVeryEnd() {
        // Spreadsheets end what they copy with one.
        assertEquals(List.of(List.of("a", "b")),
                ClipboardText.parse("a\tb\r\n"));
    }

    @Test
    void keepsAnEmptyLastCellAfterATab() {
        assertEquals(List.of(List.of("a", "")), ClipboardText.parse("a\t"));
    }

    @Test
    void takesAQuoteInsideACellAsItIs() {
        assertEquals(List.of(List.of("5\" pipe")),
                ClipboardText.parse("5\" pipe"));
    }

    @Test
    void takesAnUnclosedQuoteAsItIs() {
        assertEquals(List.of(List.of("\"open", "b")),
                ClipboardText.parse("\"open\tb"));
    }

    @Test
    void readsNothingFromNothing() {
        assertEquals(List.of(), ClipboardText.parse(""));
    }

    @Test
    void pastingACellWithALineBreak_fillsOneCell() {
        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setSelection("A1");

        TestHelper.fireClientEvent(spreadsheet, "onPaste",
                "{\"text\": \"\\\"line 1\\nline 2\\\"\\tb\\r\\n\"}");

        assertEquals("line 1\nline 2", spreadsheet
                .getCell(new CellReference("A1")).getStringCellValue());
        assertEquals("b", spreadsheet.getCell(new CellReference("B1"))
                .getStringCellValue());
        assertEquals(null, spreadsheet.getCell(new CellReference("A2")));
    }

    // Written as the client writes the cells it copies: the server writes the
    // text of those the client does not have.

    private static String write(List<List<String>> rows) {
        return ClipboardText.write(rows.size(),
                rows.isEmpty() ? 0 : rows.get(0).size(),
                (row, column) -> rows.get(row).get(column));
    }

    @Test
    void writesCellsAndRows() {
        assertEquals("a\tb\nc\td",
                write(List.of(List.of("a", "b"), List.of("c", "d"))));
    }

    @Test
    void writesNoValueAsAnEmptyCell() {
        assertEquals("a\t\tc",
                write(List.of(java.util.Arrays.asList("a", null, "c"))));
    }

    @Test
    void writesNoLineBreakAfterTheLastRow() {
        assertEquals("a\nb", write(List.of(List.of("a"), List.of("b"))));
    }

    @Test
    void writesNothingForNoRows() {
        assertEquals("", write(List.of()));
    }

    @Test
    void quotesACellWithALineBreak() {
        assertEquals("\"a\nb\"\tc", write(List.of(List.of("a\nb", "c"))));
    }

    @Test
    void quotesACellWithACarriageReturn() {
        assertEquals("\"a\rb\"", write(List.of(List.of("a\rb"))));
    }

    @Test
    void quotesACellWithATab() {
        assertEquals("\"a\tb\"", write(List.of(List.of("a\tb"))));
    }

    @Test
    void quotesACellWithAQuote_doublingIt() {
        assertEquals("\"say \"\"hi\"\"\"",
                write(List.of(List.of("say \"hi\""))));
    }

    @Test
    void readsBackWhatItWrites() {
        List<List<String>> rows = List.of(List.of("a\tb", "say \"hi\""),
                List.of("line 1\nline 2", "plain"));

        assertEquals(rows, ClipboardText.parse(write(rows)));
    }
}
