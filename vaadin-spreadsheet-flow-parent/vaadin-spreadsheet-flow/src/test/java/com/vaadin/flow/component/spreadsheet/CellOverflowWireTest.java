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

import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * The spill width reaching the client.
 *
 * <p>
 * {@link CellOverflowCalculator} owns the rule; these check that a real
 * spreadsheet feeds it the right things and puts the answer on the wire — the
 * width only exists once the client has reported how wide a character is in
 * each style, so a fresh sheet sends nothing.
 */
class CellOverflowWireTest {

    /** Ten pixels per character, for the default style. */
    private static final float WIDE = 10f;

    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        spreadsheet = new Spreadsheet();
    }

    private void reportWidthRatios(float ratio) {
        HashMap<Integer, Float> ratios = new HashMap<>();
        for (int style = 0; style < 8; style++) {
            ratios.put(style, ratio);
        }
        spreadsheet.getCellValueManager().onCellStyleWidthRatioUpdate(ratios);
    }

    private CellData dataFor(Cell cell) {
        return spreadsheet.getCellValueManager().createCellDataForCell(cell);
    }

    @Test
    void wideText_withEmptyNeighbours_getsASpillWidth() {
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");

        CellData data = dataFor(a1);

        assertTrue(data.overflowWidth > spreadsheet.getColW()[0],
                "The text should be given room past its own column, was "
                        + data.overflowWidth);
    }

    @Test
    void wideText_withTheNextCellTaken_staysInItsColumn() {
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");
        spreadsheet.createCell(0, 1, "in the way");

        // Its own column's width: the client still knows to cut it short
        // with an ellipsis rather than clip it mid-letter.
        assertEquals(spreadsheet.getColW()[0], dataFor(a1).overflowWidth);
    }

    @Test
    void wideText_withABlankNeighbour_runsAcrossIt() {
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");
        spreadsheet.createCell(0, 1, "");

        assertTrue(dataFor(a1).overflowWidth > 0,
                "An empty string is not something to stop at");
    }

    @Test
    void textThatFits_getsNoSpillWidth() {
        reportWidthRatios(1f);
        Cell a1 = spreadsheet.createCell(0, 0, "narrow");

        assertEquals(0, dataFor(a1).overflowWidth);
    }

    @Test
    void rightAlignedText_runsOnToTheRightToo() {
        // The GWT client let a value too wide for its column overflow its
        // cell whatever the alignment.
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");
        CellStyle style = spreadsheet.getWorkbook().createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        a1.setCellStyle(style);

        assertTrue(dataFor(a1).overflowWidth > spreadsheet.getColW()[0],
                "The text should be given room past its own column");
    }

    @Test
    void dateTooWideForItsColumn_runsOn() {
        // A date is never hashed the way a number is: the GWT client let one
        // too wide for its column run on into the empty cells beside it.
        // The server left dates out of every width check, so the Lit client
        // clipped them mid-digit.
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0,
                new GregorianCalendar(1999, 2, 19).getTime());
        CellStyle style = spreadsheet.getWorkbook().createCellStyle();
        style.setDataFormat(spreadsheet.getWorkbook().createDataFormat()
                .getFormat("yyyy-mm-dd"));
        a1.setCellStyle(style);

        CellData data = dataFor(a1);

        assertEquals("1999-03-19", data.value);
        assertTrue(data.overflowWidth > spreadsheet.getColW()[0],
                "The date should be given room past its own column, was "
                        + data.overflowWidth);
    }

    @Test
    void dateThatFits_getsNoSpillWidth() {
        reportWidthRatios(1f);
        Cell a1 = spreadsheet.createCell(0, 0,
                new GregorianCalendar(1999, 2, 19).getTime());
        CellStyle style = spreadsheet.getWorkbook().createCellStyle();
        style.setDataFormat(spreadsheet.getWorkbook().createDataFormat()
                .getFormat("yyyy-mm-dd"));
        a1.setCellStyle(style);

        assertEquals(0, dataFor(a1).overflowWidth);
    }

    @Test
    void wrappedText_getsNoSpillWidth() {
        // Wrapped text grows downwards; it has no business in the next
        // column.
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");
        CellStyle style = spreadsheet.getWorkbook().createCellStyle();
        style.setWrapText(true);
        a1.setCellStyle(style);

        assertEquals(0, dataFor(a1).overflowWidth);
    }

    @Test
    void mergedCells_doNotSpill() {
        // A merged cell already owns the columns it spans, and the cells it
        // would run into are its own.
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");
        spreadsheet.addMergedRegion(0, 0, 0, 1);

        assertEquals(0, dataFor(a1).overflowWidth);
    }

    @Test
    void aNumberShownInScientificNotation_runsOnLikeTheGwtClientsDid() {
        // Excel would hash it. The GWT client showed "1.23457E+14" and let it
        // overflow its right-aligned cell into the empty one beside it, which
        // is what this branch matches; the Lit client used to clip it.
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, 123456789012345d);

        CellData data = dataFor(a1);

        // The decimal separator is the locale's.
        assertTrue(data.value.endsWith("E+14"), data.value);
        assertTrue(data.overflowWidth > spreadsheet.getColW()[0],
                "The number should be given room past its own column, was "
                        + data.overflowWidth);
    }

    @Test
    void aLinksLabel_runsOnLikeText() {
        // The GWT client measured every cell it drew, so a link whose label
        // was too wide ran on into the empty cells beside it.
        reportWidthRatios(WIDE);
        Cell a1 = spreadsheet.createCell(0, 0, null);
        a1.setCellFormula(
                "HYPERLINK(\"https://vaadin.com\",\"a link label far too wide\")");

        assertTrue(dataFor(a1).overflowWidth > spreadsheet.getColW()[0]);
    }

    @Test
    void anErrorTooWideForItsColumn_runsOnLikeText() {
        reportWidthRatios(WIDE);
        spreadsheet.setColumnWidth(0, 40);
        Cell a1 = spreadsheet.createCell(0, 0, null);
        a1.setCellErrorValue(FormulaError.DIV0.getCode());

        assertTrue(dataFor(a1).overflowWidth > spreadsheet.getColW()[0]);
    }

    @Test
    void aTruthValueTooWideForItsColumn_runsOnLikeText() {
        reportWidthRatios(WIDE);
        spreadsheet.setColumnWidth(0, 30);
        Cell a1 = spreadsheet.createCell(0, 0, Boolean.FALSE);

        assertTrue(dataFor(a1).overflowWidth > spreadsheet.getColW()[0]);
    }

    @Test
    void beforeTheClientReportsWidths_nothingSpills() {
        // Without a ratio the server cannot tell how wide the text is, and
        // guessing would show a spill that snaps away on the next update.
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");

        assertEquals(0, dataFor(a1).overflowWidth);
    }
}
