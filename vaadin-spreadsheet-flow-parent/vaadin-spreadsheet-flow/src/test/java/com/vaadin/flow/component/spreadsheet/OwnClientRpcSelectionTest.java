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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;
import com.vaadin.tests.MockUIExtension;

/**
 * An application that gives the spreadsheet its own
 * {@link SpreadsheetClientRpc}, through {@code getRpcProxy()}, changes what the
 * client is sent and nothing else: a cell typed into the name box is selected,
 * and the listeners hear of it before the client is told, as with the
 * spreadsheet's own sender. The selection used to change inside that sender, so
 * with another the listeners heard of it only after the client was told.
 */
class OwnClientRpcSelectionTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final SpreadsheetClientRpc rpc = mock(SpreadsheetClientRpc.class);
    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        spreadsheet = new Spreadsheet() {
            @Override
            protected SpreadsheetClientRpc getRpcProxy() {
                return rpc;
            }
        };
        ui.add(spreadsheet);
    }

    private boolean selectionSent() {
        return mockingDetails(rpc).getInvocations().stream().anyMatch(
                call -> call.getMethod().getName().equals("showSelectedCell"));
    }

    @Test
    void aCellTypedIntoTheNameBox_isSelected_andTheListenersHearOfItFirst() {
        List<String> heard = new ArrayList<>();
        spreadsheet.addSelectionChangeListener(event -> heard
                .add(event.getSelectedCellReference().formatAsString(false)
                        + (selectionSent() ? " after the client" : "")));

        spreadsheet.getCellSelectionManager().onSheetAddressChanged("C3",
                false);

        assertEquals("C3", spreadsheet.getCellSelectionManager()
                .getSelectedCellReference().formatAsString(false));
        assertEquals(List.of("C3"), heard);
        verify(rpc).showSelectedCell(isNull(), eq(3), eq(3), anyString(),
                anyBoolean(), anyBoolean(), eq(false));
    }
}
