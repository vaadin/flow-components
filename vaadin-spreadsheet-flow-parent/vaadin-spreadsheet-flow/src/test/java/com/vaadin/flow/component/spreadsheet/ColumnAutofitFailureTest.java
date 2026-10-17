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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.tests.CapturedLogs;
import com.vaadin.flow.component.spreadsheet.tests.ExpectWarnings;

/**
 * A column POI cannot autofit keeps its width, with a warning. What autofitting
 * does otherwise, {@link SpreadsheetColumnAutofitTest} shows.
 */
class ColumnAutofitFailureTest {

    @Test
    @ExpectWarnings
    void aColumnPoiCannotAutofit_keepsItsWidth_withAWarning() {
        // POI's measuring of text can throw where fonts are missing.
        Spreadsheet spreadsheet = mock(Spreadsheet.class);
        Sheet sheet = mock(Sheet.class);
        when(spreadsheet.getActiveSheet()).thenReturn(sheet);
        doThrow(new NullPointerException("poi")).when(sheet).autoSizeColumn(1);

        List<CapturedLogs.Entry> logged;
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            new SheetSizes(spreadsheet).autofit(1);
            logged = logs.entries();
        }

        verify(spreadsheet, never()).setColumnWidth(anyInt(), anyInt());
        assertEquals(List.of("Could not autofit column 2"),
                logged.stream().map(CapturedLogs.Entry::message).toList());
    }
}
