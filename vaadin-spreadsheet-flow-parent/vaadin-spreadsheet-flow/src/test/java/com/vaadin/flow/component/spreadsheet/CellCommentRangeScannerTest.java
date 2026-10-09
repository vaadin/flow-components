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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;

/**
 * Collecting the comments and invalid-formula marks of a cell range with
 * {@link CellCommentRangeScanner}.
 *
 * <p>
 * Pins:
 *
 * <ul>
 * <li>Comment harvest: text + author into separate maps, visible comments into
 * a list.</li>
 * <li>Invalid-formula markers harvested into a set.</li>
 * <li>Skip conditions: null rows pass (cells in null rows read null from
 * {@code getCellComment}); zero-height rows skip; hidden columns skip; cells
 * inside merged regions (not anchor) skip-ahead to {@code col2}; merged-region
 * anchor IS scanned.</li>
 * <li>Defensive new-collection writes, so that each write reaches the element
 * property.</li>
 * </ul>
 *
 * <p>
 * Mocks all collaborators (no real POI workbook). The
 * {@code Sheet.getCellComment(CellAddress)} method is mocked per cell.
 */
class CellCommentRangeScannerTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Sheet sheet;
    @Mock
    private MergedRegionContainer regionContainer;

    private CellCommentRangeScanner scanner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scanner = new CellCommentRangeScanner(spreadsheet);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.getMergedRegionContainer())
                .thenReturn(regionContainer);
        lenient().when(spreadsheet.getCellComments())
                .thenReturn(new HashMap<>());
        lenient().when(spreadsheet.getCellCommentAuthors())
                .thenReturn(new HashMap<>());
        lenient().when(spreadsheet.getVisibleCellComments())
                .thenReturn(new ArrayList<>());
        lenient().when(spreadsheet.getInvalidFormulaCells())
                .thenReturn(new HashSet<>());
        lenient().when(regionContainer.getMergedRegion(anyInt(), anyInt()))
                .thenReturn(null);
    }

    @Test
    void scan_emptyRange_writesAllFourMapsBack() {
        // Pin: even with no cells, all four state setters fire (with the
        // initially-copied empty collections). The mirror-write contract
        // matches CellCommentLoader's expectation that every scan refreshes
        // the property.
        scanner.scan(1, 1, 1, 1);

        verify(spreadsheet).setCellComments(any());
        verify(spreadsheet).setCellCommentAuthors(any());
        verify(spreadsheet).setVisibleCellComments(any());
        verify(spreadsheet).setInvalidFormulaCells(any());
    }

    @Test
    void scan_cellWithComment_harvestsTextAndAuthor() {
        Comment comment = mock(Comment.class);
        RichTextString rich = mock(RichTextString.class);
        when(comment.getString()).thenReturn(rich);
        when(rich.getString()).thenReturn("the comment");
        when(comment.getAuthor()).thenReturn("alice");
        when(comment.isVisible()).thenReturn(false);
        when(sheet.getCellComment(new CellAddress(0, 0))).thenReturn(comment);

        scanner.scan(1, 1, 1, 1);

        HashMap<String, String> comments = captureCellComments();
        assertEquals("the comment", comments.get("col1 row1"));
        HashMap<String, String> authors = captureCellCommentAuthors();
        assertEquals("alice", authors.get("col1 row1"));
        ArrayList<String> visible = captureVisibleCellComments();
        assertFalse(visible.contains("col1 row1"),
                "Invisible comment should not be in the visible list");
    }

    @Test
    void scan_cellWithVisibleComment_addsToVisibleList() {
        Comment comment = mock(Comment.class);
        RichTextString rich = mock(RichTextString.class);
        when(comment.getString()).thenReturn(rich);
        when(rich.getString()).thenReturn("hi");
        when(comment.getAuthor()).thenReturn("bob");
        when(comment.isVisible()).thenReturn(true);
        when(sheet.getCellComment(new CellAddress(0, 0))).thenReturn(comment);

        scanner.scan(1, 1, 1, 1);

        ArrayList<String> visible = captureVisibleCellComments();
        assertTrue(visible.contains("col1 row1"));
    }

    @Test
    void scan_cellWithoutComment_addsNothingToCommentMaps() {
        // Pin: getCellComment returns null → no entries in any of the
        // comment-state maps. The invalid-formula marker is still
        // checked independently.
        when(sheet.getCellComment(new CellAddress(0, 0))).thenReturn(null);

        scanner.scan(1, 1, 1, 1);

        assertEquals(0, captureCellComments().size());
        assertEquals(0, captureCellCommentAuthors().size());
        assertEquals(0, captureVisibleCellComments().size());
    }

    @Test
    void scan_zeroHeightRow_skipped() {
        // Pin the zero-height-row guard.
        org.apache.poi.ss.usermodel.Row row = mock(
                org.apache.poi.ss.usermodel.Row.class);
        when(sheet.getRow(0)).thenReturn(row);
        when(row.getZeroHeight()).thenReturn(true);

        scanner.scan(1, 1, 1, 1);

        verify(sheet, never()).getCellComment(any(CellAddress.class));
    }

    @Test
    void scan_hiddenColumn_skipped() {
        // Pin the hidden-column skip — checked per-column inside the row
        // loop, AFTER the row-level zero-height check.
        when(sheet.isColumnHidden(0)).thenReturn(true);

        scanner.scan(1, 1, 1, 1);

        verify(sheet, never()).getCellComment(any(CellAddress.class));
    }

    @Test
    void scan_cellInsideMergedRegion_notAnchor_skipsAheadToCol2() {
        // Pin the merged-region skip-ahead: a cell at (c, r) that's inside
        // a merged region whose anchor is NOT (c, r) doesn't get its
        // comment scanned. AND the loop counter jumps ahead to
        // region.col2 - 1 (so the next iteration starts at col2 + 1,
        // skipping the rest of the merge).
        MergedRegion region = new MergedRegion(1, 1, 3, 1); // (col1=1..col2=3,
                                                            // row=1)
        // Cell at (col=2, row=1): inside the merge but not the anchor.
        when(regionContainer.getMergedRegion(2, 1)).thenReturn(region);
        // The scanner should NOT call getCellComment for cell (col=2, row=1).
        // It SHOULD jump to c=region.col2-1=2 next iteration, so the loop
        // exits without scanning col=3.

        scanner.scan(1, 2, 1, 3);

        // Cell at (col=2, row=1) skipped, cell at (col=3, row=1) skipped
        // due to skip-ahead.
        verify(sheet, never()).getCellComment(any(CellAddress.class));
    }

    @Test
    void scan_mergedRegionAnchor_isScanned() {
        // Pin: a cell that IS the anchor of its merged region (col1, row1
        // match the query) gets its comment scanned normally.
        MergedRegion region = new MergedRegion(1, 1, 3, 1);
        when(regionContainer.getMergedRegion(1, 1)).thenReturn(region);
        Comment comment = mock(Comment.class);
        RichTextString rich = mock(RichTextString.class);
        when(comment.getString()).thenReturn(rich);
        when(rich.getString()).thenReturn("anchor comment");
        when(comment.getAuthor()).thenReturn("anchor author");
        when(sheet.getCellComment(new CellAddress(0, 0))).thenReturn(comment);

        scanner.scan(1, 1, 1, 1);

        assertEquals("anchor comment", captureCellComments().get("col1 row1"));
    }

    @Test
    void scan_invalidFormulaMarker_addsKeyToInvalidSet() {
        when(spreadsheet.isMarkedAsInvalidFormula(1, 1)).thenReturn(true);

        scanner.scan(1, 1, 1, 1);

        HashSet<String> invalid = captureInvalidFormulaCells();
        assertTrue(invalid.contains("col1 row1"));
    }

    @Test
    void scan_invalidFormulaAndComment_bothHarvestedIndependently() {
        // Pin: the comment harvest and the invalid-formula check are
        // independent within the same cell-iteration block. A cell can
        // contribute to BOTH the comments map and the invalid-formula set.
        Comment comment = mock(Comment.class);
        RichTextString rich = mock(RichTextString.class);
        when(comment.getString()).thenReturn(rich);
        when(rich.getString()).thenReturn("warn");
        when(comment.getAuthor()).thenReturn("sys");
        when(sheet.getCellComment(new CellAddress(0, 0))).thenReturn(comment);
        when(spreadsheet.isMarkedAsInvalidFormula(1, 1)).thenReturn(true);

        scanner.scan(1, 1, 1, 1);

        assertEquals("warn", captureCellComments().get("col1 row1"));
        assertTrue(captureInvalidFormulaCells().contains("col1 row1"));
    }

    @Test
    void scan_preservesExistingMapEntries_fromPriorScans() {
        // Pin: the scanner copies existing entries from the four state
        // maps into the new collections it builds, so prior scans'
        // entries survive. The collection writes are full snapshots —
        // every key from the prior + new ones from this scan.
        HashMap<String, String> existingComments = new HashMap<>();
        existingComments.put("col9 row9", "kept");
        when(spreadsheet.getCellComments()).thenReturn(existingComments);

        scanner.scan(1, 1, 1, 1);

        assertEquals("kept", captureCellComments().get("col9 row9"));
    }

    // ----- helpers ----------------------------------------------------------

    @SuppressWarnings("unchecked")
    private HashMap<String, String> captureCellComments() {
        ArgumentCaptor<HashMap<String, String>> captor = ArgumentCaptor
                .forClass(HashMap.class);
        verify(spreadsheet).setCellComments(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, String> captureCellCommentAuthors() {
        ArgumentCaptor<HashMap<String, String>> captor = ArgumentCaptor
                .forClass(HashMap.class);
        verify(spreadsheet).setCellCommentAuthors(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> captureVisibleCellComments() {
        ArgumentCaptor<ArrayList<String>> captor = ArgumentCaptor
                .forClass(ArrayList.class);
        verify(spreadsheet).setVisibleCellComments(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private HashSet<String> captureInvalidFormulaCells() {
        ArgumentCaptor<HashSet<String>> captor = ArgumentCaptor
                .forClass(HashSet.class);
        verify(spreadsheet).setInvalidFormulaCells(captor.capture());
        return captor.getValue();
    }
}
