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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.JsonNode;

/**
 * What the spreadsheet sends the client goes out as JSON, never as text holding
 * JSON: as text, every quote of it was escaped, a sixth of what a cell took on
 * the wire, and the client parsed it a second time. A sheet with something of
 * every kind is shown, scrolled, edited, given a context menu and switched, and
 * nothing sent on the way may be text that holds JSON.
 */
class NoJsonTextSentTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;
    private final List<String> textHoldingJson = new ArrayList<>();
    /**
     * The calls and JSON properties seen, by name, to know the check saw them.
     */
    private final Set<String> seen = new TreeSet<>();

    @Test
    void nothingTheClientIsSent_isTextHoldingJson() {
        XSSFWorkbook workbook = FreshLoadFixture.workbook();
        Sheet first = workbook.getSheet("First");
        first.groupRow(21, 23);
        first.groupColumn(9, 10);
        spreadsheet = new Spreadsheet(workbook);
        FreshLoadFixture.configure(spreadsheet);
        ui.add(spreadsheet);
        check("load");

        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": 10, \"firstColumn\": 5, \"lastRow\": 25,"
                        + " \"lastColumn\": 12}");
        check("scroll");
        TestHelper.fireClientEvent(spreadsheet, "cellSelected",
                "{\"row\": 11, \"column\": 5,"
                        + " \"oldSelectionRangeDiscarded\": true}");
        TestHelper.fireClientEvent(spreadsheet, "cellValueEdited",
                "{\"row\": 11, \"col\": 5, \"value\": \"typed\"}");
        check("edit");
        TestHelper.fireClientEvent(spreadsheet, "contextMenuOpenOnSelection",
                "{\"row\": 11, \"column\": 5}");
        check("context menu");
        TestHelper.fireClientEvent(spreadsheet, "sheetSelected",
                "{\"sheetIndex\": 1, \"scrollLeft\": 0, \"scrollTop\": 40}");
        check("sheet switched");

        assertEquals(List.of(), textHoldingJson);
        assertTrue(
                seen.containsAll(Set.of("cellsUpdated", "showActions",
                        "addPopupButton", "property colGroupingData",
                        "property rowGroupingData", "property mergedRegions",
                        "property overlays", "property cellKeysToEditorIdMap",
                        "property sheetNames", "property namedRanges",
                        "property cellStyleToCSSStyle", "property rowH")),
                "the check saw them all: " + seen);
    }

    private void check(String step) {
        ui.fakeClientCommunication();
        for (PendingJavaScriptInvocation invocation : ui
                .dumpPendingJavaScriptInvocations()) {
            List<Object> parameters = invocation.getInvocation()
                    .getParameters();
            for (Object parameter : parameters) {
                if (holdsJson(parameter)) {
                    textHoldingJson.add(
                            step + ": " + parameters.get(0) + " " + parameter);
                } else if (parameter instanceof JsonNode
                        && parameters.get(0) instanceof String function) {
                    seen.add(function);
                }
            }
        }
        Element element = spreadsheet.getElement();
        element.getPropertyNames().forEach(name -> {
            Object value = element.getPropertyRaw(name);
            if (holdsJson(value)) {
                textHoldingJson.add(step + ": property " + name + " " + value);
            } else if (value instanceof JsonNode) {
                seen.add("property " + name);
            }
        });
    }

    private static boolean holdsJson(Object value) {
        return value instanceof String text
                && (text.startsWith("[") || text.startsWith("{"));
    }
}
