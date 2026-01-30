/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand;

public class SizeChangeCommandTest {

    @Test
    public void execute_columnWidthChange_undoRestoresOriginalWidth() {
        Spreadsheet spreadsheet = new Spreadsheet();

        int originalWidth = (int) spreadsheet.getActiveSheet()
                .getColumnWidthInPixels(0);

        // Create command, capture original width
        SizeChangeCommand command = new SizeChangeCommand(spreadsheet,
                SizeChangeCommand.Type.COLUMN);
        command.captureValues(new Integer[] { 1 });

        // Set a different width
        spreadsheet.setColumnWidth(0, 300);
        assertEquals(300,
                (int) spreadsheet.getActiveSheet().getColumnWidthInPixels(0));

        // Execute (undo) should restore original width
        command.execute();
        assertEquals(originalWidth,
                (int) spreadsheet.getActiveSheet().getColumnWidthInPixels(0));
    }
}
