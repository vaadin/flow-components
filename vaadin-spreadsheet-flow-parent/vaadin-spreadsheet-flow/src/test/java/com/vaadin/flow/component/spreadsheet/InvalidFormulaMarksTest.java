/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

/**
 * The undo commands reach a cell's invalid-formula flag through
 * {@link InvalidFormulaMarks}, so the flag's methods on {@link Spreadsheet}
 * stay package-private, as they are on the GWT-era component.
 */
class InvalidFormulaMarksTest {

    @Test
    void mark_flagsTheCellOnTheSpreadsheet() {
        Spreadsheet spreadsheet = new Spreadsheet();

        InvalidFormulaMarks.mark(spreadsheet, 2, 3);

        assertTrue(InvalidFormulaMarks.isMarked(spreadsheet, 2, 3));
        assertTrue(spreadsheet.isMarkedAsInvalidFormula(2, 3));
        assertFalse(InvalidFormulaMarks.isMarked(spreadsheet, 3, 2));
    }

    @Test
    void spreadsheetFlagMethods_areNotPublic() throws NoSuchMethodException {
        assertFalse(Modifier.isPublic(Spreadsheet.class
                .getDeclaredMethod("markInvalidFormula", int.class, int.class)
                .getModifiers()));
        assertFalse(Modifier.isPublic(
                Spreadsheet.class.getDeclaredMethod("isMarkedAsInvalidFormula",
                        int.class, int.class).getModifiers()));
    }
}
