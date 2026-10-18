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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * A sheet shown anew has the client reload it, every time: what the client is
 * sent of the sheet in that round trip it would drop, so it is given the cells
 * it shows once it reports where it is, and the cell the sheet was left with,
 * or A1 on a sheet never shown.
 */
class SheetReloadTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Second", 50, 20);
        spreadsheet.setActiveSheetIndex(0);
        ui.add(spreadsheet);
        respond();
        clientReportsWhereItIs();
    }

    /** Ends the request; returns the client calls of its response. */
    private List<String> respond() {
        ui.fakeClientCommunication();
        List<String> calls = new ArrayList<>();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            String name = String.valueOf(parameters.get(0));
            if (name.equals("showSelectedCell")) {
                calls.add(name + " "
                        + address(parameters.get(2), parameters.get(3)));
            }
            for (Object parameter : parameters) {
                if (parameter instanceof JsonNode json && json.isArray()
                        && json.path(0).has("col")) {
                    for (JsonNode cell : json) {
                        calls.add(address(cell.get("col").asInt(),
                                cell.get("row").asInt()) + "="
                                + cell.path("value").asString(""));
                    }
                }
            }
        }
        return calls;
    }

    /** The client numbers rows and columns from 1. */
    private static String address(Object column, Object row) {
        return new CellReference(Integer.parseInt(String.valueOf(row)) - 1,
                Integer.parseInt(String.valueOf(column)) - 1).formatAsString();
    }

    private List<String> clientReportsWhereItIs() {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 1, \"firstColumn\": 1, \"lastRow\": 20, \"lastColumn\": 10}");
        return respond();
    }

    private Object reload() {
        return spreadsheet.getElement().getPropertyRaw("reload");
    }

    @Test
    void cellsChangedAsASheetIsShown_areSentOnceTheClientReportsWhereItIs() {
        spreadsheet.setActiveSheetIndex(1);
        spreadsheet.createCell(1, 1, "new");

        assertEquals(List.of(), respond());

        assertEquals(Set.of("B2=new", "showSelectedCell A1"),
                new HashSet<>(clientReportsWhereItIs()));
    }

    @Test
    void aSheetShownAnewManyTimes_hasTheClientReloadItEveryTime() {
        for (int time = 0; time < 200; time++) {
            Object before = reload();

            spreadsheet.setActiveSheetIndex((time + 1) % 2);
            respond();

            assertNotEquals(before, reload(), "shown anew, time " + time);
        }
    }
}
