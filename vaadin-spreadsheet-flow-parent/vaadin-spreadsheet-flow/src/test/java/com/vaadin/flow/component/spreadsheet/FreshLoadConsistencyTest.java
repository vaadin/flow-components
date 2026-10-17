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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.vaadin.flow.component.spreadsheet.FreshLoadHarness.Allowance;
import com.vaadin.tests.MockUIExtension;

/**
 * After each thing a user or an application does to a spreadsheet, the client
 * has what a new spreadsheet on the same workbook would give it, and got it
 * without anything sent for nothing, logged, changed on other sheets, or, for
 * what is refused, changed at all. See {@link FreshLoadHarness} for the rules,
 * and {@link FreshLoadFixture} for the workbook.
 */
class FreshLoadConsistencyTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            FreshLoadFixture::configure, FreshLoadFixture.VIEWPORT);

    /** One thing done to the spreadsheet: by its client, or by the server. */
    @FunctionalInterface
    interface Step {
        void run(FreshLoadHarness harness, Spreadsheet spreadsheet);

        default Step andThen(Step next) {
            return (harness, spreadsheet) -> {
                run(harness, spreadsheet);
                next.run(harness, spreadsheet);
            };
        }
    }

    /**
     * An operation, in one step or more, each step checked on its own, with
     * what it is known to do wrong until that is fixed: the problems, by a part
     * of their text, and how many of each kind of thing it sends for nothing.
     */
    record Case(String name, List<Step> steps,
            Consumer<Spreadsheet> configureFresh, Allowance[] allowances,
            boolean refused, List<String> knownProblems,
            Map<String, Integer> knownWaste,
            Consumer<Spreadsheet> application) {

        Case known(String... problems) {
            return new Case(name, steps, configureFresh, allowances, refused,
                    List.of(problems), knownWaste, application);
        }

        /** Kinds of {@link FreshLoadHarness#wasteKind}, each with a count. */
        Case waste(Object... kindsAndCounts) {
            Map<String, Integer> waste = new TreeMap<>();
            for (int i = 0; i < kindsAndCounts.length; i += 2) {
                waste.put((String) kindsAndCounts[i],
                        (Integer) kindsAndCounts[i + 1]);
            }
            return new Case(name, steps, configureFresh, allowances, refused,
                    knownProblems, waste, application);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** What the application does to the spreadsheet. */
    private static Step api(Consumer<Spreadsheet> action) {
        return (harness, spreadsheet) -> action.accept(spreadsheet);
    }

    private static Case op(String name, Step step, Allowance... allowances) {
        return new Case(name, List.of(step), FreshLoadFixture::configure,
                allowances, false, List.of(), Map.of(), null);
    }

    private static Case op(String name, Consumer<Spreadsheet> action,
            Allowance... allowances) {
        return new Case(name, List.of(api(action)), FreshLoadFixture::configure,
                allowances, false, List.of(), Map.of(), action);
    }

    private static Case steps(String name, Step... steps) {
        return new Case(name, Arrays.asList(steps), FreshLoadFixture::configure,
                new Allowance[0], false, List.of(), Map.of(), null);
    }

    private static Case refused(String name, Consumer<Spreadsheet> action) {
        return new Case(name, List.of(api(action)), FreshLoadFixture::configure,
                new Allowance[0], true, List.of(), Map.of(), null);
    }

    private static Case reconfigured(String name, Consumer<Spreadsheet> action,
            Consumer<Spreadsheet> configureFresh) {
        return new Case(name, List.of(api(action)), configureFresh,
                new Allowance[0], false, List.of(), Map.of(), action);
    }

    /** Writes the workbook to a file, which the spreadsheet reads back. */
    private static void writeToAFile(Spreadsheet spreadsheet) {
        try {
            Path file = Files.createTempFile("spreadsheet", ".xlsx");
            file.toFile().deleteOnExit();
            spreadsheet.write(file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The client sends an event, its data as JSON. */
    private static Step event(String type, String data) {
        return (harness, spreadsheet) -> harness.clientEvent(type, data);
    }

    private static Step select(int row, int column) {
        return event("cellSelected", "{\"row\": " + row + ", \"column\": "
                + column + ", \"oldSelectionRangeDiscarded\": true}");
    }

    private static Step selectRange(int row1, int col1, int row2, int col2) {
        return event("cellRangeSelected", "{\"row1\": " + row1 + ", \"col1\": "
                + col1 + ", \"row2\": " + row2 + ", \"col2\": " + col2 + "}");
    }

    private static Step type(int row, int column, String value) {
        return select(row, column).andThen(
                event("cellValueEdited", "{\"row\": " + row + ", \"col\": "
                        + column + ", \"value\": \"" + value + "\"}"));
    }

    private static final String AGAIN = "sent again";
    private static final String TWICE = "sent twice";
    private static final String NOT_THERE = "taken off but not there";
    private static final String POPUPS = "pop-up button added again";

    /**
     * What the operations are known to do wrong today, by case name: the
     * problems, by a part of their text, and how many of each kind of thing
     * they send for nothing. A fix takes out what it fixes, so the test fails
     * without it.
     */
    private static final Map<String, UnaryOperator<Case>> KNOWN = Map.ofEntries(
            Map.entry("rows shifted down", c -> c.known("cell H13")),
            Map.entry("rows of their own heights shifted down",
                    c -> c.known("cell H13")),
            Map.entry("a hidden row shifted down", c -> c.known("cell H13")));

    static Stream<Case> cases() {
        return Stream.of(
                // What a user does
                op("a value typed into a cell", type(10, 5, "7")),
                op("a value typed into an empty cell", type(20, 8, "new")),
                op("a formula typed into a cell", type(12, 8, "=E10*3")),
                op("a cell's value typed away", type(11, 6, "")),
                op("the selected cells deleted",
                        selectRange(10, 5, 11, 6)
                                .andThen(event("deleteSelectedCells", "{}"))),
                op("a selection filled down",
                        selectRange(10, 5, 10, 6).andThen(event(
                                "selectionIncreasePainted",
                                "{\"r1\": 10, \"c1\": 5, \"r2\": 12, \"c2\": 6}"))),
                op("cells pasted",
                        select(14, 7).andThen(event("onPaste",
                                "{\"text\": \"a\\tb\\nc\\td\"}"))),
                op("cells cut",
                        selectRange(10, 5, 10, 6).andThen(
                                event("clearSelectedCellsOnCut", "{}"))),
                steps("an edit undone", type(10, 5, "7"),
                        event("onUndo", "{}")),
                steps("an edit undone and redone", type(10, 5, "7"),
                        event("onUndo", "{}"), event("onRedo", "{}")),
                op("a comment edited", event("updateCellComment",
                        "{\"row\": 12, \"col\": 5, \"text\": \"edited\"}")),
                op("a column resized",
                        event("columnResized",
                                "{\"colIndexes\": [6], \"newWidths\": [120]}")),
                op("a row resized", event("rowsResized",
                        "{\"rowIndexes\": [11], \"newHeights\": [40]}")),
                op("a column under a picture resized",
                        event("columnResized",
                                "{\"colIndexes\": [7], \"newWidths\": [150]}")),
                op("a row under a picture resized", event("rowsResized",
                        "{\"rowIndexes\": [15], \"newHeights\": [40]}")),
                op("a column dragged to nothing",
                        event("columnResized",
                                "{\"colIndexes\": [7], \"newWidths\": [0]}")),
                op("a row dragged to nothing",
                        event("rowsResized",
                                "{\"rowIndexes\": [19], \"newHeights\": [0]}")),
                op("a hidden row dragged open", event("rowsResized",
                        "{\"rowIndexes\": [18], \"newHeights\": [20]}")),
                op("rows dragged, one to nothing", event("rowsResized",
                        "{\"rowIndexes\": [11, 12], \"newHeights\": [0, 40]}")),
                op("columns dragged, one to nothing", event("columnResized",
                        "{\"colIndexes\": [7, 8], \"newWidths\": [0, 120]}")),
                steps("a hidden column dragged open", api(
                        spreadsheet -> spreadsheet.setColumnHidden(6, true)),
                        event("columnResized",
                                "{\"colIndexes\": [7], \"newWidths\": [120]}")),
                op("a column autofitted",
                        event("onColumnAutofit", "{\"columnIndex\": 7}")),
                op("a row autofitted",
                        event("onRowAutofit", "{\"rowIndex\": 11}")),
                op("another sheet selected", event("sheetSelected",
                        "{\"sheetIndex\": 1, \"scrollLeft\": 0, \"scrollTop\": 0}")),
                op("a sheet created",
                        event("sheetCreated",
                                "{\"scrollLeft\": 0, \"scrollTop\": 0}")),
                op("a sheet not shown renamed", event("sheetRenamed",
                        "{\"sheetIndex\": 1, \"newName\": \"Renamed\"}"),
                        Allowance.OTHER_SHEETS),
                op("an address typed into the name box",
                        event("sheetAddressChanged", "{\"value\": \"G10\"}")),
                op("a named range typed into the name box",
                        event("sheetAddressChanged", "{\"value\": \"Total\"}")),

                // What an application does
                op("a cell created",
                        spreadsheet -> spreadsheet.refreshCells(
                                spreadsheet.createCell(19, 7, "created"))),
                op("a cell's value set", spreadsheet -> {
                    var cell = FreshLoadFixture
                            .cell(spreadsheet.getActiveSheet(), "F11");
                    cell.setCellValue("changed");
                    spreadsheet.refreshCells(cell);
                }), op("a cell's style set", spreadsheet -> {
                    CellStyle italic = spreadsheet.getWorkbook()
                            .createCellStyle();
                    Font font = spreadsheet.getWorkbook().createFont();
                    font.setItalic(true);
                    italic.setFont(font);
                    var cell = FreshLoadFixture
                            .cell(spreadsheet.getActiveSheet(), "F11");
                    cell.setCellStyle(italic);
                    spreadsheet.refreshCells(cell);
                }),
                op("a cell deleted",
                        spreadsheet -> spreadsheet.deleteCell(9, 4)),
                op("rows shifted down",
                        spreadsheet -> spreadsheet.shiftRows(10, 13, 1)),
                op("rows shifted up",
                        spreadsheet -> spreadsheet.shiftRows(11, 14, -1)),
                op("a row shifted down past rows not shifted",
                        spreadsheet -> spreadsheet.shiftRows(10, 10, 3)),
                steps("rows of their own heights shifted down",
                        api(spreadsheet -> spreadsheet.setRowHeight(10, 40f)),
                        api(spreadsheet -> spreadsheet.setRowHeight(13, 50f)),
                        api(spreadsheet -> spreadsheet.shiftRows(10, 13, 1))),
                steps("rows of their own heights shifted up",
                        api(spreadsheet -> spreadsheet.setRowHeight(12, 40f)),
                        api(spreadsheet -> spreadsheet.setRowHeight(14, 50f)),
                        api(spreadsheet -> spreadsheet.shiftRows(11, 14, -1))),
                steps("a hidden row shifted down",
                        api(spreadsheet -> spreadsheet.setRowHidden(10, true)),
                        api(spreadsheet -> spreadsheet.shiftRows(10, 13, 1))),
                op("rows deleted",
                        spreadsheet -> spreadsheet.deleteRows(11, 12)),
                steps("a hidden row deleted",
                        api(spreadsheet -> spreadsheet.setRowHidden(11, true)),
                        api(spreadsheet -> spreadsheet.deleteRows(11, 11))),
                steps("a row of its own height deleted",
                        api(spreadsheet -> spreadsheet.setRowHeight(11, 40f)),
                        api(spreadsheet -> spreadsheet.deleteRows(11, 11))),
                op("cells merged",
                        spreadsheet -> spreadsheet.addMergedRegion(
                                CellRangeAddress.valueOf("F13:G14"))),
                op("cells unmerged",
                        spreadsheet -> spreadsheet.removeMergedRegion(0)),
                refused("a merge over another merge",
                        spreadsheet -> spreadsheet.addMergedRegion(
                                CellRangeAddress.valueOf("F15:G16"))),
                op("a row hidden",
                        spreadsheet -> spreadsheet.setRowHidden(11, true)),
                op("a hidden row shown",
                        spreadsheet -> spreadsheet.setRowHidden(17, false)),
                op("a row given no height",
                        spreadsheet -> spreadsheet.setRowHeight(11, 0f)),
                op("a column given no width",
                        spreadsheet -> spreadsheet.setColumnWidth(6, 0)),
                op("a hidden row given a height",
                        spreadsheet -> spreadsheet.setRowHeight(17, 20f)),
                op("a column hidden",
                        spreadsheet -> spreadsheet.setColumnHidden(6, true)),
                steps("a column hidden twice", api(
                        spreadsheet -> spreadsheet.setColumnHidden(6, true)),
                        api(spreadsheet -> spreadsheet.setColumnHidden(6,
                                true))),
                steps("a hidden column shown", api(
                        spreadsheet -> spreadsheet.setColumnHidden(6, true)),
                        api(spreadsheet -> spreadsheet.setColumnHidden(6,
                                false))),
                op("columns hidden together",
                        spreadsheet -> spreadsheet
                                .setColumnsHidden(Map.of(6, true, 7, true))),
                op("a column shown that was not hidden",
                        spreadsheet -> spreadsheet.setColumnHidden(5, false)),
                op("the default row height set",
                        spreadsheet -> spreadsheet.setDefaultRowHeight(20f)),
                steps("a hidden column given a width", api(
                        spreadsheet -> spreadsheet.setColumnHidden(6, true)),
                        api(spreadsheet -> spreadsheet.setColumnWidth(6, 80))),
                op("a column's width set",
                        spreadsheet -> spreadsheet.setColumnWidth(5, 150)),
                op("the width of a column under a picture set",
                        spreadsheet -> spreadsheet.setColumnWidth(6, 150)),
                op("a row's height set",
                        spreadsheet -> spreadsheet.setRowHeight(12, 30f)),
                op("the row of a component given too little height",
                        spreadsheet -> spreadsheet.setRowHeight(12, 10f)),
                op("the height of a row under a picture set",
                        spreadsheet -> spreadsheet.setRowHeight(14, 30f)),
                op("a column autofitted by the application",
                        spreadsheet -> spreadsheet.autofitColumn(4)),
                steps("a hidden column autofitted by the application", api(
                        spreadsheet -> spreadsheet.setColumnHidden(6, true)),
                        api(spreadsheet -> spreadsheet.autofitColumn(6))),
                op("the sheet protected",
                        spreadsheet -> spreadsheet.setActiveSheetProtected("")),
                steps("the sheet protected, then not", api(
                        spreadsheet -> spreadsheet.setActiveSheetProtected("")),
                        api(spreadsheet -> spreadsheet
                                .setActiveSheetProtected(null))),
                steps("a protected sheet shown again, then left", api(
                        spreadsheet -> spreadsheet.setActiveSheetProtected("")),
                        api(spreadsheet -> spreadsheet.setActiveSheetIndex(1)),
                        api(spreadsheet -> spreadsheet.setActiveSheetIndex(0)),
                        api(spreadsheet -> spreadsheet.setActiveSheetIndex(1))),
                op("panes frozen elsewhere",
                        spreadsheet -> spreadsheet.createFreezePane(3, 3)),
                op("another sheet shown",
                        spreadsheet -> spreadsheet.setActiveSheetIndex(1)),
                op("the sheet shown deleted",
                        spreadsheet -> spreadsheet.deleteSheet(0),
                        Allowance.OTHER_SHEETS),
                op("a sheet not shown hidden",
                        spreadsheet -> spreadsheet.setSheetHidden(1,
                                SheetVisibility.HIDDEN)),
                op("the sheet shown hidden",
                        spreadsheet -> spreadsheet.setSheetHidden(0,
                                SheetVisibility.HIDDEN)),
                op("a new sheet created",
                        spreadsheet -> spreadsheet.createNewSheet("Third", 20,
                                10)),
                op("another workbook shown",
                        spreadsheet -> spreadsheet
                                .setWorkbook(FreshLoadFixture.workbook())),
                op("the workbook written to a file",
                        FreshLoadConsistencyTest::writeToAFile),
                steps("a cell's style set after the workbook is written",
                        api(FreshLoadConsistencyTest::writeToAFile),
                        api(spreadsheet -> {
                            var cell = FreshLoadFixture
                                    .cell(spreadsheet.getActiveSheet(), "F11");
                            cell.setCellStyle(spreadsheet.getWorkbook()
                                    .createCellStyle());
                            spreadsheet.refreshCells(cell);
                        })),
                reconfigured("a row given a height, the size of the sheet set",
                        spreadsheet -> {
                            spreadsheet.setMaxRows(300);
                            spreadsheet.setMaxColumns(60);
                            spreadsheet.setRowHeight(12, 30f);
                        }, spreadsheet -> {
                            FreshLoadFixture.configure(spreadsheet);
                            spreadsheet.setMaxRows(300);
                            spreadsheet.setMaxColumns(60);
                        }),
                reconfigured("a row hidden, the size of the sheet set",
                        spreadsheet -> {
                            spreadsheet.setMaxRows(300);
                            spreadsheet.setMaxColumns(60);
                            spreadsheet.setRowHidden(12, true);
                        }, spreadsheet -> {
                            FreshLoadFixture.configure(spreadsheet);
                            spreadsheet.setMaxRows(300);
                            spreadsheet.setMaxColumns(60);
                        }),
                reconfigured("the default column width set",
                        spreadsheet -> spreadsheet.setDefaultColumnWidth(90),
                        spreadsheet -> {
                            FreshLoadFixture.configure(spreadsheet);
                            spreadsheet.setDefaultColumnWidth(90);
                        }),
                reconfigured("the component factory taken away",
                        spreadsheet -> spreadsheet
                                .setSpreadsheetComponentFactory(null),
                        spreadsheet -> spreadsheet.setLocale(Locale.US)),
                op("the cells shown reloaded",
                        spreadsheet -> spreadsheet.reloadVisibleCellContents(),
                        Allowance.RESEND),
                op("every cell value refreshed",
                        spreadsheet -> spreadsheet.refreshAllCellValues(),
                        Allowance.RESEND),
                op("the spreadsheet moved in the UI",
                        (harness, spreadsheet) -> harness.reattach()),
                op("a cell selected by the application",
                        spreadsheet -> spreadsheet.setSelection("G10")))
                .map(operation -> KNOWN.getOrDefault(operation.name(),
                        UnaryOperator.identity()).apply(operation));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void theClient_hasWhatANewSpreadsheetGivesIt(Case operation) {
        harness.show(FreshLoadFixture.workbook());

        List<String> problems = new ArrayList<>();
        List<Step> steps = operation.steps();
        for (int step = 0; step < steps.size(); step++) {
            Step current = steps.get(step);
            Consumer<Spreadsheet> action = spreadsheet -> current.run(harness,
                    spreadsheet);
            List<String> broken = operation.refused()
                    ? harness.checkRefused(action)
                    : harness.check(action, operation.configureFresh(),
                            operation.allowances());
            String prefix = steps.size() > 1 ? "step " + (step + 1) + ": " : "";
            broken.forEach(problem -> problems.add(prefix + problem));
        }

        Map<String, Integer> waste = new TreeMap<>();
        List<String> others = new ArrayList<>();
        for (String problem : problems) {
            String kind = FreshLoadHarness.wasteKind(problem);
            if (kind == null) {
                others.add(problem);
            } else {
                waste.merge(kind, 1, Integer::sum);
            }
        }
        List<String> unknown = others.stream().filter(problem -> operation
                .knownProblems().stream().noneMatch(problem::contains))
                .toList();
        List<String> gone = operation.knownProblems().stream()
                .filter(known -> others.stream()
                        .noneMatch(problem -> problem.contains(known)))
                .toList();
        assertAll(
                () -> assertEquals(List.of(), unknown,
                        "Problems not known to happen"),
                () -> assertEquals(List.of(), gone,
                        "Known problems that no longer happen: take them out"),
                () -> assertEquals(operation.knownWaste(), waste,
                        "What is sent for nothing, by kind: "
                                + problems.stream()
                                        .filter(problem -> FreshLoadHarness
                                                .wasteKind(problem) != null)
                                        .toList()));
    }

    /** The operations an application does, to run before the client is. */
    static Stream<Arguments> applicationCases() {
        return cases().filter(operation -> operation.application() != null)
                .flatMap(operation -> Stream
                        .of(FreshLoadHarness.Before.values())
                        .map(before -> Arguments.of(operation, before)));
    }

    /**
     * What the operations of an application are known to do wrong today when
     * the client has not seen the spreadsheet yet, by case name and when.
     */
    private static final Map<String, List<String>> KNOWN_BEFORE_SHOWN = Map
            .of();

    @ParameterizedTest(name = "{0}, before {1}")
    @MethodSource("applicationCases")
    void anApplication_canDoItBeforeTheClientHasSeenTheSpreadsheet(
            Case operation, FreshLoadHarness.Before before) {
        List<String> problems = harness.checkBeforeShown(
                FreshLoadFixture.workbook(), before, operation.application(),
                operation.configureFresh());

        List<String> known = KNOWN_BEFORE_SHOWN.getOrDefault(
                operation.name() + ", before " + before, List.of());
        List<String> unknown = problems.stream()
                .filter(problem -> known.stream().noneMatch(problem::contains))
                .toList();
        List<String> gone = known.stream().filter(
                part -> problems.stream().noneMatch(p -> p.contains(part)))
                .toList();
        assertAll(
                () -> assertEquals(List.of(), unknown,
                        "Problems not known to happen"),
                () -> assertEquals(List.of(), gone,
                        "Known problems that no longer happen: take them out"));
    }
}
