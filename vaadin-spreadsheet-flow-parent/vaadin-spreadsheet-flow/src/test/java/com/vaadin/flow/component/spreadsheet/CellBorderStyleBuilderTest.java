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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;

/**
 * The border CSS of cell styles, built by {@link CellBorderStyleBuilder}.
 *
 * <p>
 * Covers the high-value branches without diving into the XSSF theme-border XML
 * path (which requires constructing CT-level XML fixtures).
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>5 storage maps + their reset semantics (resetAll vs resetAccumulators —
 * the accumulators clear independently of the persistent maps).</li>
 * <li>{@code appendBorderStyles}: right/bottom emit into the rule's own CSS;
 * top/left go into spillover maps keyed by cellStyle.getIndex().</li>
 * <li>{@code processCustomStyling}: default-style (key 0) early-return;
 * hidden-row/column early-return; merged-region edge → routes border CSS into
 * mergedCellBorders keyed by .col<col1>.row<row1>.</li>
 * <li>{@code buildMergedCellBorderCss}: trailing-comma stripping, empty
 * selector replaced with .notusedselector.</li>
 * <li>{@code removeCellFromShiftedStyles}: per-selector list mutation (drop
 * entire entry vs drop selector from comma-list).</li>
 * <li>{@code stripLeadingSelectorsFromShiftedMaps}: strips ".col*" prefix
 * leaving "{...}" only.</li>
 * <li>{@code appendShiftedStylesToOutput}: only entries starting with ".col"
 * make it to the output list.</li>
 * </ul>
 */
class CellBorderStyleBuilderTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private ColorConverter colorConverter;
    @Mock
    private MergedRegionContainer regionContainer;

    private CellBorderStyleBuilder builder;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        builder = new CellBorderStyleBuilder(spreadsheet, colorConverter);
        // mergedRegionContainer is a protected final field — assign via
        // reflection.
        java.lang.reflect.Field f = Spreadsheet.class
                .getDeclaredField("mergedRegionContainer");
        f.setAccessible(true);
        f.set(spreadsheet, regionContainer);
        lenient().when(regionContainer.getMergedRegion(anyInt(), anyInt()))
                .thenReturn(null);
        lenient().when(colorConverter.getBorderColorCSS(any(), anyString(),
                any(CellStyle.class))).thenReturn("color:#000;");
    }

    // ----- storage / reset --------------------------------------------------

    @Test
    void freshBuilder_hasEmptyMaps() {
        assertTrue(builder.getShiftedBorderTopStyles().isEmpty());
        assertTrue(builder.getShiftedBorderLeftStyles().isEmpty());
        assertTrue(builder.getMergedCellBorders().isEmpty());
    }

    @Test
    void resetAll_clearsAllMaps() {
        // Populate via a top + left border style emission.
        CellStyle style = newCellStyle(BorderStyle.THIN, BorderStyle.THIN,
                BorderStyle.NONE, BorderStyle.NONE, (short) 7);
        StringBuilder sb = new StringBuilder();
        builder.appendBorderStyles(sb, style);
        assertFalse(builder.getShiftedBorderTopStyles().isEmpty());
        assertFalse(builder.getShiftedBorderLeftStyles().isEmpty());

        builder.resetAll();

        assertTrue(builder.getShiftedBorderTopStyles().isEmpty());
        assertTrue(builder.getShiftedBorderLeftStyles().isEmpty());
        assertTrue(builder.getMergedCellBorders().isEmpty());
    }

    @Test
    void resetAccumulators_keepsPersistentMaps() {
        // Pin the load-bearing distinction: resetAccumulators only clears
        // the two temp colToRow accumulators, leaving the final styles
        // intact for the next pass to find them.
        CellStyle style = newCellStyle(BorderStyle.THIN, BorderStyle.THIN,
                BorderStyle.NONE, BorderStyle.NONE, (short) 7);
        builder.appendBorderStyles(new StringBuilder(), style);
        assertFalse(builder.getShiftedBorderTopStyles().isEmpty());

        builder.resetAccumulators();

        // Persistent maps preserved.
        assertFalse(builder.getShiftedBorderTopStyles().isEmpty());
        assertFalse(builder.getShiftedBorderLeftStyles().isEmpty());
    }

    // ----- appendBorderStyles -----------------------------------------------

    @Test
    void appendBorderStyles_rightAndBottom_emitInOwnCss_noSpillover() {
        // Right + bottom borders write into the rule's own CSS builder.
        // No spillover map entry, the top/left maps stay empty.
        CellStyle style = newCellStyle(BorderStyle.NONE, BorderStyle.NONE,
                BorderStyle.THIN, BorderStyle.THIN, (short) 3);
        StringBuilder sb = new StringBuilder();

        builder.appendBorderStyles(sb, style);

        assertTrue(sb.toString().contains("border-right:"));
        assertTrue(sb.toString().contains("border-bottom:"));
        assertTrue(builder.getShiftedBorderTopStyles().isEmpty());
        assertTrue(builder.getShiftedBorderLeftStyles().isEmpty());
    }

    @Test
    void appendBorderStyles_top_emitsBorderBottomIntoSpilloverMap() {
        // Pin: top border → spillover entry "{border-bottom:...}" keyed by
        // cellStyle.getIndex(). The spillover CSS targets the cell ABOVE.
        CellStyle style = newCellStyle(BorderStyle.NONE, BorderStyle.THIN,
                BorderStyle.NONE, BorderStyle.NONE, (short) 9);
        StringBuilder sb = new StringBuilder();

        builder.appendBorderStyles(sb, style);

        String topEntry = builder.getShiftedBorderTopStyles().get(9);
        assertTrue(topEntry.startsWith("{border-bottom:"));
        assertTrue(topEntry.endsWith("}"));
        // The rule's own CSS is empty — top doesn't go there.
        assertEquals(0, sb.length());
    }

    @Test
    void appendBorderStyles_left_emitsBorderRightIntoSpilloverMap() {
        // Pin: left border → spillover entry "{border-right:...}" for
        // the cell to the LEFT.
        CellStyle style = newCellStyle(BorderStyle.THIN, BorderStyle.NONE,
                BorderStyle.NONE, BorderStyle.NONE, (short) 11);
        StringBuilder sb = new StringBuilder();

        builder.appendBorderStyles(sb, style);

        String leftEntry = builder.getShiftedBorderLeftStyles().get(11);
        assertTrue(leftEntry.startsWith("{border-right:"));
        assertTrue(leftEntry.endsWith("}"));
    }

    @Test
    void appendBorderStyles_allFourBorders_writeRightBottom_spilloverTopLeft() {
        // Pin: a cell with all four borders writes right + bottom into
        // its OWN CSS, AND registers top + left into the spillover maps.
        CellStyle style = newCellStyle(BorderStyle.THIN, BorderStyle.THIN,
                BorderStyle.THIN, BorderStyle.THIN, (short) 5);
        StringBuilder sb = new StringBuilder();

        builder.appendBorderStyles(sb, style);

        assertTrue(sb.toString().contains("border-right:"));
        assertTrue(sb.toString().contains("border-bottom:"));
        assertTrue(builder.getShiftedBorderTopStyles().containsKey(5));
        assertTrue(builder.getShiftedBorderLeftStyles().containsKey(5));
    }

    @Test
    void appendBorderStyles_allNone_emitsNothing() {
        CellStyle style = newCellStyle(BorderStyle.NONE, BorderStyle.NONE,
                BorderStyle.NONE, BorderStyle.NONE, (short) 0);
        StringBuilder sb = new StringBuilder();

        builder.appendBorderStyles(sb, style);

        assertEquals(0, sb.length());
        assertTrue(builder.getShiftedBorderTopStyles().isEmpty());
        assertTrue(builder.getShiftedBorderLeftStyles().isEmpty());
    }

    // ----- processCustomStyling ---------------------------------------------

    @Test
    void processCustomStyling_defaultStyleKey0_earlyReturn() {
        // Pin: cellStyle.getIndex() == 0 → return immediately. No region
        // lookup, no map mutation.
        Cell cell = mockCellWithStyle((short) 0);

        builder.processCustomStyling(cell);

        org.mockito.Mockito.verify(regionContainer, org.mockito.Mockito.never())
                .getMergedRegion(anyInt(), anyInt());
    }

    @Test
    void processCustomStyling_hiddenColumn_skipped() {
        Cell cell = mockCellWithStyle((short) 5);
        when(cell.getColumnIndex()).thenReturn(2);
        when(cell.getRowIndex()).thenReturn(0);
        when(spreadsheet.isColumnHidden(2)).thenReturn(true);

        builder.processCustomStyling(cell);

        org.mockito.Mockito.verify(regionContainer, org.mockito.Mockito.never())
                .getMergedRegion(anyInt(), anyInt());
    }

    @Test
    void processCustomStyling_hiddenRow_skipped() {
        Cell cell = mockCellWithStyle((short) 5);
        when(cell.getColumnIndex()).thenReturn(0);
        when(cell.getRowIndex()).thenReturn(3);
        when(spreadsheet.isRowHidden(3)).thenReturn(true);

        builder.processCustomStyling(cell);

        org.mockito.Mockito.verify(regionContainer, org.mockito.Mockito.never())
                .getMergedRegion(anyInt(), anyInt());
    }

    @Test
    void processCustomStyling_mergedRegionEdge_addsBordersToMergedMap() {
        // Pin: a cell inside a merged region with right OR bottom borders
        // gets its CSS routed into mergedCellBorders keyed by the merged
        // region's top-left selector (.col<col1>.row<row1>).
        CellStyle style = newCellStyle(BorderStyle.NONE, BorderStyle.NONE,
                BorderStyle.THIN, BorderStyle.THIN, (short) 7);
        Cell cell = mockCell(style, 0, 0); // POI 0-based → 1-based (1,1)
        MergedRegion region = new MergedRegion(1, 1, 3, 3);
        when(regionContainer.getMergedRegion(1, 1)).thenReturn(region);

        builder.processCustomStyling(cell);

        String merged = builder.getMergedCellBorders().get(".col1.row1");
        assertTrue(merged != null && merged.contains("border-right"));
        assertTrue(merged.contains("border-bottom"));
    }

    @Test
    void processCustomStyling_mergedMap_doesNotDuplicateExistingBorders() {
        // Pin: subsequent calls with the same cell skip re-adding
        // "border-right" / "border-bottom" if the merged-map entry
        // already contains them.
        CellStyle style = newCellStyle(BorderStyle.NONE, BorderStyle.NONE,
                BorderStyle.THIN, BorderStyle.THIN, (short) 7);
        Cell cell = mockCell(style, 0, 0);
        MergedRegion region = new MergedRegion(1, 1, 3, 3);
        when(regionContainer.getMergedRegion(1, 1)).thenReturn(region);

        builder.processCustomStyling(cell);
        String first = builder.getMergedCellBorders().get(".col1.row1");

        builder.processCustomStyling(cell);
        String second = builder.getMergedCellBorders().get(".col1.row1");

        // Same content — no duplication.
        assertEquals(first, second);
    }

    // ----- buildMergedCellBorderCss -----------------------------------------

    @Test
    void buildMergedCellBorderCss_emptySelector_replacedWithNotUsed() {
        String result = builder.buildMergedCellBorderCss("",
                "border-right:1px solid;");

        assertEquals(".notusedselector{border-right:1px solid;}", result);
    }

    @Test
    void buildMergedCellBorderCss_trailingComma_stripped() {
        String result = builder.buildMergedCellBorderCss(".col1.row1,",
                "border-right:1px;");

        assertEquals(".col1.row1{border-right:1px;}", result);
    }

    @Test
    void buildMergedCellBorderCss_normalSelector_wrappedInBraces() {
        String result = builder.buildMergedCellBorderCss(".col1.row1",
                "border-right:1px;");

        assertEquals(".col1.row1{border-right:1px;}", result);
    }

    // ----- removeCellFromShiftedStyles --------------------------------------

    @Test
    void removeCellFromShiftedStyles_onlySelector_entryDropped() {
        // Pin: an entry whose selector list contains ONLY the target
        // selector is dropped entirely.
        ArrayList<String> styles = new ArrayList<>();
        styles.add(".col2.row3{border-right:1px;}");
        styles.add(".col5.row5{border-bottom:2px;}");

        builder.removeCellFromShiftedStyles(styles, ".col2.row3");

        assertEquals(1, styles.size());
        assertEquals(".col5.row5{border-bottom:2px;}", styles.get(0));
    }

    @Test
    void removeCellFromShiftedStyles_selectorInList_droppedFromList() {
        // Pin: an entry with a comma-separated selector list has the
        // target selector removed, leaving the others.
        ArrayList<String> styles = new ArrayList<>();
        styles.add(".col1.row1,.col2.row2,.col3.row3{border-right:1px;}");

        builder.removeCellFromShiftedStyles(styles, ".col2.row2");

        assertEquals(1, styles.size());
        assertTrue(styles.get(0).contains(".col1.row1"));
        assertTrue(styles.get(0).contains(".col3.row3"));
        assertFalse(styles.get(0).contains(".col2.row2"));
    }

    @Test
    void removeCellFromShiftedStyles_nonMatchingSelector_listUnchanged() {
        ArrayList<String> styles = new ArrayList<>();
        styles.add(".col1.row1{border-right:1px;}");
        styles.add(".col2.row2{border-bottom:2px;}");

        builder.removeCellFromShiftedStyles(styles, ".col9.row9");

        assertEquals(2, styles.size());
    }

    // ----- stripLeadingSelectorsFromShiftedMaps -----------------------------

    @Test
    void stripLeadingSelectorsFromShiftedMaps_stripsColPrefix() {
        // Populate maps via appendBorderStyles, then flatten with a
        // synthetic prefix, then verify strip removes it.
        CellStyle style = newCellStyle(BorderStyle.THIN, BorderStyle.THIN,
                BorderStyle.NONE, BorderStyle.NONE, (short) 5);
        builder.appendBorderStyles(new StringBuilder(), style);
        // The append leaves the maps with "{..}" entries. To exercise
        // strip, prepend a fake .col selector.
        HashMap<Integer, String> top = builder.getShiftedBorderTopStyles();
        top.put(5, ".col1.row1" + top.get(5));

        builder.stripLeadingSelectorsFromShiftedMaps();

        assertTrue(top.get(5).startsWith("{"),
                "Selector prefix stripped, leaving only the {...} body");
    }

    // ----- appendShiftedStylesToOutput --------------------------------------

    @Test
    void appendShiftedStylesToOutput_onlyEntriesStartingWithDotCol() {
        // Pin: entries that start with "{" (no flattened selector) are
        // KEPT in the cache but NOT emitted to the output. Only ".col*"
        // entries reach the output list.
        HashMap<Integer, String> top = builder.getShiftedBorderTopStyles();
        top.put(1, "{border-bottom:1px;}"); // not yet flattened
        top.put(2, ".col1.row1{border-bottom:2px;}"); // flattened

        ArrayList<String> out = new ArrayList<>();
        builder.appendShiftedStylesToOutput(out);

        assertEquals(1, out.size());
        assertTrue(out.get(0).startsWith(".col1.row1"));
    }

    @Test
    void appendShiftedStylesToOutput_emitsMergedCellBorders_viaCssBuilder() {
        // Pin: merged-cell borders are wrapped via buildMergedCellBorderCss
        // (selector + "{" + rules + "}") and added to the output.
        builder.getMergedCellBorders().put(".col1.row1", "border-right:1px;");

        ArrayList<String> out = new ArrayList<>();
        builder.appendShiftedStylesToOutput(out);

        assertEquals(1, out.size());
        assertEquals(".col1.row1{border-right:1px;}", out.get(0));
    }

    // ----- helpers ----------------------------------------------------------

    private CellStyle newCellStyle(BorderStyle left, BorderStyle top,
            BorderStyle right, BorderStyle bottom, short index) {
        CellStyle style = org.mockito.Mockito.mock(CellStyle.class);
        when(style.getBorderLeft()).thenReturn(left);
        when(style.getBorderTop()).thenReturn(top);
        when(style.getBorderRight()).thenReturn(right);
        when(style.getBorderBottom()).thenReturn(bottom);
        when(style.getIndex()).thenReturn(index);
        return style;
    }

    private Cell mockCellWithStyle(short index) {
        CellStyle style = org.mockito.Mockito.mock(CellStyle.class);
        when(style.getIndex()).thenReturn(index);
        Cell cell = org.mockito.Mockito.mock(Cell.class);
        when(cell.getCellStyle()).thenReturn(style);
        return cell;
    }

    private Cell mockCell(CellStyle style, int rowIndex, int colIndex) {
        Cell cell = org.mockito.Mockito.mock(Cell.class);
        when(cell.getCellStyle()).thenReturn(style);
        when(cell.getRowIndex()).thenReturn(rowIndex);
        when(cell.getColumnIndex()).thenReturn(colIndex);
        return cell;
    }
}
