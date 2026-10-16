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

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * A picture over rows and columns that change size, or are hidden or shown, is
 * kept on the client under the key the client has it by, rather than sent to it
 * anew. That it is sized again, {@link FreshLoadConsistencyTest} shows.
 */
class SpreadsheetPicturesTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            FreshLoadFixture::configure, FreshLoadFixture.VIEWPORT);
    private Spreadsheet spreadsheet;
    private Set<String> keys;

    @BeforeEach
    void show() {
        // The picture of the fixture is over G15:H16.
        spreadsheet = harness.show(FreshLoadFixture.workbook());
        keys = Set.copyOf(spreadsheet.getResources().keySet());
        assertFalse(keys.isEmpty(), "the picture is shown");
    }

    @Test
    void thePicture_isKept_whenAColumnUnderItIsHiddenAndShown() {
        spreadsheet.setColumnHidden(6, true);
        spreadsheet.setColumnHidden(6, false);

        assertEquals(keys, spreadsheet.getResources().keySet());
    }

    @Test
    void thePicture_isKept_whenAColumnUnderItIsGivenAWidth() {
        spreadsheet.setColumnWidth(6, 150);

        assertEquals(keys, spreadsheet.getResources().keySet());
    }

    @Test
    void thePicture_isKept_whenTheUserDragsAColumnUnderIt() {
        // The client numbers the columns from 1: G.
        harness.clientEvent("columnResized",
                "{\"colIndexes\": [7], \"newWidths\": [150]}");

        assertEquals(keys, spreadsheet.getResources().keySet());
    }

    @Test
    void thePicture_isKept_whenTheUserDragsARowUnderIt() {
        harness.clientEvent("rowsResized",
                "{\"rowIndexes\": [15], \"newHeights\": [40]}");

        assertEquals(keys, spreadsheet.getResources().keySet());
    }

    @Test
    void thePicture_isKept_whenARowUnderItIsGivenAHeight() {
        spreadsheet.setRowHeight(14, 30f);

        assertEquals(keys, spreadsheet.getResources().keySet());
    }
}
