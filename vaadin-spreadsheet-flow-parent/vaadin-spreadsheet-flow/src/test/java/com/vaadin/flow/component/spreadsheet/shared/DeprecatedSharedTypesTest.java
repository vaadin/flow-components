/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.shared;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.rpc.PopupButtonClientRpc;

/**
 * The public types the component no longer uses, kept as main has them so that
 * code referring to them still compiles and runs.
 */
@SuppressWarnings("deprecation")
class DeprecatedSharedTypesTest {

    @Test
    void errorLevels_compareBySeverity() {
        assertArrayEquals(new ErrorLevel[] { ErrorLevel.INFO,
                ErrorLevel.WARNING, ErrorLevel.ERROR, ErrorLevel.CRITICAL,
                ErrorLevel.SYSTEM }, ErrorLevel.values());
        assertTrue(ErrorLevel.WARNING.intValue() < ErrorLevel.ERROR.intValue());
        assertEquals(4, ErrorLevel.SYSTEM.intValue());
    }

    @Test
    void contentModes_areTheThreeOfMain() {
        assertArrayEquals(new ContentMode[] { ContentMode.TEXT,
                ContentMode.PREFORMATTED, ContentMode.HTML },
                ContentMode.values());
    }

    @Test
    void urlReference_keepsItsUrl() {
        URLReference reference = new URLReference();
        assertNull(reference.getURL());

        reference.setURL("theme://image.png");

        assertEquals("theme://image.png", reference.getURL());
    }

    @Test
    void spreadsheetState_startsWithTheDefaultsOfMain() {
        SpreadsheetState state = new SpreadsheetState();

        assertEquals(200, state.rowBufferSize);
        assertEquals(200, state.columnBufferSize);
        assertEquals(1, state.sheetIndex);
        assertTrue(state.displayGridlines);
        assertTrue(state.displayRowColHeadings);
        assertTrue(state.enabled);
        assertEquals(ContentMode.PREFORMATTED, state.descriptionContentMode);
        assertTrue(state.resources.isEmpty());
    }

    @Test
    void popupButtonClientRpc_opensAndCloses() {
        assertEquals(Arrays.asList("closePopup", "openPopup"),
                Arrays.stream(PopupButtonClientRpc.class.getDeclaredMethods())
                        .map(method -> method.getName()).sorted().toList());
    }
}
