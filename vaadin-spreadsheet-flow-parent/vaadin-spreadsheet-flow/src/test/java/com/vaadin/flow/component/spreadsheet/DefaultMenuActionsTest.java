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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.action.SpreadsheetAction;
import com.vaadin.flow.component.spreadsheet.action.SpreadsheetDefaultActionHandler;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.JsFunctionCallUtil;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The context-menu actions the component offers by itself, opened and picked as
 * the client does: Create table, Hide and Unhide row and column, Edit comment
 * and Hide comment, which no test ran; the actions the menu must not offer; and
 * an application's own actions in a default action handler.
 */
class DefaultMenuActionsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        workbook.createSheet();
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
    }

    @Test
    void createTable_onARangeOfRows_makesAFilterTableThere() {
        spreadsheet.setSelection("A1:C4");
        rightClickCell(1, 1);
        String key = actionsShown().get("Create Table on A1:C4");
        assertNotNull(key);

        pick("actionOnCurrentSelection", key);

        List<SpreadsheetTable> tables = spreadsheet.getTablesForActiveSheet();
        assertEquals(1, tables.size());
        assertInstanceOf(SpreadsheetFilterTable.class, tables.get(0));
        assertEquals("A1:C4",
                tables.get(0).getFullTableRegion().formatAsString());
    }

    @Test
    void createTable_isNotOfferedOverATableAlreadyThere() {
        // Offered once first, so the action carries a caption from before.
        spreadsheet.setSelection("A1:C4");
        rightClickCell(1, 1);
        assertTrue(actionsShown().containsKey("Create Table on A1:C4"));
        spreadsheet.registerTable(new SpreadsheetFilterTable(spreadsheet,
                CellRangeAddress.valueOf("B3:D6")));

        spreadsheet.setSelection("A1:C4");
        rightClickCell(1, 1);

        assertFalse(actionsShown().keySet().stream()
                .anyMatch(caption -> caption.startsWith("Create Table")));
    }

    @Test
    void hideRow_fromItsHeader_hidesTheRow() {
        rightClickRowHeader(3);
        String key = actionsShown().get("Hide row 3");

        pick("actionOnRowHeader", key);

        assertTrue(spreadsheet.isRowHidden(2));
        assertFalse(spreadsheet.isRowHidden(1));
    }

    @Test
    void hideColumn_fromItsHeader_hidesTheColumn() {
        rightClickColumnHeader(2);
        String key = actionsShown().get("Hide column B");

        pick("actionOnColumnHeader", key);

        assertTrue(spreadsheet.getActiveSheet().isColumnHidden(1));
        assertFalse(spreadsheet.getActiveSheet().isColumnHidden(2));
    }

    @Test
    void unhideRow_fromTheRowBelow_showsTheHiddenRowAbove() {
        spreadsheet.setRowHidden(2, true);
        rightClickRowHeader(4);
        String key = actionsShown().get("Unhide row 3");

        pick("actionOnRowHeader", key);

        assertFalse(spreadsheet.isRowHidden(2));
    }

    @Test
    void unhideRow_fromTheRowAbove_showsTheHiddenRowBelow() {
        spreadsheet.setRowHidden(2, true);
        rightClickRowHeader(2);
        String key = actionsShown().get("Unhide row 3");

        pick("actionOnRowHeader", key);

        assertFalse(spreadsheet.isRowHidden(2));
    }

    @Test
    void unhideColumn_fromTheColumnAfter_showsTheHiddenColumnBefore() {
        spreadsheet.setColumnHidden(2, true);
        rightClickColumnHeader(4);
        String key = actionsShown().get("Unhide column C");

        pick("actionOnColumnHeader", key);

        assertFalse(spreadsheet.getActiveSheet().isColumnHidden(2));
    }

    @Test
    void unhideColumn_fromTheColumnBefore_showsTheHiddenColumnAfter() {
        spreadsheet.setColumnHidden(2, true);
        rightClickColumnHeader(2);
        String key = actionsShown().get("Unhide column C");

        pick("actionOnColumnHeader", key);

        assertFalse(spreadsheet.getActiveSheet().isColumnHidden(2));
    }

    @Test
    void editComment_onACellWithAComment_opensItsCommentOnTheClient() {
        addComment("B2", false);
        spreadsheet.setSelection("B2");
        rightClickCell(2, 2);
        String key = actionsShown().get("Edit comment");

        pick("actionOnCurrentSelection", key);

        assertEquals(List.of(List.of(1, 1)), callsOf("editCellComment"));
    }

    @Test
    void hideComment_hidesAShownComment_andShowCommentShowsItAgain() {
        Comment comment = addComment("B2", true);
        spreadsheet.setSelection("B2");
        rightClickCell(2, 2);
        pick("actionOnCurrentSelection", actionsShown().get("Hide comment"));
        assertFalse(comment.isVisible());

        rightClickCell(2, 2);
        pick("actionOnCurrentSelection", actionsShown().get("Show comment"));

        assertTrue(comment.isVisible());
    }

    @Test
    void theCommentActions_areNotOfferedOnACellWithoutAComment() {
        spreadsheet.setSelection("B2");
        rightClickCell(2, 2);

        Map<String, String> shown = actionsShown();

        assertFalse(shown.containsKey("Edit comment"));
        assertFalse(shown.containsKey("Hide comment"));
        assertFalse(shown.containsKey("Show comment"));
    }

    @Test
    void deleteRow_isOfferedOnARowHeader_butNotOnAProtectedSheet() {
        rightClickRowHeader(3);
        assertTrue(actionsShown().containsKey("Delete row 3"));

        spreadsheet.getActiveSheet().protectSheet("secret");
        rightClickRowHeader(3);

        assertFalse(actionsShown().containsKey("Delete row 3"));
    }

    @Test
    void withoutTheDefaultActionHandler_theClientIsToldThereIsNoMenu() {
        spreadsheet.removeDefaultActionHandler();

        assertFalse(spreadsheet.getElement().getProperty("hasActions", true));
    }

    @Test
    void aMenuOpenedAsTheLastActionHandlerWent_showsNothing() {
        // Right-clicks the client sent before it heard there is no menu.
        spreadsheet.removeDefaultActionHandler();

        rightClickRowHeader(3);
        rightClickColumnHeader(2);
        rightClickCell(2, 2);
        pick("actionOnCurrentSelection", "1");

        assertTrue(actionsShown().isEmpty());
    }

    @Test
    void anOwnActionInADefaultActionHandler_isOfferedWithTheDefaults_andRuns() {
        SpreadsheetDefaultActionHandler handler = new SpreadsheetDefaultActionHandler();
        handler.addSpreadsheetAction(new MarkDone());
        spreadsheet.removeDefaultActionHandler();
        spreadsheet.addActionHandler(handler);
        addComment("B2", false);
        spreadsheet.setSelection("B2");
        rightClickCell(2, 2);
        Map<String, String> shown = actionsShown();
        assertTrue(shown.containsKey("Edit comment"));

        pick("actionOnCurrentSelection", shown.get("Mark done"));

        assertEquals("done", spreadsheet.getCell("B2").getStringCellValue());
    }

    @Test
    void anOwnActionTakenOutOfItsHandler_isNoLongerOffered() {
        SpreadsheetDefaultActionHandler handler = new SpreadsheetDefaultActionHandler();
        MarkDone markDone = new MarkDone();
        handler.addSpreadsheetAction(markDone);
        spreadsheet.removeDefaultActionHandler();
        spreadsheet.addActionHandler(handler);

        assertTrue(handler.removeSpreadsheetAction(markDone));
        spreadsheet.setSelection("B2");
        rightClickCell(2, 2);

        assertFalse(actionsShown().containsKey("Mark done"));
        assertFalse(handler.removeSpreadsheetAction(markDone));
    }

    /** An application's own action on one cell: writes "done" into it. */
    private static class MarkDone extends SpreadsheetAction {

        MarkDone() {
            super("Mark done");
        }

        @Override
        public boolean isApplicableForSelection(Spreadsheet spreadsheet,
                SelectionChangeEvent event) {
            return event.getCellRangeAddresses().isEmpty()
                    && event.getIndividualSelectedCells().isEmpty();
        }

        @Override
        public boolean isApplicableForHeader(Spreadsheet spreadsheet,
                CellRangeAddress headerRange) {
            return false;
        }

        @Override
        public void executeActionOnSelection(Spreadsheet spreadsheet,
                SelectionChangeEvent event) {
            CellReference cell = event.getSelectedCellReference();
            spreadsheet.createCell(cell.getRow(), cell.getCol(), "done");
        }

        @Override
        public void executeActionOnHeader(Spreadsheet spreadsheet,
                CellRangeAddress headerRange) {
            throw new UnsupportedOperationException();
        }
    }

    private Comment addComment(String address, boolean visible) {
        Sheet sheet = spreadsheet.getActiveSheet();
        CreationHelper factory = workbook.getCreationHelper();
        CellAddress at = new CellAddress(address);
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(at.getColumn());
        anchor.setCol2(at.getColumn() + 2);
        anchor.setRow1(at.getRow());
        anchor.setRow2(at.getRow() + 3);
        Comment comment = sheet.createDrawingPatriarch()
                .createCellComment(anchor);
        comment.setAddress(at);
        comment.setString(factory.createRichTextString("A note"));
        comment.setVisible(visible);
        return comment;
    }

    private void rightClickCell(int row, int column) {
        TestHelper.fireClientEvent(spreadsheet, "contextMenuOpenOnSelection",
                "{\"row\": " + row + ", \"column\": " + column + "}");
    }

    private void rightClickRowHeader(int row) {
        TestHelper.fireClientEvent(spreadsheet, "rowHeaderContextMenuOpen",
                "{\"rowIndex\": " + row + "}");
    }

    private void rightClickColumnHeader(int column) {
        TestHelper.fireClientEvent(spreadsheet, "columnHeaderContextMenuOpen",
                "{\"columnIndex\": " + column + "}");
    }

    private void pick(String type, String key) {
        assertNotNull(key, "the action is offered");
        TestHelper.fireClientEvent(spreadsheet, type,
                "{\"actionKey\": \"" + key + "\"}");
    }

    /**
     * Ends the request; the actions the menu shows, caption to key. Empty when
     * no menu opens.
     */
    private Map<String, String> actionsShown() {
        Map<String, String> keys = new LinkedHashMap<>();
        JsonMapper mapper = JsonMapper.builder().build();
        for (List<Object> arguments : callsOf("showActions")) {
            for (JsonNode action : mapper.readTree((String) arguments.get(0))) {
                keys.put(action.get("caption").asString(),
                        action.get("key").asString());
            }
        }
        return keys;
    }

    /** Ends the request; the arguments of each call of a client function. */
    private List<List<Object>> callsOf(String function) {
        ui.fakeClientCommunication();
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .filter(invocation -> function
                        .equals(JsFunctionCallUtil.getFunctionName(invocation)))
                .map(JsFunctionCallUtil::getArguments).toList();
    }
}
