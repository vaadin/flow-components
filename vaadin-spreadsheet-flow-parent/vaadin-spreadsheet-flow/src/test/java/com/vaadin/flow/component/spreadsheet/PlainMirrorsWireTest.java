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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.internal.change.MapPutChange;
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap;
import com.vaadin.tests.MockUIExtension;

/**
 * What the client is sent of the plain mirrored properties: a value only once
 * it is set, not again while it stays the same, and the element has it at once,
 * attached or not. Element property bindings to signals were tried for these
 * and sent every value on attach, held null as a JSON node, and left the
 * element as it was while detached.
 */
class PlainMirrorsWireTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private static final Set<String> PLAIN = Set.of("sheetProtected",
            "showCustomEditorOnFocus", "hasComponentFactory", "infoLabelValue");

    private final Spreadsheet spreadsheet = new Spreadsheet();

    private final SpreadsheetComponentFactory factory = new SpreadsheetComponentFactory() {
        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
        }
    };

    /** The plain properties the response gives the element, sorted. */
    private List<String> sent() {
        UI current = ui.getUI();
        StateNode node = spreadsheet.getElement().getNode();
        current.getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();
        List<String> sent = new ArrayList<>();
        current.getInternals().getStateTree().collectChanges(change -> {
            if (change.getNode() == node && change instanceof MapPutChange put
                    && put.getFeature() == ElementPropertyMap.class
                    && PLAIN.contains(put.getKey())) {
                sent.add(put.getKey() + "=" + put.getValue());
            }
        });
        current.getInternals().dumpPendingJavaScriptInvocations();
        sent.sort(null);
        return sent;
    }

    private void setAll() {
        spreadsheet.setActiveSheetProtected("");
        spreadsheet.setShowCustomEditorOnFocus(true);
        spreadsheet.setSpreadsheetComponentFactory(factory);
        spreadsheet.setStatusLabelValue("ready");
    }

    @Test
    void attached_sendsOnlyWhatTheSheetSet() {
        ui.add(spreadsheet);

        // Showing the sheet sets whether it is protected; the rest are left
        // to the client's own defaults.
        assertEquals(List.of("sheetProtected=false"), sent());
    }

    @Test
    void set_sendsEachValue_andSetAgain_sendsNothing() {
        ui.add(spreadsheet);
        sent();

        setAll();
        assertEquals(
                List.of("hasComponentFactory=true", "infoLabelValue=ready",
                        "sheetProtected=true", "showCustomEditorOnFocus=true"),
                sent());

        setAll();
        assertEquals(List.of(), sent());
    }

    @Test
    void cleared_sendsFalseAndNull() {
        ui.add(spreadsheet);
        setAll();
        sent();

        spreadsheet.setActiveSheetProtected(null);
        spreadsheet.setShowCustomEditorOnFocus(false);
        spreadsheet.setSpreadsheetComponentFactory(null);
        spreadsheet.setStatusLabelValue(null);

        assertEquals(List.of("hasComponentFactory=false", "infoLabelValue=null",
                "sheetProtected=false", "showCustomEditorOnFocus=false"),
                sent());
    }

    @Test
    void setBeforeAttach_isSentOnAttach() {
        spreadsheet.setStatusLabelValue("before attach");
        spreadsheet.setShowCustomEditorOnFocus(true);

        ui.add(spreadsheet);

        assertEquals(List.of("infoLabelValue=before attach",
                "sheetProtected=false", "showCustomEditorOnFocus=true"),
                sent());
    }

    @Test
    void setWhileDetached_isOnTheElementAtOnce() {
        spreadsheet.setSpreadsheetComponentFactory(factory);

        assertEquals(true, spreadsheet.getElement()
                .getProperty("hasComponentFactory", false));
    }
}
