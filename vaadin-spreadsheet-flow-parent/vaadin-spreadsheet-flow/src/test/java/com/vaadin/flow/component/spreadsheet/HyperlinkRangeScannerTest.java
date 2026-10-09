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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.HyperlinkCellClickHandler;

/**
 * Collecting the hyperlink tooltips of a cell range with
 * {@link HyperlinkRangeScanner}.
 *
 * <p>
 * Pins the per-cell decision tree:
 *
 * <ul>
 * <li>Cell has a {@link Hyperlink} attached:
 * <ul>
 * <li>XSSF anchor → tooltip text, falling back to address.</li>
 * <li>Non-XSSF (e.g. HSSF) → always address (no per-link tooltip text).</li>
 * </ul>
 * </li>
 * <li>Cell is a {@code HYPERLINK(...)} formula AND a click handler is
 * registered → tooltip via {@code handler.getHyperlinkFunctionTarget}.</li>
 * <li>Anything else → no tooltip entry.</li>
 * </ul>
 *
 * <p>
 * Uses a real {@link XSSFWorkbook} for the sheet/cells (the static
 * {@code DefaultHyperlinkCellClickHandler.isHyperlinkFormulaCell} check reads
 * {@code cell.getCellFormula()}, so a real cell is the simplest substrate).
 * Mocks the parent {@link Spreadsheet} and the {@link Hyperlink} types.
 */
class HyperlinkRangeScannerTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private HyperlinkCellClickHandler handler;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private HashMap<String, String> tooltips;
    private HyperlinkRangeScanner scanner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        tooltips = new HashMap<>();
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        lenient().when(spreadsheet.getHyperlinksTooltips())
                .thenReturn(tooltips);
        lenient().when(spreadsheet.getHyperlinkCellClickHandler())
                .thenReturn(handler);
        scanner = new HyperlinkRangeScanner(spreadsheet);
    }

    @AfterEach
    void tearDown() throws Exception {
        workbook.close();
    }

    @Test
    void scan_emptyRange_writesEmptyTooltipsBack() {
        // Pin: even with nothing to harvest, the scan re-pushes the
        // (still empty) tooltips map back to the spreadsheet. That's the
        // mirror-fire — the loader expects every scan to refresh.
        scanner.scan(1, 1, 5, 5);

        verify(spreadsheet).setHyperlinksTooltips(tooltips);
        assertEquals(0, tooltips.size());
    }

    @Test
    void scan_nullRow_skipsRowWithoutThrowing() {
        // Pin the null-row guard. Row 1 exists; rows 2-3 don't.
        sheet.createRow(0).createCell(0).setCellValue("v");

        scanner.scan(1, 1, 3, 1);

        // No tooltip — the only existing cell has no hyperlink and no
        // HYPERLINK formula.
        assertEquals(0, tooltips.size());
    }

    @Test
    void scan_nullCell_skipsCellWithoutThrowing() {
        // Row exists but the target column is missing.
        sheet.createRow(0);

        scanner.scan(1, 1, 1, 3);

        assertEquals(0, tooltips.size());
    }

    @Test
    void scan_cellWithXSSFHyperlink_andTooltip_usesTooltip() {
        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("clicky");
        XSSFHyperlink link = mock(XSSFHyperlink.class);
        when(link.getTooltip()).thenReturn("hover me");
        cell.setHyperlink(link);

        scanner.scan(1, 1, 1, 1);

        assertEquals("hover me", tooltips.get("col1 row1"));
    }

    @Test
    void scan_cellWithXSSFHyperlink_nullTooltip_fallsBackToAddress() {
        // Pin the Excel-matching fallback: an XSSF link without a tooltip
        // text shows its address as the tooltip.
        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("clicky");
        XSSFHyperlink link = mock(XSSFHyperlink.class);
        when(link.getTooltip()).thenReturn(null);
        when(link.getAddress()).thenReturn("https://example.com");
        cell.setHyperlink(link);

        scanner.scan(1, 1, 1, 1);

        assertEquals("https://example.com", tooltips.get("col1 row1"));
    }

    @Test
    void scan_cellWithNonXSSFHyperlink_alwaysUsesAddress() {
        // Pin: non-XSSF links (HSSF or plain Hyperlink) skip the tooltip
        // check entirely — they just use the address. No fallback path.
        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("clicky");
        // Need to use the same workbook's CreationHelper for a valid
        // hyperlink, but a mock is simpler since we control the path.
        Hyperlink plain = mock(Hyperlink.class);
        when(plain.getAddress()).thenReturn("mailto:test@example.com");
        cell.setHyperlink(plain);

        scanner.scan(1, 1, 1, 1);

        assertEquals("mailto:test@example.com", tooltips.get("col1 row1"));
    }

    @Test
    void scan_hyperlinkFormulaCell_withHandler_usesHandlerTarget() {
        // Pin the formula-cell branch: HYPERLINK("addr","name") + a
        // registered handler → the handler's getHyperlinkFunctionTarget
        // result becomes the tooltip.
        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellFormula("HYPERLINK(\"https://link\",\"name\")");
        when(handler.getHyperlinkFunctionTarget(cell))
                .thenReturn("https://link");

        scanner.scan(1, 1, 1, 1);

        assertEquals("https://link", tooltips.get("col1 row1"));
    }

    @Test
    void scan_hyperlinkFormulaCell_withoutHandler_skipped() {
        // Pin: no handler set → the HYPERLINK formula cell is silently
        // skipped (no tooltip). The cell stays untouched.
        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellFormula("HYPERLINK(\"https://link\",\"name\")");
        when(spreadsheet.getHyperlinkCellClickHandler()).thenReturn(null);

        scanner.scan(1, 1, 1, 1);

        assertFalse(tooltips.containsKey("col1 row1"));
        verify(handler, never()).getHyperlinkFunctionTarget(any());
    }

    @Test
    void scan_cellWithNeitherHyperlinkNorFormula_skipped() {
        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("plain");

        scanner.scan(1, 1, 1, 1);

        assertFalse(tooltips.containsKey("col1 row1"));
    }

    @Test
    void scan_preservesExistingTooltipsAcrossRanges() {
        // Pin: the scanner reads the SAME map that was set on the
        // spreadsheet (in-place mutation), adds to it, then writes back.
        // Pre-existing entries from earlier scans survive.
        tooltips.put("col9 row9", "kept");
        Cell cell = sheet.createRow(0).createCell(0);
        cell.setCellValue("link");
        XSSFHyperlink link = mock(XSSFHyperlink.class);
        when(link.getTooltip()).thenReturn("new");
        cell.setHyperlink(link);

        scanner.scan(1, 1, 1, 1);

        assertEquals("kept", tooltips.get("col9 row9"));
        assertEquals("new", tooltips.get("col1 row1"));
    }

    @Test
    void scan_oneBasedRange_doesNotVisitCellsOutsideRange() {
        // Pin the 1-based contract via a HYPERLINK formula cell at (1,1)
        // and a scan range that starts at (2,1) — the formula cell at
        // (1,1) should NOT be visited. Uses formula cells (instead of
        // hyperlink cells) because XSSFCell.setHyperlink has a quirk
        // where mocked hyperlinks past the first don't survive
        // attachment.
        Cell outside = sheet.createRow(0).createCell(0);
        outside.setCellFormula("HYPERLINK(\"https://outside\",\"x\")");
        when(handler.getHyperlinkFunctionTarget(outside))
                .thenReturn("https://outside");

        Cell inside = sheet.createRow(1).createCell(0);
        inside.setCellFormula("HYPERLINK(\"https://inside\",\"y\")");
        when(handler.getHyperlinkFunctionTarget(inside))
                .thenReturn("https://inside");

        scanner.scan(2, 1, 2, 1);

        // Only the in-range cell was visited.
        assertFalse(tooltips.containsKey("col1 row1"),
                "Out-of-range cell must not be visited");
        assertEquals("https://inside", tooltips.get("col1 row2"));
    }
}
