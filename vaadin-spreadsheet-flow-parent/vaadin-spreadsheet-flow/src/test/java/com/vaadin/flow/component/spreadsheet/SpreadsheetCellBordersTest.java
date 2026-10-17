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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

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
     * a merged region B6:C7 before D6, H2, H4 and C7 of a style without
     * borders, and the bottom border of I3. Rows 8 to 11 hold a merged region
     * B9:D11, the middle cell of each edge and the middle cell inside it with a
     * top or a left border.
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
        FreshLoadFixture.cell(sheet, "C7").setCellStyle(filled);
        CellStyle thickLeft = workbook.createCellStyle();
        thickLeft.setBorderLeft(BorderStyle.THICK);
        set(sheet, "B9", "edges");
        FreshLoadFixture.cell(sheet, "B10").setCellStyle(left);
        FreshLoadFixture.cell(sheet, "C9").setCellStyle(top);
        FreshLoadFixture.cell(sheet, "D10").setCellStyle(top);
        FreshLoadFixture.cell(sheet, "C11").setCellStyle(left);
        FreshLoadFixture.cell(sheet, "C10").setCellStyle(thickLeft);
        sheet.addMergedRegion(CellRangeAddress.valueOf("B9:D11"));
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
                byApplication("a style given a left border in place",
                        spreadsheet -> {
                            Cell filled = cell(spreadsheet, "H2");
                            filled.getCellStyle()
                                    .setBorderLeft(BorderStyle.THICK);
                            spreadsheet.refreshCells(filled);
                        }),
                byApplication("a style given a right border in place",
                        spreadsheet -> {
                            Cell filled = cell(spreadsheet, "H2");
                            filled.getCellStyle()
                                    .setBorderRight(BorderStyle.THICK);
                            spreadsheet.refreshCells(filled);
                        }),
                byApplication("a style given a bottom border in place",
                        spreadsheet -> {
                            Cell filled = cell(spreadsheet, "H2");
                            filled.getCellStyle()
                                    .setBorderBottom(BorderStyle.THICK);
                            spreadsheet.refreshCells(filled);
                        }),
                byApplication(
                        "a style's top border taken off in place, the workbook's styles reloaded",
                        spreadsheet -> {
                            cell(spreadsheet, "B2").getCellStyle()
                                    .setBorderTop(BorderStyle.NONE);
                            spreadsheet.getSpreadsheetStyleFactory()
                                    .reloadWorkbookStyles();
                        }),
                byApplication(
                        "a style given a top border in place, the workbook's styles reloaded",
                        spreadsheet -> {
                            cell(spreadsheet, "H2").getCellStyle()
                                    .setBorderTop(BorderStyle.THICK);
                            spreadsheet.getSpreadsheetStyleFactory()
                                    .reloadWorkbookStyles();
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
                        }, false),
                // A2, which is empty, filled right over B2: the top border B2
                // drew goes with its style.
                new Operation("an empty cell filled over a top border",
                        spreadsheet -> {
                            TestHelper.fireClientEvent(spreadsheet,
                                    "cellSelected",
                                    "{\"row\": 2, \"column\": 1, "
                                            + "\"oldSelectionRangeDiscarded\": true}");
                            TestHelper.fireClientEvent(spreadsheet,
                                    "selectionIncreasePainted",
                                    "{\"r1\": 2, \"c1\": 1, \"r2\": 2, \"c2\": 2}");
                        }, false));
    }

    private static final String BORDERS = "property shiftedCellBorderStyles";

    /**
     * What the operations are known to do wrong today, by case name, whenever
     * they are run: a part of the text of each problem. A fix takes out what it
     * fixes, so the test fails without it.
     */
    private static final Map<String, List<String>> KNOWN = Map.of();

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

        assertKnown(KNOWN.getOrDefault(operation.name(), List.of()),
                harness.check(operation.action()));
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
    void theBordersDrawnOnOtherCells_areGivenAsOneRuleForEachStyleAndSide() {
        Spreadsheet spreadsheet = shown(workbook());

        // The left borders, the top borders, then the merged cells, each on
        // its cells in the order of the rows.
        assertEquals(List.of(
                ".col3.row3,.col2.row6,.col2.row9,.col1.row10{border-right:solid 1pt;border-right-color:var(--default-color);}",
                ".col2.row1,.col5.row1,.col3.row8,.col2.row9{border-bottom:solid 1pt;border-bottom-color:var(--default-color);}",
                ".col6.row2{border-right:solid 2pt;border-right-color:var(--default-color);border-bottom:dashed 1pt;border-bottom-color:var(--default-color);}"),
                rules(spreadsheet));
    }

    @Test
    void insideAMergedRegion_onlyTheCellsOnItsEdgesDrawOnOtherCells() {
        Spreadsheet spreadsheet = shown(workbook());

        // B10 on the left edge, C9 on the top edge; D10 on the right edge
        // and C11 on the bottom edge draw on the merged cell B9.
        assertTrue(drawnOn(spreadsheet, "A10").startsWith("border-right:"));
        assertTrue(drawnOn(spreadsheet, "C8").startsWith("border-bottom:"));
        String merged = drawnOn(spreadsheet, "B9");
        assertTrue(merged.contains("border-right:solid 1pt;"), merged);
        assertTrue(merged.contains("border-bottom:solid 1pt;"), merged);
        // Not the thick left border of C10, inside it.
        assertFalse(merged.contains("solid 3pt"), merged);
    }

    @Test
    void aLeftBorderWithOnlyColumnsHiddenBeforeIt_isDrawnOnNoCell() {
        // B10's, with column A hidden.
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).setColumnHidden(0, true);

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals("", drawnOn(spreadsheet, "A10"));
        assertTrue(
                rules(spreadsheet).stream()
                        .noneMatch(rule -> rule.contains(".col0.")),
                rules(spreadsheet).toString());
    }

    @Test
    void aTopBorderWithOnlyRowsHiddenAboveIt_isDrawnOnNoCell() {
        // B2's and E2's, with row 1 hidden.
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).getRow(0).setZeroHeight(true);

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals("", drawnOn(spreadsheet, "B1"));
        assertEquals("", drawnOn(spreadsheet, "E1"));
        assertTrue(
                rules(spreadsheet).stream()
                        .noneMatch(rule -> rule.contains(".row0,")
                                || rule.contains(".row0{")),
                rules(spreadsheet).toString());
    }

    @Test
    void theBordersOfAStyleNotApplyingThem_areDrawn() {
        // Some files leave the borders of a style in its border entry with
        // applyBorder off, which POI then reads as no border.
        XSSFWorkbook workbook = workbook();
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.getCoreXf().setApplyBorder(false);
        set(workbook.getSheetAt(0), "F6", "border entry").setCellStyle(style);

        Spreadsheet spreadsheet = shown(workbook);

        assertTrue(drawnOn(spreadsheet, "F5")
                .startsWith("border-bottom:solid 1pt;"));
        assertTrue(drawnOn(spreadsheet, "E6")
                .startsWith("border-right:solid 1pt;"));
        assertTrue(cssOf(spreadsheet, "F6").contains("border-right:solid 3pt;"),
                cssOf(spreadsheet, "F6"));
        assertTrue(
                cssOf(spreadsheet, "F6").contains("border-bottom:solid 3pt;"),
                cssOf(spreadsheet, "F6"));
    }

    @Test
    void aStyleNotApplyingABorderEntryWithoutSides_drawsNoBorder() {
        // Only a diagonal, which is not drawn: an entry without sides is
        // taken for the first one, of sides without lines.
        XSSFWorkbook workbook = workbook();
        CTBorder entry = CTBorder.Factory.newInstance();
        entry.addNewDiagonal().setStyle(STBorderStyle.THIN);
        int empty = workbook.getStylesSource()
                .putBorder(new XSSFCellBorder(entry));
        XSSFCellStyle style = workbook.createCellStyle();
        style.getCoreXf().setBorderId(empty);
        style.getCoreXf().setApplyBorder(false);
        set(workbook.getSheetAt(0), "F6", "no sides").setCellStyle(style);

        Spreadsheet spreadsheet = shown(workbook);

        assertFalse(cssOf(spreadsheet, "F6").contains("border"),
                cssOf(spreadsheet, "F6"));
        assertEquals("", drawnOn(spreadsheet, "F5"));
        assertEquals("", drawnOn(spreadsheet, "E6"));
    }

    @Test
    void aMergedCellWithOneSideInsideIt_isGivenOnlyThatSide() {
        // The bottom border of I3 inside I2:I3, the right one of H6 inside
        // G6:H6.
        XSSFWorkbook workbook = workbook();
        Sheet sheet = workbook.getSheetAt(0);
        FreshLoadFixture.cell(sheet, "H6")
                .setCellStyle(cell(workbook, "G2").getCellStyle());
        sheet.addMergedRegion(CellRangeAddress.valueOf("I2:I3"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("G6:H6"));

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals(
                "border-bottom:dashed 1pt;"
                        + "border-bottom-color:var(--default-color);",
                drawnOn(spreadsheet, "I2"));
        assertEquals(
                "border-right:solid 2pt;"
                        + "border-right-color:var(--default-color);",
                drawnOn(spreadsheet, "G6"));
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
        // G3, after G2 and F3, with a right and a bottom border of its own.
        XSSFWorkbook workbook = workbook();
        CellStyle thick = workbook.createCellStyle();
        thick.setBorderRight(BorderStyle.THICK);
        thick.setBorderBottom(BorderStyle.THICK);
        FreshLoadFixture.cell(workbook.getSheetAt(0), "G3").setCellStyle(thick);

        Spreadsheet spreadsheet = shown(workbook);

        assertEquals(
                "border-right:solid 2pt;"
                        + "border-right-color:var(--default-color);"
                        + "border-bottom:dashed 1pt;"
                        + "border-bottom-color:var(--default-color);",
                drawnOn(spreadsheet, "F2"));
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
