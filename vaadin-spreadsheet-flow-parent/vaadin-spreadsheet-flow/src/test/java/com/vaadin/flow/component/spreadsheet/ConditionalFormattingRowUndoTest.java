/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.command.RowInsertOrDeleteCommand;

/**
 * What happens to conditional formatting when a row is deleted and the delete
 * is undone.
 *
 * <p>
 * Undo does not bring back a conditional formatting that lived only on the
 * deleted row - the open issue
 * <a href="https://github.com/vaadin/spreadsheet/issues/331">spreadsheet
 * #331</a>, which keeps
 * {@code UndoRedoIT.undo_conditionalFormattedCellsRemovedAndUndo_cellsAreStillConditionallyFormatted}
 * skipped. These tests pin where it goes and why the obvious repair does not
 * work, so whoever picks the issue up starts from facts.
 */
class ConditionalFormattingRowUndoTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(
                "test_sheets/conditional_formatting.xlsx")) {
            spreadsheet = new Spreadsheet(WorkbookFactory.create(in));
        }
    }

    @Test
    void fixtureHasThreeFormattings_oneOfThemOnTheFirstRowOnly() {
        assertArrayEquals(new String[] { "A1:B1", "A3", "A5" }, ranges());
    }

    @Test
    void firstRowDeleted_theFormattingOnItIsGone_theOthersShiftUp() {
        deleteFirstRow();

        // POI drops a formatting whose whole range was inside the row it
        // removed, and moves the rest.
        assertArrayEquals(new String[] { "A2", "A4" }, ranges());
    }

    @Test
    void firstRowDeletedAndUndone_theOthersComeBack_theDroppedOneDoesNot() {
        RowInsertOrDeleteCommand command = deleteFirstRow();

        command.execute();

        // A3 and A5 are back where they started. A1:B1 is not, and nothing
        // in the undo path knows it ever existed - this is issue #331.
        assertArrayEquals(new String[] { "A3", "A5" }, ranges());
    }

    @Test
    void droppedFormattingsRule_readAfterTheDelete_throws() {
        // Why the obvious repair - remember the ConditionalFormatting before
        // the delete and add it back afterwards - does not work: POI
        // disconnects the removed rule's XML, so the remembered object is
        // dead by the time the undo runs. Putting the formatting back means
        // copying every rule's condition and styling out while it is still
        // alive, which loses whatever a copy does not cover.
        ConditionalFormattingRule rule = formattings()
                .getConditionalFormattingAt(0).getRule(0);
        assertEquals("\"AAAAA\"", rule.getFormula1());

        deleteFirstRow();

        assertThrows(RuntimeException.class, rule::getFormula1);
    }

    private SheetConditionalFormatting formattings() {
        return spreadsheet.getActiveSheet().getSheetConditionalFormatting();
    }

    private String[] ranges() {
        SheetConditionalFormatting all = formattings();
        List<String> out = new ArrayList<>();
        for (int i = 0; i < all.getNumConditionalFormattings(); i++) {
            ConditionalFormatting formatting = all
                    .getConditionalFormattingAt(i);
            for (CellRangeAddress range : formatting.getFormattingRanges()) {
                out.add(range.formatAsString());
            }
        }
        return out.toArray(new String[0]);
    }

    private RowInsertOrDeleteCommand deleteFirstRow() {
        RowInsertOrDeleteCommand command = new RowInsertOrDeleteCommand(
                spreadsheet, new CellRangeAddress(0, 0, 0, 0));
        command.execute();
        return command;
    }
}
