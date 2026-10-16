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
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.tests.CapturedLogs;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.json.JsonMapper;

/**
 * That {@link FreshLoadHarness} sees each kind of mistake it is there to find,
 * and nothing when there is none.
 */
class FreshLoadHarnessTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            FreshLoadFixture::configure, FreshLoadFixture.VIEWPORT);
    private Workbook workbook;

    @BeforeEach
    void show() {
        workbook = FreshLoadFixture.workbook();
        harness.show(workbook);
    }

    private static CellData data(int row, int column) {
        CellData data = new CellData();
        data.row = row;
        data.col = column;
        return data;
    }

    private static void assertReported(List<String> problems, String expected) {
        assertTrue(problems.stream().anyMatch(p -> p.contains(expected)),
                "Expected a problem with '" + expected + "' in " + problems);
    }

    @Test
    void nothingDone_breaksNoRule() {
        assertEquals(List.of(), harness.check(spreadsheet -> {
        }));
    }

    @Test
    void aCellChangedBehindTheSpreadsheetsBack_differsFromAFreshLoad() {
        List<String> problems = harness.check(spreadsheet -> FreshLoadFixture
                .cell(spreadsheet.getActiveSheet(), "E10").setCellValue(42));

        assertReported(problems, "cell E10: ");
    }

    @Test
    void aCellSentTwiceInOneResponse_isReported() {
        List<String> problems = harness.check(spreadsheet -> {
            CellData changed = data(10, 5);
            changed.value = "x";
            spreadsheet.getRpcProxy()
                    .cellsUpdated(new ArrayList<>(List.of(changed, changed)));
        });

        assertReported(problems, "E10 sent twice in one response");
    }

    @Test
    void aCellSentAgainAsTheClientHasIt_isReported() {
        List<String> problems = harness
                .check(spreadsheet -> spreadsheet.refreshCells(FreshLoadFixture
                        .cell(spreadsheet.getActiveSheet(), "F11")));

        assertReported(problems, "F11 sent again as the client has it");
    }

    @Test
    void aCellTakenOffTheClientThatDoesNotHaveIt_isReported() {
        List<String> problems = harness
                .check(spreadsheet -> spreadsheet.getRpcProxy()
                        .cellsUpdated(new ArrayList<>(List.of(data(30, 30)))));

        assertReported(problems,
                "AD30 taken off the client, which does not have it");
    }

    @Test
    void aPopupButtonTakenOffTheClientThatDoesNotHaveIt_isReported() {
        List<String> problems = harness
                .check(spreadsheet -> spreadsheet.getElement().callJsFunction(
                        "removePopupButton", "{\"row\": 30, \"col\": 30}"));

        assertReported(problems, "pop-up button AD30 taken off the client");
    }

    @Test
    void aWarningLogged_isReported() {
        List<String> problems = harness.check(spreadsheet -> LoggerFactory
                .getLogger(Spreadsheet.class).warn("lost {}", "something"));

        assertReported(problems, "logged WARN " + Spreadsheet.class.getName()
                + ": lost something");
    }

    @Test
    void aSheetNotShownChanged_isReported() {
        List<String> problems = harness.check(spreadsheet -> FreshLoadFixture
                .cell(workbook.getSheet("Second"), "A1").setCellValue(5));

        assertReported(problems, "sheet Second not shown changed");
    }

    @Test
    void anOperationRefused_thatChangesTheWorkbook_isReported() {
        List<String> problems = harness
                .checkRefused(spreadsheet -> FreshLoadFixture
                        .cell(spreadsheet.getActiveSheet(), "E10")
                        .setCellValue(42));

        assertReported(problems, "sheet First changed");
    }

    @Test
    void anOperationRefused_thatChangesTheClient_isReported() {
        List<String> problems = harness.checkRefused(spreadsheet -> {
            CellData changed = data(10, 5);
            changed.value = "x";
            spreadsheet.getRpcProxy()
                    .cellsUpdated(new ArrayList<>(List.of(changed)));
        });

        assertReported(problems, "the client changed: cell E10");
    }

    @Test
    void anOperationRefusedByThrowing_thatChangesNothing_breaksNoRule() {
        assertEquals(List.of(), harness.checkRefused(spreadsheet -> {
            throw new IllegalArgumentException("refused");
        }));
    }

    @Test
    void aClientCallTheModelDoesNotKnow_failsTheCheck() {
        assertThrows(AssertionError.class,
                () -> harness.check(spreadsheet -> spreadsheet.getElement()
                        .callJsFunction("somethingNew")));
    }

    @Test
    void theLogs_areKeptOnlyWhileCaptured_withWhatWasThrown() {
        IllegalStateException thrown = new IllegalStateException("broken");
        LoggerFactory.getLogger(Spreadsheet.class).warn("before");

        List<CapturedLogs.Entry> entries;
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            LoggerFactory.getLogger(Spreadsheet.class).info("not kept");
            LoggerFactory.getLogger(Spreadsheet.class).error("failed {}", 3,
                    thrown);
            entries = logs.entries();
        }
        LoggerFactory.getLogger(Spreadsheet.class).warn("after");

        assertEquals(1, entries.size());
        assertEquals("error", entries.get(0).level());
        assertEquals("failed 3", entries.get(0).message());
        assertEquals(thrown, entries.get(0).throwable());
    }

    @Test
    void theFirstDifference_isTheFirstLineThatDiffers() {
        assertNull(FreshLoadHarness.firstDifference("a\nb", "a\nb"));
        assertEquals("'b' became 'c'",
                FreshLoadHarness.firstDifference("a\nb\nd", "a\nc\nd"));
        assertEquals("'b' became '(nothing)'",
                FreshLoadHarness.firstDifference("a\nb", "a"));
    }

    @Test
    void theWasteKinds_areToldApart_withAStepBefore() {
        assertEquals("sent twice", FreshLoadHarness
                .wasteKind("E10 sent twice in one response (cellsUpdated)"));
        assertEquals("sent again", FreshLoadHarness.wasteKind(
                "step 2: E10 sent again as the client has it (cellsUpdated)"));
        assertEquals("taken off but not there", FreshLoadHarness
                .wasteKind("E10 taken off the client, which does not have it"));
        assertEquals("pop-up button added again", FreshLoadHarness.wasteKind(
                "step 1: pop-up button E19 added again as the client has it"));
        assertEquals("pop-up button taken off but not there",
                FreshLoadHarness.wasteKind("pop-up button E19 taken off the "
                        + "client, which does not have it"));
        assertNull(FreshLoadHarness.wasteKind("cell E10: .value differs"));
    }

    @Test
    void emptyListsAndMaps_andFalseWhereTheClientStartsFalse_areUnset() {
        for (Object empty : new Object[] { null, "", "[]", "{}" }) {
            assertNull(FreshLoadHarness.normalize("mergedRegions", empty));
        }
        assertNull(FreshLoadHarness.normalize("hasComponentFactory", false));
        assertEquals("false", FreshLoadHarness
                .normalize("displayGridlines", false).toString());
        assertEquals("true", FreshLoadHarness
                .normalize("hasComponentFactory", true).toString());
    }

    @Test
    void mergedRegions_areComparedWithoutTheirNumbers_inAnyOrder() {
        assertEquals(
                FreshLoadHarness.normalize("mergedRegions",
                        "[{\"col1\":1,\"id\":3},{\"col1\":2,\"id\":4}]"),
                FreshLoadHarness.normalize("mergedRegions",
                        "[{\"col1\":2},{\"col1\":1,\"id\":7}]"));
    }

    @Test
    void mapsKeyedByIds_areComparedByWhatTheyHold() {
        assertEquals(FreshLoadHarness.normalize("overlays",
                "{\"sheet-overlay-1\":{\"row\":2},\"sheet-overlay-2\":{\"row\":1}}"),
                FreshLoadHarness.normalize("overlays",
                        "{\"sheet-overlay-9\":{\"row\":1},\"sheet-overlay-8\":{\"row\":2}}"));
        assertEquals(
                FreshLoadHarness.normalize("componentIDtoCellKeysMap",
                        "{\"7\":\"col1 row2\"}"),
                FreshLoadHarness.normalize("componentIDtoCellKeysMap",
                        "{\"9\":\"col1 row2\"}"));
        assertEquals(
                FreshLoadHarness.normalize("cellKeysToEditorIdMap",
                        "{\"col1 row2\":\"7\",\"col1 row1\":\"8\"}"),
                FreshLoadHarness.normalize("cellKeysToEditorIdMap",
                        "{\"col1 row1\":\"9\",\"col1 row2\":\"5\"}"));
    }

    @Test
    void cellData_isComparedWithoutWhatTheClientTakesAsUnset() {
        assertEquals("{\"col\":1,\"row\":2,\"value\":\"x\",\"locked\":true}",
                FreshLoadHarness.normalizeCell(MAPPER.readTree(
                        "{\"col\":1,\"row\":2,\"value\":\"x\",\"locked\":true,"
                                + "\"needsMeasure\":false,\"overflowWidth\":0,"
                                + "\"textColor\":null}"))
                        .toString());
        assertEquals("{\"overflowWidth\":5}",
                FreshLoadHarness
                        .normalizeCell(MAPPER.readTree("{\"overflowWidth\":5}"))
                        .toString());
    }

    @Test
    void aValueTyped_showsAtOnceInItsCellsStyle_butAFormulaWaitsForTheServer() {
        FreshLoadHarness.ClientModel model = new FreshLoadHarness.ClientModel();
        model.apply(List.of(new FreshLoadHarness.Call("cellsUpdated", List.of(
                "[{\"col\":5,\"row\":10,\"value\":\"1\",\"cellStyle\":\"cs3\","
                        + "\"overflowWidth\":40}]"))),
                null);

        model.commitLocally(10, 5, "typed");
        model.commitLocally(11, 5, "=E10");
        model.commitLocally(12, 5, "+E10");

        assertEquals(
                "{\"col\":5,\"row\":10,\"value\":\"typed\",\"cellStyle\":\"cs3\"}",
                model.cells.get("E10").toString());
        assertEquals(List.of("E10"), List.copyOf(model.cells.keySet()));
    }

    @Test
    void aValueTypedByTheClient_showsBeforeTheServerAnswers() {
        String[] shown = new String[1];

        harness.check(spreadsheet -> {
            harness.clientEvent("cellValueEdited",
                    "{\"row\": 10, \"col\": 5, \"value\": \"typed\"}");
            shown[0] = harness.clientModel().cells.get("E10").get("value")
                    .asString();
        });

        assertEquals("typed", shown[0]);
    }

    @Test
    void aCommentWrittenOnTheClient_showsUntilTheServerSendsTheComments() {
        harness.clientModel().writeCommentLocally(13, 6, "written");

        List<String> problems = harness.check(spreadsheet -> {
        });

        assertReported(problems, "property cellComments: .col6 row13 the "
                + "client has \"written\", a new spreadsheet sends null");
        assertEquals(Map.of("col6 row13", "written"),
                harness.clientModel().localComments);
    }

    @Test
    void theCommentsSentAgain_replaceThoseWrittenOnTheClient() {
        harness.clientModel().writeCommentLocally(13, 6, "written");

        // Scrolling sends the comments of the cells shown: none there. What
        // the client then shows differs from a new spreadsheet showing the
        // viewport, which is not what this looks at.
        harness.check(spreadsheet -> TestHelper.fireClientEvent(spreadsheet,
                "onSheetScroll", "{\"firstRow\": 30, \"firstColumn\": 5, "
                        + "\"lastRow\": 40, \"lastColumn\": 8}"));

        assertEquals(Map.of(), harness.clientModel().localComments);
    }

    @Test
    void aPopupButtonAddedAgainAsTheClientHasIt_isWaste() {
        FreshLoadHarness.ClientModel model = new FreshLoadHarness.ClientModel();
        List<String> waste = new ArrayList<>();
        FreshLoadHarness.Call add = new FreshLoadHarness.Call("addPopupButton",
                List.of("{\"row\": 19, \"col\": 5}"));

        model.apply(List.of(add), waste);
        model.apply(List.of(add), waste);

        assertEquals(
                List.of("pop-up button E19 added again as the client has it"),
                waste);
    }

    @Test
    void theResourceKeys_areReplacedByEachList() {
        FreshLoadHarness.ClientModel model = new FreshLoadHarness.ClientModel();

        model.apply(List.of(new FreshLoadHarness.Call("resources=",
                List.of(List.of("a", "b")))), null);
        model.apply(List.of(
                new FreshLoadHarness.Call("resources=", List.of(List.of("c")))),
                null);

        assertEquals(List.of("c"), model.resources);
    }

    @Test
    void aReload_forgetsTheCells_butNotThePopupButtons() {
        FreshLoadHarness.ClientModel model = new FreshLoadHarness.ClientModel();
        model.apply(List.of(
                new FreshLoadHarness.Call("cellsUpdated",
                        List.of("[{\"col\":5,\"row\":10,\"value\":\"1\"}]")),
                new FreshLoadHarness.Call("addPopupButton",
                        List.of("{\"row\": 19, \"col\": 5}"))),
                null);

        model.reload();

        assertTrue(model.cells.isEmpty());
        assertEquals(List.of("E19"), List.copyOf(model.popupButtons.keySet()));
    }

    @Test
    void anOperationThatThrows_isReported_andTheRestStillChecked() {
        List<String> problems = harness.check(spreadsheet -> {
            FreshLoadFixture.cell(spreadsheet.getActiveSheet(), "E10")
                    .setCellValue(42);
            throw new IllegalStateException("broken");
        });

        assertReported(problems,
                "threw java.lang.IllegalStateException: broken");
        assertReported(problems, "cell E10: ");
    }

    @Test
    void aNewSpreadsheetThatThrows_isReported() {
        List<String> problems = harness.check(spreadsheet -> {
        }, fresh -> {
            throw new IllegalStateException("cannot");
        });

        assertEquals(List.of(
                "a new spreadsheet threw java.lang.IllegalStateException: cannot"),
                problems);
    }

    @Test
    void aNewSpreadsheetThatChangesTheWorkbook_isReported() {
        List<String> problems = harness.check(spreadsheet -> {
        }, fresh -> {
            FreshLoadFixture.configure(fresh);
            FreshLoadFixture.cell(fresh.getWorkbook().getSheet("Second"), "C3")
                    .setCellValue("added");
        });

        assertReported(problems,
                "showing the workbook in a new spreadsheet changed it");
    }

    @Test
    void theAllowances_letAnOperationDoWhatTheyName() {
        assertEquals(List.of(),
                harness.check(
                        spreadsheet -> spreadsheet.refreshCells(FreshLoadFixture
                                .cell(spreadsheet.getActiveSheet(), "F11")),
                        FreshLoadHarness.Allowance.RESEND));
        assertEquals(List.of(),
                harness.check(
                        spreadsheet -> LoggerFactory
                                .getLogger(Spreadsheet.class).warn("allowed"),
                        FreshLoadHarness.Allowance.WARNINGS));
        assertEquals(List.of(),
                harness.check(
                        spreadsheet -> FreshLoadFixture
                                .cell(workbook.getSheet("Second"), "B2")
                                .setCellValue("allowed"),
                        FreshLoadHarness.Allowance.OTHER_SHEETS));
    }

    @Test
    void aSheetTheOperationShows_isNotASheetNotShown() {
        List<String> problems = harness.check(spreadsheet -> {
            FreshLoadFixture.cell(workbook.getSheet("Second"), "B2")
                    .setCellValue("changed");
            spreadsheet.setActiveSheetIndex(1);
        });

        assertEquals(List.of(), problems);
    }

    @Test
    void whereTwoValuesDiffer_isNamedByItsPath() {
        assertEquals(".a [1] the client has 3, a new spreadsheet sends 4",
                FreshLoadHarness.describe(MAPPER.readTree("{\"a\":[1,3]}"),
                        MAPPER.readTree("{\"a\":[1,4]}"),
                        "a new spreadsheet sends"));
        assertEquals("the client has 1 entries, before it had 2: [1] vs [1,2]",
                FreshLoadHarness.describe(MAPPER.readTree("[1]"),
                        MAPPER.readTree("[1,2]"), "before it had"));
    }

    @Test
    void anOperationBeforeTheClient_runsOutOfTheUi_orBeforeTheFirstResponse() {
        List<String> seen = new ArrayList<>();
        for (FreshLoadHarness.Before before : FreshLoadHarness.Before
                .values()) {
            List<String> problems = harness
                    .checkBeforeShown(FreshLoadFixture.workbook(), before,
                            spreadsheet -> seen.add(before + " "
                                    + spreadsheet.getUI().isPresent() + " "
                                    + spreadsheet.getElement()
                                            .getPropertyRaw("reload")),
                            FreshLoadFixture::configure);
            assertEquals(List.of(), problems);
        }

        assertEquals(List.of("ATTACHED false null", "FIRST_RESPONSE true null"),
                seen);
    }

    @Test
    void anOperationBeforeTheClientThatThrows_isReported() {
        List<String> problems = harness.checkBeforeShown(
                FreshLoadFixture.workbook(),
                FreshLoadHarness.Before.FIRST_RESPONSE, spreadsheet -> {
                    throw new IllegalStateException("too early");
                }, FreshLoadFixture::configure);

        assertEquals(
                List.of("threw java.lang.IllegalStateException: too early"),
                problems);
    }
}
