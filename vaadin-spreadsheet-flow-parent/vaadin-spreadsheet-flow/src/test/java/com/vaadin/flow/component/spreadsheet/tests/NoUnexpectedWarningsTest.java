/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Which warnings and errors fail a test: the component's, unless the test
 * expects them.
 */
class NoUnexpectedWarningsTest {

    private static final CapturedLogs.Entry COMPONENT = new CapturedLogs.Entry(
            "com.vaadin.flow.component.spreadsheet.Spreadsheet", "warn", "lost",
            null);
    private static final CapturedLogs.Entry OTHER = new CapturedLogs.Entry(
            "org.apache.poi.Something", "error", "theirs", null);

    @Test
    void theComponentsWarnings_areUnexpected() {
        assertEquals(List.of(COMPONENT), NoUnexpectedWarnings
                .unexpected(List.of(COMPONENT, OTHER), false));
    }

    @Test
    void warningsExpected_areNot() {
        assertEquals(List.of(), NoUnexpectedWarnings
                .unexpected(List.of(COMPONENT, OTHER), true));
    }
}
