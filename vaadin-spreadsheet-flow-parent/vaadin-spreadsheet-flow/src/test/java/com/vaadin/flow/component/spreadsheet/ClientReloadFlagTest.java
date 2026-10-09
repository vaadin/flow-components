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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

/**
 * The pending full reload {@link ClientReloadFlag} tracks and sends to the
 * client.
 *
 * <p>
 * The helper has three observable contracts:
 *
 * <ol>
 * <li>{@code isPending} read.</li>
 * <li>{@code markPending} sets the flag true (always; the UI-callback
 * registration is conditional on {@code UI.getCurrent()} but the flag mutation
 * happens unconditionally after).</li>
 * <li>{@code flush} pushes a timestamp to the element {@code "reload"}
 * property, clears the flag, and defaults
 * {@code spreadsheet.initialSheetSelection} when not already set.</li>
 * </ol>
 *
 * <p>
 * The {@code beforeClientResponse} callback registration path inside
 * {@code markPending} is intentionally NOT exercised here — {@code
 * UI.getCurrent()} returns null in unit-test context, so the
 * {@link java.util.Optional#ifPresent} guard short-circuits. The integration
 * path (UI present → callback fires → flush called) is covered by the existing
 * {@link SpreadsheetHistoryManagerPayloadTest} golden snapshot.
 */
class ClientReloadFlagTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Element element;
    @Mock
    private SheetState sheetState;
    @Mock
    private Sheet activeSheet;

    private ClientReloadFlag flag;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        lenient().when(spreadsheet.getElement()).thenReturn(element);
        lenient().when(spreadsheet.getActiveSheet()).thenReturn(activeSheet);

        // sheetState is a package-private field on Spreadsheet — wire it
        // via reflection on the mock (same pattern as CellDeletionGuardTest
        // and CellMarkUpdaterTest).
        java.lang.reflect.Field f = Spreadsheet.class
                .getDeclaredField("sheetState");
        f.setAccessible(true);
        f.set(spreadsheet, sheetState);

        flag = new ClientReloadFlag();
    }

    // ----- isPending / markPending -----------------------------------------

    @Test
    void freshFlag_isPendingReturnsFalse() {
        // Pin: the flag starts in the "not pending" state.
        assertFalse(flag.isPending());
    }

    @Test
    void markPending_setsFlagToTrue_evenWhenUIIsNotPresent() {
        // Pin: the `pending = true` line is OUTSIDE the
        // Optional.ifPresent block — the UI-callback registration is
        // optional, but the flag mutation MUST happen unconditionally.
        // A regression that moved the assignment inside the lambda
        // would silently no-op in headless contexts (where
        // UI.getCurrent() is null) and the next isPending() would
        // wrongly return false.
        flag.markPending(spreadsheet);

        assertTrue(flag.isPending(),
                "markPending MUST set isPending true regardless of UI presence");
    }

    @Test
    void markPending_calledTwice_remainsTrue_coalescesIntoSinglePending() {
        // Pin: multiple back-to-back marks don't toggle or accumulate;
        // the flag is idempotent on repeated marks.
        flag.markPending(spreadsheet);
        flag.markPending(spreadsheet);
        flag.markPending(spreadsheet);

        assertTrue(flag.isPending());
    }

    // ----- flush: timestamp push -------------------------------------------

    @Test
    void flush_pushesFreshTimestampToElementReloadProperty() {
        // Pin: flush MUST push the epoch-millis timestamp into
        // element.setProperty("reload", double). Vaadin's Element only
        // offers setProperty for boolean / double / String, so the long
        // from System.currentTimeMillis() widens to double.
        long before = System.currentTimeMillis();

        flag.flush(spreadsheet);

        long after = System.currentTimeMillis();
        ArgumentCaptor<Double> valueCaptor = ArgumentCaptor
                .forClass(Double.class);
        verify(element).setProperty(org.mockito.ArgumentMatchers.eq("reload"),
                valueCaptor.capture());
        double timestamp = valueCaptor.getValue();
        assertTrue(timestamp >= before && timestamp <= after,
                "Timestamp must be a System.currentTimeMillis() value "
                        + "from inside the flush window. Got: " + timestamp);
    }

    @Test
    void flush_clearsPendingFlag() {
        // Pin: after flush, isPending returns false. A regression that
        // forgot to clear the flag would cause subsequent
        // beforeClientResponse callbacks (registered by future
        // markPending calls) to redundantly flush.
        flag.markPending(spreadsheet);
        assertTrue(flag.isPending());

        flag.flush(spreadsheet);

        assertFalse(flag.isPending());
    }

    // ----- flush: initialSheetSelection defaulting -------------------------

    @Test
    void flush_initialSelectionAlreadySet_doesNotOverwrite() {
        // Pin: the seed logic guard `if (initialSheetSelection == null)`
        // protects an existing seed (e.g. CommandReplayer set it for an
        // undo replay). A regression that always seeded would clobber
        // CommandReplayer's seed and break undo selection restoration.
        spreadsheet.initialSheetSelection = "C5";

        flag.flush(spreadsheet);

        assertEquals("C5", spreadsheet.initialSheetSelection,
                "Existing initialSheetSelection MUST be preserved");
        verify(sheetState, never()).getSelectedCellsOnSheet(
                org.mockito.ArgumentMatchers.any(Sheet.class));
    }

    @Test
    void flush_initialSelectionNull_sheetStateHasRememberedSelection_usesIt() {
        // Pin: when there's no pre-set selection, the flush picks up
        // the SheetState's remembered selection address (e.g. the user
        // had B4 selected on this sheet before navigating away).
        when(sheetState.getSelectedCellsOnSheet(activeSheet)).thenReturn("B4");
        assertNull(spreadsheet.initialSheetSelection);

        flag.flush(spreadsheet);

        assertEquals("B4", spreadsheet.initialSheetSelection);
    }

    @Test
    void flush_initialSelectionNull_sheetStateReturnsNull_defaultsToA1() {
        // Pin: when both the seed AND the remembered selection are
        // null, fall back to "A1". A regression that defaulted to ""
        // or null would leave the client without a target cell to
        // render-select after the reload.
        when(sheetState.getSelectedCellsOnSheet(activeSheet)).thenReturn(null);
        assertNull(spreadsheet.initialSheetSelection);

        flag.flush(spreadsheet);

        assertEquals("A1", spreadsheet.initialSheetSelection,
                "Null + null MUST default to A1");
    }

    @Test
    void flush_neverConsultsSheetState_whenInitialSelectionAlreadySet() {
        // Pin: the order — null-check FIRST, sheetState read SECOND.
        // A regression that always called getSelectedCellsOnSheet
        // (even with non-null initialSheetSelection) would cause
        // unnecessary work and could surface SheetState side effects
        // (e.g. a memoization that flagged the sheet as "asked about").
        spreadsheet.initialSheetSelection = "Z99";

        flag.flush(spreadsheet);

        verify(sheetState, never()).getSelectedCellsOnSheet(
                org.mockito.ArgumentMatchers.any(Sheet.class));
    }

    // ----- ordering: timestamp then clear then seed ------------------------

    @Test
    void flush_pushesTimestamp_thenClearsFlag_thenSeedsSelection() {
        // Pin the order of side effects via InOrder. The timestamp push
        // MUST happen before the flag clear (so isRerenderPending stays
        // true if anyone observes the element-update event). The seed
        // happens last.
        when(sheetState.getSelectedCellsOnSheet(activeSheet)).thenReturn("D7");
        flag.markPending(spreadsheet);
        assertTrue(flag.isPending());

        flag.flush(spreadsheet);

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(element,
                sheetState);
        inOrder.verify(element).setProperty(
                org.mockito.ArgumentMatchers.eq("reload"),
                org.mockito.ArgumentMatchers.anyDouble());
        inOrder.verify(sheetState).getSelectedCellsOnSheet(activeSheet);
        assertFalse(flag.isPending());
        assertEquals("D7", spreadsheet.initialSheetSelection);
    }
}
