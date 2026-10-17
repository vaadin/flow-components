/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Excel draws the top border of a cell at the bottom of the cell above it, and
 * the left border at the right of the cell before it, past the rows and columns
 * hidden, on the merged cell when that is a merged region. It draws the right
 * and the bottom borders of the cells inside a merged region on the merged
 * cell. The client is given those borders as rules for the cells they are drawn
 * on, and after each change it has the rules a new spreadsheet gives it.
 */
class SpreadsheetCellBordersTest {

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private static final Consumer<Spreadsheet> CONFIGURE = spreadsheet -> spreadsheet
            .setLocale(Locale.US);

    private final FreshLoadHarness harness = new FreshLoadHarness(ui, CONFIGURE,
            new FreshLoadHarness.Viewport(1, 1, 12, 9));

    /**
     * The top borders of B2 and E2, the left borders of D3 and D6, a merged
     * region F2:G3 with a right border inside it on G2 and a bottom one on F3,
     * a merged region B6:C7 before D6, H2 and H4 of a style without borders,
     * and the bottom border of I3. Rows 8 and down are empty.
     */
    static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Borders");
        CellStyle top = workbook.createCellStyle();
        top.setBorderTop(BorderStyle.THIN);
        CellStyle left = workbook.createCellStyle();
        left.setBorderLeft(BorderStyle.THIN);
        CellStyle right = workbook.createCellStyle();
        right.setBorderRight(BorderStyle.MEDIUM);
        CellStyle bottom = workbook.createCellStyle();
        bottom.setBorderBottom(BorderStyle.DASHED);
        CellStyle filled = workbook.createCellStyle();
        filled.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        filled.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        set(sheet, "A1", "x");
        set(sheet, "B1", "above");
        set(sheet, "B2", "top").setCellStyle(top);
        set(sheet, "E2", "top too").setCellStyle(top);
        set(sheet, "D3", "left").setCellStyle(left);
        set(sheet, "F2", "merged");
        FreshLoadFixture.cell(sheet, "G2").setCellStyle(right);
        FreshLoadFixture.cell(sheet, "F3").setCellStyle(bottom);
        sheet.addMergedRegion(CellRangeAddress.valueOf("F2:G3"));
        set(sheet, "B6", "region");
        sheet.addMergedRegion(CellRangeAddress.valueOf("B6:C7"));
        set(sheet, "D6", "left of region").setCellStyle(left);
        set(sheet, "H2", "filled").setCellStyle(filled);
        set(sheet, "H4", "filled too").setCellStyle(filled);
        FreshLoadFixture.cell(sheet, "I3").setCellStyle(bottom);
        return workbook;
    }

    private static Cell set(Sheet sheet, String name, String value) {
        Cell cell = FreshLoadFixture.cell(sheet, name);
        cell.setCellValue(value);
        return cell;
    }

    private static Cell cell(Spreadsheet spreadsheet, String name) {
        return FreshLoadFixture.cell(spreadsheet.getActiveSheet(), name);
    }

    /** Gives the cell the style of another, as an application does. */
    private static Consumer<Spreadsheet> styleOf(String cell, String from) {
        return spreadsheet -> {
            Cell target = cell(spreadsheet, cell);
            target.setCellStyle(cell(spreadsheet, from).getCellStyle());
            spreadsheet.refreshCells(target);
        };
    }

    record Operation(String name, Consumer<Spreadsheet> action,
            boolean ofTheApplication) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static Operation byApplication(String name,
            Consumer<Spreadsheet> action) {
        return new Operation(name, action, true);
    }

    static Stream<Operation> operations() {
        return Stream.of(
                byApplication("a cell given a style with a top border",
                        styleOf("B4", "B2")),
                byApplication("the top border of a cell taken away",
                        styleOf("B2", "A1")),
                byApplication("a cell given a style where a border is drawn",
                        styleOf("B1", "H2")),
                byApplication("a cell with a top border deleted",
                        spreadsheet -> spreadsheet.deleteCell(1, 1)),
                byApplication("a cell a border is drawn on deleted",
                        spreadsheet -> spreadsheet.deleteCell(0, 1)),
                byApplication(
                        "a cell with a top border taken out in POI, marked deleted",
                        spreadsheet -> {
                            Cell top = cell(spreadsheet, "B2");
                            top.getRow().removeCell(top);
                            spreadsheet.markCellAsDeleted(top, true);
                        }),
                byApplication("a style given a top border in place",
                        spreadsheet -> {
                            Cell filled = cell(spreadsheet, "H2");
                            filled.getCellStyle()
                                    .setBorderTop(BorderStyle.THICK);
                            spreadsheet.refreshCells(filled);
                        }),
                byApplication("cells merged before a cell with a left border",
                        spreadsheet -> spreadsheet.addMergedRegion("B3:C4")),
                byApplication("cells unmerged before a cell with a left border",
                        spreadsheet -> spreadsheet.removeMergedRegion(1)),
                byApplication("cells with borders inside them unmerged",
                        spreadsheet -> spreadsheet.removeMergedRegion(0)),
                byApplication("cells with a border inside them merged",
                        spreadsheet -> spreadsheet.addMergedRegion("I2:I3")),
                byApplication("rows with borders shifted down",
                        spreadsheet -> spreadsheet.shiftRows(1, 2, 2)),
                byApplication("rows shifted up under a top border",
                        spreadsheet -> spreadsheet.shiftRows(3, 6, -1)),
                byApplication("a row with a left border deleted",
                        spreadsheet -> spreadsheet.deleteRows(2, 2)),
                byApplication("a column hidden before a left border",
                        spreadsheet -> spreadsheet.setColumnHidden(2, true)),
                byApplication("a row hidden above a top border",
                        spreadsheet -> spreadsheet.setRowHidden(0, true)),
                byApplication("a hidden row shown above a top border",
                        spreadsheet -> {
                            spreadsheet.setRowHidden(0, true);
                            spreadsheet.setRowHidden(0, false);
                        }),
                // A1 filled right, over B1.
                new Operation("a cell a border is drawn on filled",
                        spreadsheet -> {
                            TestHelper.fireClientEvent(spreadsheet,
                                    "cellSelected",
                                    "{\"row\": 1, \"column\": 1, "
                                            + "\"oldSelectionRangeDiscarded\": true}");
                            TestHelper.fireClientEvent(spreadsheet,
                                    "selectionIncreasePainted",
                                    "{\"r1\": 1, \"c1\": 1, \"r2\": 1, \"c2\": 2}");
                        }, false));
    }

    private static final String BORDERS = "property shiftedCellBorderStyles";

    /**
     * What the operations are known to do wrong today, by case name, whenever
     * they are run: a part of the text of each problem. A fix takes out what it
     * fixes, so the test fails without it.
     */
    private static final Map<String, List<String>> KNOWN = Map.ofEntries(
            Map.entry("a cell given a style with a top border",
                    List.of(BORDERS)),
            Map.entry("the top border of a cell taken away", List.of(BORDERS)),
            Map.entry("a cell given a style where a border is drawn",
                    List.of(BORDERS)),
            Map.entry("a cell with a top border deleted", List.of(BORDERS)),
            Map.entry("a cell a border is drawn on deleted", List.of(BORDERS)),
            Map.entry(
                    "a cell with a top border taken out in POI, marked deleted",
                    List.of("threw org.apache.xmlbeans.impl.values"
                            + ".XmlValueDisconnectedException", BORDERS)),
            Map.entry("a style given a top border in place", List.of(BORDERS)),
            Map.entry("cells merged before a cell with a left border",
                    List.of(BORDERS)),
            Map.entry("cells unmerged before a cell with a left border",
                    List.of(BORDERS)),
            Map.entry("rows with borders shifted down", List.of(BORDERS)),
            Map.entry("rows shifted up under a top border", List.of(BORDERS)),
            Map.entry("a row with a left border deleted", List.of(BORDERS)),
            Map.entry("a cell a border is drawn on filled", List.of(BORDERS)));

    /**
     * What they are known to do wrong only when the client has seen the
     * spreadsheet, on top of {@link #KNOWN}.
     */
    private static final Map<String, List<String>> KNOWN_WHEN_SHOWN = Map.of(
            "a cell with a top border taken out in POI, marked deleted",
            List.of("cell B2"));

    private static void assertKnown(List<String> known, List<String> problems) {
        assertAll(
                () -> assertEquals(List.of(),
                        problems.stream()
                                .filter(problem -> known.stream()
                                        .noneMatch(problem::contains))
                                .toList(),
                        "Problems not known to happen"),
                () -> assertEquals(List.of(),
                        known.stream()
                                .filter(part -> problems.stream().noneMatch(
                                        problem -> problem.contains(part)))
                                .toList(),
                        "Known problems that no longer happen: take them out"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("operations")
    void theClient_hasTheBordersANewSpreadsheetGivesIt(Operation operation) {
        harness.show(workbook());

        List<String> known = new ArrayList<>(
                KNOWN.getOrDefault(operation.name(), List.of()));
        known.addAll(
                KNOWN_WHEN_SHOWN.getOrDefault(operation.name(), List.of()));
        assertKnown(known, harness.check(operation.action()));
    }

    static Stream<Arguments> operationsBeforeShown() {
        return operations().filter(Operation::ofTheApplication)
                .flatMap(operation -> Stream
                        .of(FreshLoadHarness.Before.values())
                        .map(before -> Arguments.of(operation, before)));
    }

    @ParameterizedTest(name = "{0}, before {1}")
    @MethodSource("operationsBeforeShown")
    void anApplication_canChangeBordersBeforeTheClientHasSeenThem(
            Operation operation, FreshLoadHarness.Before before) {
        assertKnown(KNOWN.getOrDefault(operation.name(), List.of()),
                harness.checkBeforeShown(workbook(), before, operation.action(),
                        CONFIGURE));
    }

    /** A spreadsheet on the workbook, as its client has it. */
    private Spreadsheet shown(XSSFWorkbook workbook) {
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        CONFIGURE.accept(spreadsheet);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        return spreadsheet;
    }

    /**
     * What the rules of the borders drawn on other cells than their own give
     * the cell, in the order the client applies them.
     */
    private static String drawnOn(Spreadsheet spreadsheet, String name) {
        CellReference reference = new CellReference(name);
        String selector = ".col" + (reference.getCol() + 1) + ".row"
                + (reference.getRow() + 1);
        StringBuilder declarations = new StringBuilder();
        for (String rule : rules(spreadsheet)) {
            int open = rule.indexOf('{');
            if (Arrays.asList(rule.substring(0, open).split(","))
                    .contains(selector)) {
                declarations.append(rule, open + 1, rule.length() - 1);
            }
        }
        return declarations.toString();
    }

    private static List<String> rules(Spreadsheet spreadsheet) {
        String property = spreadsheet.getElement()
                .getProperty("shiftedCellBorderStyles");
        List<String> rules = new ArrayList<>();
        if (property != null && !property.isEmpty()) {
            for (JsonNode rule : JSON.readTree(property)) {
                rules.add(rule.asString());
            }
        }
        return rules;
    }

    private static String cssOf(Spreadsheet spreadsheet, String name) {
        return spreadsheet.getCellStyleToCSSStyle()
                .get((int) cell(spreadsheet, name).getCellStyle().getIndex());
    }

    @Test
    void aTopBorder_isDrawnAtTheBottomOfTheCellAbove() {
        Spreadsheet spreadsheet = shown(workbook());

        assertTrue(drawnOn(spreadsheet, "B1").startsWith("border-bottom:"),
                drawnOn(spreadsheet, "B1"));
        assertFalse(cssOf(spreadsheet, "B2").contains("border"),
                cssOf(spreadsheet, "B2"));
    }

    @Test
    void aLeftBorder_isDrawnAtTheRightOfTheCellBefore() {
        Spreadsheet spreadsheet = shown(workbook());

        assertTrue(drawnOn(spreadsheet, "C3").startsWith("border-right:"),
                drawnOn(spreadsheet, "C3"));
        assertFalse(cssOf(spreadsheet, "D3").contains("border"),
                cssOf(spreadsheet, "D3"));
    }

    @Test
    void aRightOrBottomBorder_isDrawnOnItsOwnCell() {
        Spreadsheet spreadsheet = shown(workbook());

        assertTrue(cssOf(spreadsheet, "G2").contains("border-right:"));
        assertTrue(cssOf(spreadsheet, "F3").contains("border-bottom:"));
    }

    @Test
    void aLeftBorder_isDrawnPastTheColumnsHidden() {
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).setColumnHidden(2, true);

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals("", drawnOn(spreadsheet, "C3"));
        assertTrue(drawnOn(spreadsheet, "B3").startsWith("border-right:"));
    }

    @Test
    void aTopBorder_isDrawnPastTheRowsHidden() {
        XSSFWorkbook workbook = workbook();
        set(workbook.getSheetAt(0), "E9", "top")
                .setCellStyle(cell(workbook, "B2").getCellStyle());
        workbook.getSheetAt(0).createRow(7).setZeroHeight(true);

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals("", drawnOn(spreadsheet, "E8"));
        assertTrue(drawnOn(spreadsheet, "E7").startsWith("border-bottom:"));
    }

    @Test
    void aBorderNextToAMergedRegion_isDrawnOnTheMergedCell() {
        Spreadsheet spreadsheet = shown(workbook());

        assertTrue(drawnOn(spreadsheet, "B6").startsWith("border-right:"));
    }

    @Test
    void theBordersInsideAMergedRegion_areDrawnOnTheMergedCell() {
        Spreadsheet spreadsheet = shown(workbook());

        String merged = drawnOn(spreadsheet, "F2");
        assertTrue(merged.contains("border-right:solid 2pt;"), merged);
        assertTrue(merged.contains("border-bottom:dashed 1pt;"), merged);
    }

    @Test
    void ofTheBordersInsideAMergedRegion_theFirstOfEachSideIsDrawn() {
        XSSFWorkbook workbook = workbook();
        CellStyle thick = workbook.createCellStyle();
        thick.setBorderRight(BorderStyle.THICK);
        FreshLoadFixture.cell(workbook.getSheetAt(0), "G3").setCellStyle(thick);

        Spreadsheet spreadsheet = shown(workbook);

        String merged = drawnOn(spreadsheet, "F2");
        assertTrue(merged.contains("border-right:solid 2pt;"), merged);
        assertFalse(merged.contains("border-right:solid 3pt;"), merged);
    }

    @Test
    void aCellHidden_drawsNoBorderOnAnother() {
        // Row 2 and column D hidden: B2, D3 and G2.
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).getRow(1).setZeroHeight(true);
        workbook.getSheetAt(0).setColumnHidden(3, true);

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals("", drawnOn(spreadsheet, "B1"));
        assertEquals("", drawnOn(spreadsheet, "C3"));
        assertFalse(drawnOn(spreadsheet, "F2").contains("border-right"),
                drawnOn(spreadsheet, "F2"));
    }

    @Test
    void theDefaultStyle_drawsNoBorderOnAnotherCell() {
        XSSFWorkbook workbook = workbook();
        workbook.getCellStyleAt(0).setBorderTop(BorderStyle.THIN);
        set(workbook.getSheetAt(0), "A10", "default");

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals("", drawnOn(spreadsheet, "A9"));
    }

    private static Cell cell(XSSFWorkbook workbook, String name) {
        return FreshLoadFixture.cell(workbook.getSheetAt(0), name);
    }
}
