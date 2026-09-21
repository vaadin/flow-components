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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * POI drops a formatting whose only row is removed, and shifting the rows back
 * does not bring it back. Remembering the {@link ConditionalFormatting} object
 * across the delete does not work either - POI disconnects the removed rule's
 * XML - so what is remembered is the XML itself. That was
 * <a href="https://github.com/vaadin/spreadsheet/issues/331">spreadsheet
 * #331</a>.
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
    void firstRowDeletedAndUndone_everyFormattingComesBack() {
        RowInsertOrDeleteCommand command = deleteFirstRow();

        command.execute();

        List<String> back = List.of(ranges());
        assertEquals(3, back.size(), "all three are back, was " + back);
        assertTrue(back.contains("A1:B1"),
                "the one that lived only on the deleted row is back too");
        assertTrue(back.contains("A3") && back.contains("A5"),
                "and the others are where they started");
    }

    @Test
    void firstRowDeletedAndUndone_theRestoredRuleStillCarriesItsCondition() {
        // The XML is copied rather than the rule rebuilt field by field, so
        // the condition survives exactly - along with everything a rebuild
        // would have had to know about.
        RowInsertOrDeleteCommand command = deleteFirstRow();

        command.execute();

        ConditionalFormattingRule restored = ruleForRange("A1:B1");
        assertEquals("\"AAAAA\"", restored.getFormula1());
    }

    @Test
    void droppedFormattingsRule_readAfterTheDelete_throws() {
        // Why the formatting is remembered as XML and not as the object:
        // POI disconnects the removed rule, so the object is dead by the
        // time the undo runs.
        ConditionalFormattingRule rule = formattings()
                .getConditionalFormattingAt(0).getRule(0);
        assertEquals("\"AAAAA\"", rule.getFormula1());

        deleteFirstRow();

        assertThrows(RuntimeException.class, rule::getFormula1);
    }

    /** The first rule of the formatting covering exactly {@code range}. */
    private ConditionalFormattingRule ruleForRange(String range) {
        SheetConditionalFormatting all = formattings();
        for (int i = 0; i < all.getNumConditionalFormattings(); i++) {
            ConditionalFormatting formatting = all
                    .getConditionalFormattingAt(i);
            for (CellRangeAddress address : formatting.getFormattingRanges()) {
                if (range.equals(address.formatAsString())) {
                    return formatting.getRule(0);
                }
            }
        }
        throw new AssertionError("no formatting covers " + range);
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
