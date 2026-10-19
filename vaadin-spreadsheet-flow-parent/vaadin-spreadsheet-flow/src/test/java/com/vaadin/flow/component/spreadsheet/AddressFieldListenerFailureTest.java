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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.poi.ss.usermodel.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.tests.MockUIExtension;

/**
 * What a selection listener throws as an address typed into the address field
 * is selected: it goes through, whatever its type, and the address is not taken
 * for an invalid one. An {@link IllegalArgumentException} is POI's word for an
 * address it cannot read, and one a listener threw was taken for that, with the
 * spreadsheet's own sender, which changed the selection.
 */
class AddressFieldListenerFailureTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final IllegalArgumentException thrown = new IllegalArgumentException(
            "the listener's");
    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        spreadsheet = new Spreadsheet();
        ui.add(spreadsheet);
        ui.dumpPendingJavaScriptInvocations();
        spreadsheet.addSelectionChangeListener(event -> {
            throw thrown;
        });
    }

    private void typeAddress(String address) {
        assertSame(thrown,
                assertThrows(IllegalArgumentException.class,
                        () -> spreadsheet.getCellSelectionManager()
                                .onSheetAddressChanged(address, false)));
    }

    private List<String> calls() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(invocation -> String
                        .valueOf(invocation.getParameters().get(0)))
                .toList();
    }

    @Test
    void aCell_isNotTakenForAnInvalidAddress() {
        typeAddress("C3");

        assertEquals(List.of(),
                calls().stream().filter("invalidCellAddress"::equals).toList());
    }

    @Test
    void aRange_isNotTakenForAnInvalidAddress() {
        typeAddress("B2:C3");

        assertEquals(List.of(),
                calls().stream().filter("invalidCellAddress"::equals).toList());
    }

    @Test
    void aNamedRange_isNotTakenForAnInvalidAddress() {
        Name total = spreadsheet.getWorkbook().createName();
        total.setNameName("Total");
        total.setRefersToFormula(
                spreadsheet.getActiveSheet().getSheetName() + "!$D$4");

        typeAddress("Total");

        assertEquals(List.of(),
                calls().stream().filter("invalidCellAddress"::equals).toList());
    }
}
