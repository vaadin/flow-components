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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apache.poi.ss.usermodel.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;

/**
 * What happens when the address field is given something that is not an
 * address.
 *
 * <p>
 * Plain words are not invalid: POI classifies {@code NOTACELL} as a named
 * range, and typing a name into the address field names the current selection,
 * which is what Excel's Name Box does. What is invalid is text POI cannot
 * classify at all.
 *
 * <p>
 * Two kinds of it, and only one was handled. Text starting with punctuation
 * throws out of the classifier and the catch reports it. Text that classifies
 * as neither a cell nor a name - {@code A 1}, {@code B2!} - matched none of the
 * three branches and there was no else, so it fell through in silence: the
 * client was never told, and the typed text stayed in the field naming a cell
 * nobody was standing on.
 */
class InvalidCellAddressTest {

    private SpreadsheetClientRpc rpc;
    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        rpc = mock(SpreadsheetClientRpc.class);
        spreadsheet = new Spreadsheet() {
            @Override
            protected SpreadsheetClientRpc getRpcProxy() {
                return rpc;
            }
        };
        spreadsheet.setSelection("B2");
    }

    private void typeAddress(String address) {
        spreadsheet.getCellSelectionManager().onSheetAddressChanged(address,
                false);
    }

    @Test
    void textTheClassifierRejects_isReportedBack() {
        typeAddress("A 1");

        verify(rpc).invalidCellAddress();
    }

    @Test
    void textTheClassifierRejects_leavesTheSelectionWhereItWas() {
        typeAddress("A 1");

        assertEquals("Sheet1!B2",
                spreadsheet.getSelectedCellReference().formatAsString());
    }

    @Test
    void textThatThrowsOutOfTheClassifier_isReportedBack() {
        typeAddress("!!!");

        verify(rpc).invalidCellAddress();
    }

    @Test
    void emptyText_isReportedBack() {
        typeAddress("");

        verify(rpc).invalidCellAddress();
    }

    @Test
    void aCellReference_isAccepted() {
        typeAddress("D4");

        verify(rpc, never()).invalidCellAddress();
        assertEquals("Sheet1!D4",
                spreadsheet.getSelectedCellReference().formatAsString());
    }

    @Test
    void aPlainWord_namesTheSelectionRatherThanBeingRejected() {
        // Excel's Name Box: typing a name there names what is selected.
        typeAddress("MYRANGE");

        verify(rpc, never()).invalidCellAddress();
        assertEquals("MYRANGE",
                spreadsheet.getWorkbook().getName("MYRANGE").getNameName());
    }

    @Test
    void aRange_isAccepted() {
        typeAddress("B2:C3");

        verify(rpc, never()).invalidCellAddress();
    }

    @Test
    void aNamedRangeOfAnotherSheet_showsThatSheet_andIsNotInvalid() {
        spreadsheet.createNewSheet("Other", 5, 5);
        spreadsheet.setActiveSheetIndex(0);
        Name elsewhere = spreadsheet.getWorkbook().createName();
        elsewhere.setNameName("Elsewhere");
        elsewhere.setRefersToFormula("Other!$B$2");

        typeAddress("Elsewhere");

        assertEquals("Other", spreadsheet.getActiveSheet().getSheetName());
        verify(rpc, never()).invalidCellAddress();
    }
}
