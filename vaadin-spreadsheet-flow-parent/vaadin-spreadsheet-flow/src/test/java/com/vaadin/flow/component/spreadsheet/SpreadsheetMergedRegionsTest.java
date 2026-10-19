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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.internal.change.MapPutChange;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * Merging and unmerging cells: the regions POI and the client have, the cells a
 * merge hides from the client and gives back, the borders drawn around the
 * merged cell, the regions a merge replaces or is refused over, and the
 * selection that follows.
 */
class SpreadsheetMergedRegionsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final Sheet sheet = workbook.createSheet();
    private Spreadsheet spreadsheet;

    /** Fills A1:D4, each cell with its own name, but for the cells given. */
    private void fillBut(String... missing) {
        Set<String> skipped = Set.of(missing);
        for (int r = 0; r < 4; r++) {
            Row row = sheet.createRow(r);
            for (int c = 0; c < 4; c++) {
                String name = new CellReference(r, c).formatAsString();
                if (!skipped.contains(name)) {
                    row.createCell(c).setCellValue(name);
                }
            }
        }
    }

    private void style(String cell, Consumer<CellStyle> styling) {
        CellReference reference = new CellReference(cell);
        CellStyle style = workbook.createCellStyle();
        styling.accept(style);
        sheet.getRow(reference.getRow()).getCell(reference.getCol())
                .setCellStyle(style);
    }

    /** Shows the sheet, as the client reports rows 1-20 of columns A-J. */
    private void show(String... regions) {
        for (String region : regions) {
            sheet.addMergedRegion(CellRangeAddress.valueOf(region));
        }
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        // The client reports where it is once it has the sheet.
        sent();
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, \"lastColumn\": 10}");
        sent();
    }

    /** Cells sent to the client: those taken off it, and those with values. */
    private record Sent(Set<String> takenOff, Set<String> withValues) {
    }

    private Sent sent() {
        ui.fakeClientCommunication();
        Sent sent = new Sent(new TreeSet<>(), new TreeSet<>());
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            if (!parameters.contains("cellsUpdated")) {
                continue;
            }
            for (Object parameter : parameters) {
                if (parameter instanceof JsonNode json && json.isArray()) {
                    for (JsonNode cell : json) {
                        String name = new CellReference(
                                cell.get("row").asInt() - 1,
                                cell.get("col").asInt() - 1).formatAsString();
                        (cell.path("value").isNull() ? sent.takenOff()
                                : sent.withValues()).add(name);
                    }
                }
            }
        }
        return sent;
    }

    /**
     * The region lists the response gives the client, each as POI names its
     * regions.
     */
    private List<List<String>> regionsSent() {
        UI current = ui.getUI();
        StateNode node = spreadsheet.getElement().getNode();
        current.getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();
        List<List<String>> sent = new ArrayList<>();
        current.getInternals().getStateTree().collectChanges(change -> {
            if (change.getNode() == node && change instanceof MapPutChange put
                    && put.getFeature() == ElementPropertyMap.class
                    && "mergedRegions".equals(put.getKey())) {
                List<String> regions = new ArrayList<>();
                for (JsonNode region : (JsonNode) put.getValue()) {
                    // 1-based
                    regions.add(
                            new CellRangeAddress(region.get("row1").asInt() - 1,
                                    region.get("row2").asInt() - 1,
                                    region.get("col1").asInt() - 1,
                                    region.get("col2").asInt() - 1)
                                    .formatAsString());
                }
                sent.add(regions);
            }
        });
        ui.dumpPendingJavaScriptInvocations();
        return sent;
    }

    private List<String> poiRegions() {
        return sheet.getMergedRegions().stream()
                .map(CellRangeAddress::formatAsString).toList();
    }

    private List<String> clientRegions() {
        List<String> regions = new ArrayList<>();
        if (spreadsheet.sheetModel().mergedRegions.get() != null) {
            for (MergedRegion region : spreadsheet.sheetModel().mergedRegions
                    .get()) {
                regions.add(format(region));
            }
        }
        return regions;
    }

    private static String format(MergedRegion region) {
        // 1-based
        return new CellRangeAddress(region.row1 - 1, region.row2 - 1,
                region.col1 - 1, region.col2 - 1).formatAsString();
    }

    private List<Integer> ids() {
        return spreadsheet.sheetModel().mergedRegions.get().stream()
                .map(region -> region.id).toList();
    }

    /**
     * The borders the client is given for a cell over its own style, as
     * "border-right:solid 3pt", in the order the client gets them.
     */
    private List<String> borders(String cell) {
        CellReference reference = new CellReference(cell);
        String selector = ".col" + (reference.getCol() + 1) + ".row"
                + (reference.getRow() + 1) + "{";
        List<String> borders = new ArrayList<>();
        for (String rule : spreadsheet.getShiftedCellBorderStyles()) {
            if (rule.startsWith(selector)) {
                for (String declaration : rule
                        .substring(selector.length(), rule.length() - 1)
                        .split(";")) {
                    if (!declaration.contains("-color")) {
                        borders.add(declaration);
                    }
                }
            }
        }
        return borders;
    }

    @Test
    void merging_givesTheRegionToPoiAndTheClient_eachWithAnIdOfItsOwn() {
        fillBut();
        show();

        spreadsheet.addMergedRegion("A1:B2");
        spreadsheet.addMergedRegion("D1:D3");

        assertEquals(List.of("A1:B2", "D1:D3"), poiRegions());
        assertEquals(List.of("A1:B2", "D1:D3"), clientRegions());
        assertEquals(List.of(0, 1), ids());
    }

    @Test
    void merging_aRangeAroundRegions_replacesThem_keepingTheOthersInOrder() {
        fillBut();
        show("A1:A2", "F1:F2", "B1:B2", "A5:B5");

        spreadsheet.addMergedRegion("A1:C3");

        List<String> regions = List.of("F1:F2", "A5:B5", "A1:C3");
        assertEquals(regions, poiRegions());
        assertEquals(regions, clientRegions());
    }

    @Test
    void merging_aRangeOverlappingARegion_isRefused() {
        fillBut();
        show("A1:B2");

        IllegalArgumentException refused = assertThrows(
                IllegalArgumentException.class,
                () -> spreadsheet.addMergedRegion("B2:C3"));

        assertTrue(refused.getMessage().contains("overlaps"),
                refused.getMessage());
        assertEquals(List.of("A1:B2"), poiRegions());
        assertEquals(List.of("A1:B2"), clientRegions());
    }

    @Test
    void merging_aRangeInsideARegion_isRefused() {
        fillBut();
        show("A1:C3");

        IllegalArgumentException refused = assertThrows(
                IllegalArgumentException.class,
                () -> spreadsheet.addMergedRegion("A1:B1"));

        assertTrue(refused.getMessage().contains("encloses"),
                refused.getMessage());
        assertEquals(List.of("A1:C3"), poiRegions());
        assertEquals(List.of("A1:C3"), clientRegions());
    }

    @Test
    void merging_refused_changesNothing() {
        // A1:A2 is inside the range, and C1:D1 overlaps it.
        fillBut();
        show("A1:A2", "C1:D1");

        assertThrows(IllegalArgumentException.class,
                () -> spreadsheet.addMergedRegion("A1:C2"));

        assertEquals(List.of("A1:A2", "C1:D1"), poiRegions());
        assertEquals(List.of("A1:A2", "C1:D1"), clientRegions());
        assertEquals(Set.of(), sent().withValues());
    }

    @Test
    void merging_aRegionAgain_replacesIt() {
        fillBut();
        show("A1:C3", "E1:E2");

        spreadsheet.addMergedRegion("A1:C3");

        assertEquals(List.of("E1:E2", "A1:C3"), poiRegions());
        assertEquals(List.of("E1:E2", "A1:C3"), clientRegions());
    }

    @Test
    void unmerging_removesTheRegionAtTheIndex_fromPoiAndTheClient() {
        fillBut();
        show("A1:B1", "A3:B3", "A5:B5");

        spreadsheet.removeMergedRegion(1);

        assertEquals(List.of("A1:B1", "A5:B5"), poiRegions());
        assertEquals(List.of("A1:B1", "A5:B5"), clientRegions());
    }

    @Test
    void mergingAndUnmerging_leaveTheRegionsTheClientWasGivenAsTheyWere() {
        // The regions are looked up through an index of the list, kept for as
        // long as it is the same list of the same size.
        fillBut();
        show("A1:B2");
        List<MergedRegion> before = spreadsheet.sheetModel().mergedRegions
                .get();

        spreadsheet.addMergedRegion("D1:D2");
        List<MergedRegion> merged = spreadsheet.sheetModel().mergedRegions
                .get();
        spreadsheet.removeMergedRegion(0);

        assertEquals(List.of("A1:B2"),
                before.stream().map(region -> format(region)).toList());
        assertEquals(List.of("A1:B2", "D1:D2"),
                merged.stream().map(region -> format(region)).toList());
    }

    @Test
    void merging_sendsNothingForTheCellsItHides() {
        // The client keeps them, as a new spreadsheet gives them too, and
        // draws the merged cell over them.
        fillBut();
        show();

        spreadsheet.addMergedRegion("A1:B2");

        Sent sent = sent();
        assertEquals(Set.of(), sent.takenOff());
        assertTrue(Collections.disjoint(Set.of("A2", "B1", "B2"),
                sent.withValues()), sent.withValues().toString());
    }

    @Test
    void mergingOverMissingRowsAndCells_sendsNothingForTheCellsThereAre() {
        fillBut("B1");
        sheet.removeRow(sheet.getRow(1));
        show();

        spreadsheet.addMergedRegion("A1:B3");

        Sent sent = sent();
        assertEquals(Set.of(), sent.takenOff());
        assertTrue(Collections.disjoint(Set.of("A3", "B3"), sent.withValues()),
                sent.withValues().toString());
    }

    @Test
    void unmerging_sendsNothingForTheCellsTheClientHas() {
        fillBut();
        show("A1:B2");

        spreadsheet.removeMergedRegion(0);

        Sent sent = sent();
        assertEquals(Set.of(), sent.takenOff());
        assertTrue(Collections.disjoint(Set.of("A2", "B1", "B2"),
                sent.withValues()), sent.withValues().toString());
    }

    @Test
    void unmergingOverMissingRowsAndCells_sendsNothingForTheCellsThereAre() {
        fillBut("B1");
        sheet.removeRow(sheet.getRow(1));
        show("A1:B3");

        spreadsheet.removeMergedRegion(0);

        Sent sent = sent();
        assertEquals(Set.of(), sent.takenOff());
        assertTrue(Collections.disjoint(Set.of("A1", "A3", "B3"),
                sent.withValues()), sent.withValues().toString());
    }

    /**
     * C1 has a thick left border and A3 a thick top one, which the client draws
     * on the cells next to them, and B2 dashed right and bottom ones.
     */
    private void showWithBorders() {
        fillBut();
        style("C1", style -> style.setBorderLeft(BorderStyle.THICK));
        style("A3", style -> style.setBorderTop(BorderStyle.THICK));
        style("B2", style -> {
            style.setBorderRight(BorderStyle.DASHED);
            style.setBorderBottom(BorderStyle.DASHED);
        });
        show();
    }

    @Test
    void merging_drawsTheBordersOfTheCellsAroundOnTheMergedCell() {
        showWithBorders();
        assertEquals(List.of("border-right:solid 3pt"), borders("B1"));
        assertEquals(List.of("border-bottom:solid 3pt"), borders("A2"));

        spreadsheet.addMergedRegion("A1:B2");

        assertEquals(
                List.of("border-right:solid 3pt", "border-bottom:solid 3pt",
                        "border-right:dashed 1pt", "border-bottom:dashed 1pt"),
                borders("A1"));
        assertEquals(List.of(), borders("B1"));
        assertEquals(List.of(), borders("A2"));
    }

    @Test
    void unmerging_drawsTheBordersOnTheCellsAgain() {
        showWithBorders();
        spreadsheet.addMergedRegion("A1:B2");

        spreadsheet.removeMergedRegion(0);

        assertEquals(List.of(), borders("A1"));
        assertEquals(List.of("border-right:solid 3pt"), borders("B1"));
        assertEquals(List.of("border-bottom:solid 3pt"), borders("A2"));
    }

    @Test
    void unmerging_withTheFirstCellMissing_leavesNoBordersOfTheMergedCell() {
        fillBut("A1");
        style("B2", style -> {
            style.setBorderRight(BorderStyle.DASHED);
            style.setBorderBottom(BorderStyle.DASHED);
        });
        show();
        spreadsheet.addMergedRegion("A1:B2");
        assertEquals(
                List.of("border-right:dashed 1pt", "border-bottom:dashed 1pt"),
                borders("A1"));

        spreadsheet.removeMergedRegion(0);

        assertEquals(List.of(), borders("A1"));
    }

    @Test
    void unmerging_withACellMissing_leavesTheBordersOfTheCellsAround() {
        // D2's left border is drawn on C2.
        fillBut("B1");
        style("D2", style -> style.setBorderLeft(BorderStyle.THICK));
        show("A1:B2");
        assertEquals(List.of("border-right:solid 3pt"), borders("C2"));

        spreadsheet.removeMergedRegion(0);

        assertEquals(List.of("border-right:solid 3pt"), borders("C2"));
    }

    @Test
    void merging_overTheSelectedCell_selectsTheMergedCell() {
        fillBut();
        show();
        spreadsheet.setSelection("B2");
        List<SelectionChangeEvent> events = new ArrayList<>();
        spreadsheet.addSelectionChangeListener(events::add);

        spreadsheet.addMergedRegion("A1:B2");

        assertEquals(1, events.size());
        assertEquals(
                new CellReference(sheet.getSheetName(), 0, 0, false, false),
                events.get(0).getSelectedCellReference());
        assertEquals(CellRangeAddress.valueOf("A1:B2"),
                events.get(0).getSelectedCellMergedRegion());
    }

    @Test
    void unmerging_theSelectedCell_selectsTheCellsItWas() {
        fillBut();
        show("A1:B2");
        spreadsheet.setSelection("A1");
        List<SelectionChangeEvent> events = new ArrayList<>();
        spreadsheet.addSelectionChangeListener(events::add);

        spreadsheet.removeMergedRegion(0);

        assertEquals(1, events.size());
        assertEquals(List.of(CellRangeAddress.valueOf("A1:B2")),
                events.get(0).getCellRangeAddresses());
    }

    @Test
    void rowsShifted_moveTheRegions_numberedOnFromTheLast() {
        fillBut();
        show();
        spreadsheet.addMergedRegion("A1:B2");

        spreadsheet.shiftRows(0, 3, 2);

        assertEquals(List.of("A3:B4"), clientRegions());
        assertEquals(List.of(1), ids());
    }

    @Test
    void rowsShiftedPastTheRegions_sendThemNotAgain_andOverOne_sendTheMovedOnes() {
        fillBut();
        show("A1:B2", "C3:D3");
        regionsSent();

        // Rows below every region: the regions stay where they are.
        spreadsheet.shiftRows(5, 8, 2);
        assertEquals(List.of(), regionsSent());

        spreadsheet.shiftRows(0, 3, 2);
        assertEquals(List.of(List.of("A3:B4", "C5:D5")), regionsSent());
    }

    @Test
    void rowsShiftedOverTheOnlyRegion_leaveTheClientNone() {
        fillBut();
        show("A1:B2");

        // Rows 3-4 move up by two, over rows 1-2.
        spreadsheet.shiftRows(2, 3, -2);

        assertEquals(List.of(), poiRegions());
        assertNull(spreadsheet.sheetModel().mergedRegions.get());
    }

    @Test
    void reloadingTheRegions_afterPoiLostThemAll_leavesTheClientNone() {
        fillBut();
        show("A1:B2");
        sheet.removeMergedRegion(0);

        spreadsheet.reloadAllMergedRegions();

        assertNull(spreadsheet.sheetModel().mergedRegions.get());
    }

    @Test
    void reloadingTheRegions_givesTheClientPoisRegions_numberedAfresh() {
        fillBut();
        show();
        spreadsheet.addMergedRegion("A1:B2");
        spreadsheet.addMergedRegion("D1:D2");
        sheet.addMergedRegion(CellRangeAddress.valueOf("F1:F2"));

        spreadsheet.reloadAllMergedRegions();

        assertEquals(List.of("A1:B2", "D1:D2", "F1:F2"), clientRegions());
        assertEquals(List.of(0, 1, 2), ids());
    }
}
