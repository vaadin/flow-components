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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.command.Command;
import com.vaadin.flow.component.spreadsheet.command.ValueChangeCommand;
import com.vaadin.tests.MockUIExtension;

/**
 * Server-side payload-recording test for {@link SpreadsheetHistoryManager}.
 * Captures the byte-shape of the server↔client wire (element properties +
 * pending JS invocations) before and after a representative {@code undo}
 * scenario, formats it as a stable text snapshot, and asserts against a golden
 * constant inlined below.
 *
 * <p>
 * Purpose: a stronger safety gate than the pure-state assertions in
 * {@link SpreadsheetHistoryManagerTest}. Any future refactor that touches
 * {@code SpreadsheetHistoryManager} must keep this snapshot
 * <strong>byte-identical</strong> after normalization.
 *
 * <p>
 * Normalization: the {@code reload} count, which depends on what came before,
 * is replaced with {@code <T>}. The map of properties is sorted by name.
 * JS-invocation parameters are rendered in their declaration order.
 */
class SpreadsheetHistoryManagerPayloadTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    /**
     * Scenario: a command targets sheet 0 with a selected-cell-only selection;
     * the active sheet is then switched to sheet 1; undo is called, flipping
     * the active sheet back and seeding {@code initialSheetSelection}. This
     * exercises both {@code makeSureCorrectSheetActive} and
     * {@code changeSelection} (which is short-circuited because
     * {@code isRerenderPending()} is true after the sheet swap).
     */
    @Test
    void undoSheetSwap_payloadMatchesGolden() {
        var spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Second", 10, 10);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();

        var cmd = new RecordedCommand();
        cmd.activeSheetIndex = 0;
        cmd.selectedCellReference = new CellReference(2, 3);
        spreadsheet.getSpreadsheetHistoryManager().addCommand(cmd);

        spreadsheet.setActiveSheetIndex(1);
        ui.fakeClientCommunication();
        // Drain any pending invocations from the setup so the captured
        // delta isolates the undo-driven payload.
        ui.dumpPendingJavaScriptInvocations();

        var before = capturePropertyState(spreadsheet);

        spreadsheet.getSpreadsheetHistoryManager().undo();
        ui.fakeClientCommunication();

        var after = capturePropertyState(spreadsheet);
        var jsCalls = captureJsCalls();
        var snapshot = renderDiffAndCalls(before, after, jsCalls);

        // Observation pinned: undo flips sheetIndex 2.0 -> 1.0 (the
        // property is 1-based) and produces no other element property
        // changes and no pending JS invocations. The Flow JSON
        // serialization renders ints as doubles ("1.0", "2.0") for
        // wire-format consistency; the trailing ".0" is part of the
        // pinned shape.
        var golden = String.join("\n", "== property delta ==",
                "  sheetIndex: 2.0 -> 1.0", "== js calls ==", "  (none)", "");

        assertEquals(golden, snapshot);
    }

    /**
     * Scenario: undo for a {@link ValueChangeCommand}, with the active sheet
     * already matching the command's sheet. {@code changeSelection} runs
     * normally (no rerender pending) and routes through
     * {@code reSelectSelectedCell()} because the command has no
     * selectedCellReference.
     */
    @Test
    void undoValueChangeOnSameSheet_payloadMatchesGolden() {
        var spreadsheet = new Spreadsheet();
        ui.add(spreadsheet);
        ui.fakeClientCommunication();

        var changed = new HashSet<CellReference>();
        changed.add(new CellReference(0, 0));
        var cmd = new RecordedValueChangeCommand(changed);
        cmd.activeSheetIndex = spreadsheet.getActiveSheetIndex();
        spreadsheet.getSpreadsheetHistoryManager().addCommand(cmd);

        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        var before = capturePropertyState(spreadsheet);

        spreadsheet.getSpreadsheetHistoryManager().undo();
        ui.fakeClientCommunication();

        var after = capturePropertyState(spreadsheet);
        var jsCalls = captureJsCalls();
        var snapshot = renderDiffAndCalls(before, after, jsCalls);

        var golden = String.join("\n", "== property delta ==",
                "  (no property changes)", "== js calls ==", "  (none)", "");

        assertEquals(golden, snapshot);
    }

    // ------------------------------------------------------------------
    // Snapshot helpers
    // ------------------------------------------------------------------

    private TreeMap<String, String> capturePropertyState(Spreadsheet s) {
        var out = new TreeMap<String, String>();
        s.getElement().getPropertyNames().forEach(name -> {
            var value = s.getElement().getPropertyRaw(name);
            out.put(name, normalize(name, String.valueOf(value)));
        });
        return out;
    }

    private List<String> captureJsCalls() {
        var out = new ArrayList<String>();
        for (PendingJavaScriptInvocation pji : ui
                .dumpPendingJavaScriptInvocations()) {
            var inv = pji.getInvocation();
            out.add(inv.getExpression() + " " + inv.getParameters().toString());
        }
        return out;
    }

    private String normalize(String name, String value) {
        // The reloads counted so far, which depend on what came before.
        if ("reload".equals(name)) {
            return "<T>";
        }
        return value;
    }

    private String renderDiffAndCalls(TreeMap<String, String> before,
            TreeMap<String, String> after, List<String> jsCalls) {
        var sb = new StringBuilder();
        sb.append("== property delta ==\n");
        var keys = new TreeMap<String, Object>();
        before.keySet().forEach(k -> keys.put(k, null));
        after.keySet().forEach(k -> keys.put(k, null));
        var anyDiff = false;
        for (String k : keys.keySet()) {
            var b = before.get(k);
            var a = after.get(k);
            if (b == null && a != null) {
                sb.append("  +").append(k).append(": ").append(a).append('\n');
                anyDiff = true;
            } else if (b != null && a == null) {
                sb.append("  -").append(k).append(": ").append(b).append('\n');
                anyDiff = true;
            } else if (b != null && !b.equals(a)) {
                sb.append("  ").append(k).append(": ").append(b).append(" -> ")
                        .append(a).append('\n');
                anyDiff = true;
            }
        }
        if (!anyDiff) {
            sb.append("  (no property changes)\n");
        }
        sb.append("== js calls ==\n");
        if (jsCalls.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            for (String call : jsCalls) {
                sb.append("  ").append(call).append('\n');
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Fake Command implementations
    // ------------------------------------------------------------------

    private static class RecordedCommand implements Command {
        int activeSheetIndex;
        CellReference selectedCellReference;
        CellRangeAddress paintedCellRange;

        @Override
        public void execute() {
        }

        @Override
        public int getActiveSheetIndex() {
            return activeSheetIndex;
        }

        @Override
        public void setActiveSheetIndex(int index) {
            this.activeSheetIndex = index;
        }

        @Override
        public CellReference getSelectedCellReference() {
            return selectedCellReference;
        }

        @Override
        public CellRangeAddress getPaintedCellRange() {
            return paintedCellRange;
        }
    }

    private static class RecordedValueChangeCommand extends RecordedCommand
            implements ValueChangeCommand {
        private final Set<CellReference> changed;

        RecordedValueChangeCommand(Set<CellReference> changed) {
            this.changed = changed;
        }

        @Override
        public Set<CellReference> getChangedCells() {
            return changed;
        }
    }
}
